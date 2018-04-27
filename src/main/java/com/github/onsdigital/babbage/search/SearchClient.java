package com.github.onsdigital.babbage.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import com.github.onsdigital.babbage.search.model.Suggestions;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;

/**
 * @author sullid (David Sullivan) on 17/04/2018
 * @project babbage
 *
 * A single search client to replace all search functionality in Babbage with an external HTTP
 * request to a dedicated service.
 */
public class SearchClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String RESULT_KEY = "result";
    private static final String SUGGESTIONS_KEY = "suggestions";
    private static final String WHITESPACE = " ";

    private static SearchClient INSTANCE = new SearchClient(Configuration.SEARCH_SERVICE.getSearchServiceAddress());

    private final String host;

    private SearchClient(String host) {
        this.host = host;
    }

    public static SearchClient getInstance() {
        return INSTANCE;
    }

    private HttpGet autocompleteGet(HttpServletRequest request) throws UnsupportedEncodingException {
        String searchTerm = extractSearchTerm(request);

        HttpGet httpGet = new HttpGet(this.getAutocompleteUrl(searchTerm));
        return httpGet;
    }

    private HttpPost searchPost(String searchTerm, HttpServletRequest request) throws UnsupportedEncodingException {
        Set<TypeFilter> typeFilters = extractSelectedFilters(request, TypeFilter.getAllFilters(), false);
        SortBy sortBy = extractSortBy(request, SortBy.relevance);
        int page = extractPage(request);
        int pageSize = extractSize(request);

        // Build form params
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(RequestParameters.SORT_BY.parameter, sortBy.name()));
        postParams.add(new BasicNameValuePair(RequestParameters.PAGE.parameter, String.valueOf(page)));
        postParams.add(new BasicNameValuePair(RequestParameters.SIZE.parameter, String.valueOf(pageSize)));

        for (TypeFilter typeFilter : typeFilters) {
            for (ContentType contentType : typeFilter.getTypes()) {
                postParams.add(new BasicNameValuePair(RequestParameters.FILTER.parameter, contentType.name()));
            }
        }

        // Build query url
        String path = this.getSearchQueryUrl(searchTerm);

        HttpPost post = new HttpPost(path);
        // Set form params
        post.setEntity(new UrlEncodedFormEntity(postParams, CharEncoding.UTF_8));

        return post;
    }

    private String execute(HttpServletRequest request, HttpRequestBase requestBase) throws IOException {
        CookieStore cookieStore = extractCookies(request);

        try (CloseableHttpClient client = httpClient(cookieStore)) {
            HttpClientContext context = httpClientContext(cookieStore);
            try (CloseableResponse response = new CloseableResponse(client.execute(requestBase, context))) {
                int statusCode = response.getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    String entityString = EntityUtils.toString(entity);

                    return entityString;
                } else {
                    throw new IOException(String.format("Got status code %d from search client", statusCode));
                }
            } finally {
                requestBase.releaseConnection();
            }
        }
    }

    public LinkedHashMap<String, LinkedHashMap<String, Suggestions>> autocomplete(HttpServletRequest request) throws IOException {
        HttpGet get = this.autocompleteGet(request);

        String entityString = this.execute(request, get);
        LinkedHashMap<String, LinkedHashMap<String, Suggestions>> data = MAPPER.readValue(entityString,
                new TypeReference<LinkedHashMap<String, LinkedHashMap<String, Suggestions>>>(){});

        return data;
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest request) throws IOException {
        long start = System.currentTimeMillis();

        String searchTerm = extractSearchTerm(request);
        HttpPost post = this.searchPost(searchTerm, request);

        String entityString = this.execute(request, post);
        LinkedHashMap<String, SearchResult> data = MAPPER.readValue(entityString, new TypeReference<LinkedHashMap<String, SearchResult>>() {
        });
        long end = System.currentTimeMillis();

        System.out.println(String.format("Search POST request took %d ms", (end - start)));

        start = System.currentTimeMillis();
        // Get suggestions
        LinkedHashMap<String, LinkedHashMap<String, Suggestions>> suggestionsData = this.autocomplete(request);
        if (suggestionsData != null && suggestionsData.containsKey(SUGGESTIONS_KEY)) {
            LinkedHashMap<String, Suggestions> suggestionsResponse = suggestionsData.get(SUGGESTIONS_KEY);

            if (suggestionsResponse.size() > 0) {
                List<String> availableSuggestions = new LinkedList<>();
                for (String key : suggestionsResponse.keySet()) {
                    Suggestions suggestions = suggestionsResponse.get(key);
                    if (suggestions.getSuggestions() != null && suggestions.getSuggestions().size() > 0) {
                        // Get first (best) suggestion
                        Suggestions.Suggestion suggestion = suggestions.getSuggestions().get(0);

                        if (!searchTerm.contains(suggestion.getSuggestion())) {
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
                    if (data.containsKey(RESULT_KEY)) {
                        SearchResult result = data.get(RESULT_KEY);
                        result.setSuggestions(suggestionsList);
                    }
                }
            }
        }
        end = System.currentTimeMillis();
        System.out.println(String.format("Autocomplete GET request took %d ms", (end - start)));

        return data;
    }

    private String getAutocompleteUrl(String searchTerm) throws UnsupportedEncodingException {
        String path = new StringBuilder(this.host)
                .append(Endpoints.AUTOCOMPLETE.getQueryPath(searchTerm))
                .toString();
        return path;
    }

    private String getSearchQueryUrl(String searchTerm) throws UnsupportedEncodingException {
        String path = new StringBuilder(this.host)
                .append(Endpoints.SEARCH.getQueryPath(searchTerm))
                .toString();
        return path;
    }

    public String getHost() {
        return this.host;
    }

    private static CookieStore extractCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        CookieStore cookieStore = new BasicCookieStore();

        if (null != cookies) {
            for (Cookie cookie : cookies) {
                BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                clientCookie.setDomain(request.getServerName());
                clientCookie.setPath(request.getContextPath());
                cookieStore.addCookie(clientCookie);
            }
        }
        return cookieStore;
    }

    private static HttpClientContext httpClientContext(CookieStore cookieStore) {
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        return context;
    }

    private static CloseableHttpClient httpClient(CookieStore cookieStore) {
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();

        return client;
    }

    private static class CloseableResponse implements AutoCloseable {

        private final HttpResponse response;

        public CloseableResponse(HttpResponse response) {
            this.response = response;
        }

        public int getStatusCode() {
            return this.response.getStatusLine().getStatusCode();
        }

        public HttpEntity getEntity() {
            return this.response.getEntity();
        }

        @Override
        public void close() throws IOException {
            EntityUtils.consume(this.response.getEntity());
        }
    }

    private enum Endpoints {
        SEARCH("search/ons"),
        AUTOCOMPLETE("suggest/autocomplete");

        String path;

        Endpoints(String path) {
            this.path = path;
        }

        String getQueryPath(String query) throws UnsupportedEncodingException {
            return new StringBuilder(this.path)
                    .append(String.format("?q=%s", URLEncoder.encode(query, CharEncoding.UTF_8)))
                    .toString();
        }
    }

    private enum RequestParameters {
        SORT_BY("sort_by"),
        PAGE("page"),
        SIZE("size"),
        FILTER("filter");

        String parameter;

        RequestParameters(String parameter) {
            this.parameter = parameter;
        }
    }
}
