package com.github.onsdigital.babbage.api.endpoint.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import com.github.onsdigital.babbage.api.util.LegacyCacheProxyMaxAge;

import java.io.IOException;

public class LegacyCacheProxyMaxAgeTest {
    private CloseableHttpResponse mockResponse;
    private MockedStatic<HttpClients> mockHttpClients;
    private LegacyCacheProxyMaxAge legacyCacheProxyMaxAge;

    @Before
    public void setUp() throws IOException {
        mockResponse = mock(CloseableHttpResponse.class);

        CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
        when(mockClient.execute(any())).thenReturn(mockResponse);

        mockHttpClients = mockStatic(HttpClients.class);
        mockHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);

        legacyCacheProxyMaxAge = new LegacyCacheProxyMaxAge("mock-legacy-cache-proxy-url");
    }

    @After
    public void tearDown() {
        mockHttpClients.close();
    }

    @Test
    public void testGetMaxAgeValueFromLegacyCacheProxy_WhenCacheControlHeaderIsNull() {
        when(mockResponse.getFirstHeader("Cache-Control")).thenReturn(null);

        int result = legacyCacheProxyMaxAge.getMaxAgeValueFromLegacyCacheProxy("/test/resource/uri");

        assertEquals(0, result);
    }

    @Test
    public void testGetMaxAgeValueFromLegacyCacheProxy_WhenCacheControlHeaderIsDeclared() {
        Header cacheControl = new BasicHeader("Cache-Control", "public, max-age=3600");

        when(mockResponse.getFirstHeader("Cache-Control")).thenReturn(cacheControl);

        int result = legacyCacheProxyMaxAge.getMaxAgeValueFromLegacyCacheProxy("/test/resource/uri");

        assertEquals(3600, result);
    }
}
