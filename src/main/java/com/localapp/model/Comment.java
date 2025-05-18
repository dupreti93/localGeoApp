package com.localapp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    @NotBlank(message = "Text cannot be empty")
    @Size(max = 200, message = "Text must be 200 characters or less")
    private String text;
    private String timestamp;

    @DynamoDbPartitionKey
    public String getCommentId() { return commentId; }
}