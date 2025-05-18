package com.localapp.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class Notification {
    private String notificationId;
    private String userId;
    private String postId;
    private String message;
    private String timestamp;

    @DynamoDbPartitionKey
    public String getNotificationId() { return notificationId; }
}