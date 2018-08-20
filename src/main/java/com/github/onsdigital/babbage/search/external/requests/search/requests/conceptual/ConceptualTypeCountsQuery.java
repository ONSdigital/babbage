package com.github.onsdigital.babbage.search.external.requests.search.requests.conceptual;

import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.requests.search.requests.ListType;
import com.github.onsdigital.babbage.search.external.requests.search.requests.TypeCountsQuery;

/**
 * Queries the new conceptual search API to populate type counts
 */
public class ConceptualTypeCountsQuery extends TypeCountsQuery {

    public ConceptualTypeCountsQuery(String searchTerm, ListType listType) {
        super(searchTerm, listType);
    }

    @Override
    protected SearchEndpoints getEndpoint() {
        return SearchEndpoints.CONCEPTUAL_SEARCH_ONS;
    }
}
