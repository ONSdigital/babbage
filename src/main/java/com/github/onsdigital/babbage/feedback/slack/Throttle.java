package com.github.onsdigital.babbage.feedback.slack;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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

    Consumer<String> debug;

    /**
     * Construct a new Throttle.
     *
     * @param maxTokens              the max number of feedback notification tokens to hold in the token bucket.
     * @param timeUntilNewTokenAdded the number of milliseconds to wait before adding a new token to the bucket.
     */
    public Throttle(final int maxTokens, final long timeUntilNewTokenAdded, Consumer<String> debug) {
        this.maxTokens = maxTokens;
        this.timeUntilNewTokenAdded = timeUntilNewTokenAdded;
        this.notificationTokens = new AtomicInteger(maxTokens);
        this.debug = debug;
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {

            @Override
            public void run() {
                task();
            }

            private void task() {
                if (notificationTokens.get() < maxTokens) {
                    addToken();
                }
                if (timeoutEnabled.get() && notificationTokens.get() == maxTokens) {
                    disableTimeout();
                }
            }

        }, 0, this.timeUntilNewTokenAdded);
    }

    public void executeTask(ThrottleTask task) {
        if (timeoutEnabled.get()) {
            debug("1");
            return;
        } else if (notificationTokens.get() == 0) {
            enableTimeout();
            task.triggerTimeoutTask();
            debug("2");
        } else {
            takeToken();
            task.tokensAvailableTask();
            debug("3");
        }
    }

    public String debug() {
        return "Thread: " + Thread.currentThread().getName() + " Tokens; " + notificationTokens.get() + " timeout enabled: " + timeoutEnabled.get();
    }

    public String debug(String name) {
        return name + "Thread: " + Thread.currentThread().getName() + " Tokens; " + notificationTokens.get() + " timeout enabled: " + timeoutEnabled.get();
    }

    private synchronized void addToken() {
        notificationTokens.incrementAndGet();
    }

    private synchronized void takeToken() {
        notificationTokens.decrementAndGet();
    }

    private synchronized void enableTimeout() {
        timeoutEnabled.set(true);
    }

    private synchronized void disableTimeout() {
        timeoutEnabled.set(false);
    }

    public int availableTokens() {
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
