package com.github.onsdigital.babbage.search.external;

public enum Endpoint {
    SEARCH("search/ons/"),
    CONCEPTUAL_SEARCH("search/conceptual/ons/"),
    RECOMMEND("recommend/");

    private String uri;

    Endpoint(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
