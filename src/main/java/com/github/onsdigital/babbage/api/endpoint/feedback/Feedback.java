package com.github.onsdigital.babbage.api.endpoint.feedback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;
import com.github.onsdigital.babbage.api.endpoint.form.FormError;
import com.github.onsdigital.babbage.api.endpoint.form.PostFormResponse;
import com.github.onsdigital.babbage.feedback.FeedbackRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.util.function.Predicate;

@Api
public class Feedback {

    private Predicate<FeedbackForm> formNotNull = (form) -> form != null;
    private Predicate<FeedbackForm> commentsNotEmpty = (form) -> !StringUtils.isEmpty(form.getComments());

    @POST
    public PostFormResponse submitFeedback(HttpServletRequest request, HttpServletResponse response, FeedbackForm feedbackForm) throws Exception {
        if (!formNotNull.and(commentsNotEmpty).test(feedbackForm)) {
            return new PostFormResponse(feedbackForm, HttpStatus.SC_BAD_REQUEST,
                    new FormError("error", "Comments missing"));
        }
        FeedbackRepository.getInstance().save(feedbackForm);
        return new PostFormResponse(feedbackForm, HttpStatus.SC_OK);
    }

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(new FeedbackForm("TEST USER FEEDBACK", true, false).toJSON());
    }
}
