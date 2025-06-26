package com.localapp.model.helpers;

import lombok.ToString;

import java.util.List;

@ToString
public class PlaceAutocompleteResponse {
    private List<Prediction> predictions;

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }
}