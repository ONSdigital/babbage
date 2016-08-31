package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 *
 */
public class FileSystemFeedbackRepository extends FeedbackRepository {

    static final Path feedbackPath = Paths.get("/Users/dave/Desktop/feedback");
    static final String JSON_EXT = ".json";
    static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    private SlackFeedbackNotifier slackBot;

    FileSystemFeedbackRepository() {
        slackBot = new SlackFeedbackNotifier();
    }

    @Override
    public void save(FeedbackForm feedbackForm) throws Exception {
        Path dir = feedbackPath.resolve(SDF.format(new Date()));
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }
        Path filePath = generateName(dir);
        Files.createFile(filePath);
        Files.write(filePath, feedbackForm.toJSON().getBytes());
        slackBot.sendNotification(filePath, feedbackForm);
    }

    private Path generateName(Path dir) {
        return dir.resolve(
                new StringBuilder()
                        .append(UUID.randomUUID().toString())
                        .append(JSON_EXT)
                        .toString());
    }
}
