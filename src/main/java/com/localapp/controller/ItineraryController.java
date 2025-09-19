package com.localapp.controller;

import com.localapp.model.Itinerary;
import com.localapp.model.Itinerary.ItineraryStatus;
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

        // Validate eventId is not null or empty
        if (itinerary.getEventId() == null || itinerary.getEventId().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventId cannot be null or empty");
        }

        // Validate and normalize status
        ItineraryStatus status;
        try {
            status = ItineraryStatus.fromString(itinerary.getStatus().name());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be 'GOING' or 'INTERESTED'");
        }
        itinerary.setUserId(userId);
        itinerary.setStatus(status);
        itineraryService.saveOrUpdateItinerary(itinerary);
        return ResponseEntity.ok().build();
    }
}
