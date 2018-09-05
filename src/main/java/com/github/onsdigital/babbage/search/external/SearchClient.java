package com.github.onsdigital.babbage.search.external;

import com.github.onsdigital.babbage.search.external.requests.base.SearchClosable;
import com.github.onsdigital.babbage.search.external.requests.base.ShutdownThread;
import com.github.onsdigital.babbage.search.external.requests.recommend.models.UserSession;
import com.github.onsdigital.babbage.search.external.requests.recommend.requests.UpdateUserRecommendations;
import com.github.onsdigital.babbage.search.external.requests.search.requests.*;
import com.github.onsdigital.babbage.search.external.requests.search.requests.conceptual.ConceptualContentQuery;
import com.github.onsdigital.babbage.search.external.requests.search.requests.conceptual.ConceptualTypeCountsQuery;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;

public class SearchClient implements SearchClosable {

    private static SearchClient INSTANCE;

    public static SearchClient getInstance() throws Exception {
        if (INSTANCE == null) {
            synchronized (SearchClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SearchClient();
                    System.out.println("Initialising external search client");
                    INSTANCE.start();
                    Runtime.getRuntime().addShutdownHook(new ShutdownThread(INSTANCE));
                    System.out.println("Initialised external search client successfully");
                }
            }
        }
        return INSTANCE;
    }

    private final HttpClient client;

    public SearchClient() {
        this.client = new HttpClient();
    }

    public void start() throws Exception {
        this.client.start();
    }

    @Override
    public void close() throws Exception {
        this.client.stop();
    }

    public Request request(String uri) {
        return client.newRequest(uri);
    }

    public Request get(String uri) {
        return this.request(uri).method(HttpMethod.GET);
    }

    public Request post(String uri) {
        return this.request(uri).method(HttpMethod.POST);
    }

    public LinkedHashMap<String, SearchResult> proxyQueries(List<ONSQuery> queryList) throws Exception {
        Map<String, Future<SearchResult>> futures = new HashMap<>();

        for (ONSQuery query : queryList) {
            ProxyONSQuery request = new ProxyONSQuery(query);
            Future<SearchResult> future = SearchClientExecutorService.getInstance().submit(request);
            futures.put(query.name(), future);
        }

        return processFutures(futures);
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest request, String listType) throws Exception {
        return this.search(request, listType, false);
    }

    /**
     * Performs an external search request
     * @param request The original babbage request
     * @param listType The search listType
     * @param isConceptualSearch Whether to use conceptual or ordinary search
     * @return Map containing JSON expected by babbage
     * @throws Exception
     */
    public LinkedHashMap<String, SearchResult> search(HttpServletRequest request, String listType, boolean isConceptualSearch) throws Exception {
        Map<String, Future<SearchResult>> futures = new HashMap<>();

        final String searchTerm = extractSearchTerm(request);
        final int page = extractPage(request);
        final int pageSize = extractSize(request);
        final SortBy sortBy = extractSortBy(request, ContentQuery.DEFAULT_SORT_BY);

        ListType listTypeEnum = ListType.forString(listType);

        final Set<TypeFilter> typeFilters = extractSelectedFilters(request, listTypeEnum.getTypeFilters(), false);

        SearchType[] searchTypes;
        if (!listTypeEnum.equals(ListType.ONS)) {
            searchTypes = new SearchType[]{SearchType.CONTENT, SearchType.COUNTS};
        } else {
            // Submit content
            searchTypes = SearchType.getBaseSearchTypes();
        }

        for (SearchType searchType : searchTypes) {
            SearchQuery searchQuery;
            switch (searchType) {
                case CONTENT:
                    if (isConceptualSearch) {
                        Cookie[] cookies = request.getCookies();
                        searchQuery = new ConceptualContentQuery(searchTerm, listTypeEnum, page, pageSize, sortBy,
                                typeFilters, cookies);
                    } else {
                        searchQuery = new ContentQuery(searchTerm, listTypeEnum, page, pageSize, sortBy, typeFilters);
                    }
                    break;
                case COUNTS:
                    if (isConceptualSearch) {
                        searchQuery = new ConceptualTypeCountsQuery(searchTerm, listTypeEnum);
                    } else {
                        searchQuery = new TypeCountsQuery(searchTerm, listTypeEnum);
                    }
                    break;
                case FEATURED:
                    searchQuery = new FeaturedResultQuery(searchTerm, listTypeEnum);
                    break;
                default:
                    throw new Exception(String.format("Unknown searchType: %s", searchType.getSearchType()));
            }
            // Submit concurrent requests
            Future<SearchResult> future = SearchClientExecutorService.getInstance().submit(searchQuery);
            futures.put(searchType.getResultKey(), future);
        }

        // Wait until complete
        return processFutures(futures);
    }

    public UserSession updateUserByPage(HttpServletRequest request, String uri) throws Exception {

        Cookie[] cookies = request.getCookies();

        String userId = null;
        String sessionId = null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(UpdateUserRecommendations.USER_ID_COOKIE)) {
                userId = cookie.getValue();
            } else if (cookie.getName().equals(UpdateUserRecommendations.SESSION_ID_COOKIE)) {
                sessionId = cookie.getValue();
            }
        }

        if (null != userId && null != sessionId) {
            UpdateUserRecommendations updateUserRequest = new UpdateUserRecommendations(uri, userId, sessionId);
            Future<UserSession> future = SearchClientExecutorService.getInstance().submit(updateUserRequest);
            UserSession userSession = future.get();

            return userSession;
        } else {
            throw new Exception("User/Session ID cookies not specified");
        }
    }

    private static LinkedHashMap<String, SearchResult> processFutures(Map<String, Future<SearchResult>> futures) throws ExecutionException, InterruptedException {
        // Collect results
        LinkedHashMap<String, SearchResult> results = new LinkedHashMap<>();

        for (String key : futures.keySet()) {
            SearchResult result = futures.get(key).get();
            results.put(key, result);
        }

        return results;
    }
}
