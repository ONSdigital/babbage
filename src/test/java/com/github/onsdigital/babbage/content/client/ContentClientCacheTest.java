package com.github.onsdigital.babbage.content.client;

import org.apache.hc.client5.http.*;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.*;


import org.junit.*;
import org.mockito.*;

import java.util.*;

import static org.mockito.Mockito.*;

public class ContentClientCacheTest {
    @Mock
    private ContentClientCache mockClient;

    private static final String TAXONOMY_ENDPOINT = "/taxonomy";
    private static final String NAVIGATION_ENDPOINT = "/navigation";


    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetTaxonomy_EmptyParams() throws ContentReadException {
        Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        try {
            ContentResponse response = mockClient.getTaxonomy(queryParameters);
            verify(mockClient, atLeastOnce()).getTaxonomy(queryParameters);
        } catch (ContentReadException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetTaxonomy_nullParams() throws ContentReadException {
        Map<String, String[]> queryParameters = null;
        try {
            ContentResponse response = mockClient.getTaxonomy(queryParameters);
            verify(mockClient, atLeastOnce()).getTaxonomy(queryParameters);
        } catch (ContentReadException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetNavigation_EmptyParams() {
        Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        try {
            ContentResponse response = mockClient.getNavigation(queryParameters);
            verify(mockClient, atLeastOnce()).getNavigation(queryParameters);
        } catch (ContentReadException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetNavigation_nullParams() throws ContentReadException {
        Map<String, String[]> queryParameters = null;
        ContentResponse response = mockClient.getNavigation(queryParameters);
        try {
            verify(mockClient, atLeastOnce()).getNavigation(queryParameters);
        } catch (ContentReadException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetNavigation_cy() throws ContentReadException {
        Map<String, String[]> queryParameters = new HashMap<String, String[]>();
        String[] param = {"2"};
        queryParameters.put("depth", param);

        List<NameValuePair> returnParams = new ArrayList<NameValuePair>();
        returnParams.add(new BasicNameValuePair("depth", "2"));
        returnParams.add(new BasicNameValuePair("lang", "cy"));
        when(mockClient.getParameters(queryParameters)).thenReturn(returnParams);

        try {
            ContentResponse response = mockClient.getNavigation(queryParameters);
            verify(mockClient, atLeastOnce()).getNavigation(queryParameters);
        } catch (ContentReadException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testNavigationSendGet_empty() throws ContentReadException {
        ContentResponse response = mock(ContentResponse.class);

        List<NameValuePair> returnParams = new ArrayList<NameValuePair>();
        returnParams.add(new BasicNameValuePair("depth", "2"));
        returnParams.add(new BasicNameValuePair("lang", "cy"));

        when(mockClient.sendGet(anyString(), anyListOf(NameValuePair.class))).thenReturn(response);

        response = mockClient.sendGet(NAVIGATION_ENDPOINT, returnParams);
        verify(mockClient, atLeastOnce()).sendGet(anyString(), anyList());
        org.junit.Assert.assertNotNull(response);

    }

    @Test
    public void testNavigationSendGet_null() throws ContentReadException {
        ContentResponse response = null;

        List<NameValuePair> returnParams = new ArrayList<NameValuePair>();
        returnParams.add(new BasicNameValuePair("depth", "2"));
        returnParams.add(new BasicNameValuePair("lang", "cy"));
        when(mockClient.sendGet(anyString(), anyListOf(NameValuePair.class))).thenReturn(response);

        response = mockClient.sendGet(NAVIGATION_ENDPOINT, returnParams);
        verify(mockClient, atLeastOnce()).sendGet(anyString(), anyList());
        org.junit.Assert.assertNull(response);

    }

    @Test
    public void testTasxonomySendGet_empty() throws ContentReadException {
        ContentResponse response = mock(ContentResponse.class);

        List<NameValuePair> returnParams = new ArrayList<NameValuePair>();
        returnParams.add(new BasicNameValuePair("depth", "2"));
        returnParams.add(new BasicNameValuePair("lang", "cy"));

        when(mockClient.sendGet(anyString(), anyListOf(NameValuePair.class))).thenReturn(response);

        response = mockClient.sendGet(TAXONOMY_ENDPOINT, returnParams);
        verify(mockClient, atLeastOnce()).sendGet(anyString(), anyList());
        org.junit.Assert.assertNotNull(response);

    }

    @Test
    public void testTaxonomySendGet_null() throws ContentReadException {
        ContentResponse response = null;

        List<NameValuePair> returnParams = new ArrayList<NameValuePair>();
        returnParams.add(new BasicNameValuePair("depth", "2"));
        returnParams.add(new BasicNameValuePair("lang", "cy"));
        when(mockClient.sendGet(anyString(), anyListOf(NameValuePair.class))).thenReturn(response);

        response = mockClient.sendGet(TAXONOMY_ENDPOINT, returnParams);
        verify(mockClient, atLeastOnce()).sendGet(anyString(), anyList());
        org.junit.Assert.assertNull(response);

    }
}
