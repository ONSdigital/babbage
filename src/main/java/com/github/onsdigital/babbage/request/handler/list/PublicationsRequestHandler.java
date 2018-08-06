package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.api.endpoint.rss.service.RssService;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

import static com.github.onsdigital.babbage.api.util.SearchUtils.*;
import static com.github.onsdigital.babbage.search.builders.ONSFilterBuilders.filterLatest;
import static com.github.onsdigital.babbage.search.builders.ONSFilterBuilders.filterUriAndTopics;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.typeCountsQuery;

/**
 * Render a list page for bulletins under the given URI.
 */
public class PublicationsRequestHandler extends BaseRequestHandler implements ListRequestHandler {

    private static Set<TypeFilter> publicationFilters = TypeFilter.getPublicationFilters();
    private static ContentType[] contentTypesToCount = TypeFilter.contentTypes(publicationFilters);

    private static RssService rssService = RssService.getInstance();

    private final static String REQUEST_TYPE = "publications";

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        if (rssService.isRssRequest(request)) {
            return rssService.getPublicationListFeedResponse(request);
        }
        return listPage(REQUEST_TYPE, queries(request, uri));
    }

    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException {
        return listJson(request, REQUEST_TYPE, queries(request, uri));
    }

    private SearchQueries queries(HttpServletRequest request, String uri) {
        ONSQuery listQuery = buildListQuery(request, publicationFilters, filters(request, uri), false);
        return () -> toList(
                listQuery,
                typeCountsQuery(listQuery.query()).types(contentTypesToCount)
        );
    }

    private SearchFilter filters(HttpServletRequest request, String uri) {
        return (listQuery) -> {
            filterUriAndTopics(uri, listQuery);
            filterLatest(request, listQuery);
        };
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

}
