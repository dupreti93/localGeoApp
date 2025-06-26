package com.localapp.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@ToString
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MapboxSuggestResponse {
    @JsonProperty("suggestions")
    private List<MapboxSuggestion> suggestions;
}