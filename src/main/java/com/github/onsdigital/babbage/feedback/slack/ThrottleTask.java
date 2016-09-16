package com.github.onsdigital.babbage.feedback.slack;

/**
 * Created by dave on 8/30/16.
 */
public interface ThrottleTask {

    void tokensAvailableTask();

    void triggerTimeoutTask();
}
