package com.github.onsdigital.babbage.search.external.requests.recommend.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSession {

    private String _id;

    @JsonProperty("user_oid")
    private String userId;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("session_vector")
    private double[] sessionVector;

    private UserSession() {
        // For Jackson
    }

    public String get_id() {
        return _id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public double[] getSessionVector() {
        return sessionVector;
    }
}
