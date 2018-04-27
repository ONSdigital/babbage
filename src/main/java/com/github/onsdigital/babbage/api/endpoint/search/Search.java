package com.github.onsdigital.babbage.api.endpoint.search;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.util.List;
import java.util.Set;

import static com.github.onsdigital.babbage.api.util.SearchUtils.buildSearchQuery;
import static com.github.onsdigital.babbage.api.util.SearchUtils.search;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.*;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractPage;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;
import static org.apache.commons.lang.ArrayUtils.isEmpty;

@Api
public class Search {

    //available allFilters on the page
    private static Set<TypeFilter> allFilters = TypeFilter.getAllFilters();
    private static ContentType[] contentTypesToCount = TypeFilter.contentTypes(allFilters);


    @GET
    public void get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String searchTerm = extractSearchTerm(request);
        String[] filter = request.getParameterValues("filter");
        boolean searchAdditionalContent = false;
        if (extractPage(request) == 1 && isEmpty(filter)) {
            searchAdditionalContent = true;
        }
        search(request, getClass().getSimpleName(), searchTerm, queries(request, searchTerm, searchAdditionalContent), searchAdditionalContent)
                .apply(request, response);
    }

    private SearchQueries queries(HttpServletRequest request, String searchTerm, boolean searchAdditionalContent) {
        ONSQuery query = buildSearchQuery(request, searchTerm, allFilters);
        List<ONSQuery> queries = toList(
                query,
                typeCountsQuery(query.query()).types(contentTypesToCount)
        );
        if (searchAdditionalContent) {
            queries.add(bestTopicMatchQuery(searchTerm).name("featuredResult").highlight(true));
        }
        return () -> queries;
    }
}