package com.localapp.repository;

import com.localapp.model.entity.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserRepository {
    private final DynamoDbTable<User> userTable;

    public UserRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.userTable = dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(User.class));
    }

    public User findByUserId(String userId) {
        return userTable.getItem(GetItemEnhancedRequest.builder().key(k -> k.partitionValue(userId)).build());
    }

    public User findByUsername(String username) {
        Key key = Key.builder().partitionValue(username).build();
        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .build();
        return userTable.index("UsernameIndex")
                .query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst()
                .orElse(null);
    }

    public void save(User user) {
        userTable.putItem(user);
    }

    public List<User> findAll() {
        return userTable.scan().items().stream().collect(Collectors.toList());
    }

    public void updateProfile(String userId, String displayName, String bio) {
        User user = findByUserId(userId);
        if (user != null) {
            user.setDisplayName(displayName);
            user.setBio(bio);
            save(user);
        }
    }
}