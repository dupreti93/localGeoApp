package com.localapp.model;

import lombok.Data;

import jakarta.validation.constraints.Size;

@Data
public class UserProfileDTO {
    @Size(max = 50, message = "Display name must be 50 characters or less")
    private String displayName;
    @Size(max = 200, message = "Bio must be 200 characters or less")
    private String bio;
}