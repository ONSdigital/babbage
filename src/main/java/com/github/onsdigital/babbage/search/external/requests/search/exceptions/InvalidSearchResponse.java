package com.github.onsdigital.babbage.search.external.requests.search.exceptions;

import org.eclipse.jetty.client.api.ContentResponse;

public class InvalidSearchResponse extends Exception {

    public InvalidSearchResponse(ContentResponse response) {
        super(String.format("Received invalid response JSON from search service: [status=%d, response=%s]",
                response.getStatus(), response.getContentAsString()));
    }

}
