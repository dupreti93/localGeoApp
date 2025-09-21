package com.localapp.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
public class Activity {
    private String time;
    private String eventId; // Reference to a saved event if applicable
    private String title;
    private String description;
    private String location;
    private String type; // food, attraction, event, etc.
    private String duration;
}
