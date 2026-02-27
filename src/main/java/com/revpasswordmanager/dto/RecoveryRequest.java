package com.revpasswordmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class RecoveryRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String newMasterPassword;
    private java.util.List<SecurityQuestionInput> answers;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNewMasterPassword() { return newMasterPassword; }
    public void setNewMasterPassword(String newMasterPassword) { this.newMasterPassword = newMasterPassword; }
    public java.util.List<SecurityQuestionInput> getAnswers() { return answers; }
    public void setAnswers(java.util.List<SecurityQuestionInput> answers) { this.answers = answers; }
}

