package com.github.onsdigital.babbage.request.handler.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.feedback.FeedbackRepository;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.BabbageStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by dave on 9/7/16.
 */
public class SubmitFeedbackHandler extends BaseRequestHandler {

    static FeedbackRepository feedbackRepository = null;
    static TemplateService templateService = TemplateService.getInstance();
    static ContentClient contentClient = ContentClient.getInstance();

    static final String MIME = "text/html";
    static final String EMAIL_PARAM = "email";
    static final String FEEDBACK_PARAM = "feedback";
    static final String FOUND_PARAM = "found";
    static final String UNDERSTOOD_PARAM = "understood";
    static final String URI_PARAM = "uri";
    static final String SITE_ROOT = "/";
    static final String FEEDBACK_URL = "/feedback";
    static final String JSON_PARAM = "json";
    static final String RESPONSE_FORMAT_PARAM = "response-format";
    static String JSON_RESPONSE = "{\"result\": \"success\"}";
    static final String REQ_TYPE = "submitfeedback";
    static final String SHOW_FORM_PARAM = "show_form";
    static final String RETURN_URL_PARAM = "returnURL";

    static final List<String> URL_BLACKLIST = new ImmutableList
            .Builder<String>()
            .add("/" + FEEDBACK_PARAM)
            .add("/" + REQ_TYPE)
            .build();

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        if (JSON_PARAM.equalsIgnoreCase(request.getParameter(RESPONSE_FORMAT_PARAM))) {
            return processJSON(request);
        }
        return processForm(request);
    }

    private BabbageResponse processJSON(HttpServletRequest request) throws Exception {
        FeedbackForm form = getForm(request);
        saveFeedback(form);
        return new BabbageStringResponse(JSON_RESPONSE);
    }

    private BabbageResponse processForm(HttpServletRequest request) throws Exception {
        FeedbackForm form = getForm(request);
        Map<String, Object> additionalArgs = new HashMap<>();
        ContentResponse contentResponse = contentClient.getContent(FEEDBACK_URL);

        if (saveFeedback(form)) {
            additionalArgs.put(SHOW_FORM_PARAM, false);
        }
        additionalArgs.put(RETURN_URL_PARAM, getReturnURL(form));
        String html = templateService.renderContent(contentResponse.getDataStream(), additionalArgs);
        return new BabbageContentBasedStringResponse(contentResponse, html, MIME);
    }

    private boolean saveFeedback(FeedbackForm form) throws Exception {
        if (!isValid(form)) {
            return false;
        }
        repository().save(form);
        return true;
    }

    private String getReturnURL(FeedbackForm form) {
        if (StringUtils.isEmpty(form.getUri()) || URL_BLACKLIST.contains(form.getUri())) {
            return SITE_ROOT;
        }
        return form.getUri();
    }

    private FeedbackForm getForm(HttpServletRequest req) {
        return new FeedbackForm()
                .emailAddress(req.getParameter(EMAIL_PARAM))
                .feedback(req.getParameter(FEEDBACK_PARAM))
                .found(req.getParameter(FOUND_PARAM))
                .understood(req.getParameter(UNDERSTOOD_PARAM))
                .uri(defaultIfBlank(req.getParameter(URI_PARAM), SITE_ROOT));
    }

    private boolean isValid(FeedbackForm form) {
        return !(isEmpty(form.getEmailAddress()) && isEmpty(form.getFound())
                && isEmpty(form.getUnderstood()) && isEmpty(form.getFeedback()));
    }

    private static FeedbackRepository repository() {
        if (feedbackRepository == null) {
            feedbackRepository = FeedbackRepository.getInstance();
        }
        return feedbackRepository;
    }

    @Override
    public String getRequestType() {
        return REQ_TYPE;
    }
}
