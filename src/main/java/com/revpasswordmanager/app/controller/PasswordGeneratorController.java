package com.revpasswordmanager.app.controller;

import com.revpasswordmanager.app.service.PasswordGeneratorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/password-generator")
public class PasswordGeneratorController {

    @Autowired
    private PasswordGeneratorService passwordGeneratorService;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(
            @RequestParam(defaultValue = "12") int length,
            @RequestParam(defaultValue = "true") boolean upper,
            @RequestParam(defaultValue = "true") boolean lower,
            @RequestParam(defaultValue = "true") boolean digits,
            @RequestParam(defaultValue = "true") boolean special,
            @RequestParam(defaultValue = "false") boolean excludeSimilar) {
        return ResponseEntity
                .ok(passwordGeneratorService.generatePassword(length, upper, lower, digits, special, excludeSimilar));
    }

    @GetMapping("/generate-multiple")
    public ResponseEntity<List<Map<String, Object>>> generateMultiple(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "12") int length,
            @RequestParam(defaultValue = "true") boolean upper,
            @RequestParam(defaultValue = "true") boolean lower,
            @RequestParam(defaultValue = "true") boolean digits,
            @RequestParam(defaultValue = "true") boolean special,
            @RequestParam(defaultValue = "false") boolean excludeSimilar) {
        return ResponseEntity.ok(passwordGeneratorService.generateMultiplePasswords(count, length, upper, lower, digits,
                special, excludeSimilar));
    }

    @GetMapping("/check-strength")
    public ResponseEntity<Map<String, Object>> checkStrength(@RequestParam String password) {
        return ResponseEntity.ok(passwordGeneratorService.checkStrength(password));
    }
}
