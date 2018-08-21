package com.github.onsdigital.babbage.search.external.requests.search.requests;

import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.SearchType;

public class DepartmentsQuery extends SearchQuery {

    public DepartmentsQuery(String searchTerm) {
        super(searchTerm, ListType.ONS, SearchType.DEPARTMENTS);
    }

    @Override
    protected SearchEndpoints getEndpoint() {
        return SearchEndpoints.SEARCH_ONS;
    }
}
