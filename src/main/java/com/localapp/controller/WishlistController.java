package com.localapp.controller;

import com.github.davidmoten.geo.GeoHash;
import com.localapp.model.helpers.PlaceResponse;
import com.localapp.model.WishlistItem;
import com.localapp.repository.WishlistRepository;
import com.localapp.service.AppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final AppConfigService appConfigService;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PLACES_API_URL = "https://places.googleapis.com/v1/places/%s?fields=types,displayName&key=%s";

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
        String apiKey = appConfigService.getGoogleApiKey();
        String url = String.format(PLACES_API_URL, placeId, apiKey);
        ResponseEntity<PlaceResponse> response = restTemplate.getForEntity(url, PlaceResponse.class);
        List<String> types = response.getBody().getTypes();
        if (types.contains("premise") || types.contains("street_address") || types.contains("subpremise")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only public locations allowed");
        }

        item.setId(UUID.randomUUID().toString());
        item.setUserId(userId);
        item.setTimestamp(Instant.now().toString());
        item.setCity(response.getBody().getDisplayName().getText());
        String geoHash = GeoHash.encodeHash(item.getLatitude(), item.getLongitude(), 6);
        item.setGeoHash(geoHash);

        wishlistRepository.save(item);
        return item;
    }

    @GetMapping
    public List<WishlistItem> getWishlist(@AuthenticationPrincipal String userId) {
        return wishlistRepository.findByUserId(userId);
    }
}