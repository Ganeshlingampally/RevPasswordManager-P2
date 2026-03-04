package com.revpasswordmanager.app.service;

import com.revpasswordmanager.app.dto.*;
import com.revpasswordmanager.app.entity.SecurityQuestion;
import com.revpasswordmanager.app.entity.User;
import com.revpasswordmanager.app.entity.VerificationCode;
import com.revpasswordmanager.app.exception.ResourceNotFoundException;
import com.revpasswordmanager.app.mapper.EntityMapper;
import com.revpasswordmanager.app.repository.SecurityQuestionRepository;
import com.revpasswordmanager.app.repository.UserRepository;
import com.revpasswordmanager.app.repository.VerificationCodeRepository;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class);
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.verification-code.expiry-minutes}")
    private int codeExpiryMinutes;



    public boolean verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return passwordEncoder.matches(password, user.getMasterPasswordHash());
    }



    @Transactional
    public UserResponseDTO register(UserRequestDTO dto) {
        logger.info("Registering user: " + dto.getUsername());

        if (userRepository.existsByUsername(dto.getUsername()))
            throw new IllegalArgumentException("Username already exists");
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Email already exists");

        User user = EntityMapper.toUser(dto);
        user.setMasterPasswordHash(passwordEncoder.encode(dto.getMasterPassword()));
        User saved = userRepository.save(user);

        logger.info("User registered with ID: " + saved.getUserId());
        return EntityMapper.toUserResponse(saved);
    }

    public UserResponseDTO login(UserRequestDTO dto) {
        logger.info("Login attempt: " + dto.getUsername());

        User user = findUserByUsername(dto.getUsername());
        if (!passwordEncoder.matches(dto.getMasterPassword(), user.getMasterPasswordHash()))
            throw new IllegalArgumentException("Invalid master password");

        logger.info("Login successful: " + dto.getUsername());
        return EntityMapper.toUserResponse(user);
    }

    public UserResponseDTO getUserById(Long userId) {
        return EntityMapper.toUserResponse(findUserById(userId));
    }

    @Transactional
    public UserResponseDTO toggleTwoFactor(Long userId) {
        User user = findUserById(userId);
        user.setTwoFactorEnabled(!user.getTwoFactorEnabled());
        logger.info("2FA " + (user.getTwoFactorEnabled() ? "enabled" : "disabled") + " for user: " + userId);
        return EntityMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO updateProfile(Long userId, UserRequestDTO dto) {
        logger.info("Updating profile for user: " + userId);
        User user = findUserById(userId);
        if (dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            user.setLastName(dto.getLastName());
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null)
            user.setPhoneNumber(dto.getPhoneNumber());
        return EntityMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO changeMasterPassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing master password for user: " + userId);
        User user = findUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getMasterPasswordHash()))
            throw new IllegalArgumentException("Current master password is incorrect");
        user.setMasterPasswordHash(passwordEncoder.encode(newPassword));
        return EntityMapper.toUserResponse(userRepository.save(user));
    }



    @Transactional
    public List<SecurityQuestionDTO> saveSecurityQuestions(Long userId, List<SecurityQuestionDTO> questions) {
        logger.info("Saving security questions for user: " + userId);

        if (questions == null || questions.size() < 3)
            throw new IllegalArgumentException("Minimum 3 security questions required");

        User user = findUserById(userId);
        securityQuestionRepository.deleteByUserUserId(userId);

        return questions.stream().map(dto -> {
            SecurityQuestion sq = new SecurityQuestion(user, dto.getQuestion(),
                    passwordEncoder.encode(dto.getAnswer().toLowerCase().trim()));
            SecurityQuestion saved = securityQuestionRepository.save(sq);
            return EntityMapper.toSecurityQuestionDTO(saved);
        }).collect(Collectors.toList());
    }

    public List<SecurityQuestionDTO> getSecurityQuestions(Long userId) {
        return securityQuestionRepository.findByUserUserId(userId).stream()
                .map(EntityMapper::toSecurityQuestionDTO).collect(Collectors.toList());
    }



    @Transactional
    public UserResponseDTO recoverPassword(PasswordRecoveryDTO dto) {
        logger.info("Password recovery for: " + dto.getUsername());

        User user = findUserByUsername(dto.getUsername());
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserUserId(user.getUserId());

        if (questions.isEmpty())
            throw new IllegalArgumentException("No security questions found");
        if (dto.getSecurityAnswers() == null || dto.getSecurityAnswers().size() < 3)
            throw new IllegalArgumentException("At least 3 answers required");

        // Verify each answer
        for (SecurityQuestionDTO answerDTO : dto.getSecurityAnswers()) {
            SecurityQuestion q = questions.stream()
                    .filter(sq -> sq.getQuestionId().equals(answerDTO.getQuestionId()))
                    .findFirst().orElseThrow(() -> new ResourceNotFoundException("Question not found"));

            if (!passwordEncoder.matches(answerDTO.getAnswer().toLowerCase().trim(), q.getAnswerHash()))
                throw new IllegalArgumentException("Incorrect security answer");
        }

        user.setMasterPasswordHash(passwordEncoder.encode(dto.getNewMasterPassword()));
        logger.info("Password recovered for: " + dto.getUsername());
        return EntityMapper.toUserResponse(userRepository.save(user));
    }



    @Transactional
    public Map<String, Object> generateVerificationCode(Long userId, String purpose) {
        logger.info("Generating verification code for user: " + userId);
        User user = findUserById(userId);

        String code = String.valueOf(100000 + random.nextInt(900000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(codeExpiryMinutes);

        VerificationCode vc = new VerificationCode(user, code, purpose, expiresAt);
        verificationCodeRepository.save(vc);

        logger.info("Code generated: " + code + " expires: " + expiresAt);
        return Map.<String, Object>of("code", code, "purpose", purpose,
                "expiresAt", expiresAt.toString(), "message", "Code generated successfully");
    }

    @Transactional
    public Map<String, Object> validateVerificationCode(Long userId, String code, String purpose) {
        logger.info("Validating code for user: " + userId);

        VerificationCode vc = verificationCodeRepository
                .findByUserUserIdAndCodeAndPurposeAndUsedFalse(userId, code, purpose)
                .orElseThrow(() -> new ResourceNotFoundException("Code not found or already used"));

        if (vc.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Verification code has expired");

        vc.setUsed(true);
        verificationCodeRepository.save(vc);

        logger.info("Code validated for user: " + userId);
        return Map.<String, Object>of("message", "Code validated successfully", "valid", true);
    }

    @Transactional
    public Map<String, Object> generate2FACode(Long userId) {
        Map<String, Object> result = generateVerificationCode(userId, "2FA_LOGIN");
        return Map.<String, Object>of("code", result.get("code"),
                "message", "[2FA SIMULATION] Your code: " + result.get("code")
                        + ". Expires in " + codeExpiryMinutes + " minutes.");
    }

    @Transactional
    public Map<String, Object> validate2FACode(Long userId, String code) {
        validateVerificationCode(userId, code, "2FA_LOGIN");
        return Map.<String, Object>of("message", "2FA verification successful. Login complete.", "valid", true);
    }




    public List<SecurityQuestionDTO> getSecurityQuestionsByUsername(String username) {
        User user = findUserByUsername(username);
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserUserId(user.getUserId());
        if (questions.isEmpty())
            throw new IllegalArgumentException("No security questions found for this user");
        return questions.stream()
                .map(EntityMapper::toSecurityQuestionDTO).collect(Collectors.toList());
    }


    @Transactional
    public Map<String, Object> verifySecurityAnswers(String username, List<SecurityQuestionDTO> answers) {
        logger.info("Verifying security answers for: " + username);

        User user = findUserByUsername(username);
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserUserId(user.getUserId());

        if (questions.isEmpty())
            throw new IllegalArgumentException("No security questions found");
        if (answers == null || answers.size() < 3)
            throw new IllegalArgumentException("At least 3 answers required");

        for (SecurityQuestionDTO answerDTO : answers) {
            SecurityQuestion q = questions.stream()
                    .filter(sq -> sq.getQuestionId().equals(answerDTO.getQuestionId()))
                    .findFirst().orElseThrow(() -> new ResourceNotFoundException("Question not found"));

            if (!passwordEncoder.matches(answerDTO.getAnswer().toLowerCase().trim(), q.getAnswerHash()))
                throw new IllegalArgumentException("Incorrect security answer");
        }


        Map<String, Object> otpResult = generateVerificationCode(user.getUserId(), "PASSWORD_RESET");
        logger.info("Security answers verified, OTP generated for: " + username);

        return Map.<String, Object>of(
                "message", "[SIMULATION] Your OTP: " + otpResult.get("code")
                        + ". Expires in " + codeExpiryMinutes + " minutes.",
                "code", otpResult.get("code"),
                "userId", user.getUserId());
    }


    @Transactional
    public UserResponseDTO resetPasswordWithOTP(String username, String otpCode, String newPassword) {
        logger.info("Resetting password with OTP for: " + username);

        User user = findUserByUsername(username);
        validateVerificationCode(user.getUserId(), otpCode, "PASSWORD_RESET");

        user.setMasterPasswordHash(passwordEncoder.encode(newPassword));
        logger.info("Password reset successful via OTP for: " + username);
        return EntityMapper.toUserResponse(userRepository.save(user));
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}