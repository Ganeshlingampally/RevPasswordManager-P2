package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.GeneratePasswordRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PasswordGeneratorService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUM = "0123456789";
    private static final String SYM = "!@#$%^&*()-_=+[]{}<>?";
    private final SecureRandom secureRandom = new SecureRandom();

    public List<Map<String, String>> generate(GeneratePasswordRequest request) {
        int options = Math.max(1, Math.min(request.getOptions(), 10));
        List<Map<String, String>> out = new ArrayList<>();
        for (int i = 0; i < options; i++) {
            String p = generateSingle(request);
            Map<String, String> item = new HashMap<>();
            item.put("password", p);
            item.put("strength", evaluateStrength(p));
            out.add(item);
        }
        return out;
    }

    public String evaluateStrength(String password) {
        int score = 0;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;

        if (score <= 2) return "WEAK";
        if (score <= 4) return "MEDIUM";
        return "STRONG";
    }

    private String generateSingle(GeneratePasswordRequest request) {
        StringBuilder charset = new StringBuilder();
        if (request.isIncludeUppercase()) charset.append(UPPER);
        if (request.isIncludeLowercase()) charset.append(LOWER);
        if (request.isIncludeNumbers()) charset.append(NUM);
        if (request.isIncludeSymbols()) charset.append(SYM);
        if (charset.length() == 0) charset.append(UPPER).append(LOWER).append(NUM).append(SYM);

        StringBuilder generated = new StringBuilder();
        for (int i = 0; i < request.getLength(); i++) {
            generated.append(charset.charAt(secureRandom.nextInt(charset.length())));
        }
        return generated.toString();
    }
}

