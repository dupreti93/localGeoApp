package com.localapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unified service that aggregates data from Ticketmaster and Google Places APIs
 * to provide comprehensive location-based information across different categories.
 * Cost-optimized by using only Google Places for both restaurants and attractions.
 */
@Service
public class PlacesService {
    private static final Logger logger = LoggerFactory.getLogger(PlacesService.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private GooglePlacesService googlePlacesService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * Fetches all types of places (events, restaurants, attractions) for a location.
     *
     * @param location The city or location to search
     * @param latitude Optional latitude for precise location
     * @param longitude Optional longitude for precise location
     * @param date Optional date for events (YYYY-MM-DD format)
     * @return Map containing categorized results
     */
    public Map<String, List<Map<String, Object>>> fetchAllPlaces(String location, Double latitude, Double longitude, String date) {
        Map<String, List<Map<String, Object>>> results = new HashMap<>();

        try {
            // Run all API calls concurrently for better performance
            CompletableFuture<List<Map<String, Object>>> eventsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return eventService.fetchEvents(location, date != null ? date : java.time.LocalDate.now().toString());
                } catch (Exception e) {
                    logger.error("Error fetching events: {}", e.getMessage());
                    return new ArrayList<>();
                }
            }, executorService);

            CompletableFuture<List<Map<String, Object>>> restaurantsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return googlePlacesService.fetchRestaurants(location, latitude, longitude, 5000);
                } catch (Exception e) {
                    logger.error("Error fetching restaurants: {}", e.getMessage());
                    return new ArrayList<>();
                }
            }, executorService);

            CompletableFuture<List<Map<String, Object>>> attractionsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return googlePlacesService.fetchAttractions(location, latitude, longitude, 5000);
                } catch (Exception e) {
                    logger.error("Error fetching attractions: {}", e.getMessage());
                    return new ArrayList<>();
                }
            }, executorService);

            // Wait for all futures to complete
            CompletableFuture.allOf(eventsFuture, restaurantsFuture, attractionsFuture).join();

            // Collect results
            results.put("events", eventsFuture.get());
            results.put("restaurants", restaurantsFuture.get());
            results.put("attractions", attractionsFuture.get());

        } catch (Exception e) {
            logger.error("Error fetching all places: {}", e.getMessage(), e);
            // Return empty lists if there's an error
            results.put("events", new ArrayList<>());
            results.put("restaurants", new ArrayList<>());
            results.put("attractions", new ArrayList<>());
        }

        return results;
    }

    /**
     * Fetches places by category.
     *
     * @param category The category: "events", "restaurants", or "attractions"
     * @param location The city or location to search
     * @param latitude Optional latitude for precise location
     * @param longitude Optional longitude for precise location
     * @param date Optional date for events
     * @return List of places in the specified category
     */
    public List<Map<String, Object>> fetchPlacesByCategory(String category, String location, Double latitude, Double longitude, String date) {
        try {
            return switch (category.toLowerCase()) {
                case "events", "concerts" ->
                    eventService.fetchEvents(location, date != null ? date : java.time.LocalDate.now().toString());

                case "restaurants", "food", "dining" ->
                    googlePlacesService.fetchRestaurants(location, latitude, longitude, 5000);

                case "attractions", "parks", "museums", "nature" ->
                    googlePlacesService.fetchAttractions(location, latitude, longitude, 5000);

                default -> {
                    logger.warn("Unknown category: {}", category);
                    yield new ArrayList<>();
                }
            };
        } catch (Exception e) {
            logger.error("Error fetching places for category {}: {}", category, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets a specific place by ID and source.
     *
     * @param id The place/business/event ID
     * @param source The data source: "ticketmaster", "yelp", or "google_places"
     * @return Place details or null if not found
     */
    public Map<String, Object> getPlaceById(String id, String source) {
        try {
            return switch (source.toLowerCase()) {
                case "ticketmaster" -> eventService.getEventById(id);
                case "google_places" -> googlePlacesService.getPlaceById(id);
                default -> {
                    logger.warn("Unknown source: {}", source);
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Error fetching place by ID {} from source {}: {}", id, source, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Search places across all categories with a unified query.
     *
     * @param query Search query
     * @param location Location to search in
     * @param latitude Optional latitude
     * @param longitude Optional longitude
     * @return Map of categorized search results
     */
    public Map<String, List<Map<String, Object>>> searchAllPlaces(String query, String location, Double latitude, Double longitude) {
        Map<String, List<Map<String, Object>>> results = new HashMap<>();

        try {
            // Fetch all places and filter by query
            Map<String, List<Map<String, Object>>> allPlaces = fetchAllPlaces(location, latitude, longitude, null);

            // Filter results based on query (case-insensitive)
            String lowerQuery = query.toLowerCase();

            results.put("events", filterByQuery(allPlaces.get("events"), lowerQuery));
            results.put("restaurants", filterByQuery(allPlaces.get("restaurants"), lowerQuery));
            results.put("attractions", filterByQuery(allPlaces.get("attractions"), lowerQuery));

        } catch (Exception e) {
            logger.error("Error searching places with query {}: {}", query, e.getMessage(), e);
            results.put("events", new ArrayList<>());
            results.put("restaurants", new ArrayList<>());
            results.put("attractions", new ArrayList<>());
        }

        return results;
    }

    /**
     * Filters a list of places based on a search query.
     */
    private List<Map<String, Object>> filterByQuery(List<Map<String, Object>> places, String query) {
        return places.stream()
            .filter(place -> {
                String name = (String) place.get("name");
                String address = (String) place.get("address");
                return (name != null && name.toLowerCase().contains(query)) ||
                       (address != null && address.toLowerCase().contains(query));
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
