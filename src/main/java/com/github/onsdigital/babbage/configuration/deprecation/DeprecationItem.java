package com.github.onsdigital.babbage.configuration.deprecation;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DeprecationItem {
    private LocalDateTime deprecationDate;
    private LocalDateTime sunsetDate;
    private String link;
    private Pattern matchPattern;
    private List<String> pageTypes;
    private DeprecationType deprecationType;
    private String message;

    public enum DeprecationType {
        FILTER,
        DATA;
    }

    public DeprecationItem(@JsonProperty("deprecationDate") String deprecationDate,
            @JsonProperty("sunsetDate") String sunsetDate, @JsonProperty("link") String link,
            @JsonProperty("matchPattern") String matchPatternString,
            @JsonProperty("pageTypes") String pageTypesString,
            @JsonProperty("message") String message) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        if (StringUtils.isNotBlank(deprecationDate)) {
            this.deprecationDate = LocalDateTime.parse(deprecationDate, formatter);
        }

        if (StringUtils.isNotBlank(sunsetDate)) {
            this.sunsetDate = LocalDateTime.parse(sunsetDate, formatter);
        }

        if (StringUtils.isNotBlank(link)) {
            this.link = formatSunsetLink(link);
        }

        if (matchPatternString != null && ".*/data$".equals(matchPatternString)) {
            this.deprecationType = DeprecationType.DATA;
        } else {
            this.deprecationType = DeprecationType.FILTER;
        }

        if (StringUtils.isNotBlank(matchPatternString)) {
            this.matchPattern = Pattern.compile(matchPatternString);
        }
        if (StringUtils.isNotBlank(pageTypesString)) {
            this.pageTypes = Arrays.asList(pageTypesString.split(","));
        }
        if (StringUtils.isNotBlank(message)) {
            this.message = message;
        }
    }

    public LocalDateTime getDeprecationDate() {
        return deprecationDate;
    }

    private long getDeprecationDateTimeStamp() {
        if (deprecationDate != null) {
            return deprecationDate.toEpochSecond(ZoneOffset.UTC);
        }
        return 0;
    }

    public String getDeprecationDateTimeStampString() {
        if (deprecationDate != null) {
            return String.format("%s%s", "@", Long.toString(getDeprecationDateTimeStamp()));
        }
        return null;
    }

    public LocalDateTime getSunsetDate() {
        return sunsetDate;
    }

    public String getLink() {
        return link;
    }

    public Pattern getMatchPattern() {
        return matchPattern;
    }

    public List<String> getPageTypes() {
        return pageTypes;
    }

    public DeprecationType getDeprecationType() {
        return deprecationType;
    }

    public String getMessage() {
        return message;
    }

    public boolean requestMatch(String requestURI) {
        if (matchPattern != null) {
            Matcher matcher = matchPattern.matcher(requestURI);
            return matcher.find();
        }
        return false;
    }

    public boolean isSunsetPassed() {
        if (sunsetDate != null) {
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(sunsetDate);
        }
        return false;
    }

    private String formatSunsetLink(String link) {
        return String.format("<%s>; rel=\"sunset\"", link);
    }
}
