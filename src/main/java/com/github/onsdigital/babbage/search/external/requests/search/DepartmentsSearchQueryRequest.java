package com.github.onsdigital.babbage.search.external.requests.search;

import com.github.onsdigital.babbage.search.external.Endpoint;
import org.apache.http.client.utils.URIBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractPage;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSize;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractUserVectorQuery;

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

    @Override
    protected String getContentQueryUrl() {
        int page = extractPage(super.babbageRequest);
        int pageSize = extractSize(super.babbageRequest);

        URIBuilder ub = new URIBuilder(this.queryUri());
        ub.addParameter(SearchRequestParameters.QUERY.parameter, this.searchTerm);
        ub.addParameter(SearchRequestParameters.PAGE.parameter, String.valueOf(page));
        ub.addParameter(SearchRequestParameters.SIZE.parameter, String.valueOf(pageSize));
        return this.host + ub.toString();
    }
}
