package com.github.onsdigital.babbage.search.external.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;

public class SearchRequest extends AbstractSearchRequest {

    private final String searchTerm;
    private final boolean conceptualSearch;

    public SearchRequest(HttpServletRequest babbageRequest, String host) {
        super(babbageRequest, host);
        this.searchTerm = extractSearchTerm(babbageRequest);
        this.conceptualSearch = extractConceptualSearch(babbageRequest);
    }

    @Override
    public HttpRequestBase generateRequest() throws IOException {
        Set<TypeFilter> typeFilters = extractSelectedFilters(super.babbageRequest, TypeFilter.getAllFilters(), false);
        SortBy sortBy = extractSortBy(super.babbageRequest, SortBy.relevance);
        int page = extractPage(super.babbageRequest);
        int pageSize = extractSize(super.babbageRequest);

        // Build form params
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(SearchRequestParameters.SORT_BY.parameter, sortBy.name()));
        postParams.add(new BasicNameValuePair(SearchRequestParameters.PAGE.parameter, String.valueOf(page)));
        postParams.add(new BasicNameValuePair(SearchRequestParameters.SIZE.parameter, String.valueOf(pageSize)));

        for (TypeFilter typeFilter : typeFilters) {
            for (ContentType contentType : typeFilter.getTypes()) {
                postParams.add(new BasicNameValuePair(SearchRequestParameters.FILTER.parameter, contentType.name()));
            }
        }

        // Build query url
        String path = this.getSearchQueryUrl(this.searchTerm);

        HttpPost post = new HttpPost(path);
        // Set form params
        post.setEntity(new UrlEncodedFormEntity(postParams, CharEncoding.UTF_8));

        return post;
    }

    private final String getSearchQueryUrl(String searchTerm) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(super.host);
        if (this.conceptualSearch) {
            sb.append(Endpoints.CONCEPTUAL_SEARCH.getQueryPath(searchTerm));
        } else {
            sb.append(Endpoints.SEARCH.getQueryPath(searchTerm));
        }

        String path = sb.toString();
        return path;
    }

    public LinkedHashMap<String, SearchResult> search() throws IOException {
        long start = System.currentTimeMillis();

        String entityString = super.execute();
        LinkedHashMap<String, SearchResult> data = MAPPER.readValue(entityString, new TypeReference<LinkedHashMap<String, SearchResult>>() {
        });
        long end = System.currentTimeMillis();

        System.out.println(String.format("Search POST request took %d ms", (end - start)));

        return data;
    }

}

enum SearchRequestParameters {
    SORT_BY("sort_by"),
    PAGE("page"),
    SIZE("size"),
    FILTER("filter");

    String parameter;

    SearchRequestParameters(String parameter) {
        this.parameter = parameter;
    }
}
