package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.CredentialRequest;
import com.revpasswordmanager.model.Credential;
import com.revpasswordmanager.repository.CredentialRepository;
import com.revpasswordmanager.security.EncryptionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VaultService {

    private final CredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final PasswordGeneratorService passwordGeneratorService;
    private final ObjectMapper objectMapper;

    public VaultService(CredentialRepository credentialRepository,
                        EncryptionService encryptionService,
                        PasswordGeneratorService passwordGeneratorService,
                        ObjectMapper objectMapper) {
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
        this.passwordGeneratorService = passwordGeneratorService;
        this.objectMapper = objectMapper;
    }

    public Long create(Long userId, CredentialRequest request) {
        Credential c = new Credential();
        c.setUserId(userId);
        c.setAccountName(request.getAccountName());
        c.setUsername(request.getUsername());
        c.setEncryptedPassword(encryptionService.encrypt(request.getPassword()));
        c.setUrl(request.getUrl());
        c.setNotes(request.getNotes());
        c.setCategory(request.getCategory());
        c.setFavorite(request.isFavorite());
        c.setPasswordStrength(passwordGeneratorService.evaluateStrength(request.getPassword()));
        return credentialRepository.save(c);
    }

    public List<Map<String, Object>> list(Long userId, String q, String category, String sortBy, boolean favoritesOnly) {
        return credentialRepository.search(userId, q, category, sortBy, favoritesOnly).stream().map(c -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", c.getId());
            row.put("accountName", c.getAccountName());
            row.put("username", c.getUsername());
            row.put("url", c.getUrl());
            row.put("notes", c.getNotes());
            row.put("category", c.getCategory());
            row.put("favorite", c.isFavorite());
            row.put("passwordStrength", c.getPasswordStrength());
            row.put("createdAt", c.getCreatedAt());
            row.put("updatedAt", c.getUpdatedAt());
            return row;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getWithSecret(Long userId, Long credentialId) {
        Credential c = credentialRepository.findByIdAndUserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found"));
        credentialRepository.markAccessed(credentialId, userId);

        Map<String, Object> row = new HashMap<>();
        row.put("id", c.getId());
        row.put("accountName", c.getAccountName());
        row.put("username", c.getUsername());
        row.put("password", encryptionService.decrypt(c.getEncryptedPassword()));
        row.put("url", c.getUrl());
        row.put("notes", c.getNotes());
        row.put("category", c.getCategory());
        row.put("favorite", c.isFavorite());
        row.put("passwordStrength", c.getPasswordStrength());
        return row;
    }

    public void update(Long userId, Long credentialId, CredentialRequest request) {
        Credential existing = credentialRepository.findByIdAndUserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found"));

        existing.setAccountName(request.getAccountName());
        existing.setUsername(request.getUsername());
        existing.setEncryptedPassword(encryptionService.encrypt(request.getPassword()));
        existing.setUrl(request.getUrl());
        existing.setNotes(request.getNotes());
        existing.setCategory(request.getCategory());
        existing.setFavorite(request.isFavorite());
        existing.setPasswordStrength(passwordGeneratorService.evaluateStrength(request.getPassword()));
        credentialRepository.update(existing);
    }

    public void delete(Long userId, Long credentialId) {
        credentialRepository.delete(credentialId, userId);
    }

    public Map<String, Integer> dashboardSummary(Long userId) {
        Map<String, Integer> out = new HashMap<>();
        out.put("total", credentialRepository.countAll(userId));
        out.put("weak", credentialRepository.countWeak(userId));
        out.put("recent", credentialRepository.countRecent(userId, 7));
        return out;
    }

    public String exportVault(Long userId) {
        try {
            List<Map<String, Object>> entries = credentialRepository.search(userId, null, null, "updated", false)
                    .stream()
                    .map(c -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("accountName", c.getAccountName());
                        item.put("username", c.getUsername());
                        item.put("password", encryptionService.decrypt(c.getEncryptedPassword()));
                        item.put("url", c.getUrl());
                        item.put("notes", c.getNotes());
                        item.put("category", c.getCategory());
                        item.put("favorite", c.isFavorite());
                        return item;
                    }).collect(Collectors.toList());
            return encryptionService.encrypt(objectMapper.writeValueAsString(entries));
        } catch (Exception ex) {
            throw new IllegalStateException("Vault export failed", ex);
        }
    }

    public void importVault(Long userId, String encryptedPayload) {
        try {
            String plain = encryptionService.decrypt(encryptedPayload);
            if (plain == null || plain.isBlank()) {
                throw new IllegalArgumentException("Invalid vault payload");
            }
            List<Map<String, Object>> entries = objectMapper.readValue(plain, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> entry : entries) {
                CredentialRequest req = new CredentialRequest();
                req.setAccountName((String) entry.getOrDefault("accountName", "Imported Account"));
                req.setUsername((String) entry.getOrDefault("username", ""));
                req.setPassword((String) entry.getOrDefault("password", ""));
                req.setUrl((String) entry.getOrDefault("url", ""));
                req.setNotes((String) entry.getOrDefault("notes", ""));
                req.setCategory((String) entry.getOrDefault("category", "Imported"));
                Object favorite = entry.get("favorite");
                req.setFavorite(favorite instanceof Boolean && (Boolean) favorite);
                if (!req.getUsername().isBlank() && !req.getPassword().isBlank()) {
                    create(userId, req);
                }
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid vault payload format", ex);
        }
    }

    public Map<String, Object> audit(Long userId) {
        List<Map<String, Object>> entries = list(userId, null, null, "updated", false);
        int weak = 0;
        int missingUrls = 0;
        for (Map<String, Object> e : entries) {
            String strength = (String) e.get("passwordStrength");
            if ("WEAK".equals(strength)) weak++;
            String url = (String) e.get("url");
            if (url == null || url.isBlank()) missingUrls++;
        }
        Map<String, Object> audit = new HashMap<>();
        audit.put("totalEntries", entries.size());
        audit.put("weakPasswords", weak);
        audit.put("entriesWithoutUrl", missingUrls);
        audit.put("recommendation", weak > 0 ? "Rotate weak passwords immediately" : "Vault strength is healthy");
        return audit;
    }
}

