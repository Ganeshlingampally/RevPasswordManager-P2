package com.revpasswordmanager.controller;

import com.revpasswordmanager.dto.GeneratePasswordRequest;
import com.revpasswordmanager.service.PasswordGeneratorService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passwords")
public class PasswordToolController {

    private final PasswordGeneratorService passwordGeneratorService;

    public PasswordToolController(PasswordGeneratorService passwordGeneratorService) {
        this.passwordGeneratorService = passwordGeneratorService;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@Valid @RequestBody GeneratePasswordRequest request) {
        List<Map<String, String>> options = passwordGeneratorService.generate(request);
        Map<String, Object> out = new HashMap<>();
        out.put("options", options);
        out.put("copyToClipboardHint", "Use client-side navigator.clipboard API for secure copy action");
        return out;
    }
}

