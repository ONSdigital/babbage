package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.response.base.BabbageResponse;
import org.eclipse.jetty.http.HttpHeader;

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
        String forwardedHost = request.getHeader(HttpHeader.X_FORWARDED_HOST.asString());
        String forwardedProto = request.getHeader(HttpHeader.X_FORWARDED_PROTO.asString());

        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

        if ((null != forwardedHost && !forwardedHost.isEmpty()) && (null != forwardedProto && !forwardedProto.isEmpty())) {
            String url = buildHttpsRedirectUrl(forwardedProto, forwardedHost, redirectUri);
            response.setHeader(HttpHeaders.LOCATION, url);
        } else {
            response.setHeader(HttpHeaders.LOCATION, redirectUri);
        }
    }

    /**
     *
     * @param scheme
     * @param host
     * @param redirectUri
     * @return
     */
    private static String buildHttpsRedirectUrl(String scheme, String host, String redirectUri) {
        redirectUri = !redirectUri.startsWith("/") ? String.format("/%s", redirectUri) : redirectUri;
        return scheme + "://" + host + redirectUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
