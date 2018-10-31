package com.github.onsdigital.babbage.search.external;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpVersion;

import java.util.List;

public class MockedSearchErrorResponse implements ContentResponse {
    @Override
    public String getMediaType() {
        return null;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public byte[] getContent() {
        return new byte[0];
    }

    @Override
    public String getContentAsString() {
        return "Error";
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public <T extends ResponseListener> List<T> getListeners(Class<T> aClass) {
        return null;
    }

    @Override
    public HttpVersion getVersion() {
        return null;
    }

    @Override
    public int getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR_500;
    }

    @Override
    public String getReason() {
        return null;
    }

    @Override
    public HttpFields getHeaders() {
        return null;
    }

    @Override
    public boolean abort(Throwable throwable) {
        return false;
    }
}
