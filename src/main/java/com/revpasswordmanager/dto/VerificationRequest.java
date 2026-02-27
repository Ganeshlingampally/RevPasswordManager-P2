package com.revpasswordmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class VerificationRequest {
    @NotBlank
    private String purpose;
    @NotBlank
    private String code;

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

