package com.github.onsdigital.babbage.util.http;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Http;
import com.github.davidcarboni.httpino.Response;

import java.io.IOException;

/**
 * Created by dave on 8/24/16.
 */
public class HttpClient {

    private static final HttpClient HTTP_CLIENT = new HttpClient();

    public static HttpClient getInstance() {
        return HttpClient.HTTP_CLIENT;
    }

    private HttpClient() {
        // hide constructor
    }

    public <T> Response<T> postJson(Endpoint endpoint, Object message, Class<T> t) {
        try (Http http = new Http()) {
            return http.postJson(endpoint, message, t);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
