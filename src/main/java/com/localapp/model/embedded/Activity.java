package com.localapp.model.embedded;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
public class Activity {
    private int day;           // Which day of the trip (1, 2, 3...)
    private String date;       // Date for this activity (2025-10-15)
    private String time;       // Time of day (10:00 AM)
    private String eventId;    // Reference to a saved event if applicable
    private String title;      // What to do (Visit Statue of Liberty)
    private String description;// Details about the activity
    private String location;   // Where (Liberty Island)
    private String type;       // Category: food, attraction, event, etc.
    private String duration;   // How long (3 hours)
}
