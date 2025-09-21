package com.localapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.localapp.model.Itinerary;
import com.localapp.model.Activity;
import com.localapp.model.DayPlan;
import com.localapp.model.Event;
import com.localapp.model.EventInputDTO;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ItineraryService {
    private static final Logger logger = LoggerFactory.getLogger(ItineraryService.class);

    private final RestTemplate restTemplate;
    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final ItineraryRepository itineraryRepository;
    private final AppConfigService appConfigService;

    @Value("${ai.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String apiUrl;

    @Value("${ai.api.model:gemini-1.5-flash-latest}")
    private String model;

    @Value("${ai.api.type:gemini}")
    private String apiType;

    @Autowired
    public ItineraryService(RestTemplate restTemplate, EventService eventService,
                             ObjectMapper objectMapper, ItineraryRepository itineraryRepository,
                             AppConfigService appConfigService) {
        this.restTemplate = restTemplate;
        this.eventService = eventService;
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

    public Itinerary generateItinerary(String userId, List<EventInputDTO> events) {
        logger.info("Generating itinerary for user {} with {} events", userId, events.size());
        try {
            if (events == null || events.isEmpty()) {
                throw new IllegalArgumentException("No events provided to generate an itinerary");
            }
            events.sort(Comparator.comparing(EventInputDTO::getStartDate));
            LocalDate startDate = LocalDate.parse(events.get(0).getStartDate().substring(0, 10));
            LocalDate endDate = LocalDate.parse(events.get(events.size() - 1).getStartDate().substring(0, 10));
            StringBuilder prompt = new StringBuilder();
            prompt.append("Create a detailed travel itinerary for a trip to ")
                  .append(events.get(0).getVenue())
                  .append(" from ")
                  .append(startDate.format(DateTimeFormatter.ISO_DATE))
                  .append(" to ")
                  .append(endDate.format(DateTimeFormatter.ISO_DATE))
                  .append(".\n\n");
            prompt.append("The traveler has already selected the following events that must be included:\n");
            for (EventInputDTO event : events) {
                prompt.append("- ")
                      .append(event.getName())
                      .append(" at ")
                      .append(event.getVenue())
                      .append(" on ")
                      .append(event.getStartDate())
                      .append("\n");
            }
            prompt.append("\nPlease create a day-by-day itinerary that includes these events and suggests additional activities, " +
                          "restaurants, and attractions around them. For each day, include a morning, afternoon, and evening plan. " +
                          "For each suggested activity, include a brief description, location, and approximate duration. " +
                          "Also provide general travel tips for the destination and any notes about transportation between activities.\n\n" +
                          "Format the response as a structured JSON with the following format:\n" +
                          "{\n" +
                          "  \"title\": \"Trip title\",\n" +
                          "  \"description\": \"Brief overview of the trip\",\n" +
                          "  \"dayPlans\": [\n" +
                          "    {\n" +
                          "      \"day\": 1,\n" +
                          "      \"date\": \"YYYY-MM-DD\",\n" +
                          "      \"activities\": [\n" +
                          "        {\n" +
                          "          \"time\": \"09:00 AM\",\n" +
                          "          \"title\": \"Activity name\",\n" +
                          "          \"description\": \"Brief description\",\n" +
                          "          \"location\": \"Location name\",\n" +
                          "          \"type\": \"food/attraction/event\",\n" +
                          "          \"duration\": \"2 hours\"\n" +
                          "        }\n" +
                          "      ],\n" +
                          "      \"notes\": \"Day-specific notes\"\n" +
                          "    }\n" +
                          "  ],\n" +
                          "  \"notes\": \"General trip notes and tips\"\n" +
                          "}\n");
            String aiResponse;
            try {
                if ("gemini".equals(apiType)) {
                    aiResponse = callGeminiAPI(prompt.toString());
                } else {
                    aiResponse = callOpenAIAPI(prompt.toString());
                }
            } catch (Exception e) {
                logger.error("Error calling AI API", e);
                throw new RuntimeException("Failed to call AI API: " + e.getMessage(), e);
            }
            String jsonStr;
            try {
                try {
                    objectMapper.readTree(aiResponse);
                    jsonStr = aiResponse;
                } catch (Exception e) {
                    int startIdx = aiResponse.indexOf('{');
                    int endIdx = aiResponse.lastIndexOf('}');
                    if (startIdx >= 0 && endIdx >= 0 && endIdx > startIdx) {
                        String jsonCandidate = aiResponse.substring(startIdx, endIdx + 1);
                        objectMapper.readTree(jsonCandidate);
                        jsonStr = jsonCandidate;
                    } else {
                        throw new RuntimeException("Could not extract valid JSON from AI response");
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to extract JSON from AI response", e);
                throw new RuntimeException("Failed to extract valid JSON from AI response");
            }
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
                List<DayPlan> dayPlans = new ArrayList<>();
                JsonNode daysNode = jsonNode.path("dayPlans");
                if (daysNode.isArray()) {
                    for (JsonNode dayNode : daysNode) {
                        DayPlan dayPlan = new DayPlan();
                        dayPlan.setDay(dayNode.path("day").asInt());
                        dayPlan.setDate(dayNode.path("date").asText(""));
                        dayPlan.setNotes(dayNode.path("notes").asText(""));
                        List<Activity> activities = new ArrayList<>();
                        JsonNode activitiesNode = dayNode.path("activities");
                        if (activitiesNode.isArray()) {
                            for (JsonNode actNode : activitiesNode) {
                                Activity activity = new Activity();
                                activity.setTime(actNode.path("time").asText(""));
                                activity.setTitle(actNode.path("title").asText(""));
                                activity.setDescription(actNode.path("description").asText(""));
                                activity.setLocation(actNode.path("location").asText(""));
                                activity.setType(actNode.path("type").asText(""));
                                activity.setDuration(actNode.path("duration").asText(""));
                                for (EventInputDTO event : events) {
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
                        dayPlan.setActivities(activities);
                        dayPlans.add(dayPlan);
                    }
                }
                itinerary.setDayPlans(dayPlans);
            } catch (Exception e) {
                logger.error("Failed to parse AI response", e);
            }
            if (itinerary.getDayPlans() == null || itinerary.getDayPlans().isEmpty()) {
                logger.warn("AI response did not contain valid day plans, using fallback generator");
                LocalDate start = LocalDate.parse(startDate.format(DateTimeFormatter.ISO_DATE));
                LocalDate end = LocalDate.parse(endDate.format(DateTimeFormatter.ISO_DATE));
                long daysBetween = ChronoUnit.DAYS.between(start, end) + 1;
                Map<LocalDate, List<Event>> eventsByDate = events.stream()
                    .collect(Collectors.groupingBy(e -> LocalDate.parse(e.getStartDate().substring(0, 10))));
                List<DayPlan> basicPlans = IntStream.rangeClosed(1, (int) daysBetween)
                    .mapToObj(dayNum -> {
                        LocalDate date = start.plusDays(dayNum - 1);
                        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
                        DayPlan dayPlan = new DayPlan();
                        dayPlan.setDay(dayNum);
                        dayPlan.setDate(dateStr);
                        dayPlan.setNotes("Enjoy your day in " + (events.isEmpty() ? "the city" : events.get(0).getCity()));
                        List<Activity> activities = new ArrayList<>();
                        if (eventsByDate.containsKey(date)) {
                            for (Event event : eventsByDate.get(date)) {
                                Activity activity = new Activity();
                                activity.setTime(event.getStartDate().substring(11, 16));
                                activity.setTitle(event.getName());
                                activity.setLocation(event.getVenue());
                                activity.setType("event");
                                activity.setEventId(event.getId());
                                activity.setDuration("2 hours");
                                activities.add(activity);
                            }
                        }
                        if (activities.isEmpty()) {
                            Activity breakfast = new Activity();
                            breakfast.setTime("09:00");
                            breakfast.setTitle("Breakfast");
                            breakfast.setType("food");
                            breakfast.setDuration("1 hour");
                            activities.add(breakfast);
                            Activity explore = new Activity();
                            explore.setTime("11:00");
                            explore.setTitle("Explore the city");
                            explore.setType("attraction");
                            explore.setDuration("3 hours");
                            activities.add(explore);
                            Activity dinner = new Activity();
                            dinner.setTime("18:00");
                            dinner.setTitle("Dinner");
                            dinner.setType("food");
                            dinner.setDuration("1.5 hours");
                            activities.add(dinner);
                        }
                        dayPlan.setActivities(activities);
                        return dayPlan;
                    })
                    .collect(Collectors.toList());
                if (itinerary.getTitle() == null) {
                    itinerary.setTitle("Your " + events.get(0).getVenue() + " Itinerary");
                    itinerary.setDescription("An itinerary based on your selected events");
                }
                itinerary.setDayPlans(basicPlans);
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
            generationConfig.put("maxOutputTokens", 4000);
            requestBody.set("generationConfig", generationConfig);
            String urlWithKey = apiUrl + "?key=" + apiKey;
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            String response = restTemplate.postForObject(urlWithKey, entity, String.class);
            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            logger.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String callOpenAIAPI(String prompt) {
        try {
            String apiKey = appConfigService.getOpenAIApiKey();
            logger.info("Calling OpenAI API for itinerary generation");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4000);
            requestBody.put("top_p", 1);
            ObjectNode messageObj = objectMapper.createObjectNode();
            messageObj.put("role", "user");
            messageObj.put("content", prompt);
            requestBody.putArray("messages").add(messageObj);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            logger.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
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
