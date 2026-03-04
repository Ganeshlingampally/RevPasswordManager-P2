package com.revpasswordmanager.app.dto;

import java.util.List;


public class PasswordRecoveryDTO {
    private String username;
    private String newMasterPassword;
    private String otpCode;
    private List<SecurityQuestionDTO> securityAnswers;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNewMasterPassword() {
        return newMasterPassword;
    }

    public void setNewMasterPassword(String newMasterPassword) {
        this.newMasterPassword = newMasterPassword;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public List<SecurityQuestionDTO> getSecurityAnswers() {
        return securityAnswers;
    }

    public void setSecurityAnswers(List<SecurityQuestionDTO> securityAnswers) {
        this.securityAnswers = securityAnswers;
    }
}
