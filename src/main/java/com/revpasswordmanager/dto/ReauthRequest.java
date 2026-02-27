package com.revpasswordmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class ReauthRequest {
    @NotBlank
    private String masterPassword;

    public String getMasterPassword() { return masterPassword; }
    public void setMasterPassword(String masterPassword) { this.masterPassword = masterPassword; }
}

