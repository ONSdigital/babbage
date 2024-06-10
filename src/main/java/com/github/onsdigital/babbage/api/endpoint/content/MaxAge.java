package com.github.onsdigital.babbage.api.endpoint.content;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

import com.github.davidcarboni.cryptolite.Password;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.BabbageException;
import com.github.onsdigital.babbage.error.BadRequestException;
import com.github.onsdigital.logging.v2.event.SimpleEvent;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;

/**
 * End point for getting the maxAge for the given uri
 */
@Api
public class MaxAge {
    private static final String MAXAGE_KEY_HASH = appConfig().babbage().getMaxAgeSecret();

    @GET
    public Object get(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_GONE);
        return "MaxAge endpoint is no longer available within Babbage";
    }

    private String handleError(@Context HttpServletResponse response, int statusCode, Throwable e) {
        SimpleEvent.error().exception(e).log("api " + getApiName() + ": error calculating max age");
        response.setStatus(statusCode);
        return "Failed calculating max age";
    }

    protected boolean verifyKey(String key) {
        return Password.verify(key, MAXAGE_KEY_HASH);
    }

    protected int getMaxAge(HttpServletRequest request) throws Exception {
        String uri = request.getParameter("uri");

        ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);
        return contentResponse.getMaxAge();
    }

    protected String getApiName() {
        return "MaxAge";
    }
}
