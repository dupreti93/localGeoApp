package com.localapp.model.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/**
 * Represents a user in the application.
 * This class is mapped to a DynamoDB table and includes basic user information
 * such as username, password, and profile details.
 */
@Data
@DynamoDbBean
public class User {
    private String userId;
    @NotBlank(message = "Username cannot be empty")
    @Size(max = 50, message = "Username must be 50 characters or less")
    private String username;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @Size(max = 50, message = "Display name must be 50 characters or less")
    private String displayName;
    @Size(max = 200, message = "Bio must be 200 characters or less")
    private String bio;

    /**
     * Gets the unique identifier for the user.
     * @return The user's ID
     */
    @DynamoDbPartitionKey
    public String getUserId() { return userId; }

    /**
     * Sets the unique identifier for the user.
     * @param userId The user's ID to set
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * Gets the username used for authentication.
     * @return The username
     */
    @DynamoDbSecondaryPartitionKey(indexNames = "UsernameIndex")
    public String getUsername() { return username; }

    /**
     * Sets the username used for authentication.
     * @param username The username to set
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Gets the user's password (hashed).
     * @return The password
     */
    public String getPassword() { return password; }

    /**
     * Sets the user's password.
     * @param password The password to set
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Gets the user's display name shown in the UI.
     * @return The display name
     */
    public String getDisplayName() { return displayName; }

    /**
     * Sets the user's display name.
     * @param displayName The display name to set
     */
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    /**
     * Gets the user's bio/description.
     * @return The bio text
     */
    public String getBio() { return bio; }

    /**
     * Sets the user's bio/description.
     * @param bio The bio text to set
     */
    public void setBio(String bio) { this.bio = bio; }
}
