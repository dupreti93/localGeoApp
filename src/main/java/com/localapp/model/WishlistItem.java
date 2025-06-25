package com.localapp.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@DynamoDbBean
public class WishlistItem {
    private String id;
    private String userId;
    private String displayName;
    private double latitude;
    private double longitude;
    private String city;
    private String geoHash;
    private String timestamp;
    private String placeId;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDbAttribute("displayName")
    public String getDisplayName() {
        return displayName;
    }

    @DynamoDbAttribute("latitude")
    public double getLatitude() {
        return latitude;
    }

    @DynamoDbAttribute("longitude")
    public double getLongitude() {
        return longitude;
    }

    @DynamoDbAttribute("city")
    public String getCity() {
        return city;
    }

    @DynamoDbAttribute("geoHash")
    public String getGeoHash() {
        return geoHash;
    }

    @DynamoDbAttribute("timestamp")
    public String getTimestamp() {
        return timestamp;
    }
}