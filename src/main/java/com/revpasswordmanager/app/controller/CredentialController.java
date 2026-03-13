package com.revpasswordmanager.app.controller;

import com.revpasswordmanager.app.dto.*;
import com.revpasswordmanager.app.service.CredentialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    @Autowired
    private CredentialService credentialService;


    @PostMapping("/{userId}")
    public ResponseEntity<CredentialResponseDTO> create(
            @PathVariable Long userId, @RequestBody CredentialRequestDTO dto) {
        return new ResponseEntity<>(credentialService.createCredential(userId, dto), HttpStatus.CREATED);
    }

    @GetMapping("/{credentialId}/user/{userId}")
    public ResponseEntity<CredentialResponseDTO> getById(
            @PathVariable Long credentialId, @PathVariable Long userId) {
        return ResponseEntity.ok(credentialService.getCredentialById(credentialId, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CredentialResponseDTO>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(credentialService.getAllCredentials(userId));
    }

    @PutMapping("/{credentialId}/user/{userId}")
    public ResponseEntity<CredentialResponseDTO> update(
            @PathVariable Long credentialId, @PathVariable Long userId,
            @RequestBody CredentialRequestDTO dto) {
        return ResponseEntity.ok(credentialService.updateCredential(credentialId, userId, dto));
    }

    @DeleteMapping("/{credentialId}/user/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long credentialId, @PathVariable Long userId) {
        credentialService.deleteCredential(credentialId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{credentialId}/user/{userId}/favorite")
    public ResponseEntity<CredentialResponseDTO> toggleFavorite(
            @PathVariable Long credentialId, @PathVariable Long userId) {
        return ResponseEntity.ok(credentialService.toggleFavorite(credentialId, userId));
    }

    @GetMapping("/user/{userId}/favorites")
    public ResponseEntity<List<CredentialResponseDTO>> getFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(credentialService.getFavorites(userId));
    }


    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<CredentialResponseDTO>> search(
            @PathVariable Long userId, @RequestParam String keyword) {
        return ResponseEntity.ok(credentialService.searchCredentials(userId, keyword));
    }

    @GetMapping("/user/{userId}/filter")
    public ResponseEntity<List<CredentialResponseDTO>> filter(
            @PathVariable Long userId, @RequestParam String category) {
        return ResponseEntity.ok(credentialService.filterByCategory(userId, category));
    }

    @GetMapping("/user/{userId}/sort")
    public ResponseEntity<List<CredentialResponseDTO>> sort(
            @PathVariable Long userId, @RequestParam(defaultValue = "name") String sortBy) {
        return ResponseEntity.ok(credentialService.sortCredentials(userId, sortBy));
    }


    @GetMapping("/user/{userId}/audit")
    public ResponseEntity<AuditReportDTO> audit(@PathVariable Long userId) {
        return ResponseEntity.ok(credentialService.generateAuditReport(userId));
    }

}
