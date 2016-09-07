package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 *
 */
public class FileSystemFeedbackRepository extends FeedbackRepository {

    private static final String JSON_EXT = ".json";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    private static Predicate<FeedbackForm> formIsNull = (form) -> form == null;
    private static Predicate<FeedbackForm> formCommentsNull = (form) -> isEmpty(form.getFeedback());
    private static Predicate<FeedbackForm> formEmailNull = (form) -> isEmpty(form.getEmailAddress());
    private static Predicate<FeedbackForm> formQuestionOneNull = (form) -> isEmpty(form.getFound());
    private static Predicate<FeedbackForm> formQuestionTwoNull = (form) -> isEmpty(form.getUnderstood());

    private Supplier<Path> nameGenerator = () -> Paths.get(new StringBuilder()
            .append(UUID.randomUUID().toString())
            .append(JSON_EXT)
            .toString());

    private FeedbackWriter feedbackWriter = (form, path) -> {
        Files.createFile(path);
        Files.write(path, form.toJSON().getBytes());
    };

    private SlackFeedbackNotifier slackBot;
    private Path feedbackPath;

    FileSystemFeedbackRepository(Path feedbackPath, SlackFeedbackNotifier slackBot) {
        this.slackBot = slackBot;
        this.feedbackPath = feedbackPath;
    }

    @Override
    public void save(FeedbackForm feedbackForm) throws Exception {

        if (formIsNull.test(feedbackForm) || formCommentsNull
                .and(formEmailNull)
                .and(formQuestionOneNull)
                .and(formQuestionTwoNull)
                .test(feedbackForm)) {
            // Nothing to save.
            return;
        }

        Path location = getFeedbackDir().resolve(nameGenerator.get());
        feedbackWriter.write(feedbackForm, location);
        //slackBot.sendNotification(location, feedbackForm);
    }

    private Path getFeedbackDir() throws IOException {
        Path parentFolder = feedbackPath.resolve(SDF.format(new Date()));
        if (!Files.exists(parentFolder)) {
            Files.createDirectory(parentFolder);
        }
        return parentFolder;
    }
}
