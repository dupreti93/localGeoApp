package com.localapp.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Data
@DynamoDbBean
public class Like {
    private String likeId;
    @NotBlank(message = "Post ID cannot be empty")
    private String postId;
    private String userId;
    private String timestamp;

    @DynamoDbPartitionKey
    public String getLikeId() { return likeId; }

    @DynamoDbSecondaryPartitionKey(indexNames = "PostIdIndex")
    public String getPostId() { return postId; }
}