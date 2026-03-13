package com.revpasswordmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String masterPassword;
    private String twoFaCode;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMasterPassword() { return masterPassword; }
    public void setMasterPassword(String masterPassword) { this.masterPassword = masterPassword; }
    public String getTwoFaCode() { return twoFaCode; }
    public void setTwoFaCode(String twoFaCode) { this.twoFaCode = twoFaCode; }
}

