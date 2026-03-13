package com.revpasswordmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String masterPassword;
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotEmpty
    private List<SecurityQuestionInput> securityQuestions;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMasterPassword() { return masterPassword; }
    public void setMasterPassword(String masterPassword) { this.masterPassword = masterPassword; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<SecurityQuestionInput> getSecurityQuestions() { return securityQuestions; }
    public void setSecurityQuestions(List<SecurityQuestionInput> securityQuestions) { this.securityQuestions = securityQuestions; }
}

