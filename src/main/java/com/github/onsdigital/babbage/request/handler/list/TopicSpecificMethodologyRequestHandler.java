package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

import static com.github.onsdigital.babbage.api.util.SearchUtils.*;
import static com.github.onsdigital.babbage.search.builders.ONSFilterBuilders.filterUriPrefix;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.typeCountsAggregate;
import static com.github.onsdigital.babbage.search.input.TypeFilter.contentTypes;

/**
 * Render a list page for bulletins under the given URI.
 */
public class TopicSpecificMethodologyRequestHandler extends BaseRequestHandler implements ListRequestHandler {

    private static Set<TypeFilter> methodologyFilters = TypeFilter.getMethodologyFilters();
    private static ContentType[] contentTypesToCount = contentTypes(methodologyFilters);


    private final static String REQUEST_TYPE = "topicspecificmethodology";


    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        return listPage(REQUEST_TYPE, queries(uri, request));
    }

    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException {
        return listJson(request, REQUEST_TYPE, queries(uri, request));
    }

    private SearchQueries queries(String uri, HttpServletRequest request) {
        return () -> toList(
                buildListQuery(request, methodologyFilters, filters(uri), true).aggregate(typeCountsAggregate())
        );
    }

    private SearchFilter filters(String uri) {
        return (listQuery) -> filterUriPrefix(uri, listQuery);
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

}
