package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.feedback.slack.Throttle;
import com.github.onsdigital.babbage.feedback.slack.ThrottleTask;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class ThrottleTest {

    static final int MAX_TOKENS = 5;
    static final int NEW_TOKEN_DELAY = 5000;

    private Throttle throttle;

    @Mock
    private ThrottleTask mockTask;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        throttle.kill();
        throttle = null;
    }


    @Test
    public void shouldExecuteTokensAvailableTask() {
        throttle = new Throttle(MAX_TOKENS, NEW_TOKEN_DELAY);
        throttle.executeTask(mockTask);
        verify(mockTask, times(1)).tokensAvailableTask();
    }

    @Test
    public void shouldExecuteTokensAvailableTaskXTimes() {
        throttle = new Throttle(MAX_TOKENS, NEW_TOKEN_DELAY);
        for (int i = 0; i <= MAX_TOKENS; i++) {
            throttle.executeTask(mockTask);
        }
        verify(mockTask, times(MAX_TOKENS)).tokensAvailableTask();
        verify(mockTask, times(1)).triggerTimeoutTask();
    }

    @Test
    public void shouldExecuteTriggerTimeoutAfterConsumingAllTokens() {
        throttle = new Throttle(MAX_TOKENS, NEW_TOKEN_DELAY);
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);

        throttle.executeTask(mockTask);

        verify(mockTask, times(MAX_TOKENS)).tokensAvailableTask();
        verify(mockTask, times(1)).triggerTimeoutTask();
        assertThat("Expected Timeout to be in effect", throttle.timeoutEnabled(), is(true));
    }

    /**
     * Test requests MAX tokens + 1 and verifies that all available tokens have been consumed and the timeout is enabled.
     * Test then checks throttle state every 1 seconds and verifies that the timeout remains enabled for the expected duration.
     *
     * @throws Exception
     */
    @Test
    public void consumeAllTokensAndVerifyTimeoutEnabledUntilBucketFullAgain() throws Exception {
        throttle = new Throttle(MAX_TOKENS, NEW_TOKEN_DELAY);

        for (int i = 0; i <= (MAX_TOKENS + 1); i++) {
            throttle.executeTask(mockTask);
        }

        assertThat("Incorrect number of tokens.", throttle.tokensInBucket(), is(0));
        assertThat("Timeout should be enabled.", throttle.timeoutEnabled(), is(true));

        // Timeout DURATION (MS) = (MAX TOKENS * NEW TOKEN DELAY)
        //
        // A new token is added after the configured delay (5 seconds)
        // Once the timeout is enabled its will stay enabled until the token bucket is full again.
        DateTime end = DateTime.now().plusMillis(MAX_TOKENS * NEW_TOKEN_DELAY);

        int count = 1;
        System.out.println("(Test will run for 25 seconds).");
        while (DateTime.now().isBefore(end)) {
            assertThat("Timeout should be enabled 1", throttle.timeoutEnabled(), is(true));
            if (count % 5 == 0) {
                System.out.print(count + " seconds ");
            }
            count++;
            Thread.sleep(1000);
        }

        assertThat("Timeout should be enabled 2.", throttle.timeoutEnabled(), is(false));
        assertThat("Timeout should be enabled 3.", throttle.tokensInBucket(), is(MAX_TOKENS));
    }

    @Test
    public void shouldAddNewTokenAfterConfiguredTime() throws Exception {
        throttle = new Throttle(MAX_TOKENS, NEW_TOKEN_DELAY);

        for (int i = 0; i <= (MAX_TOKENS + 1); i++) {
            throttle.executeTask(mockTask);
            System.out.println(debug());
        }

        assertThat("Timeout should be enabled.", throttle.timeoutEnabled(), is(true));
        assertThat("Expected no tokens to be available.", throttle.tokensInBucket(), is(0));

        long delay = NEW_TOKEN_DELAY + 10;

        for (int expected = 1; expected < MAX_TOKENS; expected++) {
            Thread.sleep(delay);
            int seconds = (expected * NEW_TOKEN_DELAY) / 1000;

            System.out.println("After " + seconds + " seconds => " + debug()
                    + " >>> ExpectedState=[AvailableTokens=" + expected + ", TimeoutEnabled=" + false + "]");

            assertThat("Timeout state was not as expected.", throttle.timeoutEnabled(), is(true));
            assertThat("Number of expected tokens is incorrect.", throttle.tokensInBucket(), is(expected));
        }
        Thread.sleep(delay); // bit of a hack - add 10 ms to make sure it happens after the new token delay.
        assertThat("Timeout state was not as expected.", throttle.timeoutEnabled(), is(false));
        assertThat("Number of expected tokens is incorrect.", throttle.tokensInBucket(), is(MAX_TOKENS));
    }

    private String debug() {
        return throttle.toString();
    }
}