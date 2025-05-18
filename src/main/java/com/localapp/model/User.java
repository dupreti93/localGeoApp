package com.localapp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

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

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSecondaryPartitionKey(indexNames = "UsernameIndex")
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

