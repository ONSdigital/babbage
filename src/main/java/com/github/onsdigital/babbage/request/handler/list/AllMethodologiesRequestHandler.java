package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.TypeFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

import static com.github.onsdigital.babbage.api.util.SearchUtils.*;
import static com.github.onsdigital.babbage.search.builders.ONSFilterBuilders.filterTopic;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.*;

/**
 * Created by bren on 21/09/15.
 */
public class AllMethodologiesRequestHandler extends BaseRequestHandler implements ListRequestHandler {

    private static Set<TypeFilter> methodologyFilters = TypeFilter.getMethodologyFilters();

    private final static String REQUEST_TYPE = "allmethodologies";

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        return listPage(REQUEST_TYPE, queries(request));
    }

    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException {
        return listJson(request, REQUEST_TYPE, queries(request));
    }

    private SearchQueries queries(HttpServletRequest request) {
        return () -> toList(
                buildListQuery(request, methodologyFilters, filters(request), true).aggregate(typeCountsAggregate()),
                topicListQuery()
        );
    }

    private SearchFilter filters(HttpServletRequest request) {
        return (listQuery) -> filterTopic(request, listQuery);
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }
}
