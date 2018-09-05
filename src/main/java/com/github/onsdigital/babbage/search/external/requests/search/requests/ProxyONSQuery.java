package com.github.onsdigital.babbage.search.external.requests.search.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.SearchHelper;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpScheme;
import org.elasticsearch.action.search.SearchRequestBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class for proxying Elasticsearch queries to dp-conceptual-search
 */
public class ProxyONSQuery extends AbstractSearchRequest<SearchResult> {

    private static final String QUERY_KEY = "query";
    private static final String FILTER_KEY = "filter";

    private final ONSQuery query;
    private final int page;
    private final int pageSize;

    public ProxyONSQuery(ONSQuery query, int page, int pageSize) {
        super(SearchResult.class);
        this.query = query;
        this.page = page;
        this.pageSize = pageSize;
    }

    @Override
    public URIBuilder targetUri() {
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(HttpScheme.HTTP.asString())
                .setHost(HOST)
                .setPath(Endpoint.SEARCH.endpoint)
                .addParameter(SearchQuery.SearchParam.PAGE.getParam(), String.valueOf(this.page))
                .addParameter(SearchQuery.SearchParam.SIZE.getParam(), String.valueOf(this.pageSize));

        return uriBuilder;
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
    protected Request getRequest() throws Exception {
        Request request = super.post()
                .content(this.stringPayload());
        return request;
    }

    enum Endpoint {
        SEARCH("/search/");

        private String endpoint;

        Endpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    enum Endpoint {
        SEARCH("/search/");

        private String endpoint;

        Endpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }
}
