package com.github.onsdigital.babbage.search.external;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.external.requests.recommend.UserUpdateRequest;
import com.github.onsdigital.babbage.search.external.requests.search.ConceptualSearchQueryRequest;
import com.github.onsdigital.babbage.search.external.requests.search.DepartmentsSearchQueryRequest;
import com.github.onsdigital.babbage.search.external.requests.search.QueryTypes;
import com.github.onsdigital.babbage.search.external.requests.search.SearchQueryRequest;
import com.github.onsdigital.babbage.search.model.SearchResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

public class SearchClient {

    private static SearchClient INSTANCE = new SearchClient(Configuration.SEARCH_SERVICE.getSearchServiceAddress());

    private final String host;

    private SearchClient(String host) {
        if (!host.endsWith("/")) {
            host = String.format("%s/", host);
        }
        this.host = host;
    }

    public static SearchClient getInstance() {
        return INSTANCE;
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest babbageRequest) throws IOException {
        String searchTerm = extractSearchTerm(babbageRequest);

        LinkedHashMap<String, SearchResult> searchResult = new LinkedHashMap<>();

        for (QueryTypes queryType : QueryTypes.values()) {
            SearchQueryRequest request;
            if (Configuration.SEARCH_SERVICE.isConceptualSearchServiceEnabled()) {
                request = new ConceptualSearchQueryRequest(babbageRequest, this.host, searchTerm, queryType);
            } else {
                request = new SearchQueryRequest(babbageRequest, this.host, searchTerm, queryType);
            }
            SearchResult result = request.call();
            searchResult.put(queryType.getResultKey(), result);
        }

        return searchResult;
    }

    public SearchResult searchDepartments(HttpServletRequest babbageRequest) throws IOException {
        String searchTerm = extractSearchTerm(babbageRequest);

        DepartmentsSearchQueryRequest request = new DepartmentsSearchQueryRequest(babbageRequest, this.host, searchTerm);
        SearchResult result = request.call();

        return result;
    }

    public Map<String, Object> updateUser(HttpServletRequest babbageRequest, String pageUri) throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(babbageRequest, this.host, pageUri);
        return request.call();
    }

}
