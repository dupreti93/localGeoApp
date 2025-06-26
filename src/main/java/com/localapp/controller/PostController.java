package com.localapp.controller;

import com.github.davidmoten.geo.GeoHash;
import com.localapp.model.Post;
import com.localapp.model.helpers.*;
import com.localapp.repository.PostRepository;
import com.localapp.service.AppConfigService;
import com.localapp.service.PostSearchService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostRepository postRepository;
    private final PostSearchService postSearchService;
    private final AppConfigService appConfigService;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String MAPBOX_SUGGEST_URL = "https://api.mapbox.com/search/searchbox/v1/suggest?q=%s&access_token=%s&language=en&limit=10&country=US&types=poi&session_token=%s&proximity=-73.98,40.74";
    private static final String MAPBOX_RETRIEVE_URL = "https://api.mapbox.com/search/searchbox/v1/retrieve/%s?access_token=%s&session_token=%s";
    private static final String MAPBOX_REVERSE_URL = "https://api.mapbox.com/search/searchbox/v1/reverse?longitude=%s&latitude=%s&access_token=%s&language=en&limit=1&country=US&types=poi,address";
    private static final String MAPBOX_TILES_URL = "https://api.mapbox.com/styles/v1/mapbox/%s/tiles/%d/%d/%d?access_token=%s";

    @Autowired
    public PostController(PostRepository postRepository,
                          PostSearchService postSearchService,
                          AppConfigService appConfigService) {
        this.postRepository = postRepository;
        this.postSearchService = postSearchService;
        this.appConfigService = appConfigService;
    }

    @PostMapping("/pin")
    public Post pinLocation(@Valid @RequestBody Post post, @AuthenticationPrincipal String userId,
                            @RequestParam(defaultValue = "false") boolean shared) {
        String placeId = post.getPlaceId();
        if (placeId == null || placeId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Place ID required");
        }
        String apiKey = appConfigService.getMapboxApiKey();
        String sessionToken = UUID.randomUUID().toString();
        String url = String.format(MAPBOX_RETRIEVE_URL, placeId, apiKey, sessionToken);
        logger.info("Formatted Mapbox Retrieve URL is {}", url);

        try {
            ResponseEntity<MapboxRetrieveResponse> response = restTemplate.getForEntity(url, MapboxRetrieveResponse.class);
            MapboxFeature feature = response.getBody().getFeatures().get(0);
            if (!"poi".equals(feature.getProperties().getFeatureType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only public locations (POIs) allowed");
            }

            post.setPostId(UUID.randomUUID().toString());
            post.setUserId(userId);
            post.setTimestamp(Instant.now().toString());
            post.setCategory("pin");
            post.setShared(shared);
            post.setCity(feature.getProperties().getPlaceFormatted());
            post.setLatitude(feature.getProperties().getCoordinates().getLatitude());
            post.setLongitude(feature.getProperties().getCoordinates().getLongitude());
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
        } catch (HttpClientErrorException e) {
            logger.error("Mapbox API error for URL {}: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place details: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch place details: {}", url, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place details", e);
        }
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
        String placeId = updatedPost.getPlaceId();
        if (placeId == null || placeId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Place ID required");
        }
        String apiKey = appConfigService.getMapboxApiKey();
        String sessionToken = UUID.randomUUID().toString();
        String url = String.format(MAPBOX_RETRIEVE_URL, placeId, apiKey, sessionToken);
        logger.info("Formatted Mapbox Retrieve URL is {}", url);

        try {
            ResponseEntity<MapboxRetrieveResponse> response = restTemplate.getForEntity(url, MapboxRetrieveResponse.class);
            MapboxFeature feature = response.getBody().getFeatures().get(0);
            if (!"poi".equals(feature.getProperties().getFeatureType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only public locations (POIs) allowed");
            }

            post.setContent(updatedPost.getContent());
            post.setShared(updatedPost.isShared());
            post.setLatitude(feature.getProperties().getCoordinates().getLatitude());
            post.setLongitude(feature.getProperties().getCoordinates().getLongitude());
            post.setCity(feature.getProperties().getPlaceFormatted());
            post.setGeoHash(GeoHash.encodeHash(post.getLatitude(), post.getLongitude(), 6));
            postRepository.save(post);

            try {
                postSearchService.indexPost(post);
                logger.info("Updated post in OpenSearch: {}", post.getPostId());
            } catch (IOException e) {
                logger.error("Failed to update post in OpenSearch: {}", post.getPostId(), e);
            }

            return post;
        } catch (HttpClientErrorException e) {
            logger.error("Mapbox API error for URL {}: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place details: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch place details: {}", url, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place details", e);
        }
    }

    @GetMapping("/search")
    public List<Post> searchPins(@RequestParam String keyword, @RequestParam(required = false) String userId) {
        logger.info("Search pins with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty");
        }
        try {
            return postSearchService.searchByKeyword(keyword).stream()
                    .filter(post -> userId == null || !post.getUserId().equals(userId))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Search failed for keyword: {}", keyword, e);
            throw new RuntimeException("Search failed: " + e.getMessage());
        }
    }

    @PostMapping("/places")
    @Cacheable("placeSuggestions")
    public ResponseEntity<PlaceAutocompleteResponse> getPlaceSuggestions(@RequestBody PlaceSearchRequest request) {
        logger.info("Place search request is {}", request);
        String apiKey = appConfigService.getMapboxApiKey();
        String sessionToken = UUID.randomUUID().toString();
        String query = URLEncoder.encode(request.getQuery().trim(), StandardCharsets.UTF_8);
        String url = String.format(MAPBOX_SUGGEST_URL, query, apiKey, sessionToken);
        logger.info("Formatted Mapbox Suggest URL is {}", url);

        try {
            ResponseEntity<MapboxSuggestResponse> response = restTemplate.getForEntity(url, MapboxSuggestResponse.class);
            logger.info("Raw Mapbox response: {}", response.getBody());
            PlaceAutocompleteResponse autocompleteResponse = new PlaceAutocompleteResponse();
            autocompleteResponse.setPredictions(response.getBody().getSuggestions().stream()
                    .map(suggestion -> {
                        Prediction prediction = new Prediction();
                        prediction.setPlace_id(suggestion.getMapboxId());
                        prediction.setDescription(suggestion.getName() + ", " + suggestion.getPlaceFormatted());
                        return prediction;
                    })
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(autocompleteResponse);
        } catch (HttpClientErrorException e) {
            logger.error("Mapbox API error for URL {}: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place suggestions: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch place suggestions: {}", url, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place suggestions", e);
        }
    }

    @PostMapping("/places/reverse")
    @Cacheable("placeSuggestions")
    public ResponseEntity<PlaceAutocompleteResponse> getReverseGeocode(@RequestBody PlaceSearchRequest request) {
        logger.info("Reverse geocode request is {}", request);
        String apiKey = appConfigService.getMapboxApiKey();
        String longitude = String.valueOf(request.getLon());
        String latitude = String.valueOf(request.getLat());
        String url = String.format(MAPBOX_REVERSE_URL, longitude, latitude, apiKey);
        logger.info("Formatted Mapbox Reverse URL is {}", url);

        try {
            ResponseEntity<MapboxRetrieveResponse> response = restTemplate.getForEntity(url, MapboxRetrieveResponse.class);
            logger.info("Raw Mapbox reverse response: {}", response.getBody());
            PlaceAutocompleteResponse autocompleteResponse = new PlaceAutocompleteResponse();
            autocompleteResponse.setPredictions(response.getBody().getFeatures().stream()
                    .map(feature -> {
                        Prediction prediction = new Prediction();
                        prediction.setPlace_id(feature.getProperties().getMapboxId());
                        prediction.setDescription(feature.getProperties().getName() + ", " + feature.getProperties().getPlaceFormatted());
                        return prediction;
                    })
                    .collect(Collectors.toList()));
            return ResponseEntity.ok(autocompleteResponse);
        } catch (HttpClientErrorException e) {
            logger.error("Mapbox API error for URL {}: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch reverse geocode: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch reverse geocode: {}", url, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch reverse geocode", e);
        }
    }

    @GetMapping("/mapbox/tiles/{style}/{z}/{x}/{y}")
    public ResponseEntity<byte[]> getMapboxTiles(@PathVariable String style, @PathVariable int z, @PathVariable int x, @PathVariable int y) {
        String mapboxKey = appConfigService.getMapboxApiKey();
        String url = String.format(MAPBOX_TILES_URL, style, z, x, y, mapboxKey);
        logger.info("Formatted mapbox url is {}", url);
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            logger.info("Successfully fetched tile: {}, status: {}", url, response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .header("Content-Type", "image/png")
                    .header("Cache-Control", response.getHeaders().getCacheControl())
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("Mapbox API error for URL {}: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch tile: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch Mapbox tile: {}", url, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch tile", e);
        }
    }

    @GetMapping("/clear-cache")
    @CacheEvict(value = "apiKeys", allEntries = true)
    public ResponseEntity<String> clearApiKeysCache() {
        logger.info("Clearing apiKeys cache");
        return ResponseEntity.ok("API keys cache cleared");
    }
}