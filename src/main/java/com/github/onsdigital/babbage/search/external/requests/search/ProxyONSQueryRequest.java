package com.github.onsdigital.babbage.search.external.requests.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.search.external.Endpoint;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.SearchHelper;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.action.search.SearchRequestBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

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

    private StringEntity queryToEntity() throws JsonProcessingException, UnsupportedEncodingException {
        SearchRequestBuilder builder = SearchHelper.prepare(this.query);
        String queryString = builder.toString();

        // TODO - Add type information to query here (not included in query body when using internal client)
        return new StringEntity(MAPPER.writeValueAsString(queryString));
    }

    @Override
    protected HttpRequestBase generateRequest() throws IOException {
        StringEntity stringEntity = this.queryToEntity();

        HttpPost post = new HttpPost(this.getQueryUrl());
        post.setEntity(stringEntity);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");

        return post;
    }

    @Override
    public SearchResult call() throws IOException {
        String entity = super.execute();

        SearchResult searchResult = MAPPER.readValue(entity, SearchResult.class);
        return searchResult;
    }
}
