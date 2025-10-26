package com.localapp.model.dto;

import jakarta.validation.constraints.Size;

public class UserProfileDTO {
    @Size(max = 100)
    private String displayName;

    @Size(max = 500)
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

