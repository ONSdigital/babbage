package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.response.util.CacheControlHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BabbageContentBasedStringResponse extends BabbageStringResponse {

    private ContentResponse contentResponse;

    public BabbageContentBasedStringResponse(ContentResponse contentResponse, String data, String mimeType) throws IOException {
        super(data, mimeType);
        this.contentResponse = contentResponse;
    }

    public BabbageContentBasedStringResponse(ContentResponse contentResponse, String data) throws IOException {
        super(data);
        this.contentResponse = contentResponse;
    }

    @Override
    protected void setCacheHeaders(HttpServletRequest request, HttpServletResponse response) {
        CacheControlHelper.setCacheHeaders(request, response, CacheControlHelper.hashData(getData()), contentResponse.getMaxAge());
    }
}
