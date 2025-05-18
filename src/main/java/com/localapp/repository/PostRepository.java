package com.localapp.repository;

import com.localapp.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.localapp.util.GeoUtils.calculateDistance;

@Repository
public class PostRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostRepository.class);
    private final DynamoDbTable<Post> postTable;

    public PostRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.postTable = dynamoDbEnhancedClient.table("Posts", TableSchema.fromBean(Post.class));
    }

    public List<Post> findAll() {
        return postTable.scan(ScanEnhancedRequest.builder().build())
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public void save(Post post) {
        postTable.putItem(post);
    }

    public List<Post> findPostsByRegion(String regionType, String regionValue) {
        return postTable.scan().items().stream()
                .filter(post ->
                        "city".equals(regionType) && regionValue.equalsIgnoreCase(post.getCity()))
                .collect(Collectors.toList());
    }

    public List<Post> findPostsNear(double lat, double lon, double radiusMiles) {
        List<Post> allPosts = postTable.scan().items().stream().collect(Collectors.toList());
        logger.info("All posts: {}", allPosts);
        List<Post> nearbyPosts = allPosts.stream()
                .filter(post -> calculateDistance(lat, lon, post.getLatitude(), post.getLongitude()) <= radiusMiles)
                .collect(Collectors.toList());
        logger.info("Nearby within {} miles: {}", radiusMiles, nearbyPosts);
        return nearbyPosts;
    }

    public void delete(Post post) {
        postTable.deleteItem(post);
    }
}