package com.localapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for fetching and managing events from Ticketmaster API.
 */
@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final AppConfigService appConfigService;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TICKETMASTER_API = "https://app.ticketmaster.com/discovery/v2/events.json";

    /**
     * Constructs a new EventService with the required AppConfigService for API key management.
     * @param appConfigService Service to retrieve API keys from AWS AppConfig
     */
    public EventService(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    /**
     * Fetches events from Ticketmaster API for a specific city and date.
     *
     * @param city The city to search events in
     * @param date The start date for events (format: YYYY-MM-DD)
     * @return List of events as maps containing event details
     * @throws RuntimeException if there's an error fetching events
     */
    public List<Map<String, Object>> fetchEvents(String city, String date) {
        try {
            String apiKey = appConfigService.getTicketmasterApiKey();
            // Only log high-level info
            logger.info("Calling Ticketmaster API for city: {} and date: {}", city, date);

            // Build URL with properly encoded parameters
            StringBuilder urlBuilder = new StringBuilder(TICKETMASTER_API);
            urlBuilder.append("?apikey=").append(apiKey);
            urlBuilder.append("&city=").append(java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8.name()));
            urlBuilder.append("&startDateTime=").append(date).append("T00:00:00Z");
            urlBuilder.append("&endDateTime=").append(date).append("T23:59:59Z");
            urlBuilder.append("&size=200");

            String url = urlBuilder.toString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> body = mapper.readValue(responseBody, Map.class);

                if (body.containsKey("_embedded") && ((Map)body.get("_embedded")).containsKey("events")) {
                    List<Map<String, Object>> events = (List<Map<String, Object>>) ((Map)body.get("_embedded")).get("events");
                    return events.stream().map(event -> {
                        Map<String, Object> transformed = new HashMap<>();
                        transformed.put("id", event.get("id"));
                        transformed.put("name", event.get("name"));
                        transformed.put("url", event.get("url"));
                        transformed.put("type", event.get("type"));
                        if (event.containsKey("_embedded")) {
                            Map embedded = (Map)event.get("_embedded");
                            if (embedded.containsKey("venues")) {
                                List<Map> venues = (List<Map>)embedded.get("venues");
                                if (!venues.isEmpty()) {
                                    Map venue = venues.get(0);
                                    transformed.put("venue", venue.get("name"));
                                    if (venue.containsKey("location")) {
                                        Map location = (Map)venue.get("location");
                                        transformed.put("latitude", location.get("latitude"));
                                        transformed.put("longitude", location.get("longitude"));
                                    }
                                }
                            }
                        }
                        if (event.containsKey("dates") && ((Map)event.get("dates")).containsKey("start")) {
                            Map dates = (Map)event.get("dates");
                            Map start = (Map)dates.get("start");
                            transformed.put("startDate", start.get("dateTime"));
                        }
                        if (event.containsKey("priceRanges")) {
                            List<Map> priceRanges = (List<Map>)event.get("priceRanges");
                            if (!priceRanges.isEmpty()) {
                                Map priceRange = priceRanges.get(0);
                                transformed.put("minPrice", priceRange.get("min"));
                                transformed.put("maxPrice", priceRange.get("max"));
                                transformed.put("currency", priceRange.get("currency"));
                            }
                        }
                        return transformed;
                    }).collect(Collectors.toList());
                }
            }
            logger.warn("No events found in response");
            return new ArrayList<>();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error from Ticketmaster: Status={}, Body={}, Headers={}",
                e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders());
            throw new RuntimeException("Failed to fetch events: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error class: {}, Message: {}", e.getClass().getName(), e.getMessage());
            throw new RuntimeException("Failed to fetch events: " + e.getMessage(), e);
        }
    }
}
