package com.revpasswordmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class CredentialRequest {
    @NotBlank
    private String accountName;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String url;
    private String notes;
    private String category;
    private boolean favorite;

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}

