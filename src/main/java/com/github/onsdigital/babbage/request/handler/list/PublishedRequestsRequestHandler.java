package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.request.handler.base.ListPageBaseRequestHandler;
import com.github.onsdigital.babbage.search.model.ContentType;

/**
 * Render a list page for bulletins under the given URI.
 */
public class PublishedRequestsRequestHandler extends ListPageBaseRequestHandler {

    private final static ContentType[] ALLOWED_TYPES = {ContentType.static_foi};
    private final static String REQEUST_TYPE = "publishedrequests";

    @Override
    public String getRequestType() {
        return REQEUST_TYPE;
    }

    @Override
    public ContentType[] getAllowedTypes() {
        return ALLOWED_TYPES;
    }

    @Override
    public boolean isLocalisedUri() {
        return false;
    }
}
