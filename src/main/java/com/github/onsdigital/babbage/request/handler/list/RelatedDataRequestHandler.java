package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.api.util.SearchUtils;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.field.Field;
import com.github.onsdigital.babbage.util.json.JsonUtil;
import org.elasticsearch.index.query.QueryBuilders;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.onsdigital.babbage.api.util.SearchUtils.*;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;

/**
 * Created by bren on 25/11/15.
 */
public class RelatedDataRequestHandler extends BaseRequestHandler implements ListRequestHandler {


    private boolean isPublication(Object typeName) {
        if (typeName == null) {
            return false;
        }
        ContentType contentType;
        try {
            contentType = ContentType.valueOf(String.valueOf(typeName));
        } catch (IllegalArgumentException e) {
            return false;
        }

        TypeFilter.getPublicationFilters();
        for (TypeFilter typeFilter : TypeFilter.getPublicationFilters()) {
            for (ContentType type : typeFilter.getTypes()) {
                if (type == contentType) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        List<Map> uriList = getRelatedDataUris(uri);
        return isEmpty(uriList) ? buildPageResponse(getRequestType(), null) : listPage(getRequestType(), queries(uriList, request));
    }


    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException, ContentReadException {
        List<Map> uriList = getRelatedDataUris(uri);
        return isEmpty(uriList) ? buildDataResponse(getRequestType(), null) : listJson(request, getRequestType(), queries(uriList, request));
    }

    private SearchQueries queries(List<Map> uriList, HttpServletRequest request) throws IOException, ContentReadException {
        String[] uriArray = new String[uriList.size()];
        for (int i = 0; i < uriList.size(); i++) {
            uriArray[i] = (String) uriList.get(i).get(Field.uri.name());

        }
        return () -> toList(
                SearchUtils.buildListQuery(request, filters(uriArray), SortBy.title).types(ContentType.dataset_landing_page, ContentType.reference_tables)
        );
    }

    private SearchFilter filters(String[] uriArray) {
        return (query) -> query.filter(QueryBuilders.termsQuery(Field.uri.fieldName(), uriArray));
    }

    private boolean isEmpty(List<Map> relatedDataUris) {
        return relatedDataUris == null || relatedDataUris.isEmpty();
    }

    @Override
    public String getRequestType() {
        return "relateddata";
    }


    private List<Map> getRelatedDataUris(String requestedUri) throws ContentReadException, IOException {
        ContentResponse contentResponse = ContentClient.getInstance().getContent(requestedUri);
        Map<String, Object> objectMap = JsonUtil.toMap(contentResponse.getDataStream());
        if (!isPublication(objectMap.get("type"))) {
            throw new ResourceNotFoundException();
        }
        return (List) objectMap.get("relatedData");
    }

}
