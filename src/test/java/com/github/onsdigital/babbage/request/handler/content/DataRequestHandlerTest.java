package com.github.onsdigital.babbage.request.handler.content;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.onsdigital.babbage.response.util.HttpHeaders;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.response.BabbageStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.configuration.deprecation.DeprecationItem;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.google.gson.JsonObject;

@RunWith(MockitoJUnitRunner.class)
public class DataRequestHandlerTest {

    private static final String URI = "/economy/environmentalaccounts/bulletins/ukenvironmentalaccounts/2015-07-09/data";
    private static final String MIME_TYPE = "application/json";

    @Mock
    private HttpServletRequest request;

    @Mock
    private ContentResponse contentResponse;

    @Spy
    private DataRequestHandler handler;

    @After
    public void cleanup() {
        ThreadContext.clear();
    }

    @Test
    public void canHandleRequestShouldReturnTrueForData() {
        assertTrue(handler.canHandleRequest(URI, "data"));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForNotData() {
        assertFalse(handler.canHandleRequest(URI, "/"));
    }

    @Test
    public void getRequestTypeShouldReturnData() {
        assertEquals("data", handler.getRequestType());
    }

    @Test
    public void getDataOfExistingContent() throws Exception {
        // Given Zebedee responds with a static_adhoc
        byte[] pageData = generateData("static_adhoc", "Adhoc title");

        when(contentResponse.getAsString()).thenReturn(new String(pageData));
        doReturn(contentResponse).when(handler).getContent(anyString(), anyMap());

        // When the data is requested from Babbage
        BabbageResponse babbageResponse = handler.get(URI, request);

        // Then a json response is served
        assertEquals(200, babbageResponse.getStatus());
        assertEquals(MIME_TYPE, babbageResponse.getMimeType());
        assertTrue(babbageResponse.getErrors().isEmpty());
        assertEquals("UTF-8", babbageResponse.getCharEncoding());
    }

    @Test
    public void getDataOfNonExistingContent() throws Exception {
        // Given Zebedee responds with a 404
        doThrow(new ContentReadException(404, "no content found")).when(handler).getContent(anyString(), anyMap());
        // Then the handler should throw an exception
        assertThrows(ContentReadException.class, () -> handler.get(URI, request));
    }

    @Test
    public void getDeprecatedDataEndpointPassed() throws Exception {
        // Given Babbage is configured with a deprecation config for the bulletin page
        // type that has passed sunset
        String testSunsetDate = "2000-01-01T05:00";
        String testDeprecationDate = "2000-01-01T05:00";
        String testLink = "link";
        String testMatchPattern = ".*/data$";
        String testPageType = "bulletin";
        String testMessage = "no content here";

        DeprecationItem deprecationItem = new DeprecationItem(testDeprecationDate, testSunsetDate, testLink,
                testMatchPattern, testPageType, testMessage);
        Map<String, DeprecationItem> config = new HashMap<>();
        config.put(testPageType, deprecationItem);

        // Creating new spy here in order to fake the config
        DataRequestHandler testHandler = Mockito.spy(new DataRequestHandler(config));

        // And Zebedee responds with a bulletin page type
        byte[] pageData = generateData(testPageType, "Bulletin title");

        when(contentResponse.getPageType()).thenReturn(testPageType);
        when(contentResponse.getAsString()).thenReturn(new String(pageData));
        doReturn(contentResponse).when(testHandler).getContent(anyString(), anyMap());

        // When the data is requested from Babbage
        BabbageStringResponse babbageResponse = (BabbageStringResponse) testHandler.get(URI, request);

        // Then a 404 json response is served
        assertEquals(HttpStatus.SC_NOT_FOUND, babbageResponse.getStatus());
        assertEquals(MIME_TYPE, babbageResponse.getMimeType());
        assertTrue(babbageResponse.getErrors().isEmpty());
        assertEquals("UTF-8", babbageResponse.getCharEncoding());

        // And it has sunset headers set
        assertEquals("<link>; rel=\"sunset\"", babbageResponse.getHeader(HttpHeaders.LINK));
        assertEquals("@946702800", babbageResponse.getHeader(HttpHeaders.DEPRECATION));
        assertEquals("2000-01-01T05:00", babbageResponse.getHeader(HttpHeaders.SUNSET));

        // And the message is served
        assertEquals(testMessage, babbageResponse.getData());
    }

    @Test
    public void getDeprecatedDataEndpointFuture() throws Exception {
        // Given Babbage is configured with a deprecation config for the bulletin page
        // type that has not passed sunset
        String testSunsetDate = "2100-01-01T05:00";
        String testDeprecationDate = "2100-01-01T05:00";
        String testLink = "link";
        String testMatchPattern = ".*/data$";
        String testPageType = "bulletin";
        String testMessage = "no content here";

        DeprecationItem deprecationItem = new DeprecationItem(testDeprecationDate, testSunsetDate, testLink,
                testMatchPattern, testPageType, testMessage);
        Map<String, DeprecationItem> config = new HashMap<>();
        config.put(testPageType, deprecationItem);

        // Creating new spy here in order to fake the config
        DataRequestHandler testHandler = Mockito.spy(new DataRequestHandler(config));

        // And Zebedee responds with a bulletin page type
        byte[] pageData = generateData(testPageType, "Bulletin title");

        when(contentResponse.getPageType()).thenReturn(testPageType);
        when(contentResponse.getAsString()).thenReturn(new String(pageData));
        doReturn(contentResponse).when(testHandler).getContent(anyString(), anyMap());

        // When the data is requested from Babbage
        BabbageStringResponse babbageResponse = (BabbageStringResponse) testHandler.get(URI, request);

        // Then a data response is served
        assertEquals(HttpStatus.SC_OK, babbageResponse.getStatus());
        assertEquals(MIME_TYPE, babbageResponse.getMimeType());
        assertTrue(babbageResponse.getErrors().isEmpty());
        assertEquals("UTF-8", babbageResponse.getCharEncoding());

        // And it has sunset headers set
        assertEquals("<link>; rel=\"sunset\"", babbageResponse.getHeader(HttpHeaders.LINK));
        assertEquals("@4102462800", babbageResponse.getHeader(HttpHeaders.DEPRECATION));
        assertEquals("2100-01-01T05:00", babbageResponse.getHeader(HttpHeaders.SUNSET));

        // And the data is served
        assertEquals(new String(pageData, StandardCharsets.UTF_8), babbageResponse.getData());
    }

    private byte[] generateData(String contentType, String title) {
        return generateObject(contentType, title).toString().getBytes();
    }

    private JsonObject generateObject(String contentType, String title) {
        JsonObject json = new JsonObject();

        JsonObject description = new JsonObject();

        if (!title.isEmpty()) {
            description.addProperty("title", title);
        }

        if (!contentType.isEmpty()) {
            json.addProperty("type", contentType);
        }

        json.add("description", description);

        return json;
    }

}
