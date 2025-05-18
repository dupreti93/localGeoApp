package com.localapp.controller;

import com.github.davidmoten.geo.GeoHash;
import com.localapp.model.Post;
import com.localapp.repository.NotificationRepository;
import com.localapp.repository.PostRepository;
import com.localapp.repository.UserRepository;
import com.localapp.service.PostSearchService;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private final PostSearchService postSearchService;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s";

    @Autowired
    public PostController(PostRepository postRepository,
        UserRepository userRepository,
        NotificationRepository notificationRepository,
        PostSearchService postSearchService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.postSearchService = postSearchService;
    }

    @PostMapping("/pin")
    public Post pinLocation(@Valid @RequestBody Post post, @AuthenticationPrincipal String userId,
                            @RequestParam(defaultValue = "false") boolean shared,
                            @RequestParam(defaultValue = "other") String type) {
        post.setPostId(UUID.randomUUID().toString());
        post.setUserId(userId);
        post.setTimestamp(Instant.now().toString());
        post.setCategory("pin");
        post.setShared(shared);
        post.setType(type);

        JSONObject regionData = getRegionFromCoordinates(post.getLatitude(), post.getLongitude());
        post.setCity(regionData.optString("city", null));
        String geoHash = GeoHash.encodeHash(post.getLatitude(), post.getLongitude(), 6);
        post.setGeoHash(geoHash);

        postRepository.save(post);

        try {
            postSearchService.indexPost(post);
            logger.info("Indexed post in OpenSearch: {}", post.getPostId());
        } catch (IOException e) {
            logger.error("Failed to index post in OpenSearch: {}", post.getPostId(), e);
        }

        return post;
    }

    @GetMapping("/my-pins")
    public List<Post> getMyPins(@AuthenticationPrincipal String userId) {
        return postRepository.findAll().stream()
                .filter(post -> post.getUserId().equals(userId) && "pin".equals(post.getCategory()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/pin/{postId}")
    public void deletePin(@PathVariable String postId, @AuthenticationPrincipal String userId) {
        Post post = postRepository.findAll().stream()
                .filter(p -> p.getPostId().equals(postId) && p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pin not found or not yours"));
        postRepository.delete(post);
    }

    @PutMapping("/pin/{postId}")
    public Post updatePin(@PathVariable String postId, @AuthenticationPrincipal String userId,
                          @Valid @RequestBody Post updatedPost) {
        Post post = postRepository.findAll().stream()
                .filter(p -> p.getPostId().equals(postId) && p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pin not found or not yours"));
        post.setContent(updatedPost.getContent());
        post.setShared(updatedPost.isShared());
        post.setLatitude(updatedPost.getLatitude());
        post.setLongitude(updatedPost.getLongitude());
        post.setGeoHash(GeoHash.encodeHash(post.getLatitude(), post.getLongitude(), 6));
        postRepository.save(post);

        try {
            postSearchService.indexPost(post);
            logger.info("Updated post in OpenSearch: {}", post.getPostId());
        } catch (IOException e) {
            logger.error("Failed to update post in OpenSearch: {}", post.getPostId(), e);
        }

        return post;
    }

    @GetMapping("/search")
    public List<Post> searchPins(@RequestParam String keyword) {
        logger.info("Search pins with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }
        try {
            return postSearchService.searchByKeyword(keyword);
        } catch (IOException e) {
            logger.error("Search failed for keyword: {}", keyword, e);
            throw new RuntimeException("Search failed: " + e.getMessage());
        }
    }

    private JSONObject getRegionFromCoordinates(double lat, double lon) {
        String url = String.format(NOMINATIM_URL, lat, lon);
        String response = restTemplate.getForObject(url, String.class);
        if (response != null) {
            JSONObject json = new JSONObject(response);
            if (json.has("address")) {
                return json.getJSONObject("address");
            }
        }
        return new JSONObject();
    }
}