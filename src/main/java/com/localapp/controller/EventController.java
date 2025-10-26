package com.localapp.controller;

import com.localapp.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling event-related endpoints.
 * Provides APIs to fetch and search events based on location and date.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    /**
     * Constructs a new EventController with the required EventService.
     * @param eventService Service to handle event operations
     */
    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Fetches events for a specific city and date.
     *
     * @param city The city to search events in
     * @param date The start date for events (format: YYYY-MM-DD)
     * @return List of events with their details
     */
    @GetMapping
    public List<Map<String, Object>> getEvents(
            @RequestParam(defaultValue = "New York") String city,
            @RequestParam(required = false) String date) {

        // If no date provided, use today's date
        if (date == null || date.isEmpty()) {
            date = java.time.LocalDate.now().toString();
        }
        return eventService.fetchEvents(city, date);
    }

    /**
     * Searches for future events by artist name across all locations.
     * Returns events with detailed pricing and availability information.
     *
     * @param artistName The name of the artist/performer to search for
     * @return List of future events for the specified artist with price and availability data
     */
    @GetMapping("/search/artist")
    public ResponseEntity<List<Map<String, Object>>> searchEventsByArtist(
            @RequestParam String artistName) {

        if (artistName == null || artistName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> events = eventService.searchFutureEventsByArtist(artistName.trim());
        return ResponseEntity.ok(events);
    }

    /**
     * New endpoint: Fetch events happening tonight (next 8 hours) near the user.
     * Accepts optional user coordinates to compute distance and travel times, and an optional mood filter.
     *
     * @param city The city to search events in (defaults to "New York")
     * @param lat Optional user latitude
     * @param lon Optional user longitude
     * @param mood Optional mood filter (e.g., Chill, Loud & Social, Date Night, Free & Fun)
     * @return List of aggregated tonight events
     */
    @GetMapping("/tonight")
    public ResponseEntity<List<Map<String, Object>>> getTonightEvents(
            @RequestParam(defaultValue = "New York") String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String mood
    ) {
        List<Map<String, Object>> events = eventService.fetchTonightEvents(city, lat, lon, mood);
        return ResponseEntity.ok(events);
    }
}
