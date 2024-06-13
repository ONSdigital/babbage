package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.response.base.BabbageResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * Sends a permanent moved response with new uri
 *
 */
public class BabbageResourceMovedResponse extends BabbageResponse {

    private String redirectUri;

    public BabbageResourceMovedResponse(String redirectAddress) {
        this.redirectUri = redirectAddress;
    }

    @Override
    public void apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        addHeader("Location", redirectUri);
        super.apply(request, response);
    }

}
