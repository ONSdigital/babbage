package com.github.onsdigital.babbage.search.external.requests;

import org.apache.commons.lang3.CharEncoding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public enum Endpoints {
    SEARCH("search/ons"),
    CONCEPTUAL_SEARCH("search/ons/conceptual"),
    AUTOCOMPLETE("suggest/autocomplete"),
    RECOMMEND("recommend/user/update");

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
