package com.localapp.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MapboxCoordinates {
    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("latitude")
    private double latitude;
}