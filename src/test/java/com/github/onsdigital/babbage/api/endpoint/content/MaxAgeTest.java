package com.github.onsdigital.babbage.api.endpoint.content;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.github.onsdigital.babbage.configuration.ApplicationConfiguration;
import com.github.onsdigital.babbage.configuration.Babbage;
import com.github.onsdigital.babbage.content.client.ContentReadException;

public class MaxAgeTest {
    private final static String KEY_HASH = "the-key";

    private AutoCloseable mockitoAnnotations;

    private MockedStatic<ApplicationConfiguration> mockAppConfig;

    private MockedStatic<HttpClients> mockHttpClients;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    @Spy
    private MaxAge endpoint;

    @Before
    public void setup() throws Exception {
        mockitoAnnotations = MockitoAnnotations.openMocks(this);
        mockAppConfig = mockStatic(ApplicationConfiguration.class);
        mockHttpClients = mockStatic(HttpClients.class);

        when(endpoint.verifyKey(KEY_HASH)).thenReturn(true);
    }

    @After
    public void closeService() throws Exception {
        mockitoAnnotations.close();
        mockAppConfig.close();
        mockHttpClients.close();
    }

    @Test
    public void testGetNoKey() throws Exception {
        Object result = endpoint.get(request, response);
        assertEquals("Wrong key, make sure you pass in the right key", result);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testGetWrongKey() throws Exception {
        when(request.getParameter("key")).thenReturn("wrong");
        Object result = endpoint.get(request, response);
        assertEquals("Wrong key, make sure you pass in the right key", result);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testGetEndpointErrorReadingContent() throws Exception {
        when(request.getParameter("key")).thenReturn(KEY_HASH);
        
        ContentReadException ex = new ContentReadException(HttpServletResponse.SC_NOT_FOUND, "Content not found");
        doThrow(ex).when(endpoint).getMaxAge(request);

        assertEquals("Failed calculating max age", endpoint.get(request, response));
        verify(response).setStatus(ex.getStatusCode());
    }

    @Test
    public void testGetEndpoint() throws Exception {
        int maxAge = 55;
        when(request.getParameter("key")).thenReturn(KEY_HASH);
        doReturn(maxAge).when(endpoint).getMaxAge(request);
        assertEquals(maxAge, endpoint.get(request, response));
    }

}
