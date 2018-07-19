package com.github.onsdigital.babbage.search.external;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.external.requests.recommend.UserUpdateRequest;
import com.github.onsdigital.babbage.search.external.requests.search.*;
import com.github.onsdigital.babbage.search.external.requests.spelling.Correction;
import com.github.onsdigital.babbage.search.external.requests.spelling.SpellCheckerRequest;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.model.SearchResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractConceptualSearch;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

public class SearchClient {

    private static final String WHITESPACE = " ";

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

    public SearchResult proxyRequest(HttpServletRequest babbageRequest, ONSQuery query) throws IOException {
        ProxyONSQueryRequest request = new ProxyONSQueryRequest(babbageRequest, this.host, query);
        SearchResult result = request.call();

        return result;
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest babbageRequest) throws IOException {
        String searchTerm = extractSearchTerm(babbageRequest);

        LinkedHashMap<String, SearchResult> searchResult = new LinkedHashMap<>();

        for (QueryTypes queryType : QueryTypes.values()) {
            SearchQueryRequest request;
            if (Configuration.SEARCH_SERVICE.isConceptualSearchServiceEnabled() && extractConceptualSearch(babbageRequest)) {
                request = new ConceptualSearchQueryRequest(babbageRequest, this.host, searchTerm, queryType);
            } else {
                request = new SearchQueryRequest(babbageRequest, this.host, searchTerm, queryType);
            }
            SearchResult result = request.call();
            searchResult.put(queryType.getResultKey(), result);
        }

        return searchResult;
    }

    public LinkedHashMap<String, SearchResult> searchAndSpellCheck(HttpServletRequest babbageRequest) throws IOException {
        LinkedHashMap<String, SearchResult> searchResults = this.search(babbageRequest);
        Map<String, Correction> correctionResult = this.spellChecker(babbageRequest);

        List<String> availableSuggestions = new LinkedList<>();
        for (String key : correctionResult.keySet()) {
            Correction correction = correctionResult.get(key);
            if (correction.getCorrection() != null && !correction.getCorrection().equals(key) && correction.getProbability() >= 0.5f) {
                availableSuggestions.add(correction.getCorrection());
            }
        }

        // Build the final suggestion
        Iterator<String> it = availableSuggestions.iterator();
        StringBuilder sb = new StringBuilder();

        while (it.hasNext()) {
            String suggestion = it.next();
            sb.append(suggestion);
            if (it.hasNext()) {
                sb.append(WHITESPACE);
            }
        }

        String suggestionString = sb.toString();
        if (suggestionString.length() > 0) {
            final List<String> suggestionsList = new ArrayList<String>() {{
                add(suggestionString);
            }};

            // Add to search results
            for (String key : searchResults.keySet()) {
                searchResults.get(key).setSuggestions(suggestionsList);
            }
        }

        // Return final search results
        return searchResults;
    }

    public Map<String, Correction> spellChecker(HttpServletRequest babbageRequest) throws IOException {
        String searchTerm = extractSearchTerm(babbageRequest);

        SpellCheckerRequest request = new SpellCheckerRequest(babbageRequest, this.host, searchTerm);
        Map<String, Correction> result = request.call();

        return result;
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
