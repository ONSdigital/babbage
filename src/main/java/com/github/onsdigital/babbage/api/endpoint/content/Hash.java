package com.github.onsdigital.babbage.api.endpoint.content;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.BabbageException;
import com.github.onsdigital.babbage.response.util.ContentHashHelper;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import java.io.IOException;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * Created by bren on 16/11/15.
 * Temporary end point for getting hash value of requested resource uri,
 */
@Api
public class Hash {
    @GET
    public void get(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {

        try {
            response.setContentType(TEXT_PLAIN);

            String uri = request.getParameter("uri");
            ContentResponse contentResponse = ContentClient.getInstance().getResource(uri);
            ContentHashHelper.resolveHash(request, response, contentResponse.getHash());
            IOUtils.write(contentResponse.getHash(), response.getOutputStream());
        } catch (ContentReadException e) {
            handleError(response, e.getStatusCode(), e);
        } catch (BabbageException e) {
            handleError(response, e.getStatusCode(), e);
        } catch (Throwable t) {
            handleError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t);
        }
    }

    void handleError(@Context HttpServletResponse response, int statusCode, Throwable e) throws IOException {
        response.setStatus(statusCode);
        IOUtils.write("Failed reading content!", response.getOutputStream());
        error().exception(e).log("api Hash: error getting resource from content API");
    }
}
