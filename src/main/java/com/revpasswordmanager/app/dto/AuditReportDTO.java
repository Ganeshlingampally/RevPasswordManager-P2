package com.revpasswordmanager.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AuditReportDTO {

    private Long userId;
    private String username;
    private int totalCredentials;
    private int weakPasswords;
    private int strongPasswords;
    private int oldPasswords;
    private int reusedPasswords;
    private List<String> categorySummary;
    private LocalDateTime reportGeneratedAt;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalCredentials() {
        return totalCredentials;
    }

    public void setTotalCredentials(int totalCredentials) {
        this.totalCredentials = totalCredentials;
    }

    public int getWeakPasswords() {
        return weakPasswords;
    }

    public void setWeakPasswords(int weakPasswords) {
        this.weakPasswords = weakPasswords;
    }

    public int getStrongPasswords() {
        return strongPasswords;
    }

    public void setStrongPasswords(int strongPasswords) {
        this.strongPasswords = strongPasswords;
    }

    public int getOldPasswords() {
        return oldPasswords;
    }

    public void setOldPasswords(int oldPasswords) {
        this.oldPasswords = oldPasswords;
    }

    public int getReusedPasswords() {
        return reusedPasswords;
    }

    public void setReusedPasswords(int reusedPasswords) {
        this.reusedPasswords = reusedPasswords;
    }

    public List<String> getCategorySummary() {
        return categorySummary;
    }

    public void setCategorySummary(List<String> categorySummary) {
        this.categorySummary = categorySummary;
    }

    public LocalDateTime getReportGeneratedAt() {
        return reportGeneratedAt;
    }

    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }
}
