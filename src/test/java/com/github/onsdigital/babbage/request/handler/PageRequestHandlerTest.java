package com.github.onsdigital.babbage.request.handler;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import javax.servlet.http.HttpServletResponse;

import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.response.BabbageRedirectResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.google.gson.JsonObject;
import com.github.onsdigital.babbage.template.TemplateService;

@RunWith(MockitoJUnitRunner.class)
public class PageRequestHandlerTest {

    private static final String URI = "/economy/environmentalaccounts/bulletins/ukenvironmentalaccounts/2015-07-09";
    private static final String MIME_TYPE = "text/html";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ContentResponse contentResponse;

    @Mock
    private TemplateService mockTemplateService;

    @Spy
    private PageRequestHandler handler;

    @After
    public void cleanup() {
        ThreadContext.clear();
    }

    @Test
    public void canHandleRequestShouldReturnTrueForPage() {
        assertTrue(handler.canHandleRequest(URI, "/"));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForNotPage() {
        assertFalse(handler.canHandleRequest(URI, "pdf"));
    }

    @Test
    public void getRequestTypeShouldReturnPage() {
        assertEquals("/", handler.getRequestType());
    }

    @Test
    public void testShouldRedirect() {
       assertFalse(handler.shouldRedirect("/mybulletin", "bulletin", "/redirect1"));
       assertFalse(handler.shouldRedirect("/mybulletin", "bulletin", ""));
       assertFalse(handler.shouldRedirect("/mybulletin/latest", "bulletin", ""));
       assertFalse(handler.shouldRedirect("/mystatic/latest", "static_adhoc", "/redirect1"));
       assertTrue(handler.shouldRedirect("/mybulletin/latest", "bulletin", "/redirect1"));
       assertFalse(handler.shouldRedirect("/mybulletin/latest", "", ""));
       assertFalse(handler.shouldRedirect("/mybulletin/latest", "", " "));
    }

    @Test
    public void getPageOfExistingContent() throws Exception {
        // Given Zebedee responds with a static_adhoc with no migration link
        byte[] pageData = generateData("static_adhoc", "", "Adhoc title");

        when(contentResponse.getAsString()).thenReturn(new String(pageData));

        doReturn(mockTemplateService).when(handler).getTemplateService();
        doReturn(contentResponse).when(handler).getContent(URI);

        // When the page is requested from Babbage
        BabbageResponse babbageResponse = handler.get(URI, request);

        // Then a page is served
        assertEquals(200, babbageResponse.getStatus());
        assertEquals(MIME_TYPE, babbageResponse.getMimeType());
        assertTrue(babbageResponse.getErrors().isEmpty());
        assertEquals("UTF-8", babbageResponse.getCharEncoding());
    }

    @Test
    public void getPageOfEditorialContent() throws Exception {
        // Given Zebedee responds with a bulletin with no migration link
        byte[] pageData = generateData("bulletin", "", "Bulletin title");

        when(contentResponse.getAsString()).thenReturn(new String(pageData));
        doReturn(mockTemplateService).when(handler).getTemplateService();
        doReturn(contentResponse).when(handler).getContent(URI);

        // When the page is requested from Babbage
        BabbageResponse babbageResponse = handler.get(URI, request);

        // Then a page is served
        assertFalse(babbageResponse instanceof BabbageRedirectResponse);
        assertEquals(200, babbageResponse.getStatus());
        assertEquals(MIME_TYPE, babbageResponse.getMimeType());
        assertTrue(babbageResponse.getErrors().isEmpty());
        assertEquals("UTF-8", babbageResponse.getCharEncoding());
    }

    @Test
    public void getPageOfEditorialContentWithRedirectNotLatest() throws Exception {
        // Given Zebedee returns a bulletin with a migrationLink
        byte[] pageData = generateData("bulletin", "/redirect1", "Bulletin title");

        when(contentResponse.getAsString()).thenReturn(new String(pageData));
        doReturn(mockTemplateService).when(handler).getTemplateService();
        doReturn(contentResponse).when(handler).getContent(URI);

        // And the page requested is not 'latest' endpoint
        // When the page is requested from Babbage
        BabbageResponse babbageResponse = handler.get(URI, request);

        // Then a page is served
        assertFalse(babbageResponse instanceof BabbageRedirectResponse);
        assertEquals(200, babbageResponse.getStatus());
        assertEquals(MIME_TYPE, babbageResponse.getMimeType());
        assertTrue(babbageResponse.getErrors().isEmpty());
        assertEquals("UTF-8", babbageResponse.getCharEncoding());
    }

    @Test
    public void getPageOfEditorialContentWithRedirect() throws Exception {
        String redirect = "/redirect1";

        // Given Zebedee returns a bulletin with a migrationLink
        byte[] pageData = generateData("bulletin", redirect, "Bulletin title");

        // And the page requested is the 'latest' endpoint
        String uri = URI + "/latest";

        when(contentResponse.getAsString()).thenReturn(new String(pageData));
        doReturn(contentResponse).when(handler).getContent(uri);

        // When the page is requested from Babbage
        BabbageResponse babbageResponse = handler.get(uri, request);

        // Then the TemplateService is not called to render content
        verify(mockTemplateService, never()).renderContent(isA(Object.class), anyMap());

        // And a BabbageRedirectResponse is returned with the correct redirectUri
        assertTrue(babbageResponse instanceof BabbageRedirectResponse);
        assertTrue(babbageResponse.getErrors().isEmpty());
        BabbageRedirectResponse babbageRedirectResponse = (BabbageRedirectResponse) babbageResponse;
        assertEquals(redirect, babbageRedirectResponse.getRedirectUri());
    }

    @Test
    public void getPageOfNonEditorialContentWithRedirect() throws Exception {
        // Given Zebedee returns a dataset with a migrationLink
        byte[] pageData = generateData("dataset_landing_page", "/redirect1", "Dataset title");

        // And the page requested is the 'latest' endpoint
        String uri = URI + "/latest";

        when(contentResponse.getAsString()).thenReturn(new String(pageData));
        doReturn(mockTemplateService).when(handler).getTemplateService();
        doReturn(contentResponse).when(handler).getContent(uri);

        // When the page is requested from Babbage
        BabbageResponse babbageResponse = handler.get(uri, request);

        // Then a page is served
        assertFalse(babbageResponse instanceof BabbageRedirectResponse);
        assertEquals(200, babbageResponse.getStatus());
        assertEquals(MIME_TYPE, babbageResponse.getMimeType());
        assertTrue(babbageResponse.getErrors().isEmpty());
        assertEquals("UTF-8", babbageResponse.getCharEncoding());
    }

    private byte[] generateData(String contentType, String migrationLink, String title) {
        return generateObject(contentType, migrationLink, title).toString().getBytes();
    }

    private JsonObject generateObject(String contentType, String migrationLink, String title) {
        JsonObject json = new JsonObject();

        JsonObject description = new JsonObject();

        if (!migrationLink.isEmpty()) {
            description.addProperty("migrationLink", migrationLink);
        }

        if (!title.isEmpty()){
            description.addProperty("title", title);
        }

        if (!contentType.isEmpty()) {
            json.addProperty("type", contentType);
        }

        json.add("description", description);

        return json;
    }
    

}


