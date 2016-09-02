package com.github.onsdigital.babbage.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.text.MessageFormat.format;

/**
 * Created by dave on 9/1/16.
 */
public class FeedbackConfiguration extends StartUpConfiguration {

    public static Path FEEDBACK_FOLDER = null;
    public static String FEEDBACK_ENV_VAR = "feedback_folder";
    public static String FOLDER_EXISTS_MSG = "Feedback folder {0} already exist, no action required.";
    public static String FOLDER_NOT_EXIST_MSG = "Feedback folder {0} does not exist, will be created.";

    @Override
    public void initialize() {
        try {
            FEEDBACK_FOLDER = Paths.get(getValue(FEEDBACK_ENV_VAR));

            if (!Files.exists(FEEDBACK_FOLDER)) {
                configDebug(format(FOLDER_NOT_EXIST_MSG, FEEDBACK_FOLDER));
                Files.createDirectory(FEEDBACK_FOLDER);
            } else {
                configDebug(format(FOLDER_EXISTS_MSG, FEEDBACK_FOLDER));
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Path getFeedbackFolder() {
        return FEEDBACK_FOLDER;
    }
}
