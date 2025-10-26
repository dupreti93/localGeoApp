package com.localapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localapp.util.DistanceUtil;
import com.localapp.util.EventFilterUtil;
import com.localapp.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private static final String API_BASE = "https://www.eventbriteapi.com/v3";
    private final AppConfigService config;
    private final RestTemplate http;
    private final ObjectMapper json = new ObjectMapper();

    public EventService(AppConfigService config, RestTemplate http) {
        this.config = config;
        this.http = http;
    }

    public List<Map<String, Object>> fetchEvents(String city, String date) {
        return fetchEvents(city, date, null, null);
    }

    public List<Map<String, Object>> fetchEvents(String city, String date, String sort, String artist) {
        String token = config.getEventbriteToken();
        if (token == null || token.isEmpty()) return List.of();
        return callAPI(token, city, date + "T00:00:00Z", date + "T23:59:59Z", artist);
    }

    public List<Map<String, Object>> searchFutureEventsByArtist(String artist) {
        return fetchEvents("", Instant.now().atZone(ZoneOffset.UTC).toLocalDate().toString(), null, artist);
    }

    public List<Map<String, Object>> fetchTonightEvents(String city, Double lat, Double lon, String mood) {
        String token = config.getEventbriteToken();
        if (token == null || token.isEmpty()) return List.of();

        Instant now = Instant.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
        List<Map<String, Object>> events = callAPI(token, city, fmt.format(now), fmt.format(now.plusSeconds(8 * 3600)), null);

        // Add distance
        if (lat != null && lon != null) {
            events.forEach(e -> {
                Double eLat = ParseUtil.dbl(e.get("latitude")), eLon = ParseUtil.dbl(e.get("longitude"));
                if (eLat != null && eLon != null) {
                    double d = DistanceUtil.haversine(lat, lon, eLat, eLon);
                    e.put("distanceMiles", Math.round(d * 10.0) / 10.0);
                    e.put("driveTimeMin", DistanceUtil.estimateDrive(d));
                    e.put("walkTimeMin", DistanceUtil.estimateWalk(d));
                }
            });
        }

        // Filter distance & mood
        events = events.stream()
            .filter(e -> ParseUtil.dbl(e.get("distanceMiles")) == null || ParseUtil.dbl(e.get("distanceMiles")) <= 25.0)
            .filter(e -> EventFilterUtil.matchMood(e, mood))
            .sorted(Comparator.comparing((Map<String, Object> e) -> ParseUtil.parseTime((String) e.get("startDate")))
                .thenComparing(e -> ParseUtil.dbl(e.get("distanceMiles")) != null ? ParseUtil.dbl(e.get("distanceMiles")) : Double.MAX_VALUE))
            .collect(Collectors.toList());

        return events;
    }

    private List<Map<String, Object>> callAPI(String token, String city, String start, String end, String query) {
        try {
            StringBuilder url = new StringBuilder(API_BASE + "/events/search/?expand=venue,logo");
            if (city != null && !city.isEmpty())
                url.append("&location.address=").append(java.net.URLEncoder.encode(city, java.nio.charset.StandardCharsets.UTF_8));
            url.append("&start_date.range_start=").append(java.net.URLEncoder.encode(start, java.nio.charset.StandardCharsets.UTF_8));
            url.append("&start_date.range_end=").append(java.net.URLEncoder.encode(end, java.nio.charset.StandardCharsets.UTF_8));
            if (query != null && !query.trim().isEmpty())
                url.append("&q=").append(java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));
            url.append("&page_size=200");

            JsonNode root = json.readTree(callRaw(token, url.toString()));
            JsonNode events = root.path("events");

            Map<String, Map<String, Object>> unique = new HashMap<>();
            if (events.isArray()) {
                for (JsonNode ev : events) {
                    Map<String, Object> parsed = parse(ev);
                    String key = ParseUtil.str(parsed.get("name")) + "|" + ParseUtil.str(parsed.get("startDate")) + "|" + ParseUtil.str(parsed.get("venue"));
                    unique.putIfAbsent(key.toLowerCase(), parsed);
                }
            }
            return new ArrayList<>(unique.values());
        } catch (Exception e) {
            log.error("API call failed: {}", e.getMessage());
            return List.of();
        }
    }

    private String callRaw(String token, String url) throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setAccept(List.of(MediaType.APPLICATION_JSON));
        return http.exchange(url, HttpMethod.GET, new HttpEntity<>(h), String.class).getBody();
    }

    private Map<String, Object> parse(JsonNode ev) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", ev.path("id").asText(null));
        m.put("name", ev.path("name").path("text").asText(ev.path("name").asText(null)));
        m.put("url", ev.path("url").asText(null));
        m.put("startDate", ev.path("start").path("utc").asText(ev.path("start").path("local").asText(null)));
        m.put("image", ev.path("logo").path("url").asText(null));

        JsonNode v = ev.path("venue");
        if (!v.isMissingNode()) {
            m.put("venue", v.path("name").asText(null));
            JsonNode a = v.path("address");
            if (!a.isMissingNode()) {
                m.put("city", a.path("city").asText(null));
                m.put("latitude", ParseUtil.dbl(a.path("latitude").asText(null)));
                m.put("longitude", ParseUtil.dbl(a.path("longitude").asText(null)));
            }
        }
        return m;
    }
}
