package com.github.onsdigital.babbage.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.onsdigital.babbage.util.URIUtil.isLatestRequest;
import static org.junit.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;

/**
 * @author sullid (David Sullivan) on 10/04/2018
 * @project babbage
 */
public class TestURIUtil {

    /**
     * Tests that the first URI segment is properly removed, i.e
     * /data/economy/inflationandpriceindices becomes /economy/inflationandpriceindices
     */
    @Test
    public void testRemoveEndpoint() {

        final Map<String, String> expected = new HashMap<String, String>() {{
            put("/data/economy/inflationandpriceindices", "/economy/inflationandpriceindices");
        }};

        for (String uri : expected.keySet()) {
            String newUri = URIUtil.removeEndpoint(uri);
            assertEquals(newUri, expected.get(uri));
        }

    }

    /**
     * Tests that the equest type is properly extracted form URLs, i.e
     * for uri "/economy/inflationandpriceindices/data" request type is "data"
     */
    @Test
    public void testResolveRequestType() {
        final Map<String, String> expected = new HashMap<String, String>() {{
            put("/economy/inflationandpriceindices/data", "data");
        }};

        for (String uri : expected.keySet()) {
            String requestType = URIUtil.resolveRequestType(uri);
            assertEquals(requestType, expected.get(uri));
        }
    }

    /**
     * Tests that isDataRequest properly identifies data requests
     */
    @Test
    public void testIsDataRequest() {
        final Map<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("/economy/inflationandpriceindices/data", Boolean.TRUE);
            put("/economy/inflationandpriceindices/", Boolean.FALSE);
        }};

        for (String uri : expected.keySet()) {
            boolean isDataRequest = URIUtil.isDataRequest(uri);
            assertEquals(isDataRequest, expected.get(uri));
        }
    }

    /**
     * Tests that resource uri is properly extracted from request type suffixed uris, i.e
     * for uri "/economy/inflationandpriceindices/data" resource uri "/economy/inflationandpriceindices"
     */
    @Test
    public void testRemoveLastSegment() {
        final Map<String, String> expected = new HashMap<String, String>() {{
            put("/economy/inflationandpriceindices/data", "/economy/inflationandpriceindices");
        }};

        for (String uri : expected.keySet()) {
            String lastSegmentRemoved = URIUtil.removeLastSegment(uri);
            assertEquals(lastSegmentRemoved, expected.get(uri));
        }
    }

    /**
     * Tests that cleanUri properly removes all trailing '/'
     */
    @Test
    public void testCleanUri() {

        final Map<String, String> expected = new HashMap<String, String>() {{
            put("example/uri///", "example/uri");
            put("another/example/uri/", "another/example/uri");
            put("yet/another/example/uri", "yet/another/example/uri");
            put("/", "/");
        }};

        for (String uri : expected.keySet()) {
            String cleanUri = URIUtil.cleanUri(uri);
            assertEquals(cleanUri, expected.get(uri));
        }
    }


    @Test
    public void testIsLatestRequest() {
        assertTrue(isLatestRequest("/bulletin/latest"));
        assertFalse(isLatestRequest("/bulletin"));
        assertFalse(isLatestRequest("/bulletin/late"));

    }

}
