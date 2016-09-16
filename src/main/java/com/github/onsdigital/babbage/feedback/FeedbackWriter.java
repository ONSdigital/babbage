package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.api.endpoint.form.FeedbackForm;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by dave on 9/2/16.
 */
@FunctionalInterface
public interface FeedbackWriter {

    void write(FeedbackForm form, Path location) throws IOException;
}
