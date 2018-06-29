package com.github.onsdigital.babbage.search.external.requests.search;

import com.github.onsdigital.babbage.search.external.Endpoint;
import org.apache.http.client.utils.URIBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

public class DepartmentsSearchQueryRequest extends SearchQueryRequest {

    private static final String DEPARTMENTS = "departments";

    public DepartmentsSearchQueryRequest(HttpServletRequest babbageRequest, String host, String searchTerm) {
        super(babbageRequest, host, searchTerm, null);
    }

    @Override
    protected URI queryUri() {
        URI uri = URI.create(Endpoint.SEARCH.getUri() + DEPARTMENTS);
        return uri;
    }
}
