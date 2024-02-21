package com.github.onsdigital.babbage.api.endpoint.content;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.response.BabbageContentBasedBinaryResponse;
import com.github.onsdigital.babbage.response.util.CacheControlHelper;
import com.github.onsdigital.babbage.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Map;

/**
 * Proxy requests to zebedee generator endpoint.
 */
@Api
public class Generator {

    @GET
    public void get(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {

        Map<String, String[]> queryParameters = RequestUtil.getQueryParameters(request);

        try {
            ContentResponse contentResponse = ContentClient.getInstance().getGenerator(request.getParameter("uri"), queryParameters);
            String contentDispositionHeader = "attachment; ";
            contentDispositionHeader += contentResponse.getName() == null ? "" : "filename=\"" + contentResponse.getName() + "\"";
            response.setCharacterEncoding("UTF-8");
            CacheControlHelper.setCacheHeaders(request, response, contentResponse.getHash(), contentResponse.getMaxAge());
            response.setHeader("Content-Disposition", contentDispositionHeader);
            new BabbageContentBasedBinaryResponse(contentResponse, contentResponse.getDataStream(), contentResponse.getMimeType()).apply(request, response);
        } catch (Throwable t) {
            ErrorHandler.handle(request, response, t);
        }
    }
}
