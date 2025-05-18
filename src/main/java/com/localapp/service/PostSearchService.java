package com.localapp.service;

import com.localapp.model.Post;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostSearchService {
    private final OpenSearchClient client;

    @Autowired
    public PostSearchService(OpenSearchClient client) {
        this.client = client;
    }

    public void indexPost(Post post) throws IOException {
        IndexRequest<Post> request = new IndexRequest.Builder<Post>()
                .index("posts")
                .id(post.getPostId())
                .document(post)
                .build();
        IndexResponse response = client.index(request);
        System.out.println("Indexed post: " + response.id());
    }

    public List<Post> searchByKeyword(String keyword) throws IOException {
        SearchRequest request = new SearchRequest.Builder()
                .index("posts")
                .query(q -> q.multiMatch(m -> m
                        .fields("content", "type") // Search content and type fields
                        .query(keyword)
                        .fuzziness("AUTO")
                ))
                .build();
        SearchResponse<Post> response = client.search(request, Post.class);
        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }
}