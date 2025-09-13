package com.localapp.service;

import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfig.model.GetConfigurationRequest;
import software.amazon.awssdk.services.appconfig.model.GetConfigurationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String application = "localGeoApp";
    private final String environment = "Production";
    private final String configProfile = "KeysProfile";
    private final String clientId = "client-id-1";

    @Cacheable(value = "apiKeys", key = "'mapboxApiKey'")
    public String getMapboxApiKey() {
        return getConfigValue("MAPBOX_KEY");
    }

    private String getConfigValue(String key) {
        try (AppConfigClient client = AppConfigClient.create()) {
            GetConfigurationRequest request = GetConfigurationRequest.builder()
                    .application(application)
                    .environment(environment)
                    .configuration(configProfile)
                    .clientId(clientId)
                    .build();
            GetConfigurationResponse response = client.getConfiguration(request);
            String json = response.content().asUtf8String();
            JsonNode node = mapper.readTree(json);
            return node.get(key).asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch " + key, e);
        }
    }


    public String getTicketmasterApiKey() {
        try (AppConfigClient client = AppConfigClient.create()) {
            GetConfigurationRequest request = GetConfigurationRequest.builder()
                    .application(application)
                    .environment(environment)
                    .configuration(configProfile)
                    .clientId(clientId)
                    .build();
            GetConfigurationResponse response = client.getConfiguration(request);
            String json = response.content().asUtf8String();
            JsonNode node = mapper.readTree(json);
            return node.get("TM_API_KEY").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Ticketmaster API key", e);
        }
    }
}