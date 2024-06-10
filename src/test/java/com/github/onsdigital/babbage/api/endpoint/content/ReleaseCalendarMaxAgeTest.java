package com.github.onsdigital.babbage.api.endpoint.content;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ReleaseCalendarMaxAgeTest {
    private final static String KEY_HASH = "the-key";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    @Spy
    private ReleaseCalendarMaxAge endpoint;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(endpoint.verifyKey(KEY_HASH)).thenReturn(true);
    }
}
