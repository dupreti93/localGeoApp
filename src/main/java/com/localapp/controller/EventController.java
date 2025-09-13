package com.localapp.controller;

import com.localapp.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
}
