package com.github.onsdigital.babbage.request.handler.feedback;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.template.TemplateService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * return the user feedback form.
 */
public class GetFeedbackFormHandler extends BaseRequestHandler {

    private static TemplateService templateService = TemplateService.getInstance();
    private static ContentClient contentClient = ContentClient.getInstance();

    private static final String MIME = "text/html";
    private static final String FEEDBACK_PARAM = "feedback";
    private static final String SHOW_FORM = "show_form";

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        Map<String, Object> additionalArgs = new HashMap<>();
        additionalArgs.put(SHOW_FORM, true);

        ContentResponse contentResponse = contentClient.getContent("/" + FEEDBACK_PARAM);
        String html = templateService.renderContent(contentResponse.getDataStream(), additionalArgs);
        return new BabbageContentBasedStringResponse(contentResponse, html, MIME);
    }

    @Override
    public String getRequestType() {
        return FEEDBACK_PARAM;
    }
}
