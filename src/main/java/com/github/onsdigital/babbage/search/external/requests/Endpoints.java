package com.github.onsdigital.babbage.search.external.requests;

import org.apache.commons.lang3.CharEncoding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public enum Endpoints {
    SEARCH("search/ons"),
    SEARCH_DATA("search/ons/data"),
    SEARCH_PUBLICATIONS("search/ons/publications"),
    SEARCH_DEPARTMENTS("search/ons/departments"),
    CONCEPTUAL_SEARCH("search/conceptual/ons"),
    AUTOCOMPLETE("suggest/autocomplete"),
    RECOMMEND("recommend/update");

    String path;

    Endpoints(String path) {
        this.path = path;
    }

    String getQueryPath(String query) throws UnsupportedEncodingException {
        return new StringBuilder(this.path)
                .append(String.format("?q=%s", URLEncoder.encode(query, CharEncoding.UTF_8)))
                .toString();
    }
}
