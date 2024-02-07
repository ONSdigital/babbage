package com.github.onsdigital.babbage.api.util;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.github.onsdigital.logging.v2.event.SimpleEvent;

public class LegacyCacheProxyMaxAge {
    private final String legacyCacheProxyUrl;

    public LegacyCacheProxyMaxAge(String legacyCacheProxyUrl) {
        this.legacyCacheProxyUrl = legacyCacheProxyUrl;
    }

    public int getMaxAgeValueFromLegacyCacheProxy(String resourceURI) {
        int maxAgeValue = 0;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequest forwardRequest = buildRequest(resourceURI);

            try (CloseableHttpResponse proxyResponse = httpClient.execute(forwardRequest)) {
                maxAgeValue = extractMaxAgeValue(proxyResponse);
            }
        } catch (IOException e) {
            SimpleEvent.error().exception(e).log("Error getting max-age from legacy cache proxy");
        }

        return maxAgeValue;    
    }
    
    private HttpUriRequest buildRequest(String resourceURI) {
        String requestUri = legacyCacheProxyUrl + resourceURI;

        return RequestBuilder.create("GET").setUri(requestUri).build();
    }

    private int extractMaxAgeValue(HttpResponse proxyResponse) {
        int maxAgeValue = 0;

        Header cacheControlHeader = proxyResponse.getFirstHeader("Cache-Control");

        if (cacheControlHeader != null) {
            String cacheControlValue = cacheControlHeader.getValue();
            String[] directives = cacheControlValue.split(",");
            for (String directive : directives) {
                String trimmedDirective = directive.trim();
                if (trimmedDirective.startsWith("max-age=")) {
                    String maxAgeValueAsString = trimmedDirective.split("=")[1];
                    maxAgeValue = Integer.parseInt(maxAgeValueAsString);
                    break;
                }
            }
        }

        return maxAgeValue;
    }
}
