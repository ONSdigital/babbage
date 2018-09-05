package com.github.onsdigital.babbage.search.external.requests.search.requests;

import com.github.onsdigital.babbage.search.external.SearchEndpoints;
import com.github.onsdigital.babbage.search.external.SearchType;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpScheme;

import javax.servlet.http.Cookie;

/**
 * Class for querying the dp-conceptual-search APIs
 */
public abstract class SearchQuery extends AbstractSearchRequest<SearchResult> {

    private final String searchTerm;
    private final ListType listType;
    private final SearchType searchType;

    public SearchQuery(String searchTerm, ListType listType, SearchType searchType) {
        this(searchTerm, listType, searchType, null);
    }

    public SearchQuery(String searchTerm, ListType listType, SearchType searchType, Cookie[] cookies) {
        super(SearchResult.class, cookies);
        this.searchTerm = searchTerm;
        this.listType = listType;
        this.searchType = searchType;
    }

    protected abstract SearchEndpoints getEndpoint();

    /**
     * Method to build the target URI with desired URL parameters
     * @return
     */
    @Override
    public URIBuilder targetUri() {
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
     * Defaults to an empty GET request
     * @return
     * @throws Exception
     */
    @Override
    protected Request getRequest() throws Exception {
        return super.get();
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
