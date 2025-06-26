package com.localapp.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapboxRetrieveResponse {
    @JsonProperty("features")
    private List<MapboxFeature> features;
}



