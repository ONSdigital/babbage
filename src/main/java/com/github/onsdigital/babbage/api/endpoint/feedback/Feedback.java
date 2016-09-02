package com.github.onsdigital.babbage.api.endpoint.feedback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;
import com.github.onsdigital.babbage.feedback.FeedbackRepository;
import com.github.onsdigital.babbage.util.http.HttpClient;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Api
public class Feedback {

    private Predicate<FeedbackForm> formNotNull = (form) -> form != null;
    private Predicate<FeedbackForm> commentsNotEmpty = (form) -> !isEmpty(form.getComments());

/*    @POST
    public PostFormResponse submitFeedback(HttpServletRequest request, HttpServletResponse response, FeedbackForm feedbackForm) throws Exception {
        if (!formNotNull.and(commentsNotEmpty).test(feedbackForm)) {
            return new PostFormResponse(feedbackForm, HttpStatus.SC_BAD_REQUEST,
                    new FormError("error", "Comments missing"));
        }
        FeedbackRepository.getInstance().save(feedbackForm);
        return new PostFormResponse(feedbackForm, HttpStatus.SC_OK);
    }*/


    // {"uri":null,"emailAddress":"ertyijo","comments":"qwuil","questionOne":"yes","questionTwo":"no"}


    @POST
    public void submitFeedback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String email = request.getParameter("emailAddr");
        String found = request.getParameter("found");
        String understood = request.getParameter("understood");
        String comments = request.getParameter("comments");
        String uri = request.getParameter("uri");

        String redirect = isEmpty(uri) ? "/" : uri;

        if (isEmpty(email) && isEmpty(found) && isEmpty(understood) && isEmpty(comments)) {
            response.sendRedirect(redirect);
        }

        FeedbackForm feedback = new FeedbackForm()
                .emailAddress(email)
                .comments(comments)
                .questionOne(found)
                .questionTwo(understood);
        FeedbackRepository.getInstance().save(feedback);
        System.out.println(feedback.toJSON());

        response.sendRedirect(redirect);
    }

    public static void main(String[] args) throws Exception {
        Endpoint e = new Endpoint(new Host("http://localhost:8080"), "feedback");

        NameValuePair email = new BasicNameValuePair("emailAddr", "EMAIL_ADDRESS");
        NameValuePair comments = new BasicNameValuePair("comments", "COMMENTS");
        NameValuePair found = new BasicNameValuePair("found", "FOUND");
        NameValuePair understood = new BasicNameValuePair("understood", "UNDERSTOOD");
        NameValuePair uri = new BasicNameValuePair("uri", "URI");

        Runnable job = () -> HttpClient.getInstance().post(e, String.class, email, comments, found, understood, uri);
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();


        job.run();

        for (int i=0; i <= 25; i++) {
            System.out.println(i);
            job.run();
            //ex.schedule(job, 1, TimeUnit.SECONDS);
        }
        System.out.println("Waiting....");
        //ex.schedule(job, 1, TimeUnit.MINUTES).get();
        System.out.println("Done.");
    }
}
