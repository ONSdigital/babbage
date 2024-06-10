package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.content.client.ContentResponse;

import java.io.IOException;

public class BabbageContentBasedStringResponse extends BabbageStringResponse {

    private ContentResponse contentResponse;

    public BabbageContentBasedStringResponse(ContentResponse contentResponse, String data, String mimeType) throws IOException {
        super(data, mimeType);
        this.contentResponse = contentResponse;
    }

    public BabbageContentBasedStringResponse(ContentResponse contentResponse, String data) throws IOException {
        super(data);
        this.contentResponse = contentResponse;
    }
}
