package com.github.onsdigital.babbage.feedback.slack;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class provides a token bucket to throttle how many slack notifications are sent over a set period.
 * Notifications can only be sent if a token is available. A timeout period is enabled the first time a token is
 * requested but none are available. This lasts until the bucket is full again.
 * <p><i>Example</i> Max tokens is <i>10</i> and a new token is added every 1 minute. If all tokens have been consumed
 * and a request for a token is made before a new one can be added the timeout state is enabled lasting 10 minutes.
 * </p> see https://en.wikipedia.org/wiki/Token_bucket
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
        Throttle selfRef = this;
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {

            @Override
            public void run() {
                task();
            }

            private void task() {
                if (notificationTokens.get() < maxTokens) {
                    notificationTokens.incrementAndGet();
                    debug("TokenAdded", selfRef);
                }
                if (timeoutEnabled.get() && notificationTokens.get() == maxTokens) {
                    timeoutEnabled.set(false);
                    debug("DisabledTimeout", selfRef);
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
                debug("TokenConsumed", this);
                task.tokensAvailableTask();
            } else {
                timeoutEnabled.set(true);
                debug("EnabledTimeout", this);
                task.triggerTimeoutTask();
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("state=[")
                .append("AvailableTokens=").append(tokensInBucket()).append(",  ")
                .append("TimeoutEnabled=").append(timeoutEnabled())
                .append("]")
                .toString();
    }

    public int tokensInBucket() {
        return notificationTokens.get();
    }

    public boolean timeoutEnabled() {
        return timeoutEnabled.get();
    }

    private void debug(String action, Throttle throttle) {
        System.out.println("SlackNotificationThrottle: action=" + action + ", " + throttle.toString());
    }

    public void kill() {
        this.timer.cancel();
        this.timer.purge();
    }

}
