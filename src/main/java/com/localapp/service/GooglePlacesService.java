package com.localapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service using the actual old/deprecated Google Places API that doesn't require billing.
 * Uses the legacy Web Service API endpoints that are still free.
 */
@Service
public class GooglePlacesService {
    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesService.class);
    private final AppConfigService appConfigService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Using the OLD Google Places Web Service API (deprecated but still free)
    private static final String GOOGLE_PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    public GooglePlacesService(AppConfigService appConfigService, RestTemplate restTemplate) {
        this.appConfigService = appConfigService;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches restaurants using the old Google Places Nearby Search (deprecated but free).
     */
    public List<Map<String, Object>> fetchRestaurants(String location, Double latitude, Double longitude, Integer radius) {
        // First get coordinates if not provided
        if (latitude == null || longitude == null) {
            double[] coords = getCoordinatesFromGeocoding(location);
            latitude = coords[0];
            longitude = coords[1];
        }

        return fetchPlacesWithOldNearbyAPI(latitude, longitude, radius, "restaurant|cafe|bar|food", "Food/Restaurants");
    }

    /**
     * Fetches attractions using the old Google Places Nearby Search (deprecated but free).
     */
    public List<Map<String, Object>> fetchAttractions(String location, Double latitude, Double longitude, Integer radius) {
        // First get coordinates if not provided
        if (latitude == null || longitude == null) {
            double[] coords = getCoordinatesFromGeocoding(location);
            latitude = coords[0];
            longitude = coords[1];
        }

        return fetchPlacesWithOldNearbyAPI(latitude, longitude, radius, "museum|park|zoo|tourist_attraction", "Parks/Museums/Nature");
    }

    /**
     * Uses the OLD Google Places Nearby Search API (deprecated but still works without billing).
     */
    private List<Map<String, Object>> fetchPlacesWithOldNearbyAPI(Double latitude, Double longitude, Integer radius, String types, String category) {
        try {
            String apiKey = appConfigService.getGooglePlacesApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("Google API key not found in configuration");
                return new ArrayList<>();
            }

            // Use the OLD nearbysearch endpoint (deprecated but free)
            String url = String.format("%s/nearbysearch/json?location=%f,%f&radius=%d&type=%s&key=%s",
                GOOGLE_PLACES_API_BASE, latitude, longitude,
                radius != null ? radius : 5000, types.split("\\|")[0], apiKey);

            logger.info("Making OLD Places API call to: {}", url.replace(apiKey, "***"));

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return parseOldPlacesResponse(response.getBody(), category);

        } catch (Exception e) {
            logger.error("Error fetching {} from OLD Google Places API: {}", category, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets coordinates using the old Geocoding API (still free with limits).
     */
    private double[] getCoordinatesFromGeocoding(String location) {
        try {
            String apiKey = appConfigService.getGooglePlacesApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                return getDefaultCoordinates(location);
            }

            String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                location.replace(" ", "+"), apiKey);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if ("OK".equals(rootNode.path("status").asText())) {
                JsonNode results = rootNode.path("results");
                if (results.isArray() && results.size() > 0) {
                    JsonNode location_node = results.get(0).path("geometry").path("location");
                    double lat = location_node.path("lat").asDouble();
                    double lng = location_node.path("lng").asDouble();
                    logger.info("Got coordinates for {}: {}, {}", location, lat, lng);
                    return new double[]{lat, lng};
                }
            }
        } catch (Exception e) {
            logger.error("Error getting coordinates for {}: {}", location, e.getMessage());
        }

        return getDefaultCoordinates(location);
    }

    /**
     * Fallback coordinates for major cities.
     */
    private double[] getDefaultCoordinates(String location) {
        String lowerLocation = location.toLowerCase();
        if (lowerLocation.contains("new york")) return new double[]{40.7128, -74.0060};
        if (lowerLocation.contains("los angeles")) return new double[]{34.0522, -118.2437};
        if (lowerLocation.contains("chicago")) return new double[]{41.8781, -87.6298};
        if (lowerLocation.contains("houston")) return new double[]{29.7604, -95.3698};
        if (lowerLocation.contains("san francisco")) return new double[]{37.7749, -122.4194};
        // Default to New York if unknown
        return new double[]{40.7128, -74.0060};
    }

    /**
     * Parses the old Google Places API response.
     */
    private List<Map<String, Object>> parseOldPlacesResponse(String responseBody, String category) {
        List<Map<String, Object>> places = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String status = rootNode.path("status").asText();

            logger.info("API Response status: {}", status);

            if ("OVER_QUERY_LIMIT".equals(status)) {
                logger.warn("Google Places API quota exceeded");
                return places;
            }

            if ("REQUEST_DENIED".equals(status)) {
                logger.error("Google Places API request denied - check API key and billing");
                return places;
            }

            if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
                logger.warn("Google Places API returned status: {}", status);
                return places;
            }

            JsonNode results = rootNode.path("results");
            logger.info("Found {} results for category: {}", results.size(), category);

            for (JsonNode place : results) {
                Map<String, Object> placeMap = new HashMap<>();

                placeMap.put("id", place.path("place_id").asText());
                placeMap.put("name", place.path("name").asText());
                placeMap.put("category", category);
                placeMap.put("source", "google_places");

                // Rating and reviews (available in free tier)
                if (!place.path("rating").isMissingNode()) {
                    placeMap.put("rating", place.path("rating").asDouble());
                }
                if (!place.path("user_ratings_total").isMissingNode()) {
                    placeMap.put("userRatingsTotal", place.path("user_ratings_total").asInt());
                }

                // Location info (free)
                placeMap.put("address", place.path("formatted_address").asText());
                placeMap.put("vicinity", place.path("vicinity").asText());

                // Coordinates (free)
                JsonNode geometry = place.path("geometry");
                JsonNode location = geometry.path("location");
                if (!location.isMissingNode()) {
                    placeMap.put("latitude", location.path("lat").asDouble());
                    placeMap.put("longitude", location.path("lng").asDouble());
                }

                // Price level (free if available)
                if (!place.path("price_level").isMissingNode()) {
                    int priceLevel = place.path("price_level").asInt();
                    placeMap.put("priceLevel", priceLevel);
                    if (priceLevel > 0) {
                        placeMap.put("price", "$".repeat(priceLevel));
                    }
                }

                // Opening hours (free basic info)
                JsonNode openingHours = place.path("opening_hours");
                if (!openingHours.isMissingNode()) {
                    placeMap.put("isOpenNow", openingHours.path("open_now").asBoolean());
                }

                // Business status (free)
                if (!place.path("business_status").isMissingNode()) {
                    String businessStatus = place.path("business_status").asText();
                    placeMap.put("businessStatus", businessStatus);
                    placeMap.put("isOperational", "OPERATIONAL".equals(businessStatus));
                }

                // Types (free)
                List<String> types = new ArrayList<>();
                for (JsonNode typeNode : place.path("types")) {
                    types.add(typeNode.asText());
                }
                placeMap.put("types", types);

                // Photo reference (free, but actual photo costs)
                JsonNode photos = place.path("photos");
                if (photos.isArray() && !photos.isEmpty()) {
                    String photoReference = photos.get(0).path("photo_reference").asText();
                    placeMap.put("photoReference", photoReference);
                    // Skip photo URL to avoid charges
                }

                places.add(placeMap);
            }

        } catch (Exception e) {
            logger.error("Error parsing Google Places response: {}", e.getMessage(), e);
        }

        return places;
    }

    /**
     * Gets place details using legacy API (basic fields only to stay free).
     */
    public Map<String, Object> getPlaceById(String placeId) {
        try {
            String apiKey = appConfigService.getGooglePlacesApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("Google API key not found in configuration");
                return null;
            }

            // Use basic fields only to minimize costs
            String url = String.format("%s/details/json?place_id=%s&fields=name,rating,formatted_address,geometry&key=%s",
                GOOGLE_PLACES_API_BASE, placeId, apiKey);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode result = rootNode.path("result");

            if (!result.isMissingNode()) {
                List<Map<String, Object>> places = parseOldPlacesResponse("{\"results\":[" + result + "], \"status\":\"OK\"}", "Place Details");
                return places.isEmpty() ? null : places.get(0);
            }

            return null;

        } catch (Exception e) {
            logger.error("Error fetching place by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Skip photo URLs to avoid charges.
     */
    public String getPhotoUrl(String photoReference, Integer maxWidth) {
        // Skip photo API to avoid charges
        return null;
    }
}
