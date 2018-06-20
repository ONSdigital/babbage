package com.github.onsdigital.babbage.search.external;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.external.requests.AutocompleteRequest;
import com.github.onsdigital.babbage.search.external.requests.SearchRequest;
import com.github.onsdigital.babbage.search.external.requests.UserInterestUpdateRequest;
import com.github.onsdigital.babbage.search.external.requests.UserPageInterestUpdateRequest;
import com.github.onsdigital.babbage.search.model.SearchResult;
import com.github.onsdigital.babbage.search.model.Suggestions;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractConceptualSearch;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractPage;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

public class SearchClient {

    private static final String RESULT_KEY = "result";
    private static final String SUGGESTIONS_KEY = "suggestions";
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

    public LinkedHashMap<String, LinkedHashMap<String, Suggestions>> autocomplete(HttpServletRequest babbageRequest) throws IOException {
        AutocompleteRequest request = new AutocompleteRequest(babbageRequest, this.host);
        return request.autocomplete();
    }

    public LinkedHashMap<String, Object> updateUserByPage(HttpServletRequest babbageRequest, String pageUri) throws IOException {
        UserPageInterestUpdateRequest request = new UserPageInterestUpdateRequest(babbageRequest, this.host, pageUri);
        return request.send();
    }

    public SearchResult searchDepartments(HttpServletRequest babbageRequest) throws IOException {
        SearchRequest request = new SearchRequest(babbageRequest, this.host, SearchRequest.ListType.DEPARTMENTS);
        return request.searchDepartments();
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest babbageRequest, SearchRequest.ListType listType) throws IOException {
        // Perform the search request
        SearchRequest request = new SearchRequest(babbageRequest, this.host, listType);

        return request.search();
    }

    public LinkedHashMap<String, SearchResult> searchAndSuggest(HttpServletRequest babbageRequest, SearchRequest.ListType listType) throws IOException {
        LinkedHashMap<String, SearchResult> searchResults = this.search(babbageRequest, listType);
        LinkedHashMap<String, LinkedHashMap<String, Suggestions>> autocompleteResults = this.autocomplete(babbageRequest);

        String searchTerm = extractSearchTerm(babbageRequest);

        if (autocompleteResults != null && autocompleteResults.containsKey(SUGGESTIONS_KEY)) {
            LinkedHashMap<String, Suggestions> suggestionsResponse = autocompleteResults.get(SUGGESTIONS_KEY);

            if (suggestionsResponse.size() > 0) {
                List<String> availableSuggestions = new LinkedList<>();
                for (String key : suggestionsResponse.keySet()) {
                    Suggestions suggestions = suggestionsResponse.get(key);
                    if (suggestions.getSuggestions() != null && suggestions.getSuggestions().size() > 0) {
                        // Get first (best) suggestion
                        Suggestions.Suggestion suggestion = suggestions.getSuggestions().get(0);

                        if (null != searchTerm && !searchTerm.contains(suggestion.getSuggestion())) {
                            availableSuggestions.add(suggestion.getSuggestion());
                        }
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
                    if (searchResults.containsKey(RESULT_KEY)) {
                        SearchResult result = searchResults.get(RESULT_KEY);
                        result.setSuggestions(suggestionsList);
                    }
                }
            }
        }

        return searchResults;
    }

}
