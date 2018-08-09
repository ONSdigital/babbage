package com.github.onsdigital.babbage.search.external.requests.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.SearchHelper;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpScheme;
import org.elasticsearch.action.search.SearchRequestBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProxyONSQuery extends AbstractSearchRequest<SearchResult> {

    private static final String QUERY_KEY = "query";
    private static final String FILTER_KEY = "filter";

    private final ONSQuery query;

    public ProxyONSQuery(ONSQuery query) {
        super(SearchResult.class);
        this.query = query;
    }

    @Override
    public String targetUri() {
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(HttpScheme.HTTP.asString())
                .setHost(HOST)
                .setPath(SearchEndpoints.SEARCH.getEndpoint());

        return uriBuilder.toString();
    }

    private String queryToString() {
        SearchRequestBuilder builder = SearchHelper.prepare(this.query);
        String queryString = builder.toString();

        return queryString;
    }

    private Set<String> typeFilters() {
        ContentType[] contentTypes = this.query.types();

        // Build form params
        Set<String> typeFilters = new HashSet<>();

        for (ContentType contentType : contentTypes) {
            typeFilters.add(contentType.name());
        }
        return typeFilters;
    }

    private Map<String, Object> buildQueryMap() {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(QUERY_KEY, this.queryToString());
        queryMap.put(FILTER_KEY, this.typeFilters());

        return queryMap;
    }

    private StringContentProvider stringPayload() throws JsonProcessingException {
        String queryString = MAPPER.writeValueAsString(this.buildQueryMap());
        return new StringContentProvider(queryString);
    }

    @Override
    protected ContentResponse getContentResponse() throws Exception {
        Request request = super.post()
                .content(this.stringPayload());
        return request.send();
    }
}
