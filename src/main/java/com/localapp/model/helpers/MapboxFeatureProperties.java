package com.localapp.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MapboxFeatureProperties {
    @JsonProperty("name")
    private String name;

    @JsonProperty("mapbox_id")
    private String mapboxId;

    @JsonProperty("feature_type")
    private String featureType;

    @JsonProperty("place_formatted")
    private String placeFormatted;

    @JsonProperty("coordinates")
    private MapboxCoordinates coordinates;
}