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
    private final RestTemplate restTemplate;
    private static final String TICKETMASTER_API = "https://app.ticketmaster.com/discovery/v2/events.json";
    private static final String TICKETMASTER_EVENT_API = "https://app.ticketmaster.com/discovery/v2/events/";

    /**
     * Constructs a new EventService with the required AppConfigService for API key management.
     * @param appConfigService Service to retrieve API keys from AWS AppConfig
     * @param restTemplate RestTemplate instance for making API calls
     */
    public EventService(AppConfigService appConfigService, RestTemplate restTemplate) {
        this.appConfigService = appConfigService;
        this.restTemplate = restTemplate;
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
        return fetchEvents(city, date, "relevance,desc", null);
    }

    /**
     * Fetches events from Ticketmaster API for a specific city and date with custom sorting.
     *
     * @param city The city to search events in
     * @param date The start date for events (format: YYYY-MM-DD)
     * @param sort The sort order (options: relevance,desc; date,asc; date,desc; name,asc; name,desc; venueName,asc)
     * @return List of events as maps containing event details
     * @throws RuntimeException if there's an error fetching events
     */
    public List<Map<String, Object>> fetchEvents(String city, String date, String sort) {
        return fetchEvents(city, date, sort, null);
    }

    /**
     * Fetches events from Ticketmaster API for a specific city, date, with custom sorting and keyword filtering.
     *
     * @param city The city to search events in
     * @param date The start date for events (format: YYYY-MM-DD)
     * @param sort The sort order (options: relevance,desc; date,asc; date,desc; name,asc; name,desc; venueName,asc)
     * @param artist The artist/performer name or event name to filter by (can be null for no filtering)
     * @return List of events as maps containing event details
     * @throws RuntimeException if there's an error fetching events
     */
    public List<Map<String, Object>> fetchEvents(String city, String date, String sort, String artist) {
        try {
            String apiKey = appConfigService.getTicketmasterApiKey();
            // Only log high-level info
            logger.info("Calling Ticketmaster API for city: {}, date: {}, sort: {}, artist: {}", city, date, sort, artist);

            // Build URL with properly encoded parameters
            StringBuilder urlBuilder = new StringBuilder(TICKETMASTER_API);
            urlBuilder.append("?apikey=").append(apiKey);
            urlBuilder.append("&city=").append(java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8.name()));
            urlBuilder.append("&startDateTime=").append(date).append("T00:00:00Z");
            urlBuilder.append("&endDateTime=").append(date).append("T23:59:59Z");
            urlBuilder.append("&size=200");
            urlBuilder.append("&sort=").append(sort);

            // Add artist filter if provided
            if (artist != null && !artist.trim().isEmpty()) {
                urlBuilder.append("&keyword=").append(java.net.URLEncoder.encode(artist, java.nio.charset.StandardCharsets.UTF_8.name()));
            }

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

                        // Extract keywords information if available
                        List<String> keywords = extractKeywords(event);
                        if (!keywords.isEmpty()) {
                            transformed.put("keywords", keywords);
                        }

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
                        // Add image support from Ticketmaster API
                        if (event.containsKey("images")) {
                            List<Map> images = (List<Map>)event.get("images");
                            logger.info("Found {} images for event: {}", images.size(), event.get("name"));
                            if (!images.isEmpty()) {
                                // Get the first image or find a suitable one
                                Map bestImage = images.stream()
                                    .filter(img -> img.containsKey("url"))
                                    .findFirst()
                                    .orElse(images.get(0));
                                String imageUrl = (String) bestImage.get("url");
                                transformed.put("image", imageUrl);
                                logger.info("Added image URL: {} for event: {}", imageUrl, event.get("name"));

                                List<String> imageUrls = images.stream()
                                    .filter(img -> img.containsKey("url"))
                                    .map(img -> (String) img.get("url"))
                                    .collect(Collectors.toList());
                                transformed.put("images", imageUrls);
                            }
                        } else {
                            logger.warn("No images found for event: {}", event.get("name"));
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

    /**
     * Fetches a specific event by its ID from Ticketmaster API and returns it as a Map.
     *
     * @param eventId The Ticketmaster event ID
     * @return Map with event details, or null if not found
     * @throws RuntimeException if there's an error fetching the event
     */
    public Map<String, Object> getEventById(String eventId) {
        try {
            String apiKey = appConfigService.getTicketmasterApiKey();
            logger.info("Fetching event details for ID: {}", eventId);

            String url = TICKETMASTER_EVENT_API + eventId + "?apikey=" + apiKey;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> eventData = mapper.readValue(responseBody, Map.class);

                Map<String, Object> event = new HashMap<>();
                event.put("id", eventData.get("id"));
                event.put("name", eventData.get("name"));
                event.put("url", eventData.get("url"));
                event.put("type", eventData.get("type"));

                // Extract venue information
                if (eventData.containsKey("_embedded")) {
                    Map embedded = (Map) eventData.get("_embedded");
                    if (embedded.containsKey("venues")) {
                        List<Map> venues = (List<Map>) embedded.get("venues");
                        if (!venues.isEmpty()) {
                            Map venue = venues.get(0);
                            event.put("venue", venue.get("name"));

                            if (venue.containsKey("city")) {
                                Map cityData = (Map) venue.get("city");
                                event.put("city", cityData.get("name"));
                            }

                            if (venue.containsKey("location")) {
                                Map location = (Map) venue.get("location");
                                event.put("latitude", location.get("latitude"));
                                event.put("longitude", location.get("longitude"));
                            }
                        }
                    }
                }

                // Extract date information
                if (eventData.containsKey("dates") && ((Map) eventData.get("dates")).containsKey("start")) {
                    Map dates = (Map) eventData.get("dates");
                    Map start = (Map) dates.get("start");
                    event.put("startDate", start.get("dateTime"));
                }

                // Extract price range information
                if (eventData.containsKey("priceRanges")) {
                    List<Map> priceRanges = (List<Map>) eventData.get("priceRanges");
                    if (!priceRanges.isEmpty()) {
                        Map priceRange = priceRanges.get(0);
                        event.put("minPrice", priceRange.get("min"));
                        event.put("maxPrice", priceRange.get("max"));
                        event.put("currency", priceRange.get("currency"));
                    }
                }

                // Extract image information
                if (eventData.containsKey("images")) {
                    List<Map> images = (List<Map>) eventData.get("images");
                    if (!images.isEmpty()) {
                        Map bestImage = images.stream()
                            .filter(img -> img.containsKey("url"))
                            .findFirst()
                            .orElse(images.get(0));
                        String imageUrl = (String) bestImage.get("url");
                        event.put("image", imageUrl);

                        List<String> imageUrls = images.stream()
                            .filter(img -> img.containsKey("url"))
                            .map(img -> (String) img.get("url"))
                            .collect(Collectors.toList());
                        event.put("images", imageUrls);
                    }
                }

                // Extract description or info
                if (eventData.containsKey("info")) {
                    event.put("description", eventData.get("info"));
                } else if (eventData.containsKey("description")) {
                    event.put("description", eventData.get("description"));
                }

                // Extract keywords
                List<String> keywords = extractKeywords(eventData);
                event.put("keywords", keywords);

                return event;
            } else {
                logger.warn("Event not found with ID: {}", eventId);
                return null;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("Event not found with ID: {}", eventId);
                return null;
            }
            logger.error("HTTP error from Ticketmaster: Status={}, Body={}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch event: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error fetching event with ID {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch event: " + e.getMessage(), e);
        }
    }

    /**
     * Extract keyword information from event data returned by Ticketmaster API.
     * Keywords include artist names and the event name itself for broader searching.
     *
     * @param event The event map from Ticketmaster API
     * @return List of keyword strings
     */
    private List<String> extractKeywords(Map<String, Object> event) {
        List<String> keywords = new ArrayList<>();

        try {
            // Primary source: _embedded.attractions
            if (event.containsKey("_embedded")) {
                Map embedded = (Map)event.get("_embedded");
                if (embedded.containsKey("attractions")) {
                    List<Map> attractions = (List<Map>) embedded.get("attractions");

                    if (attractions != null && !attractions.isEmpty()) {
                        attractions.forEach(attraction -> {
                            if (attraction.containsKey("name")) {
                                String artistName = (String) attraction.get("name");
                                keywords.add(artistName);
                            }
                        });
                    }
                }
            }

            // Add event name as a keyword
            if (event.containsKey("name")) {
                String eventName = (String) event.get("name");
                keywords.add(eventName);
            }
        } catch (Exception e) {
            logger.warn("Error extracting keyword information for event: {}",
                event.getOrDefault("name", "Unknown event"));
        }

        return keywords;
    }
}
