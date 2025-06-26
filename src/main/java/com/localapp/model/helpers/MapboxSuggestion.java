package com.localapp.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MapboxSuggestion {
    @JsonProperty("name")
    private String name;

    @JsonProperty("mapbox_id")
    private String mapboxId;

    @JsonProperty("place_formatted")
    private String placeFormatted;
}