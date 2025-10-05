package com.localapp.model.entity;

import com.localapp.model.embedded.Activity;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@DynamoDbBean
public class Itinerary {
    private String itineraryId;
    private String userId;
    private String title;
    private String city;
    private String startDate;
    private String endDate;
    private String description;
    private List<Activity> activities;  // Direct list of activities instead of dayPlans
    private String notes;
    private LocalDateTime createdAt;

    public Itinerary() {
        this.itineraryId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSortKey
    public String getItineraryId() { return itineraryId; }
    public void setItineraryId(String itineraryId) { this.itineraryId = itineraryId; }
}
