package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.LoginRequest;
import com.revpasswordmanager.dto.RecoveryRequest;
import com.revpasswordmanager.dto.RegisterRequest;
import com.revpasswordmanager.dto.SecurityQuestionInput;
import com.revpasswordmanager.model.SecurityQuestion;
import com.revpasswordmanager.model.User;
import com.revpasswordmanager.repository.SecurityQuestionRepository;
import com.revpasswordmanager.repository.UserRepository;
import com.revpasswordmanager.mapper.UserMapper;
import com.revpasswordmanager.mapper.SecurityQuestionMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFaService twoFaService;
    private final UserMapper userMapper;
    private final SecurityQuestionMapper securityQuestionMapper;

    public AuthService(UserRepository userRepository,
            SecurityQuestionRepository securityQuestionRepository,
            PasswordEncoder passwordEncoder,
            TwoFaService twoFaService,
            UserMapper userMapper,
            SecurityQuestionMapper securityQuestionMapper) {
        this.userRepository = userRepository;
        this.securityQuestionRepository = securityQuestionRepository;
        this.passwordEncoder = passwordEncoder;
        this.twoFaService = twoFaService;
        this.userMapper = userMapper;
        this.securityQuestionMapper = securityQuestionMapper;
    }

    public Map<String, Object> register(RegisterRequest request) {
        if (request.getSecurityQuestions() == null || request.getSecurityQuestions().size() < 3) {
            throw new IllegalArgumentException("At least 3 security questions are required");
        }
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });

        User user = userMapper.toEntity(request);
        user.setMasterPasswordHash(passwordEncoder.encode(request.getMasterPassword()));
        user = userRepository.save(user);
        Long userId = user.getId();

        List<SecurityQuestion> questionRows = request.getSecurityQuestions().stream()
                .map(q -> securityQuestionMapper.toEntity(q, passwordEncoder.encode(normalizeAnswer(q.getAnswer()))))
                .collect(Collectors.toList());
        securityQuestionRepository.saveAll(userId, questionRows);

        return Map.of("userId", userId, "message", "Registration successful");
    }

    public Map<String, Object> login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getMasterPassword(), user.getMasterPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!twoFaService.verifyCode(user, request.getTwoFaCode())) {
            throw new IllegalArgumentException("Invalid or missing 2FA code");
        }

        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("USER_NAME", user.getUsername());
        session.setAttribute("REAUTH_AT", Instant.now().getEpochSecond());

        return Map.of("message", "Login successful", "userId", user.getId(), "twoFaEnabled", user.isTwoFaEnabled());
    }

    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logout successful");
    }

    public void recover(RecoveryRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getAnswers() == null || request.getAnswers().size() < 3) {
            throw new IllegalArgumentException("At least 3 answers required");
        }

        List<SecurityQuestion> stored = securityQuestionRepository.findByUserId(user.getId());
        if (stored.size() < 3) {
            throw new IllegalStateException("Recovery setup is invalid for this user");
        }

        int matches = 0;
        for (SecurityQuestionInput input : request.getAnswers()) {
            for (SecurityQuestion sq : stored) {
                if (sq.getQuestion().equalsIgnoreCase(input.getQuestion())
                        && passwordEncoder.matches(normalizeAnswer(input.getAnswer()), sq.getAnswerHash())) {
                    matches++;
                    break;
                }
            }
        }

        if (matches < 3) {
            throw new IllegalArgumentException("Security answer validation failed");
        }

        userRepository.updateMasterPassword(user.getId(), passwordEncoder.encode(request.getNewMasterPassword()));
    }

    public void reauthenticate(Long userId, String masterPassword, HttpSession session) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(masterPassword, user.getMasterPasswordHash())) {
            throw new IllegalArgumentException("Master password mismatch");
        }
        session.setAttribute("REAUTH_AT", Instant.now().getEpochSecond());
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void changeMasterPassword(Long userId, String currentPassword, String newPassword) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getMasterPasswordHash())) {
            throw new IllegalArgumentException("Current master password is invalid");
        }
        userRepository.updateMasterPassword(userId, passwordEncoder.encode(newPassword));
    }

    public void updateProfile(Long userId, String name, String email) {
        userRepository.updateProfile(userId, name, email);
    }

    public void updateSecurityQuestions(Long userId, List<SecurityQuestionInput> inputs) {
        if (inputs == null || inputs.size() < 3) {
            throw new IllegalArgumentException("At least 3 security questions are required");
        }
        List<SecurityQuestion> rows = inputs.stream()
                .map(in -> securityQuestionMapper.toEntity(in, passwordEncoder.encode(normalizeAnswer(in.getAnswer()))))
                .collect(Collectors.toList());
        securityQuestionRepository.replaceAll(userId, rows);
    }

    public List<SecurityQuestion> getSecurityQuestions(Long userId) {
        return securityQuestionRepository.findByUserId(userId);
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim().toLowerCase();
    }
}
