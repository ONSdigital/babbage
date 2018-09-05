package com.github.onsdigital.babbage.search.external.requests.search.requests.conceptual;

import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.requests.search.requests.ContentQuery;
import com.github.onsdigital.babbage.search.external.requests.search.requests.ListType;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;

import javax.servlet.http.Cookie;
import java.util.Set;

/**
 * Queries the new conceptual search API to populate the SERP
 */
public class ConceptualContentQuery extends ContentQuery {

    public ConceptualContentQuery(String searchTerm, ListType listType, int page, int pageSize, SortBy sortBy, Set<TypeFilter> typeFilters,
                                  Cookie[] cookies) {
        super(searchTerm, listType, page, pageSize, sortBy, typeFilters, cookies);
    }

    @Override
    protected SearchEndpoints getEndpoint() {
        return SearchEndpoints.CONCEPTUAL_SEARCH_ONS;
    }
}
