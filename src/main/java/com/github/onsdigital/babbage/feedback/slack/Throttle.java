package com.github.onsdigital.babbage.feedback.slack;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class provides a token bucket to throttle how many slack notifications are sent over a set period.
 * Notifications can only be sent if a token is available. A timeout period is enabled the first time a token is
 * requested but none are available. The lasts until the bucket is full again.
 * <p><i>Example</i> Max tokens is <i>10</i> and a new token is added every 1 minute. If all tokens are consumed before a
 * new ones can be added the timeout period is 10 minutes.</p> see https://en.wikipedia.org/wiki/Token_bucket
 */
public class Throttle {

    private long timeUntilNewTokenAdded;
    private int maxTokens;
    private AtomicInteger notificationTokens;
    private AtomicBoolean timeoutEnabled = new AtomicBoolean(false);
    private Timer timer;

    /**
     * Construct a new Throttle.
     *
     * @param maxTokens              the max number of feedback notification tokens to hold in the token bucket.
     * @param timeUntilNewTokenAdded the number of milliseconds to wait before adding a new token to the bucket.
     */
    public Throttle(final int maxTokens, final long timeUntilNewTokenAdded) {
        this.maxTokens = maxTokens;
        this.timeUntilNewTokenAdded = timeUntilNewTokenAdded;
        this.notificationTokens = new AtomicInteger(maxTokens);
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {

            @Override
            public void run() {
                task();
            }

            private void task() {
                if (notificationTokens.get() < maxTokens) {
                    notificationTokens.incrementAndGet();
                }
                if (timeoutEnabled.get() && notificationTokens.get() == maxTokens) {
                    timeoutEnabled.set(false);
                }
            }

        }, this.timeUntilNewTokenAdded, this.timeUntilNewTokenAdded);
    }

    public void executeTask(ThrottleTask task) {
        if (timeoutEnabled.get()) {
            return;
        } else {
            if (notificationTokens.get() > 0) {
                notificationTokens.decrementAndGet();
                task.tokensAvailableTask();
            } else {
                timeoutEnabled.set(true);
                task.triggerTimeoutTask();

            }
        }
    }

    public synchronized int availableTokens() {
        return notificationTokens.get();
    }

    public boolean timeoutEnabled() {
        return timeoutEnabled.get();
    }

    public void kill() {
        this.timer.cancel();
        this.timer.purge();
    }

}
