package com.localapp.model.dto;

import jakarta.validation.constraints.Size;

public class UserProfileDTO {
    @Size(max = 50, message = "Display name must be 50 characters or less")
    private String displayName;
    @Size(max = 200, message = "Bio must be 200 characters or less")
    private String bio;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}