package com.revpasswordmanager.mapper;

import com.revpasswordmanager.dto.CredentialRequest;
import com.revpasswordmanager.model.Credential;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CredentialMapper {

    public Credential toEntity(CredentialRequest request, Long userId, String encryptedPassword,
            String passwordStrength) {
        if (request == null) {
            return null;
        }
        Credential credential = new Credential();
        credential.setUserId(userId);
        credential.setAccountName(request.getAccountName());
        credential.setUsername(request.getUsername());
        credential.setEncryptedPassword(encryptedPassword);
        credential.setUrl(request.getUrl());
        credential.setNotes(request.getNotes());
        credential.setCategory(request.getCategory());
        credential.setFavorite(request.isFavorite());
        credential.setPasswordStrength(passwordStrength);
        return credential;
    }

    public void updateEntityFromRequest(CredentialRequest request, Credential credential, String encryptedPassword,
            String passwordStrength) {
        if (request == null || credential == null) {
            return;
        }
        credential.setAccountName(request.getAccountName());
        credential.setUsername(request.getUsername());
        credential.setEncryptedPassword(encryptedPassword);
        credential.setUrl(request.getUrl());
        credential.setNotes(request.getNotes());
        credential.setCategory(request.getCategory());
        credential.setFavorite(request.isFavorite());
        credential.setPasswordStrength(passwordStrength);
    }

    public Map<String, Object> toResponseMap(Credential credential, String decryptedPassword) {
        if (credential == null) {
            return null;
        }
        Map<String, Object> row = new HashMap<>();
        row.put("id", credential.getId());
        row.put("accountName", credential.getAccountName());
        row.put("username", credential.getUsername());
        if (decryptedPassword != null) {
            row.put("password", decryptedPassword);
        }
        row.put("url", credential.getUrl());
        row.put("notes", credential.getNotes());
        row.put("category", credential.getCategory());
        row.put("favorite", credential.isFavorite());
        row.put("passwordStrength", credential.getPasswordStrength());
        row.put("createdAt", credential.getCreatedAt());
        row.put("updatedAt", credential.getUpdatedAt());
        return row;
    }

    public Map<String, Object> toExportMap(Credential credential, String decryptedPassword) {
        if (credential == null) {
            return null;
        }
        Map<String, Object> row = new HashMap<>();
        row.put("accountName", credential.getAccountName());
        row.put("username", credential.getUsername());
        if (decryptedPassword != null) {
            row.put("password", decryptedPassword);
        }
        row.put("url", credential.getUrl());
        row.put("notes", credential.getNotes());
        row.put("category", credential.getCategory());
        row.put("favorite", credential.isFavorite());
        return row;
    }
}
