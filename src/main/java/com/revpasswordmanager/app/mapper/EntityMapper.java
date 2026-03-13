package com.revpasswordmanager.app.mapper;

import com.revpasswordmanager.app.dto.*;
import com.revpasswordmanager.app.entity.*;

import java.util.HashMap;
import java.util.Map;


public class EntityMapper {



    public static User toUser(UserRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        return user;
    }

    public static UserResponseDTO toUserResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setTwoFactorEnabled(user.getTwoFactorEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }


    public static Credential toCredential(CredentialRequestDTO dto, User user) {
        Credential c = new Credential();
        c.setUser(user);
        c.setSiteName(dto.getSiteName());
        c.setSiteUrl(dto.getSiteUrl());
        c.setSiteUsername(dto.getSiteUsername());
        c.setNotes(dto.getNotes());
        c.setCategory(dto.getCategory());
        c.setFavorite(dto.getFavorite() != null ? dto.getFavorite() : false);
        return c;
    }

    public static CredentialResponseDTO toCredentialResponse(Credential c, String decryptedPassword) {
        CredentialResponseDTO dto = new CredentialResponseDTO();
        dto.setCredentialId(c.getCredentialId());
        dto.setSiteName(c.getSiteName());
        dto.setSiteUrl(c.getSiteUrl());
        dto.setSiteUsername(c.getSiteUsername());
        dto.setDecryptedPassword(decryptedPassword);
        dto.setNotes(c.getNotes());
        dto.setCategory(c.getCategory());
        dto.setFavorite(c.getFavorite());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }



    public static SecurityQuestionDTO toSecurityQuestionDTO(SecurityQuestion sq) {
        SecurityQuestionDTO dto = new SecurityQuestionDTO();
        dto.setQuestionId(sq.getQuestionId());
        dto.setQuestion(sq.getQuestion());

        return dto;
    }



    public static Map<String, Object> toVerificationCodeMap(VerificationCode vc) {
        Map<String, Object> map = new HashMap<>();
        map.put("codeId", vc.getCodeId());
        map.put("code", vc.getCode());
        map.put("purpose", vc.getPurpose());
        map.put("expiresAt", vc.getExpiresAt().toString());
        map.put("used", vc.getUsed());
        map.put("createdAt", vc.getCreatedAt() != null ? vc.getCreatedAt().toString() : null);
        return map;
    }
}
