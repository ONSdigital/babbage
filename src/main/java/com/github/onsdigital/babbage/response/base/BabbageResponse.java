package com.github.onsdigital.babbage.response.base;

import com.github.onsdigital.babbage.configuration.DeprecationItem;
import com.github.onsdigital.babbage.response.util.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by bren on 08/06/15.
 *
 * A successful response for http request
 *
 */
public abstract class BabbageResponse {

    private String mimeType = APPLICATION_JSON; //Default mimetype
    private String charEncoding = StandardCharsets.UTF_8.name();//Default encoding
    private int status = HttpServletResponse.SC_OK;//Default status
    private Map<String, String> headers;
    protected List<String> errors;

    public BabbageResponse(String mimeType) {
        this.mimeType = mimeType;
        this.errors = new ArrayList<>();
    }

    public BabbageResponse(String mimeType, int status) {
        this(mimeType);
        this.status = status;
    }

    public BabbageResponse() {
        this.errors = new ArrayList<>();
    }

    public void apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(getStatus());
        response.setCharacterEncoding(getCharEncoding());
        response.setContentType(getMimeType());
        if (getHeaders() != null) {
            Set<Map.Entry<String, String>> entries = getHeaders().entrySet();
            for (Map.Entry<String, String> next : entries) {
                response.setHeader(next.getKey(), next.getValue());
            }
        }
        setContentHash(request, response);
    }

    protected void setContentHash(HttpServletRequest request, HttpServletResponse response) {
        //This method needs to exist only because it is overridden in the BabbageContentBasedBinaryResponse and BabbageContentBasedStringResponse classes
    }

    public String getMimeType() {
        return mimeType;
    }

    public BabbageResponse setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
    }

    private Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public BabbageResponse setCharEncoding(String charEncoding) {
        this.charEncoding = charEncoding;
        return this;
    }

    public String getCharEncoding() {
        return charEncoding;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addSunsetHeaders(DeprecationItem config) {
        addHeader(HttpHeaders.DEPRECATION, config.deprecationDate().toString());
        addHeader(HttpHeaders.LINK, config.link());
        addHeader(HttpHeaders.SUNSET, config.sunsetDate().toString());
    }
}
