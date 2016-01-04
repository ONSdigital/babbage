package com.github.onsdigital.babbage.api.endpoint.search;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.request.handler.base.ListPageBaseRequestHandler;
import com.github.onsdigital.babbage.search.AggregateQuery;
import com.github.onsdigital.babbage.search.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.SearchRequestHelper;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.field.FilterableField;
import com.github.onsdigital.babbage.util.URIUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;
import static com.github.onsdigital.babbage.util.RequestUtil.getParam;

/**
 * Created by bren on 19/11/15.
 */
@Api
public class AtoZ extends ListPageBaseRequestHandler {

    private final static ContentType[] ALLOWED_TYPES = {ContentType.bulletin};


    @GET
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = URIUtil.cleanUri(request.getRequestURI());
        String requestType = URIUtil.resolveRequestType(uri);
        uri = URIUtil.removeLastSegment(uri);
        if ("data".equals(requestType)) {
            getData(uri, request).apply(request,response);
        } else {
            get(uri, request).apply(request,response);
        }
    }

    @Override
    public String getRequestType() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    @Override
    public ContentType[] getAllowedTypes() {
        return ALLOWED_TYPES;
    }

    @Override
    public boolean isLocalisedUri() {
        return false;
    }

    @Override
    protected boolean isListTopics() {
        return false;
    }

    @Override
    protected ONSQuery createQuery(String requestedUri, HttpServletRequest request) throws IOException, ContentReadException {
        ONSQuery query = super.createQuery(requestedUri, request);
        if (StringUtils.isEmpty(query.getSearchTerm())) { // sort by title if no search term available
            query.getSorts().clear();
            addSort(query, SortBy.FIRST_LETTER);
        }
        String titlePrefix = getTitlePrefix(request);
        if (titlePrefix != null) {
            SearchRequestHelper.addTermFilter(query, FilterableField.title_first_letter, titlePrefix);
        }
        return query;
    }


    @Override
    protected AggregateQuery buildAggregateQuery(ONSQuery query) {
        AggregateQuery aggregateQuery = new AggregateQuery();
        aggregateQuery.setTypes(query.getTypes())
                .setFields(query.getFields())
                .setSearchTerm(query.getSearchTerm());
        addTermFilter(aggregateQuery, FilterableField.latestRelease, true);
        addTermAggregation(aggregateQuery, "count_by_starts_with", FilterableField.title_first_letter);
        return aggregateQuery;
    }

    private String getTitlePrefix(HttpServletRequest request) {
        String prefix = StringUtils.trim(getParam(request, "az"));
        if (!StringUtils.isEmpty(prefix)) {
            return prefix.toLowerCase();
        }
        return null;

    }

    @Override
    protected boolean isFilterLatest(HttpServletRequest request) {
        return true;
    }

}