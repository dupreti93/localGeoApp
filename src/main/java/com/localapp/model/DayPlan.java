package com.localapp.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@Data
@DynamoDbBean
public class DayPlan {
    private int day;
    private String date;
    private List<Activity> activities;
    private String notes;
}
