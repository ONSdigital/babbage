package com.github.onsdigital.babbage.search.model;

import java.util.List;

public class Suggestions {

    private String original;
    private List<Suggestion> suggestions;

    private Suggestions() {
        // For Jackson
    }

    public String getOriginal() {
        return original;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public static class Suggestion {
        private float confidence;
        private String suggestion;

        private Suggestion() {
            // For Jackson
        }

        public Suggestion(float confidence, String suggestion) {
            this.confidence = confidence;
            this.suggestion = suggestion;
        }

        public float getConfidence() {
            return confidence;
        }

        public String getSuggestion() {
            return suggestion;
        }
    }

}
