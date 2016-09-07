package com.github.onsdigital.babbage.request.handler.feedback;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.request.handler.base.RequestHandler;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import com.github.onsdigital.babbage.util.TestsUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetFeedbackFormHandlerTest {

    static String TEST_HTML = "<html><div>Test<div></html>";
    static String FEEDBACK_URL = "/feedback";

    @Mock
    private HttpServletRequest requestMock;

    @Mock
    private TemplateService templateServiceMock;

    @Mock
    private ContentClient contentClientMock;

    @Mock
    private ContentResponse contentResponseMock;

    @Mock
    private InputStream inputStreamMock;
    private RequestHandler handler;

    private Map<String, Object> additionalArgs;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler = new GetFeedbackFormHandler();

        additionalArgs = new HashMap<>();
        additionalArgs.put("show_form", true);

        TestsUtil.setPrivateStaticField(handler, "templateService", templateServiceMock);
        TestsUtil.setPrivateStaticField(handler, "contentClient", contentClientMock);

    }

    @Test
    public void shouldGetForm() throws Exception {
        when(contentClientMock.getContent(FEEDBACK_URL))
                .thenReturn(contentResponseMock);
        when(contentResponseMock.getDataStream())
                .thenReturn(inputStreamMock);
        when(templateServiceMock.renderContent(inputStreamMock, additionalArgs))
                .thenReturn(TEST_HTML);

        BabbageResponse actualResponse = handler.get("", requestMock);

        assertTrue("Wrong response type.", actualResponse instanceof BabbageContentBasedStringResponse);

        verify(contentClientMock, times(1)).getContent(FEEDBACK_URL);
        verify(contentResponseMock, times(1)).getDataStream();
        verify(contentResponseMock, times(1)).getDataStream();
        verify(templateServiceMock, times(1)).renderContent(inputStreamMock, additionalArgs);
    }

    @Test
    public void shouldReturnExpectedRequestType() {
        assertThat("Inccorect request type", handler.getRequestType(), equalTo("feedback"));
    }
}
