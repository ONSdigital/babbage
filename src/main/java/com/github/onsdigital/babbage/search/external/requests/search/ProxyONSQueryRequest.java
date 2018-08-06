package com.github.onsdigital.babbage.search.external.requests.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.search.external.Endpoint;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.SearchHelper;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.elasticsearch.action.search.SearchRequestBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyONSQueryRequest extends AbstractSearchRequest<SearchResult> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ONSQuery query;

    public ProxyONSQueryRequest(HttpServletRequest babbageRequest, String host, ONSQuery query) {
        super(babbageRequest, host);
        this.query = query;
    }

    protected URI queryUri() {
        URI uri = URI.create(Endpoint.SEARCH_PROXY.getUri());
        return uri;
    }

    private String getQueryUrl() {
        URIBuilder ub = new URIBuilder(this.queryUri());
        return this.host + ub.toString();
    }

    private String queryToEntity() {
        SearchRequestBuilder builder = SearchHelper.prepare(this.query);
        String queryString = builder.toString();

        return queryString;
    }

    private List<NameValuePair> typeFilters() {
        ContentType[] contentTypes = this.query.types();

        // Build form params
        List<NameValuePair> postParams = new ArrayList<>();

        for (ContentType contentType : contentTypes) {
            postParams.add(new BasicNameValuePair(SearchQueryRequest.SearchRequestParameters.FILTER.parameter, contentType.name()));
        }
        return postParams;
    }

    @Override
    protected HttpRequestBase generateRequest() throws IOException {
        String stringEntity = this.queryToEntity();

        Map<String, String> entity = new HashMap<>();
        entity.put("query", stringEntity);

        HttpPost post = new HttpPost(this.getQueryUrl());
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");

        // Extract type information
        if (null != this.query) {
            List<NameValuePair> typeFilters = this.typeFilters();
            entity.put("filter", MAPPER.writeValueAsString(typeFilters));
        }

        List<NameValuePair> postParams = new ArrayList<>();
        for (String key : entity.keySet()) {
            postParams.add(new BasicNameValuePair(key, entity.get(key)));
        }

        post.setEntity(new StringEntity(MAPPER.writeValueAsString(entity)));

        return post;
    }

    @Override
    public SearchResult call() throws IOException {
        String entity = super.execute();

        SearchResult searchResult = MAPPER.readValue(entity, SearchResult.class);
        return searchResult;
    }
}
