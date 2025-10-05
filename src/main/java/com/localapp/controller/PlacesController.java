package com.localapp.controller;

import com.localapp.service.PlacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for handling all place-related endpoints across multiple categories.
 * Provides unified APIs for events, restaurants, and attractions from different sources.
 */
@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class PlacesController {

    @Autowired
    private PlacesService placesService;

    /**
     * Fetches all places (events, restaurants, attractions) for a location.
     *
     * @param location The city or location to search
     * @param latitude Optional latitude for precise location
     * @param longitude Optional longitude for precise location
     * @param date Optional date for events (YYYY-MM-DD format)
     * @return Map containing categorized results
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAllPlaces(
            @RequestParam String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String date) {

        Map<String, List<Map<String, Object>>> results = placesService.fetchAllPlaces(location, latitude, longitude, date);
        return ResponseEntity.ok(results);
    }

    /**
     * Fetches places by specific category.
     *
     * @param category The category: "events", "restaurants", or "attractions"
     * @param location The city or location to search
     * @param latitude Optional latitude for precise location
     * @param longitude Optional longitude for precise location
     * @param date Optional date for events
     * @return List of places in the specified category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Map<String, Object>>> getPlacesByCategory(
            @PathVariable String category,
            @RequestParam String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String date) {

        List<Map<String, Object>> results = placesService.fetchPlacesByCategory(category, location, latitude, longitude, date);
        return ResponseEntity.ok(results);
    }

    /**
     * Gets events/concerts from Ticketmaster API.
     */
    @GetMapping("/events")
    public ResponseEntity<List<Map<String, Object>>> getEvents(
            @RequestParam String location,
            @RequestParam(required = false) String date) {

        List<Map<String, Object>> events = placesService.fetchPlacesByCategory("events", location, null, null, date);
        return ResponseEntity.ok(events);
    }

    /**
     * Gets restaurants and dining places from Yelp API.
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<Map<String, Object>>> getRestaurants(
            @RequestParam String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        List<Map<String, Object>> restaurants = placesService.fetchPlacesByCategory("restaurants", location, latitude, longitude, null);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Gets parks, museums, and nature attractions from Google Places API.
     */
    @GetMapping("/attractions")
    public ResponseEntity<List<Map<String, Object>>> getAttractions(
            @RequestParam String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        List<Map<String, Object>> attractions = placesService.fetchPlacesByCategory("attractions", location, latitude, longitude, null);
        return ResponseEntity.ok(attractions);
    }

    /**
     * Gets a specific place by ID and source.
     *
     * @param id The place/business/event ID
     * @param source The data source: "ticketmaster", "yelp", or "google_places"
     * @return Place details or 404 if not found
     */
    @GetMapping("/{source}/{id}")
    public ResponseEntity<Map<String, Object>> getPlaceById(
            @PathVariable String source,
            @PathVariable String id) {

        Map<String, Object> place = placesService.getPlaceById(id, source);
        if (place != null) {
            return ResponseEntity.ok(place);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search places across all categories.
     *
     * @param query Search query
     * @param location Location to search in
     * @param latitude Optional latitude
     * @param longitude Optional longitude
     * @return Map of categorized search results
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> searchPlaces(
            @RequestParam String query,
            @RequestParam String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        Map<String, List<Map<String, Object>>> results = placesService.searchAllPlaces(query, location, latitude, longitude);
        return ResponseEntity.ok(results);
    }
}
