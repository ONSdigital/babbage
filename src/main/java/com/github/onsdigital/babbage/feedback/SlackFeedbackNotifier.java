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

import static com.github.onsdigital.babbage.configuration.SlackNotifierConfiguration.getMaxNotificationTokens;
import static com.github.onsdigital.babbage.configuration.SlackNotifierConfiguration.getMsUntilNewNotificationToken;
import static com.github.onsdigital.babbage.configuration.SlackNotifierConfiguration.getSlackEndpoint;

/**
 * Created by dave on 8/23/16.
 */
public class SlackFeedbackNotifier {

    private static final Path ROOT = Paths.get("/");
    private static final String SLACK_MSG_TEMPLATE = "User feedback Received";
    private static Endpoint SLACK_ENDPOINT = getSlackEndpoint();
    private static final String CODE_TAG = "`";
    private Throttle throttle;

    private HttpClient httpClient = HttpClient.getInstance();
    private SlackMessage throttledMsg;

    public SlackFeedbackNotifier() {
        this.throttle = new Throttle(getMaxNotificationTokens(), getMsUntilNewNotificationToken());
        this.throttledMsg = new SlackMessage()
                .setText("Currently receiving a large amount of feedback. Notifications will be throttled.");
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
        throttle.executeTask(new ThrottleTask() {
            @Override
            public void tokensAvailableTask() {
                Response<String> response = httpClient.postJson(SLACK_ENDPOINT, buildSlackMessage(location, form), String.class);
                if (response == null || response.statusLine == null || response.statusLine.getStatusCode() != 200) {
                    // TODO what to do here.
                    throw new RuntimeException("WHoops");
                }
            }

            @Override
            public void triggerTimeoutTask() {
                Response<String> response = httpClient.postJson(SLACK_ENDPOINT, throttledMsg, String.class);
                if (response == null || response.statusLine == null || response.statusLine.getStatusCode() != 200) {
                    // TODO what to do here.
                    throw new RuntimeException("WHoops");
                }
            }
        });
    }

    private SlackMessage buildSlackMessage(Path path, FeedbackForm form) {
        return new SlackMessage()
                .setText(SLACK_MSG_TEMPLATE)
                .addAttachment(
                        new Attachment()
                                .setText(location(path))
                                .setFallback(location(path))
                                .setColor("#36a64f")
                                .addField("Option One", form.getQuestionOne(), true)
                                .addField("Option Two", form.getQuestionTwo(), true)
                                .addField("Email", form.getEmailAddress(), true)
                                .addField("Comments", comments(form), false)
                                .addMarkDown("text"));
    }

    private String location(Path path) {
        Path folder = path.getParent().getFileName();
        Path relativePath = ROOT.resolve(folder.resolve(path.getFileName()));
        return CODE_TAG + "..." + relativePath.toString() + CODE_TAG;
    }

    private String strBool(Boolean bool) {
        return String.valueOf(bool);
    }

    private String comments(FeedbackForm form) {
        if (StringUtils.isNotEmpty(form.getComments())) {
            if (form.getComments().length() > 200) {
                return form.getComments().substring(0, 200) + "...";
            }
        }
        return form.getComments();
    }
}
