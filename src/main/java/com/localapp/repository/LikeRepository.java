package com.localapp.repository;

import com.localapp.model.Like;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class LikeRepository {
    private final DynamoDbTable<Like> likeTable;

    public LikeRepository(DynamoDbEnhancedClient dynamoDbClient) {
        this.likeTable = dynamoDbClient.table("Likes", TableSchema.fromBean(Like.class));
    }

    public void save(Like like) {
        likeTable.putItem(like);
    }

    public List<Like> findByPostId(String postId) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(postId).build());
        return likeTable.index("PostIdIndex")
                .query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }
}