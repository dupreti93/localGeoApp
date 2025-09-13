package com.localapp.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@DynamoDbBean
public class Itinerary {
    private String userId;
    private String city;
    private String date; // ISO format yyyy-MM-dd
    private String eventId;
    private ItineraryStatus status;

    public enum ItineraryStatus {
        GOING, INTERESTED;
        public static ItineraryStatus fromString(String value) {
            return value == null ? null : valueOf(value.trim().toUpperCase());
        }
    }

    @DynamoDbPartitionKey
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSortKey
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public ItineraryStatus getStatus() { return status; }
    public void setStatus(ItineraryStatus status) { this.status = status; }
    // For DynamoDB string mapping
    public void setStatus(String status) { this.status = ItineraryStatus.fromString(status); }
}
