package com.revpasswordmanager.controller;

import com.revpasswordmanager.dto.LoginRequest;
import com.revpasswordmanager.dto.ReauthRequest;
import com.revpasswordmanager.dto.RecoveryRequest;
import com.revpasswordmanager.dto.RegisterRequest;
import com.revpasswordmanager.service.AuthService;
import com.revpasswordmanager.service.TwoFaService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TwoFaService twoFaService;

    public AuthController(AuthService authService, TwoFaService twoFaService) {
        this.authService = authService;
        this.twoFaService = twoFaService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return authService.login(request, session);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        return authService.logout(session);
    }

    @PostMapping("/recover")
    public Map<String, Object> recover(@Valid @RequestBody RecoveryRequest request) {
        authService.recover(request);
        return Map.of("message", "Master password reset successful");
    }

    @PostMapping("/reauth")
    public Map<String, Object> reauth(@Valid @RequestBody ReauthRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        authService.reauthenticate(userId, request.getMasterPassword(), session);
        return Map.of("message", "Re-authentication successful");
    }

    @PostMapping("/2fa/{enabled}")
    public Map<String, Object> setTwoFa(@PathVariable boolean enabled, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        twoFaService.setTwoFa(userId, enabled);
        return Map.of("message", enabled ? "2FA enabled" : "2FA disabled");
    }
}

