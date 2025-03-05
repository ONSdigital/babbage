package com.github.onsdigital.babbage.request.handler.content;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;

import javax.servlet.http.HttpServletRequest;


import static com.github.onsdigital.babbage.util.RequestUtil.getQueryParameters;


/**
 * Handle data requests. Proxies data requests to content service
 */
public class DataRequestHandler extends BaseRequestHandler {

    public static final String REQUEST_TYPE = "data";

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request) throws Exception {
        return getData(requestedUri, request);
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

    public BabbageResponse getData(String uri, HttpServletRequest request) throws Exception {
        ContentResponse contentResponse = ContentClient.getInstance().getContent(uri, getQueryParameters(request));
        return new BabbageContentBasedStringResponse(contentResponse, contentResponse.getAsString());
    }
}
