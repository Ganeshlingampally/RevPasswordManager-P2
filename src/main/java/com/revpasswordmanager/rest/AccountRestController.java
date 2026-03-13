package com.revpasswordmanager.rest;

import com.revpasswordmanager.dto.SecurityQuestionInput;
import com.revpasswordmanager.repository.UserRepository;
import com.revpasswordmanager.service.AuthService;
import com.revpasswordmanager.service.VerificationCodeService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;

    public AccountRestController(AuthService authService,
            VerificationCodeService verificationCodeService,
            UserRepository userRepository) {
        this.authService = authService;
        this.verificationCodeService = verificationCodeService;
        this.userRepository = userRepository;
    }

    @PutMapping("/profile")
    public Map<String, String> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        Long userId = requireUserId(session);
        authService.updateProfile(userId, body.get("name"), body.get("email"));
        return Map.of("message", "Profile updated");
    }

    @PostMapping("/master-password/change")
    public Map<String, String> changeMaster(@RequestBody Map<String, String> body, HttpSession session) {
        Long userId = requireUserId(session);
        authService.changeMasterPassword(userId, body.get("currentMasterPassword"), body.get("newMasterPassword"));
        return Map.of("message", "Master password changed");
    }

    @GetMapping("/security-questions")
    public List<Map<String, Object>> getQuestions(HttpSession session) {
        Long userId = requireUserId(session);
        return authService.getSecurityQuestions(userId).stream()
                .map(q -> Map.<String, Object>of("id", q.getId(), "question", q.getQuestion()))
                .collect(Collectors.toList());
    }

    @PutMapping("/security-questions")
    public Map<String, String> updateQuestions(@Valid @RequestBody List<SecurityQuestionInput> questions,
            HttpSession session) {
        Long userId = requireUserId(session);
        authService.updateSecurityQuestions(userId, questions);
        return Map.of("message", "Security questions updated");
    }

    @PostMapping("/verification-code")
    public Map<String, String> sendCode(@RequestBody Map<String, String> body, HttpSession session) {
        Long userId = requireUserId(session);
        String purpose = body.getOrDefault("purpose", "GENERIC");
        String code = verificationCodeService.issueCode(userId, purpose);
        return Map.of("purpose", purpose, "verificationCode", code, "note",
                "In production, send via email/SMS instead of response");
    }

    @PostMapping("/verification-code/validate")
    public Map<String, Object> validateCode(@RequestBody Map<String, String> body, HttpSession session) {
        Long userId = requireUserId(session);
        boolean valid = verificationCodeService.verify(userId, body.get("purpose"), body.get("code"));
        return Map.of("valid", valid);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Long userId = requireUserId(session);
        return userRepository.findById(userId)
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "name", u.getName(),
                        "email", u.getEmail(),
                        "twoFaEnabled", u.isTwoFaEnabled(),
                        "createdAt", u.getCreatedAt()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Long requireUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return userId;
    }
}
