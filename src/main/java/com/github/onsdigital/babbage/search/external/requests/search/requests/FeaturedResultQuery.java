package com.github.onsdigital.babbage.search.external.requests.search.requests;

import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.SearchType;

/**
 * Replaces the internal featured results query by executing a HTTP request against the dp-conceptual-search featured API
 */
public class FeaturedResultQuery extends SearchQuery {

    public FeaturedResultQuery(String searchTerm, ListType listType) {
        super(searchTerm, listType, SearchType.FEATURED);
    }

    @Override
    protected SearchEndpoints getEndpoint() {
        return SearchEndpoints.SEARCH_ONS;
    }
}
