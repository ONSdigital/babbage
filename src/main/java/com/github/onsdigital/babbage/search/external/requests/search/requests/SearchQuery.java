package com.github.onsdigital.babbage.search.external.requests.search.requests;

import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.SearchType;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpScheme;

/**
 * Class for querying the dp-conceptual-search APIs
 */
public abstract class SearchQuery extends AbstractSearchRequest<SearchResult> {

    private final String searchTerm;
    private final ListType listType;
    private final SearchType searchType;

    public SearchQuery(String searchTerm, ListType listType, SearchType searchType) {
        super(SearchResult.class);
        this.searchTerm = searchTerm;
        this.listType = listType;
        this.searchType = searchType;
    }

    protected abstract SearchEndpoints getEndpoint();

    /**
     * Method to build the target URI with desired URL parameters
     * @return
     */
    protected URIBuilder buildUri() {
        String path = this.getEndpoint().getEndpointForListType(this.listType) +
                this.searchType.getSearchType();

        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(HttpScheme.HTTP.asString())
                .setHost(HOST)
                .setPath(path)
                .addParameter(SearchParam.QUERY.getParam(), this.searchTerm);

        return uriBuilder;
    }

    /**
     * Simply calls buildUri to get the target for HTTP requests and returns as a string
     * @return
     */
    @Override
    public String targetUri() {
        return this.buildUri().toString();
    }

    /**
     * Defaults to an empty GET request
     * @return
     * @throws Exception
     */
    @Override
    protected ContentResponse getContentResponse() throws Exception {
        return super.get().send();
    }

    /**
     * Enum of available URL parameters
     */
    public enum SearchParam {
        QUERY("q"),
        PAGE("page"),
        SIZE("size"),
        SORT("sort_by"),
        FILTER("filter");

        private String param;

        SearchParam(String param) {
            this.param = param;
        }

        public String getParam() {
            return param;
        }
    }
}
