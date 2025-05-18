package com.localapp.repository;

import com.localapp.model.Notification;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NotificationRepository {
    private final DynamoDbTable<Notification> notificationTable;

    public NotificationRepository(DynamoDbEnhancedClient dynamoDbClient) {
        this.notificationTable = dynamoDbClient.table("Notifications", TableSchema.fromBean(Notification.class));
    }

    public void save(Notification notification) {
        notificationTable.putItem(notification);
    }

    public List<Notification> findByUserId(String userId) {
        return notificationTable.scan().items().stream()
                .filter(notification -> userId.equals(notification.getUserId()))
                .collect(Collectors.toList());
    }
}