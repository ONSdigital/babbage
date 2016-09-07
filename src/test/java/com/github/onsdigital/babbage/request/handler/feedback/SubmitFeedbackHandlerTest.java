package com.github.onsdigital.babbage.request.handler.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.feedback.FeedbackRepository;
import com.github.onsdigital.babbage.request.handler.base.RequestHandler;
import com.github.onsdigital.babbage.response.BabbageStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.EMAIL_PARAM;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.FEEDBACK_PARAM;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.FOUND_PARAM;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.JSON_PARAM;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.JSON_RESPONSE;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.RESPONSE_FORMAT_PARAM;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.UNDERSTOOD_PARAM;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.URI_PARAM;
import static com.github.onsdigital.babbage.request.handler.feedback.SubmitFeedbackHandler.feedbackRepository;
import static com.github.onsdigital.babbage.util.TestsUtil.setPrivateStaticField;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class SubmitFeedbackHandlerTest {

    @Mock
    private HttpServletRequest requestMock;

    @Mock
    private FeedbackRepository repositoryMock;

    @Mock
    private TemplateService templateServiceMock;

    @Mock
    private ContentClient contentClientMock;

    @Mock
    private ContentResponse contentResponseMock;

    private RequestHandler handler;
    private FeedbackForm form;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        handler = new SubmitFeedbackHandler();
        form = new FeedbackForm()
                .emailAddress(EMAIL_PARAM)
                .feedback(FEEDBACK_PARAM)
                .understood(UNDERSTOOD_PARAM)
                .found(FOUND_PARAM)
                .uri(URI_PARAM);

        setPrivateStaticField(handler, "feedbackRepository", repositoryMock);
    }

    @Test
    public void shouldSaveValidJsonFeedback() throws Exception {
        requestParam(
                kvp(RESPONSE_FORMAT_PARAM, JSON_PARAM),
                kvp(EMAIL_PARAM, EMAIL_PARAM),
                kvp(FEEDBACK_PARAM, FEEDBACK_PARAM),
                kvp(FOUND_PARAM, FOUND_PARAM),
                kvp(UNDERSTOOD_PARAM, UNDERSTOOD_PARAM),
                kvp(URI_PARAM, URI_PARAM));

        BabbageResponse actual = handler.get(null, requestMock);

        assertThat("Incorrect Response type", actual instanceof BabbageResponse, is(true));
        BabbageStringResponse response = (BabbageStringResponse) actual;
        assertThat("Incorrect Response data ", response.getData(), equalTo(JSON_RESPONSE));

        verify(repositoryMock, times(1)).save(form);
        verifyZeroInteractions(templateServiceMock, contentClientMock, contentResponseMock);
        verify(requestMock, times(1)).getParameter(RESPONSE_FORMAT_PARAM);
        verify(requestMock, times(1)).getParameter(EMAIL_PARAM);
        verify(requestMock, times(1)).getParameter(FEEDBACK_PARAM);
        verify(requestMock, times(1)).getParameter(FOUND_PARAM);
        verify(requestMock, times(1)).getParameter(UNDERSTOOD_PARAM);
        verify(requestMock, times(1)).getParameter(URI_PARAM);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    public void shouldNotSaveInvalidFeedback() throws Exception {
        requestParam(kvp(RESPONSE_FORMAT_PARAM, JSON_PARAM));

        BabbageResponse actual = handler.get(null, requestMock);

        assertThat("Incorrect Response type", actual instanceof BabbageResponse, is(true));
        BabbageStringResponse response = (BabbageStringResponse) actual;
        assertThat("Incorrect Response data ", response.getData(), equalTo(JSON_RESPONSE));

        verifyZeroInteractions(templateServiceMock, contentClientMock, contentResponseMock, feedbackRepository);
        verify(requestMock, times(1)).getParameter(RESPONSE_FORMAT_PARAM);
        verify(requestMock, times(1)).getParameter(EMAIL_PARAM);
        verify(requestMock, times(1)).getParameter(FEEDBACK_PARAM);
        verify(requestMock, times(1)).getParameter(FOUND_PARAM);
        verify(requestMock, times(1)).getParameter(UNDERSTOOD_PARAM);
        verify(requestMock, times(1)).getParameter(URI_PARAM);
    }

    private void requestParam(KVP... kvps) {
        for (KVP kvp : kvps) {
            when(requestMock.getParameter(kvp.key)).thenReturn(kvp.value);
        }
    }

    private KVP kvp(String key, String value) {
        return new KVP(key, value);
    }

    private static class KVP {
        String key;
        String value;

        KVP(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
