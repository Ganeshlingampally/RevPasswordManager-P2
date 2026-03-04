package com.revpasswordmanager.app.controller;

import com.revpasswordmanager.app.dto.*;
import com.revpasswordmanager.app.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserRequestDTO dto) {
        return new ResponseEntity<>(userService.register(dto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    @PostMapping("/{userId}/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @PathVariable Long userId, @RequestBody Map<String, String> body) {
        boolean valid = userService.verifyPassword(userId, body.get("password"));
        if (!valid)
            throw new IllegalArgumentException("Invalid master password");
        return ResponseEntity.ok(Map.<String, Object>of("valid", true));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}/toggle-2fa")
    public ResponseEntity<UserResponseDTO> toggle2FA(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.toggleTwoFactor(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable Long userId, @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
    }

    @PutMapping("/{userId}/change-password")
    public ResponseEntity<UserResponseDTO> changeMasterPassword(
            @PathVariable Long userId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.changeMasterPassword(userId,
                body.get("currentPassword"), body.get("newPassword")));
    }

    // --- Password Recovery ---
    @PostMapping("/recover-password")
    public ResponseEntity<UserResponseDTO> recoverPassword(@RequestBody PasswordRecoveryDTO dto) {
        return ResponseEntity.ok(userService.recoverPassword(dto));
    }

    // --- Stepped Password Recovery (Security Questions + OTP) ---

    @GetMapping("/recovery/questions")
    public ResponseEntity<List<SecurityQuestionDTO>> getRecoveryQuestions(@RequestParam String username) {
        return ResponseEntity.ok(userService.getSecurityQuestionsByUsername(username));
    }

    @PostMapping("/recovery/verify-answers")
    public ResponseEntity<Map<String, Object>> verifySecurityAnswers(@RequestBody PasswordRecoveryDTO dto) {
        return ResponseEntity.ok(userService.verifySecurityAnswers(dto.getUsername(), dto.getSecurityAnswers()));
    }

    @PostMapping("/recovery/reset-password")
    public ResponseEntity<UserResponseDTO> resetPasswordWithOTP(@RequestBody PasswordRecoveryDTO dto) {
        return ResponseEntity.ok(userService.resetPasswordWithOTP(
                dto.getUsername(), dto.getOtpCode(), dto.getNewMasterPassword()));
    }

    // --- Security Questions ---
    @PostMapping("/{userId}/security-questions")
    public ResponseEntity<List<SecurityQuestionDTO>> saveQuestions(
            @PathVariable Long userId, @RequestBody List<SecurityQuestionDTO> questions) {
        return new ResponseEntity<>(userService.saveSecurityQuestions(userId, questions), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/security-questions")
    public ResponseEntity<List<SecurityQuestionDTO>> getQuestions(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getSecurityQuestions(userId));
    }

    // --- Verification Codes ---
    @PostMapping("/{userId}/verification/generate")
    public ResponseEntity<Map<String, Object>> generateCode(
            @PathVariable Long userId, @RequestParam String purpose) {
        return new ResponseEntity<>(userService.generateVerificationCode(userId, purpose), HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/verification/validate")
    public ResponseEntity<Map<String, Object>> validateCode(
            @PathVariable Long userId, @RequestParam String code, @RequestParam String purpose) {
        return ResponseEntity.ok(userService.validateVerificationCode(userId, code, purpose));
    }

    // --- 2FA Simulation ---
    @PostMapping("/{userId}/2fa/generate")
    public ResponseEntity<Map<String, Object>> generate2FA(@PathVariable Long userId) {
        return new ResponseEntity<>(userService.generate2FACode(userId), HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/2fa/validate")
    public ResponseEntity<Map<String, Object>> validate2FA(
            @PathVariable Long userId, @RequestParam String code) {
        return ResponseEntity.ok(userService.validate2FACode(userId, code));
    }
}