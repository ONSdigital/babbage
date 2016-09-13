package com.github.onsdigital.babbage.feedback;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;
import com.github.onsdigital.babbage.feedback.slack.Attachment;
import com.github.onsdigital.babbage.feedback.slack.SlackMessage;
import com.github.onsdigital.babbage.feedback.slack.Throttle;
import com.github.onsdigital.babbage.feedback.slack.ThrottleTask;
import com.github.onsdigital.babbage.util.http.HttpClient;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm.EMAIL_FIELD;
import static com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm.FEEDBACK_FIELD;
import static com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm.FOUND_FIELD;
import static com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm.UNDERSTOOD_FIELD;
import static com.github.onsdigital.babbage.configuration.SlackNotifierConfiguration.getMaxNotificationTokens;
import static com.github.onsdigital.babbage.configuration.SlackNotifierConfiguration.getMsUntilNewNotificationToken;
import static com.github.onsdigital.babbage.configuration.SlackNotifierConfiguration.getSlackEndpoint;
import static com.github.onsdigital.babbage.configuration.SlackNotifierConfiguration.isSlackThrottleEnabled;
import static java.text.MessageFormat.format;

/**
 * Created by dave on 8/23/16.
 */
public class SlackFeedbackNotifier {

    private static final String SLACK_ERROR_MSG = "[SlackFeedbackNotifier]Unexpected error sending slack notification. Response status: {0}, Response body: {1}";
    private static final String RED = "#ff0000";
    private static final String GREEN = "#36a64f";
    private static final String SLACK_MSG_TEMPLATE = "User feedback Received";
    private static final String TIMEOUT_ENABLED_TITLE = "*Notifications timeout enabled!*";
    private static final String TEXT_FIELD = "text";
    private static final String CODE_TAG = "`";
    private static final String THROTTLE_ENABLED_MSG = "Currently receiving a large amount of feedback. A notification timeout" +
            " of *{0} minutes* has been enabled. User feedback will still be saved but notifications will not be sent to" +
            " this channel until timeout period has ended.";

    private static final Path ROOT = Paths.get("/");
    private static Endpoint SLACK_ENDPOINT = getSlackEndpoint();

    private Throttle throttle;
    private HttpClient httpClient = HttpClient.getInstance();
    private SlackMessage throttledMsg;

    /**
     * Construct a new {@link SlackFeedbackNotifier}.
     */
    public SlackFeedbackNotifier() {
        if (isSlackThrottleEnabled()) {
            long timeoutPeriod = (getMaxNotificationTokens() * getMsUntilNewNotificationToken()) / 60000;
            this.throttle = new Throttle(getMaxNotificationTokens(), getMsUntilNewNotificationToken());
            this.throttledMsg = new SlackMessage()
                    .setText(TIMEOUT_ENABLED_TITLE)
                    .addAttachment(new Attachment()
                            .setText(format(THROTTLE_ENABLED_MSG, timeoutPeriod))
                            .setColor(RED)
                            .addMarkDown(TEXT_FIELD));
        }
    }

    /**
     * Will send a Feedback slack notification if the {@link Throttle} allows it. If there are
     * multiple notifications send in a short period of time a single notification will be sent to advised we are
     * receiving a lot of traffic.
     *
     * @param location the location the feedback was saved to.
     * @param form     the feedback submitted.
     * @throws IOException
     */
    public void sendNotification(Path location, FeedbackForm form) {
        if (location == null || form == null) return;

        if (isSlackThrottleEnabled()) {
            sendThrottledNotification(location, form);
        } else {
            sendUnthrottledNotification(location, form);
        }
    }

    private void sendUnthrottledNotification(Path location, FeedbackForm form) {
        Response<String> response = httpClient.postJson(SLACK_ENDPOINT, buildSlackMessage(location, form), String.class);
        verifySlackResponse(response);
    }

    private void sendThrottledNotification(Path location, FeedbackForm form) {
        throttle.executeTask(new ThrottleTask() {
            @Override
            public void tokensAvailableTask() {
                Response<String> response = httpClient.postJson(SLACK_ENDPOINT, buildSlackMessage(location, form), String.class);
                verifySlackResponse(response);
            }

            @Override
            public void triggerTimeoutTask() {
                Response<String> response = httpClient.postJson(SLACK_ENDPOINT, throttledMsg, String.class);
                verifySlackResponse(response);
            }
        });
    }

    private SlackMessage buildSlackMessage(Path path, FeedbackForm form) {
        return new SlackMessage()
                .setText(SLACK_MSG_TEMPLATE)
                .addAttachment(new Attachment()
                        .setText(location(path))
                        .setFallback(location(path))
                        .setColor(GREEN)
                        .addField(FOUND_FIELD, form.getFound(), true)
                        .addField(UNDERSTOOD_FIELD, form.getUnderstood(), true)
                        .addField(EMAIL_FIELD, form.getEmailAddress(), true)
                        .addField(FEEDBACK_FIELD, comments(form), false)
                        .addMarkDown(TEXT_FIELD));
    }

    private String location(Path path) {
        Path folder = path.getParent().getFileName();
        Path relativePath = ROOT.resolve(folder.resolve(path.getFileName()));
        return CODE_TAG + "..." + relativePath.toString() + CODE_TAG;
    }

    private String comments(FeedbackForm form) {
        if (StringUtils.isNotEmpty(form.getFeedback())) {
            if (form.getFeedback().length() > 200) {
                return form.getFeedback().substring(0, 200) + "...";
            }
        }
        return form.getFeedback();
    }

    private void verifySlackResponse(Response<String> response) {
        if (response == null || response.statusLine == null) {
            System.out.println(format(SLACK_ERROR_MSG, null, null));
        }
        if (response.statusLine.getStatusCode() != 200) {
            System.out.println(format(SLACK_ERROR_MSG, response.statusLine.getStatusCode(), response.body.toString()));
        }
    }
}
