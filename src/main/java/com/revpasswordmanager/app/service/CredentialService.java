package com.revpasswordmanager.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revpasswordmanager.app.config.EncryptionUtil;
import com.revpasswordmanager.app.dto.*;
import com.revpasswordmanager.app.entity.Credential;
import com.revpasswordmanager.app.entity.User;
import com.revpasswordmanager.app.exception.ResourceNotFoundException;
import com.revpasswordmanager.app.mapper.EntityMapper;
import com.revpasswordmanager.app.repository.CredentialRepository;
import com.revpasswordmanager.app.repository.UserRepository;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class CredentialService {

    private static final Logger logger = Logger.getLogger(CredentialService.class);

    @Autowired
    private CredentialRepository credentialRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.encryption.secret-key}")
    private String secretKey;



    @Transactional
    public CredentialResponseDTO createCredential(Long userId, CredentialRequestDTO dto) {
        logger.info("Creating credential for user: " + userId + ", site: " + dto.getSiteName());
        User user = findUser(userId);

        Credential c = EntityMapper.toCredential(dto, user);
        c.setEncryptedPassword(encrypt(dto.getPassword()));
        return toDTO(credentialRepository.save(c));
    }

    public CredentialResponseDTO getCredentialById(Long credentialId, Long userId) {
        return toDTO(findCredential(credentialId, userId));
    }

    public List<CredentialResponseDTO> getAllCredentials(Long userId) {
        return credentialRepository.findByUserUserId(userId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public CredentialResponseDTO updateCredential(Long credentialId, Long userId, CredentialRequestDTO dto) {
        logger.info("Updating credential: " + credentialId);
        Credential c = findCredential(credentialId, userId);

        c.setSiteName(dto.getSiteName());
        c.setSiteUrl(dto.getSiteUrl());
        c.setSiteUsername(dto.getSiteUsername());
        c.setEncryptedPassword(encrypt(dto.getPassword()));
        c.setNotes(dto.getNotes());
        c.setCategory(dto.getCategory());
        if (dto.getFavorite() != null)
            c.setFavorite(dto.getFavorite());
        return toDTO(credentialRepository.save(c));
    }

    @Transactional
    public void deleteCredential(Long credentialId, Long userId) {
        logger.info("Deleting credential: " + credentialId);
        credentialRepository.delete(findCredential(credentialId, userId));
    }

    @Transactional
    public CredentialResponseDTO toggleFavorite(Long credentialId, Long userId) {
        Credential c = findCredential(credentialId, userId);
        c.setFavorite(!Boolean.TRUE.equals(c.getFavorite()));
        return toDTO(credentialRepository.save(c));
    }

    public List<CredentialResponseDTO> getFavorites(Long userId) {
        return credentialRepository.findByUserUserIdAndFavoriteTrue(userId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }



    public List<CredentialResponseDTO> searchCredentials(Long userId, String keyword) {
        return credentialRepository.searchByKeyword(userId, keyword).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<CredentialResponseDTO> filterByCategory(Long userId, String category) {
        return credentialRepository.findByUserUserIdAndCategory(userId, category).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<CredentialResponseDTO> sortCredentials(Long userId, String sortBy) {
        List<Credential> list = switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "created" -> credentialRepository.findByUserIdSortedByCreatedAtDesc(userId);
            case "updated" -> credentialRepository.findByUserIdSortedByUpdatedAtDesc(userId);
            default -> credentialRepository.findByUserIdSortedBySiteName(userId);
        };
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }



    public AuditReportDTO generateAuditReport(Long userId) {
        logger.info("Generating audit report for user: " + userId);
        User user = findUser(userId);
        List<Credential> credentials = credentialRepository.findByUserUserId(userId);

        int weak = 0, strong = 0, old = 0, reused = 0;
        Map<String, Integer> categories = new HashMap<>();
        Set<String> seenPasswords = new java.util.HashSet<>();
        Set<String> reusedSet = new java.util.HashSet<>();
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

        for (Credential c : credentials) {
            String password = decrypt(c.getEncryptedPassword());
            int score = passwordGeneratorService.calculateStrengthScore(password);
            if (score < 40)
                weak++;
            else if (score >= 60)
                strong++;

            if (c.getUpdatedAt() != null && c.getUpdatedAt().isBefore(ninetyDaysAgo))
                old++;


            if (!seenPasswords.add(password) && reusedSet.add(password))
                reused++;

            String cat = c.getCategory() != null ? c.getCategory() : "Uncategorized";
            categories.merge(cat, 1, Integer::sum);
        }

        AuditReportDTO report = new AuditReportDTO();
        report.setUserId(userId);
        report.setUsername(user.getUsername());
        report.setTotalCredentials(credentials.size());
        report.setWeakPasswords(weak);
        report.setStrongPasswords(strong);
        report.setOldPasswords(old);
        report.setReusedPasswords(reused);
        report.setCategorySummary(categories.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.toList()));
        report.setReportGeneratedAt(LocalDateTime.now());
        return report;
    }



    private Credential findCredential(Long credentialId, Long userId) {
        Credential c = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found: " + credentialId));
        if (!c.getUser().getUserId().equals(userId))
            throw new ResourceNotFoundException("Credential doesn't belong to user: " + userId);
        return c;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private String encrypt(String plainText) {
        try {
            return EncryptionUtil.encrypt(plainText, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed");
        }
    }

    private String decrypt(String cipherText) {
        try {
            return EncryptionUtil.decrypt(cipherText, secretKey);
        } catch (Exception e) {
            return "[DECRYPTION_FAILED]";
        }
    }

    private CredentialResponseDTO toDTO(Credential c) {
        return EntityMapper.toCredentialResponse(c, decrypt(c.getEncryptedPassword()));
    }
}
