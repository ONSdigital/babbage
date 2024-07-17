package com.github.onsdigital.babbage.api.endpoint.search;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.api.util.SearchRendering;
import com.github.onsdigital.babbage.api.util.SearchUtils;
import com.github.onsdigital.babbage.response.BabbageRedirectResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.util.Map;
import java.util.Set;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.typeCountsQuery;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

@Api
public class SearchData {

    // available dataFilters on the page
    private static Set<TypeFilter> dataFilters = TypeFilter.getDataFilters();
    // Counting all types to show counts on tabs
    private static ContentType[] contentTypesToCount =
            TypeFilter.contentTypes(TypeFilter.getAllFilters());

    @GET
    public void get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String searchTerm = extractSearchTerm(request);

        BabbageResponse babbageResponse;
        String timeSeriesRedirect = SearchUtils.getRedirectWhenTimeSeries(request, searchTerm);
        if (timeSeriesRedirect != null) {
            babbageResponse =
                    new BabbageRedirectResponse(
                            timeSeriesRedirect);
        } else {
            Map<String, SearchResult> results =
                    SearchUtils.search(
                            searchTerm,
                            queries(request, searchTerm),
                            false);
            babbageResponse = SearchRendering.buildResponse(request, getClass().getSimpleName(), results);
        }
        babbageResponse.apply(request, response);
    }

    private SearchQueries queries(HttpServletRequest request, String searchTerm) {
        return () -> {
            ONSQuery query = SearchUtils.buildSearchQuery(request, searchTerm, dataFilters);
            return toList(query, typeCountsQuery(query.query()).types(contentTypesToCount));
        };
    }
}
