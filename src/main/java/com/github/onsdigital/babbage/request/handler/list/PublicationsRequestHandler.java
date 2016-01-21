package com.github.onsdigital.babbage.request.handler.list;

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
import static com.github.onsdigital.babbage.search.builders.ONSFilterBuilders.filterUriPrefix;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.*;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

/**
 * Render a list page for bulletins under the given URI.
 */
public class PublicationsRequestHandler implements ListRequestHandler {

    private static Set<TypeFilter> publicationFilters = TypeFilter.getPublicationFilters();
    //    private static ContentType[] contentTypesToCount = addAll(resolveContentTypes(publicationFilters), resolveContentTypes(TypeFilter.getDataFilters()));
    private static ContentType[] contentTypesToCount = TypeFilter.contentTypes(publicationFilters);

    private final static String REQUEST_TYPE = "publications";

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        return list(REQUEST_TYPE, queries(request, extractSearchTerm(request), uri));
    }

    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException {
        return listJson(REQUEST_TYPE, queries(request, extractSearchTerm(request), uri));
    }

    private SearchQueries queries(HttpServletRequest request, String searchTerm, String uri) {
        ONSQuery listQuery = buildListQuery(request, searchTerm, publicationFilters, filters(request, uri)).name("result");
        return () -> combine(
                listQuery,
                docCountsQuery(listQuery.query()).types(contentTypesToCount).name("counts")
        );
    }

    private SearchFilter filters(HttpServletRequest request, String uri) {
        return (listQuery) -> {
            filterUriPrefix(uri, listQuery);
            filterLatest(request, listQuery);
        };
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

}
