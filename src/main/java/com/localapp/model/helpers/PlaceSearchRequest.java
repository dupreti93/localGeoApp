package com.localapp.model.helpers;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
@Builder
public class PlaceSearchRequest {
    private String query;
    private Double lat;
    private Double lon;
}