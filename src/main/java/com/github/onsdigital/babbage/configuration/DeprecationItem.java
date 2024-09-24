package com.github.onsdigital.babbage.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeprecationItem {
    private LocalDateTime deprecationDate;
    private LocalDateTime sunsetDate;
    private String link;
    private Pattern matchPattern;
    
    public DeprecationItem(@JsonProperty("deprecationDate") String deprecationDate, @JsonProperty("sunsetDate") String sunsetDate, @JsonProperty("link") String link, @JsonProperty("matchPattern") String matchPatternString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        this.deprecationDate = LocalDateTime.parse(deprecationDate, formatter);
        this.sunsetDate = LocalDateTime.parse(sunsetDate, formatter);
        this.link = link;
        this.matchPattern = Pattern.compile(matchPatternString);
    }

    public LocalDateTime deprecationDate() {
        return deprecationDate;
    }

    public LocalDateTime sunsetDate() {
        return sunsetDate;
    }

    public String link() {
        return link;
    }

    public Pattern matchPattern() {
        return matchPattern;
    }

    public boolean requestMatch(String requestURI) {
        Matcher matcher = matchPattern.matcher(requestURI);
        return matcher.find();
    }

    public boolean isSunsetPassed() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(sunsetDate);
    }
}
