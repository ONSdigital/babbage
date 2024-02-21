package com.github.onsdigital.babbage.api.endpoint.content;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.response.BabbageContentBasedBinaryResponse;
import com.github.onsdigital.babbage.response.util.CacheControlHelper;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

/**
 * Created by bren on 01/07/15.
 * <p/>
 * Serves resource files from content service with no content disposition
 */
@Api
public class Resource {

    @GET
    public void get(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        String uri = null;
        try {
            uri = request.getParameter("uri");
            String width = request.getParameter("width");

            ContentResponse contentResponse;

            try {
                contentResponse = ContentClient.getInstance().getResource(uri);
            } catch (ContentReadException e) {
                error().data("uri", uri).log("content client get resource request failed returning status 404");
                ErrorHandler.renderErrorPage(404, response);
                return;
            }

            String contentDispositionHeader = "inline; ";
            contentDispositionHeader += contentResponse.getName() == null ? "" : "filename=\"" + contentResponse.getName() + "\"";

            CacheControlHelper.setCacheHeaders(request, response, contentResponse.getHash(), contentResponse.getMaxAge());

            response.setHeader("Content-Disposition", contentDispositionHeader);

            try (InputStream contentResponseBody = contentResponse.getDataStream()) {

                BufferedImage image = null;

                if (width != null) {

                    try {
                        Integer w = Integer.parseInt(width, 10);

                        if (uri.toLowerCase().endsWith(".png") || uri.toLowerCase().endsWith(".jpg") || uri.toLowerCase().endsWith(".jpeg")) {
                            image = ImageIO.read(contentResponseBody);
                            int clampedWidth = Math.max(1, Math.min(image.getWidth(), w));

                            double ratio = (double) clampedWidth / (double) image.getWidth();
                            double h = image.getHeight() * ratio;
                            int height = (int) h;

                            Image newImage = image.getScaledInstance(clampedWidth, height, Image.SCALE_SMOOTH);
                            image = new BufferedImage(newImage.getWidth(null), newImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                            image.getGraphics().drawImage(newImage, 0, 0, null);
                        }

                    } catch (IOException e) {
                        // intentionally swallowing exception so we can return original image if resize fails
                        error().exception(e).data("uri", uri).log("failed to generate image");
                    }
                }

                if (image != null) {

                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        ImageIO.write(image, "png", os);
                        try (InputStream input = new ByteArrayInputStream(os.toByteArray())) {
                            new BabbageContentBasedBinaryResponse(contentResponse, input, contentResponse.getMimeType()).apply(request, response);
                            return;
                        }

                    }
                }

                new BabbageContentBasedBinaryResponse(contentResponse, contentResponseBody, contentResponse.getMimeType()).apply(request, response);
            }
        } catch (Throwable t) {
            error().exception(t).data("uri", uri).log("get resource request threw unexpected error");
            ErrorHandler.handle(request, response, t);
        }
    }
}
