package com.github.onsdigital.babbage.search.external;

import com.github.onsdigital.babbage.search.external.requests.search.requests.ListType;

public enum SearchEndpoints {

    SEARCH_ONS("/search/%s/"),
    CONCEPTUAL_SEARCH_ONS("/search/conceptual/%s/");

    private String endpoint;

    SearchEndpoints(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpointForListType(ListType listType) {
        return String.format(this.endpoint, listType.getEndpoint());
    }
}
