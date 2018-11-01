package com.github.onsdigital.babbage.search.external;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.external.requests.base.SearchClosable;
import com.github.onsdigital.babbage.search.external.requests.base.ShutdownThread;
import com.github.onsdigital.babbage.search.external.requests.search.*;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;

public class SearchClient implements SearchClosable {

    private static SearchClient INSTANCE;

    public static SearchClient getInstance() {
        if (INSTANCE == null) {
            synchronized (SearchClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SearchClient();
                    System.out.println("Initialising external search client");
                    Runtime.getRuntime().addShutdownHook(new ShutdownThread(INSTANCE));
                    System.out.println("Initialised external search client successfully");
                }
            }
        }
        return INSTANCE;
    }

    private final SearchHttpClient client;

    private SearchClient() {
        this.client = new SearchHttpClient();
    }

    public CloseableHttpResponse execute(AbstractSearchRequest searchRequest) throws Exception {
        HttpRequestBase request = searchRequest.getRequestBase();

        // Log the request ID
        String requestId = searchRequest.getRequestId();
        System.out.println(String.format("Executing external search request [context=%s]", requestId));
        return this.client.execute(request);
    }

    @Override
    public void close() throws Exception {
        this.client.close();
    }

    public LinkedHashMap<String, SearchResult> proxyQueries(List<ONSQuery> queryList) throws Exception {
        Map<String, Future<SearchResult>> futures = new HashMap<>();

        for (ONSQuery query : queryList) {
            if (null != query && null != query.name()) {
                int page = query.page() != null ? query.page() : 1;
                int pageSize = query.size() > 0 ? query.size() : Configuration.GENERAL.getResultsPerPage();

                ProxyONSQuery proxyONSQuery = new ProxyONSQuery(query, page, pageSize);
                Future<SearchResult> future = SearchClientExecutorService.getInstance().submit(proxyONSQuery);
                futures.put(query.name(), future);
            } else {
                throw new Exception("Unable to complete search request due to null query/name");
            }
        }

        return processFutures(futures);
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest request, String listType) throws Exception {
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
                    searchQuery = new ContentQuery(searchTerm, listTypeEnum, page, pageSize, sortBy, typeFilters);
                    break;
                case COUNTS:
                    searchQuery = new TypeCountsQuery(searchTerm, listTypeEnum);
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
