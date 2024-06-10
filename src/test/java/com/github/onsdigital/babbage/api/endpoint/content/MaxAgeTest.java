package com.github.onsdigital.babbage.api.endpoint.content;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.client.HttpClients;
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
    private Babbage mockBabbage;

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

        ApplicationConfiguration mockAppConfigInstance = mock(ApplicationConfiguration.class);
        when(ApplicationConfiguration.appConfig()).thenReturn(mockAppConfigInstance);
        when(mockAppConfigInstance.babbage()).thenReturn(mockBabbage);
        when(endpoint.verifyKey(KEY_HASH)).thenReturn(true);
    }

    @After
    public void closeService() throws Exception {
        mockitoAnnotations.close();
        mockAppConfig.close();
        mockHttpClients.close();
    }

    @Test
    public void testGetEndpoint() {
        Object result = endpoint.get(request, response);

        assertEquals("MaxAge endpoint is no longer available within Babbage", result);
        verify(response).setStatus(HttpServletResponse.SC_GONE);
    }
}
