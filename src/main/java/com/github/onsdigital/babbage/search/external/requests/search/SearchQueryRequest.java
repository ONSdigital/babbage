package com.github.onsdigital.babbage.search.external.requests.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.external.Endpoint;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;

public class SearchQueryRequest extends AbstractSearchRequest<SearchResult> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    protected final String searchTerm;
    protected final QueryTypes queryType;

    public SearchQueryRequest(HttpServletRequest babbageRequest, String host, String searchTerm, QueryTypes queryType) {
        super(babbageRequest, host);

        this.searchTerm = searchTerm;
        this.queryType = queryType;
    }

    protected URI queryUri() {
        URI uri = URI.create(Endpoint.SEARCH.getUri() + this.queryType.getQueryType());
        return uri;
    }

    /**
     * Builds the content query url using the given search term.
     * @return URL to query external search service.
     */
    protected String getContentQueryUrl() {
        int page = extractPage(super.babbageRequest);
        int pageSize = extractSize(super.babbageRequest);

        URIBuilder ub = new URIBuilder(this.queryUri());
        ub.addParameter(SearchRequestParameters.QUERY.parameter, this.searchTerm);
        ub.addParameter(SearchRequestParameters.PAGE.parameter, String.valueOf(page));
        ub.addParameter(SearchRequestParameters.SIZE.parameter, String.valueOf(pageSize));

        boolean useUserVector = Configuration.SEARCH_SERVICE.isConceptualSearchUserTrackingEnabled();
        ub.addParameter(SearchRequestParameters.USER_VECTOR_QUERY.parameter, String.valueOf(useUserVector));
        return this.host + ub.toString();
    }

    /**
     * Generates the HTTP POST request
     * @return
     * @throws IOException
     */
    @Override
    protected HttpRequestBase generateRequest() throws IOException {
        SortBy sortBy = extractSortBy(super.babbageRequest, SortBy.relevance);

        // Build form params
        List<NameValuePair> postParams = generateFilterPostParams(super.babbageRequest);
        postParams.add(new BasicNameValuePair(SearchRequestParameters.SORT_BY.parameter, sortBy.name()));

        // Build query url
        String path = this.getContentQueryUrl();

        // Create the post request
        HttpPost post = new HttpPost(path);

        // Set form params
        post.setEntity(new UrlEncodedFormEntity(postParams, CharEncoding.UTF_8));

        return post;
    }

    /**
     * Executes the search request and unmarshalls the JSON response.
     * @return
     */
    @Override
    public SearchResult call() throws IOException {
        String entityString = super.execute();

        SearchResult searchResult = MAPPER.readValue(entityString, SearchResult.class);
        return searchResult;
    }

    enum SearchRequestParameters {
        QUERY("q"),
        SORT_BY("sort_by"),
        PAGE("page"),
        SIZE("size"),
        FILTER("filter"),
        USER_VECTOR_QUERY("user_vector_query");

        String parameter;

        SearchRequestParameters(String parameter) {
            this.parameter = parameter;
        }
    }

    public static List<NameValuePair> generateFilterPostParams(HttpServletRequest request) {
        Set<TypeFilter> typeFilters = extractSelectedFilters(request, TypeFilter.getAllFilters(), false);

        // Build form params
        List<NameValuePair> postParams = new ArrayList<>();

        for (TypeFilter typeFilter : typeFilters) {
            for (ContentType contentType : typeFilter.getTypes()) {
                postParams.add(new BasicNameValuePair(SearchRequestParameters.FILTER.parameter, contentType.name()));
            }
        }
        return postParams;
    }
}
