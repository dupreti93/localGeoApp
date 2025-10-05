package com.localapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.localapp.model.embedded.Activity;
import com.localapp.model.dto.SelectedEvent;
import com.localapp.model.entity.Itinerary;
import com.localapp.repository.ItineraryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ItineraryService {
    private static final Logger logger = LoggerFactory.getLogger(ItineraryService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ItineraryRepository itineraryRepository;
    private final AppConfigService appConfigService;

    @Value("${ai.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String apiUrl;

    @Autowired
    public ItineraryService(RestTemplate restTemplate, ObjectMapper objectMapper,
                             ItineraryRepository itineraryRepository, AppConfigService appConfigService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.itineraryRepository = itineraryRepository;
        this.appConfigService = appConfigService;
    }

    public List<Itinerary> getUserItinerary(String userId, String city, String date) {
        return itineraryRepository.findByUserCityDate(userId, city, date);
    }

    public void saveOrUpdateItinerary(Itinerary itinerary) {
        itineraryRepository.save(itinerary);
    }

    public Itinerary generateItinerary(String userId, List<SelectedEvent> events) {
        logger.info("Generating itinerary for user {} with {} events", userId, events.size());
        try {
            if (events.isEmpty()) {
                throw new IllegalArgumentException("No events provided to generate an itinerary");
            }
            events.sort(Comparator.comparing(SelectedEvent::getStartDate));
            LocalDate startDate = LocalDate.parse(events.get(0).getStartDate().substring(0, 10));
            LocalDate endDate = LocalDate.parse(events.get(events.size() - 1).getStartDate().substring(0, 10));

            StringBuilder prompt = new StringBuilder()
                .append("Create a detailed travel itinerary for a trip to ")
                .append(events.get(0).getVenue())
                .append(" from ")
                .append(startDate.format(DateTimeFormatter.ISO_DATE))
                .append(" to ")
                .append(endDate.format(DateTimeFormatter.ISO_DATE))
                .append(".\n\n");

            prompt.append("The traveler has already selected the following events that must be included:\n");
            for (SelectedEvent event : events) {
                prompt.append("- ")
                      .append(event.getName())
                      .append(" at ")
                      .append(event.getVenue())
                      .append(" on ")
                      .append(event.getStartDate())
                      .append("\n");
            }

            prompt.append("""
                
                Please create a day-by-day itinerary that includes these events and suggests additional activities, 
                restaurants, and attractions around them. For each day, include a morning, afternoon, and evening plan. 
                For each suggested activity, include a brief description, location, and approximate duration. 
                Also provide general travel tips for the destination and any notes about transportation between activities.
                
                Format the response as a structured JSON with the following format:
                {
                  "title": "Trip title",
                  "description": "Brief overview of the trip",
                  "dayPlans": [
                    {
                      "day": 1,
                      "date": "YYYY-MM-DD",
                      "activities": [
                        {
                          "time": "09:00 AM",
                          "title": "Activity name",
                          "description": "Brief description",
                          "location": "Location name",
                          "type": "food/attraction/event",
                          "duration": "2 hours"
                        }
                      ]
                    }
                  ],
                  "notes": "General trip notes and tips"
                }
                """);

            String aiResponse = callGeminiAPI(prompt.toString());
            String jsonStr = extractJsonFromResponse(aiResponse);

            Itinerary itinerary = parseAIResponse(jsonStr, userId, events, startDate, endDate);

            if (itinerary.getActivities() == null || itinerary.getActivities().isEmpty()) {
                throw new RuntimeException("AI failed to generate a valid itinerary with activities");
            }

            itineraryRepository.save(itinerary);
            logger.info("Successfully generated and saved itinerary with ID: {}", itinerary.getItineraryId());
            return itinerary;
        } catch (Exception e) {
            logger.error("Failed to generate itinerary", e);
            throw new RuntimeException("Failed to generate itinerary: " + e.getMessage(), e);
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            String apiKey = appConfigService.getGeminiApiKey();
            logger.info("Calling Gemini API for itinerary generation");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contentsArray = objectMapper.createArrayNode();
            ObjectNode contentObj = objectMapper.createObjectNode();
            ArrayNode partsArray = objectMapper.createArrayNode();
            ObjectNode partObj = objectMapper.createObjectNode();
            partObj.put("text", prompt);
            partsArray.add(partObj);
            contentObj.set("parts", partsArray);
            contentsArray.add(contentObj);
            requestBody.set("contents", contentsArray);

            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 16000);
            requestBody.set("generationConfig", generationConfig);

            String urlWithKey = apiUrl + "?key=" + apiKey;
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            String response = restTemplate.postForObject(urlWithKey, entity, String.class);

            logger.info("Gemini API response received: {}", response);
            JsonNode responseJson = objectMapper.readTree(response);

            // Add proper null checks for the response structure
            JsonNode candidates = responseJson.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode firstPart = parts.get(0);
                    String text = firstPart.path("text").asText();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }

            // If we get here, the response structure is unexpected
            logger.error("Unexpected Gemini API response structure: {}", response);
            throw new RuntimeException("Unexpected response structure from Gemini API");

        } catch (Exception e) {
            logger.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromResponse(String aiResponse) {
        try {
            // First, try to parse the response directly as JSON
            objectMapper.readTree(aiResponse);
            return aiResponse;
        } catch (Exception e) {
            // If direct parsing fails, look for JSON within markdown code blocks
            logger.info("Direct JSON parsing failed, extracting from markdown code blocks");

            // Look for ```json and ``` patterns (markdown code blocks)
            String jsonStart = "```json";
            String jsonEnd = "```";

            int startIdx = aiResponse.indexOf(jsonStart);
            if (startIdx >= 0) {
                startIdx += jsonStart.length();
                int endIdx = aiResponse.indexOf(jsonEnd, startIdx);
                if (endIdx > startIdx) {
                    String jsonCandidate = aiResponse.substring(startIdx, endIdx).trim();
                    try {
                        objectMapper.readTree(jsonCandidate);
                        logger.info("Successfully extracted JSON from markdown code blocks");
                        return jsonCandidate;
                    } catch (Exception ex) {
                        logger.warn("Failed to parse JSON extracted from markdown: {}", ex.getMessage());
                    }
                }
            }

            // Fallback: look for JSON by finding { and } characters
            int jsonStartIdx = aiResponse.indexOf('{');
            int jsonEndIdx = aiResponse.lastIndexOf('}');
            if (jsonStartIdx >= 0 && jsonEndIdx >= 0 && jsonEndIdx > jsonStartIdx) {
                String jsonCandidate = aiResponse.substring(jsonStartIdx, jsonEndIdx + 1);
                try {
                    objectMapper.readTree(jsonCandidate);
                    logger.info("Successfully extracted JSON using fallback method");
                    return jsonCandidate;
                } catch (Exception ex) {
                    logger.error("Failed to parse JSON using fallback method: {}", ex.getMessage());
                }
            }

            logger.error("Could not extract valid JSON from AI response. Response was: {}", aiResponse);
            throw new RuntimeException("Could not extract valid JSON from AI response");
        }
    }

    private Itinerary parseAIResponse(String jsonStr, String userId, List<SelectedEvent> events,
                                     LocalDate startDate, LocalDate endDate) {
        Itinerary itinerary = new Itinerary();
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            itinerary.setUserId(userId);
            itinerary.setCity(events.get(0).getVenue());
            itinerary.setStartDate(startDate.format(DateTimeFormatter.ISO_DATE));
            itinerary.setEndDate(endDate.format(DateTimeFormatter.ISO_DATE));
            itinerary.setTitle(jsonNode.path("title").asText("Your " + events.get(0).getVenue() + " Itinerary"));
            itinerary.setDescription(jsonNode.path("description").asText(""));
            itinerary.setNotes(jsonNode.path("notes").asText(""));

            List<Activity> activities = new ArrayList<>();
            JsonNode daysNode = jsonNode.path("dayPlans");
            if (daysNode.isArray()) {
                for (JsonNode dayNode : daysNode) {
                    int dayNumber = dayNode.path("day").asInt();
                    String dayDate = dayNode.path("date").asText("");
                    JsonNode activitiesNode = dayNode.path("activities");
                    if (activitiesNode.isArray()) {
                        for (JsonNode actNode : activitiesNode) {
                            Activity activity = new Activity();
                            activity.setDay(dayNumber);
                            activity.setDate(dayDate);
                            activity.setTime(actNode.path("time").asText(""));
                            activity.setTitle(actNode.path("title").asText(""));
                            activity.setDescription(actNode.path("description").asText(""));
                            activity.setLocation(actNode.path("location").asText(""));
                            activity.setType(actNode.path("type").asText(""));
                            activity.setDuration(actNode.path("duration").asText(""));

                            // Link to original events if possible
                            for (SelectedEvent event : events) {
                                if (activity.getTitle().equalsIgnoreCase(event.getName()) ||
                                    (activity.getLocation() != null &&
                                     event.getVenue() != null &&
                                     activity.getLocation().equalsIgnoreCase(event.getVenue()))) {
                                    activity.setEventId(event.getId());
                                    break;
                                }
                            }
                            activities.add(activity);
                        }
                    }
                }
            }
            itinerary.setActivities(activities);
        } catch (Exception e) {
            logger.error("Failed to parse AI response", e);
        }
        return itinerary;
    }

    public List<Itinerary> getUserItineraries(String userId) {
        return itineraryRepository.findByUserId(userId);
    }

    public Itinerary getItineraryById(String userId, String itineraryId) {
        return itineraryRepository.findById(userId, itineraryId);
    }

    public void deleteItinerary(String userId, String itineraryId) {
        itineraryRepository.delete(userId, itineraryId);
    }
}
