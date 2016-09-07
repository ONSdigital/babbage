package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;

import static com.github.onsdigital.babbage.configuration.FeedbackConfiguration.getFeedbackFolder;

/**
 * Created by dave on 8/22/16.
 */
public abstract class FeedbackRepository {

    private static class RepositoryHolder {
        static final FileSystemFeedbackRepository INSTANCE
                = new FileSystemFeedbackRepository(getFeedbackFolder(), new SlackFeedbackNotifier());
    }

    public static synchronized FeedbackRepository getInstance() {
        return RepositoryHolder.INSTANCE;
    }

    public abstract void save(FeedbackForm feedbackForm) throws Exception;
}
