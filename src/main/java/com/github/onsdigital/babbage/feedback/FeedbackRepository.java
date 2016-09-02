package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;

import static com.github.onsdigital.babbage.configuration.FeedbackConfiguration.getFeedbackFolder;

/**
 * Created by dave on 8/22/16.
 */
public abstract class FeedbackRepository {

    public static FeedbackRepository getInstance() {
        return new FileSystemFeedbackRepository(getFeedbackFolder(), new SlackFeedbackNotifier());
    }

    public abstract void save(FeedbackForm feedbackForm) throws Exception;
}
