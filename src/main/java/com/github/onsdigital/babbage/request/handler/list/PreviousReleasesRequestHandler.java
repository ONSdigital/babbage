package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.util.json.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.github.onsdigital.babbage.api.util.SearchUtils.*;
import static com.github.onsdigital.babbage.search.builders.ONSFilterBuilders.filterUriPrefix;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;
import static com.github.onsdigital.babbage.util.URIUtil.removeLastSegment;

/**
 * Render a list page for the given URI.
 */
public class PreviousReleasesRequestHandler extends BaseRequestHandler implements ListRequestHandler {

    private static Set<TypeFilter> publicationFilters = TypeFilter.getPublicationFilters();

    private final static String REQUEST_TYPE = "previousreleases";

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        assertPageContentType(uri);
        return listPage(REQUEST_TYPE, queries(request, uri));
    }

    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException, ContentReadException {
        assertPageContentType(uri);
        return listJson(request, REQUEST_TYPE, queries(request, uri));
    }

    private SearchQueries queries(HttpServletRequest request, String uri) {
        return () -> toList(
                buildListQuery(request, publicationFilters, filters(uri), false)
        );
    }

    private SearchFilter filters(String uri) {
        return (listQuery) -> filterUriPrefix(uri, listQuery);
    }

    private void assertPageContentType(String uri) throws IOException, ContentReadException {
        ContentResponse contentResponse = ContentClient.getInstance().getContent(removeLastSegment(removeLastSegment(uri)));
        Map<String, Object> objectMap = JsonUtil.toMap(contentResponse.getDataStream());
        if (!isProductPage(objectMap.get("type"))) {
            throw new ResourceNotFoundException("Requested content's previous releases are not available, uri: " + uri + "");
        }
    }

    private boolean isProductPage(Object type) {
        return ContentType.product_page.name().equals(type);
    }


    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }


}
