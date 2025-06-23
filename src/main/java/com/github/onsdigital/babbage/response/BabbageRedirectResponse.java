package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.response.base.BabbageResponse;
import org.apache.hc.core5.http.HttpStatus;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import java.io.IOException;

/**
 * Sends a redirect to the client
 */
public class BabbageRedirectResponse extends BabbageResponse {

    private String redirectUri;

    public BabbageRedirectResponse(String redirectAddress) {
        this.redirectUri = redirectAddress;
    }

    @Override
    public void apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.SC_PERMANENT_REDIRECT);
        response.setHeader(HttpHeaders.LOCATION, redirectUri);
    }


    public String getRedirectUri() {
        return redirectUri;
    }
}
