package com.github.onsdigital.babbage.api.endpoint.search;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.search.helpers.ONSQueryBuilder;
import com.github.onsdigital.babbage.search.helpers.SearchHelper;
import com.github.onsdigital.babbage.search.helpers.SearchRequestHelper;
import com.github.onsdigital.babbage.search.helpers.SearchResponseHelper;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;
import static org.apache.commons.lang3.ArrayUtils.addAll;

/**
 * Created by bren on 21/09/15.
 */
@Api
public class SearchData {

    //available dataFilters on the page
    private static Set<TypeFilter> dataFilters = TypeFilter.getDataFilters();
    //Counting both data and publication types to show counts on tabs
    private static ContentType[] contentTypesToCount = addAll(resolveContentTypes(dataFilters), resolveContentTypes(TypeFilter.getPublicationFilters()));

    @GET
    public void get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String searchTerm = extractSearchTerm(request);
        SearchRequestHelper.get(request, response, searchTerm, getClass().getSimpleName(), () -> {
            Set<TypeFilter> selectedFilters = extractSelectedFilters(request, dataFilters);
            ContentType[] selectedContentTypes = resolveContentTypes(selectedFilters);

            DisMaxQueryBuilder baseContentQuery = buildBaseContentQuery(searchTerm);
            FunctionScoreQueryBuilder contentQueryBuilder = boostContentTypes(baseContentQuery);

            ONSQueryBuilder contentSearch = onsQuery(request, contentQueryBuilder, selectedContentTypes);
            ONSQueryBuilder counts = countDocTypes(baseContentQuery, contentTypesToCount);

            if (contentSearch.sortBy() == null) {
                contentSearch.sortBy(SortBy.relevance);
            }

            List<SearchResponseHelper> searchResponseHelpers = SearchHelper.searchMultiple(contentSearch, counts);
            LinkedHashMap<String, SearchResult> results = new LinkedHashMap<>();
            results.put("result", searchResponseHelpers.get(0).getResult());
            results.put("counts", searchResponseHelpers.get(1).getResult());
            return results;
        });
    }
}
