package com.localapp.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapboxFeature {
    @JsonProperty("properties")
    private MapboxFeatureProperties properties;
}
