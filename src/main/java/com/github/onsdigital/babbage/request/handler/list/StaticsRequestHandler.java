package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.api.util.SearchUtils;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.builders.ONSFilterBuilders;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.model.ContentType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.github.onsdigital.babbage.api.util.SearchUtils.listJson;
import static com.github.onsdigital.babbage.api.util.SearchUtils.listPage;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;

public class StaticsRequestHandler extends BaseRequestHandler implements ListRequestHandler {

    private final static String REQUEST_TYPE = "staticlist";

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        return listPage(REQUEST_TYPE, queries(uri,request));
    }

    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException {
        return listJson(request, REQUEST_TYPE, queries(uri,request));
    }

    private SearchQueries queries(String uri, HttpServletRequest request) {
        return () -> toList(
                SearchUtils
                        .buildListQuery(request, filters(uri))
                        .types(ContentType.static_page)
        );
    }

    private SearchFilter filters(String uri) {
        return (listQuery) -> ONSFilterBuilders.filterUriPrefix(uri, listQuery);
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

}
