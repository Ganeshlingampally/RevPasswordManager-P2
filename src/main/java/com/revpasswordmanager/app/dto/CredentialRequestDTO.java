package com.revpasswordmanager.app.dto;

import jakarta.validation.constraints.NotBlank;

public class CredentialRequestDTO {

    @NotBlank(message = "Site name is required")
    private String siteName;

    private String siteUrl;

    @NotBlank(message = "Site username is required")
    private String siteUsername;

    @NotBlank(message = "Password is required")
    private String password;

    private String notes;
    private String category;
    private Boolean favorite;


    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getSiteUsername() {
        return siteUsername;
    }

    public void setSiteUsername(String siteUsername) {
        this.siteUsername = siteUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }
}
