package com.localapp.repository;

import com.localapp.model.WishlistItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class WishlistRepository {
    private final DynamoDbTable<WishlistItem> wishlistTable;

    public WishlistRepository(DynamoDbEnhancedClient enhancedClient) {
        this.wishlistTable = enhancedClient.table("Wishlist", TableSchema.fromBean(WishlistItem.class));
    }

    public void save(WishlistItem item) {
        wishlistTable.putItem(item);
    }

    public List<WishlistItem> findByUserId(String userId) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":userId", AttributeValue.builder().s(userId).build());

        Expression filterExpression = Expression.builder()
                .expression("userId = :userId")
                .expressionValues(expressionValues)
                .build();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        return wishlistTable.scan(scanRequest)
                .items()
                .stream()
                .collect(Collectors.toList());
    }
}