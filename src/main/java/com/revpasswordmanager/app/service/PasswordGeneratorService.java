package com.revpasswordmanager.app.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;


@Service
public class PasswordGeneratorService {

    private static final Logger logger = Logger.getLogger(PasswordGeneratorService.class);
    private static final SecureRandom random = new SecureRandom();

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final String SIMILAR_CHARS = "0O1lI|`";

    public Map<String, Object> generatePassword(int length, boolean upper, boolean lower,
            boolean digits, boolean special) {
        return generatePassword(length, upper, lower, digits, special, false);
    }

    public Map<String, Object> generatePassword(int length, boolean upper, boolean lower,
            boolean digits, boolean special, boolean excludeSimilar) {
        logger.info("Generating password, length: " + length + ", excludeSimilar: " + excludeSimilar);

        StringBuilder pool = new StringBuilder();
        if (upper)
            pool.append(UPPER);
        if (lower)
            pool.append(LOWER);
        if (digits)
            pool.append(DIGITS);
        if (special)
            pool.append(SPECIAL);

        if (pool.length() == 0)
            throw new IllegalArgumentException("Select at least one character type");

        // Remove similar characters if requested
        String charPool = pool.toString();
        if (excludeSimilar) {
            StringBuilder filtered = new StringBuilder();
            for (char ch : charPool.toCharArray()) {
                if (SIMILAR_CHARS.indexOf(ch) < 0)
                    filtered.append(ch);
            }
            charPool = filtered.toString();
        }

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < Math.max(length, 4); i++)
            password.append(charPool.charAt(random.nextInt(charPool.length())));

        String generated = password.toString();
        int score = calculateStrengthScore(generated);

        return Map.<String, Object>of("password", generated, "strength", getLabel(score), "score", score);
    }

    public List<Map<String, Object>> generateMultiplePasswords(int count, int length, boolean upper,
            boolean lower, boolean digits, boolean special, boolean excludeSimilar) {
        List<Map<String, Object>> passwords = new java.util.ArrayList<>();
        for (int i = 0; i < Math.min(count, 10); i++) {
            passwords.add(generatePassword(length, upper, lower, digits, special, excludeSimilar));
        }
        return passwords;
    }

    public Map<String, Object> checkStrength(String password) {
        int score = calculateStrengthScore(password);
        return Map.<String, Object>of("strength", getLabel(score), "score", score);
    }

    public int calculateStrengthScore(String password) {
        if (password == null || password.isEmpty())
            return 0;
        int score = 0;
        if (password.length() >= 8)
            score += 10;
        if (password.length() >= 12)
            score += 10;
        if (password.length() >= 16)
            score += 10;
        if (password.length() >= 20)
            score += 10;
        if (password.chars().anyMatch(Character::isUpperCase))
            score += 15;
        if (password.chars().anyMatch(Character::isLowerCase))
            score += 15;
        if (password.chars().anyMatch(Character::isDigit))
            score += 15;
        if (password.chars().anyMatch(c -> SPECIAL.indexOf(c) >= 0))
            score += 15;
        return Math.min(score, 100);
    }

    private String getLabel(int score) {
        if (score >= 80)
            return "VERY_STRONG";
        if (score >= 60)
            return "STRONG";
        if (score >= 40)
            return "MODERATE";
        if (score >= 20)
            return "WEAK";
        return "VERY_WEAK";
    }
}
