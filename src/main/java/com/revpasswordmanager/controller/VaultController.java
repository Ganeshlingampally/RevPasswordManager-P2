package com.revpasswordmanager.controller;

import com.revpasswordmanager.dto.CredentialRequest;
import com.revpasswordmanager.service.VaultService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final VaultService vaultService;
    private final int reauthWindowSeconds;

    public VaultController(VaultService vaultService,
                           @Value("${app.security.reauth-window-seconds}") int reauthWindowSeconds) {
        this.vaultService = vaultService;
        this.reauthWindowSeconds = reauthWindowSeconds;
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CredentialRequest request, HttpSession session) {
        Long userId = requireUserId(session);
        return Map.of("id", vaultService.create(userId, request), "message", "Credential created");
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false, defaultValue = "updated") String sort,
                                          @RequestParam(required = false, defaultValue = "false") boolean favoritesOnly,
                                          HttpSession session) {
        Long userId = requireUserId(session);
        return vaultService.list(userId, q, category, sort, favoritesOnly);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id, HttpSession session) {
        Long userId = requireUserId(session);
        validateReauth(session);
        return vaultService.getWithSecret(userId, id);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id,
                                      @Valid @RequestBody CredentialRequest request,
                                      HttpSession session) {
        Long userId = requireUserId(session);
        vaultService.update(userId, id, request);
        return Map.of("message", "Credential updated");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpSession session) {
        Long userId = requireUserId(session);
        vaultService.delete(userId, id);
        return Map.of("message", "Credential deleted");
    }

    @GetMapping("/dashboard/summary")
    public Map<String, Integer> summary(HttpSession session) {
        Long userId = requireUserId(session);
        return vaultService.dashboardSummary(userId);
    }

    @GetMapping("/audit")
    public Map<String, Object> audit(HttpSession session) {
        Long userId = requireUserId(session);
        return vaultService.audit(userId);
    }

    @PostMapping("/export")
    public Map<String, String> export(HttpSession session) {
        Long userId = requireUserId(session);
        validateReauth(session);
        return Map.of("encryptedVault", vaultService.exportVault(userId));
    }

    @PostMapping("/import")
    public Map<String, String> importVault(@RequestBody Map<String, String> payload, HttpSession session) {
        Long userId = requireUserId(session);
        validateReauth(session);
        vaultService.importVault(userId, payload.get("encryptedVault"));
        return Map.of("message", "Vault import processed");
    }

    private Long requireUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return userId;
    }

    private void validateReauth(HttpSession session) {
        Object reauthAt = session.getAttribute("REAUTH_AT");
        if (!(reauthAt instanceof Long)) {
            throw new IllegalArgumentException("Re-authentication required");
        }
        long secondsSinceReauth = Instant.now().getEpochSecond() - (Long) reauthAt;
        if (secondsSinceReauth > reauthWindowSeconds) {
            throw new IllegalArgumentException("Re-authentication window expired");
        }
    }
}

