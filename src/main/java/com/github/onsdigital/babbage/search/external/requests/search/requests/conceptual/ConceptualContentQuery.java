package com.github.onsdigital.babbage.search.external.requests.search.requests.conceptual;

import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.requests.search.requests.ContentQuery;
import com.github.onsdigital.babbage.search.external.requests.search.requests.ListType;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;

import java.util.Set;

/**
 * Queries the new conceptual search API to populate the SERP
 */
public class ConceptualContentQuery extends ContentQuery {

    public ConceptualContentQuery(String searchTerm, ListType listType, int page, int pageSize) {
        super(searchTerm, listType, page, pageSize);
    }

    public ConceptualContentQuery(String searchTerm, ListType listType, int page, int pageSize, SortBy sortBy) {
        super(searchTerm, listType, page, pageSize, sortBy);
    }

    public ConceptualContentQuery(String searchTerm, ListType listType, int page, int pageSize, SortBy sortBy, Set<TypeFilter> typeFilters) {
        super(searchTerm, listType, page, pageSize, sortBy, typeFilters);
    }

    @Override
    protected SearchEndpoints getEndpoint() {
        return SearchEndpoints.CONCEPTUAL_SEARCH_ONS;
    }
}
