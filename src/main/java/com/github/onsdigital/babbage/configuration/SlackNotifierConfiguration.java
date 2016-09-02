package com.github.onsdigital.babbage.configuration;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;

/**
 * Created by dave on 9/1/16.
 */
public class SlackNotifierConfiguration extends StartUpConfiguration {

    private static int MAX_NOTIFICATION_TOKENS = 0;
    private static long MS_UNTIL_NEW_NOTIFICATION_TOKEN = 0;

    private static Endpoint SLACK_ENDPOINT;
    private static Host SLACK_FEEDBACK_NOTIFICATION_HOST;
    private static String SLACK_FEEDBACK_CHANNEL_URL;

    @Override
    public void initialize() {
        MAX_NOTIFICATION_TOKENS = Integer.parseInt(getValue("max_notification_tokens"));
        MS_UNTIL_NEW_NOTIFICATION_TOKEN = Long.valueOf(getValue("ms_until_new_notification_token"));

        SLACK_FEEDBACK_CHANNEL_URL = getValue("slack_feedback_channel_url");
        SLACK_FEEDBACK_NOTIFICATION_HOST = new Host(getValue("slack_feedback_notification_host"));
        SLACK_ENDPOINT = new Endpoint(SLACK_FEEDBACK_NOTIFICATION_HOST, SLACK_FEEDBACK_CHANNEL_URL);
    }

    public static int getMaxNotificationTokens() {
        return MAX_NOTIFICATION_TOKENS;
    }

    public static long getMsUntilNewNotificationToken() {
        return MS_UNTIL_NEW_NOTIFICATION_TOKEN;
    }

    public static Endpoint getSlackEndpoint() {
        return SLACK_ENDPOINT;
    }

    public static Host getSlackFeedbackNotificationHost() {
        return SLACK_FEEDBACK_NOTIFICATION_HOST;
    }

    public static String getSlackFeedbackChannelUrl() {
        return SLACK_FEEDBACK_CHANNEL_URL;
    }
}
