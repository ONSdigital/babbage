package com.github.onsdigital.babbage.search.external.requests.search.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.SearchType;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Replaces the internal content query by executing a HTTP request against the dp-conceptual-search content API
 */
public class ContentQuery extends SearchQuery {

    public static final SortBy DEFAULT_SORT_BY = SortBy.relevance;

    private final int page;
    private final int pageSize;
    private final SortBy sortBy;
    private Set<TypeFilter> typeFilters;

    public ContentQuery(String searchTerm, ListType listType, int page, int pageSize) {
        this(searchTerm, listType, page, pageSize, DEFAULT_SORT_BY);
    }

    public ContentQuery(String searchTerm, ListType listType, int page, int pageSize, SortBy sortBy) {
        this(searchTerm, listType, page, pageSize, sortBy, listType.getTypeFilters());
    }

    public ContentQuery(String searchTerm, ListType listType, int page, int pageSize, SortBy sortBy,
                        Set<TypeFilter> typeFilters) {
        super(searchTerm, listType, SearchType.CONTENT);
        this.page = page;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.typeFilters = typeFilters;
    }

    /**
     * Builds type filters set as Strings from the specified TypeFilter enum
     * @return
     */
    private Set<String> contentTypeFilters() {
        Set<String> filters = new HashSet<>();

        for (TypeFilter typeFilter : this.typeFilters) {
            for (ContentType contentType : typeFilter.getTypes()) {
                filters.add(contentType.name());
            }
        }

        return filters;
    }

    /**
     * Returns list of content type filters as a JSON formatted string
     * @return
     * @throws JsonProcessingException
     */
    private String contentTypeFiltersAsString() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this.contentTypeFilters());
    }

    @Override
    protected SearchEndpoints getEndpoint() {
        return SearchEndpoints.SEARCH_ONS;
    }

    /**
     * Calls super.buildUri() and adds page number and size URL parameters
     * @return
     */
    @Override
    protected URIBuilder buildUri() {
        return super.buildUri()
                .addParameter(SearchParam.PAGE.getParam(), String.valueOf(this.page))
                .addParameter(SearchParam.SIZE.getParam(), String.valueOf(this.pageSize));
    }

    /**
     * Executes a HTTP POST request with type filters and sort options specified as a JSON payload
     * @return
     * @throws Exception
     */
    @Override
    protected ContentResponse getContentResponse() throws Exception {
        final String filterString = this.contentTypeFiltersAsString();
        final Map<String, String> content = new HashMap<String, String>() {{
            put(SearchParam.FILTER.getParam(), filterString);
            put(SearchParam.SORT.getParam(), sortBy.name());
        }};

        Request request = super.post()
                .content(new StringContentProvider(MAPPER.writeValueAsString(content)));
        return request.send();
    }
}
