package com.localapp.controller;

import com.github.davidmoten.geo.GeoHash;
import com.localapp.model.WishlistItem;
import com.localapp.model.helpers.MapboxFeature;
import com.localapp.model.helpers.MapboxRetrieveResponse;
import com.localapp.repository.WishlistRepository;
import com.localapp.service.AppConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    private final WishlistRepository wishlistRepository;
    private final AppConfigService appConfigService;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String MAPBOX_RETRIEVE_URL = "https://api.mapbox.com/search/searchbox/v1/retrieve/%s?access_token=%s&session_token=%s";

    @Autowired
    public WishlistController(WishlistRepository wishlistRepository, AppConfigService appConfigService) {
        this.wishlistRepository = wishlistRepository;
        this.appConfigService = appConfigService;
    }

    @PostMapping
    public WishlistItem addToWishlist(@RequestBody WishlistItem item, @AuthenticationPrincipal String userId) {
        String placeId = item.getPlaceId();
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

            item.setId(UUID.randomUUID().toString());
            item.setUserId(userId);
            item.setTimestamp(Instant.now().toString());
            item.setCity(feature.getProperties().getPlaceFormatted());
            item.setLatitude(feature.getProperties().getCoordinates().getLatitude());
            item.setLongitude(feature.getProperties().getCoordinates().getLongitude());
            String geoHash = GeoHash.encodeHash(item.getLatitude(), item.getLongitude(), 6);
            item.setGeoHash(geoHash);

            wishlistRepository.save(item);
            return item;
        } catch (HttpClientErrorException e) {
            logger.error("Mapbox API error for URL {}: {} {}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place details: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to fetch place details: {}", url, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch place details", e);
        }
    }

    @GetMapping
    public List<WishlistItem> getWishlist(@AuthenticationPrincipal String userId) {
        return wishlistRepository.findByUserId(userId);
    }
}