package com.localapp.repository;

import com.localapp.model.Comment;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CommentRepository {
    private final DynamoDbTable<Comment> commentTable;

    public CommentRepository(DynamoDbEnhancedClient dynamoDbClient) {
        this.commentTable = dynamoDbClient.table("Comments", TableSchema.fromBean(Comment.class));
    }

    public void save(Comment comment) {
        commentTable.putItem(comment);
    }

    public List<Comment> findByPostId(String postId) {
        return commentTable.scan().items().stream()
                .filter(comment -> postId.equals(comment.getPostId()))
                .collect(Collectors.toList());
    }
}