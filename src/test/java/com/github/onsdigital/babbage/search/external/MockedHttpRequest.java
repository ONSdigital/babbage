package com.github.onsdigital.babbage.search.external;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.ContentResponse;

import java.net.URI;

public class MockedHttpRequest extends HttpRequest {

    private final MockedSearchResponse contentResponse;

    public MockedHttpRequest(URI uri, MockedSearchResponse contentResponse) {
        super(new HttpClient(), null, uri);
        this.contentResponse = contentResponse;
    }

    @Override
    public ContentResponse send() {
        return this.contentResponse;
    }
}
