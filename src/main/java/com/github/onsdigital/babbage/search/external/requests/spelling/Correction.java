package com.github.onsdigital.babbage.search.external.requests.spelling;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Correction {

    private String correction;

    @JsonProperty("P")
    private float probability;

    public Correction(String correction, float probability) {
        this.correction = correction;
        this.probability = probability;
    }

    private Correction() {
        // For Jackson
    }

    public String getCorrection() {
        return correction;
    }

    @JsonProperty("P")
    public float getProbability() {
        return probability;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    @JsonProperty("P")
    public void setProbability(float probability) {
        this.probability = probability;
    }
}
