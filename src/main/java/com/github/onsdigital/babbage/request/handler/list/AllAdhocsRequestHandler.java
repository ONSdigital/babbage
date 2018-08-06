package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.api.util.SearchUtils;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.model.ContentType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.github.onsdigital.babbage.api.util.SearchUtils.listJson;
import static com.github.onsdigital.babbage.api.util.SearchUtils.listPage;
import static com.github.onsdigital.babbage.search.builders.ONSFilterBuilders.filterDates;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;

/**
 * Created by bren on 06/10/15.
 */
public class AllAdhocsRequestHandler extends BaseRequestHandler implements ListRequestHandler {

    private final static String REQUEST_TYPE = "alladhocs";

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
                SearchUtils
                        .buildListQuery(request, filters(request))
                        .types(ContentType.static_adhoc)
        );
    }

    private SearchFilter filters(HttpServletRequest request) {
        return (listQuery) -> filterDates(request, listQuery);
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }


}
