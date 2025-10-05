package com.localapp.controller;

import com.localapp.model.dto.SelectedEvent;
import com.localapp.model.entity.Itinerary;
import com.localapp.service.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {
    private final ItineraryService itineraryService;

    @Autowired
    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @GetMapping
    public ResponseEntity<List<Itinerary>> getItinerary(@RequestParam String city,
                                                        @RequestParam String date) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        List<Itinerary> itinerary = itineraryService.getUserItinerary(userId, city, date);
        return ResponseEntity.ok(itinerary);
    }

    @PostMapping
    public ResponseEntity<Void> addOrUpdateItinerary(@RequestBody Itinerary itinerary) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        itinerary.setUserId(userId);
        itineraryService.saveOrUpdateItinerary(itinerary);
        return ResponseEntity.ok().build();
    }

    /**
     * Generate an AI itinerary based on saved events
     */
    @PostMapping("/generate")
    public ResponseEntity<Itinerary> generateAIItinerary(@RequestBody List<SelectedEvent> events) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if (events == null || events.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one event is required");
        }
        try {
            Itinerary aiItinerary = itineraryService.generateItinerary(userId, events);
            return ResponseEntity.ok(aiItinerary);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to generate itinerary: " + e.getMessage(), e);
        }
    }

    /**
     * Get all AI itineraries for the current user
     */
    @GetMapping("/ai")
    public ResponseEntity<List<Itinerary>> getAIItineraries() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        List<Itinerary> itineraries = itineraryService.getUserItineraries(userId);
        return ResponseEntity.ok(itineraries);
    }

    /**
     * Get a specific AI itinerary by ID
     */
    @GetMapping("/ai/{itineraryId}")
    public ResponseEntity<Itinerary> getAIItineraryById(@PathVariable String itineraryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        Itinerary itinerary = itineraryService.getItineraryById(userId, itineraryId);
        if (itinerary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(itinerary);
    }

    /**
     * Delete an AI itinerary
     */
    @DeleteMapping("/ai/{itineraryId}")
    public ResponseEntity<Void> deleteAIItinerary(@PathVariable String itineraryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        itineraryService.deleteItinerary(userId, itineraryId);
        return ResponseEntity.noContent().build();
    }
}
