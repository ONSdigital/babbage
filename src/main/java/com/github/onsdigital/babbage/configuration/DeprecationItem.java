package com.github.onsdigital.babbage.configuration;

import java.util.Arrays;
import java.util.List;
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
    private List<String> PageTypes;
    private DeprecationType deprecationType;

    public enum DeprecationType {
        FILTER,
        DATA;
    }

    public DeprecationItem(@JsonProperty("deprecationDate") String deprecationDate, @JsonProperty("sunsetDate") String sunsetDate, @JsonProperty("link") String link, @JsonProperty("matchPattern") String matchPatternString, @JsonProperty("pageTypes") String pageTypesString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        this.deprecationDate = LocalDateTime.parse(deprecationDate, formatter);
        this.sunsetDate = LocalDateTime.parse(sunsetDate, formatter);
        this.link = formatSunsetLink(link);

        if (matchPatternString != null && ".*/data$".equals(matchPatternString)) {
            this.deprecationType = DeprecationType.DATA;
        } else {
            this.deprecationType = DeprecationType.FILTER;
        }

        if (matchPatternString != null && !matchPatternString.isEmpty()) {
            this.matchPattern = Pattern.compile(matchPatternString);
        }
        if (pageTypesString != null && !pageTypesString.isEmpty()) {
            this.PageTypes = Arrays.asList(pageTypesString.split(","));
        }
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

    public List<String> pageTypes() {
        return PageTypes;
    }

    public DeprecationType deprecationType() {
        return deprecationType;
    }

    public boolean requestMatch(String requestURI) {
        Matcher matcher = matchPattern.matcher(requestURI);
        return matcher.find();
    }

    public boolean isSunsetPassed() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(sunsetDate);
    }

    private String formatSunsetLink(String link) {
        return String.format("<%s>; rel=\"sunset\"", link);
    }
}
