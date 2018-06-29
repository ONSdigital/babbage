package com.github.onsdigital.babbage.search.external.requests.search;

import com.github.onsdigital.babbage.search.external.Endpoint;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

public class ConceptualSearchQueryRequest extends SearchQueryRequest {
    public ConceptualSearchQueryRequest(HttpServletRequest babbageRequest, String host, String searchTerm, QueryTypes queryType) {
        super(babbageRequest, host, searchTerm, queryType);
    }

    protected URI queryUri() {
        URI uri = URI.create(Endpoint.CONCEPTUAL_SEARCH.getUri() + this.queryType.getQueryType());
        return uri;
    }
}
