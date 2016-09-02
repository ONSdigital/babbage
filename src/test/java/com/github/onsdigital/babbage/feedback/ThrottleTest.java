package com.github.onsdigital.babbage.feedback;

import com.github.onsdigital.babbage.feedback.slack.Throttle;
import com.github.onsdigital.babbage.feedback.slack.ThrottleTask;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by dave on 8/24/16.
 */
@Ignore
public class ThrottleTest {

    static final String DEBUG = "{0} seconds since started, {1} tokens currently available";

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    static final int MAX_TOKENS = 5;
    static final int NEW_TOKEN_DELAY = 5000;

    private Throttle throttle;

    @Mock
    private ThrottleTask mockTask;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        throttle = new Throttle(MAX_TOKENS, NEW_TOKEN_DELAY);
    }

    @After
    public void tearDown() {
        throttle.kill();
        throttle = null;
    }


    @Test
    public void shouldExecuteTokensAvailableTask() {
        throttle.executeTask(mockTask);
        verify(mockTask, times(1)).tokensAvailableTask();
    }

    @Test
    public void shouldExecuteTokensAvailableTaskXTimes() {
        for (int i = 0; i < MAX_TOKENS; i++) {
            throttle.executeTask(mockTask);
        }
        verify(mockTask, times(MAX_TOKENS)).tokensAvailableTask();
    }

    @Test
    public void shouldExecuteTriggerTimeoutAfterConsumingAllTokens() {
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);
        throttle.executeTask(mockTask);

        throttle.executeTask(mockTask);

        verify(mockTask, times(MAX_TOKENS)).tokensAvailableTask();
        verify(mockTask, times(1)).triggerTimeoutTask();
    }

    @Test
    public void shouldAddNewTokenAfterConfiguredTime() throws Exception {
        // Take 1 token and check remaining.
        throttle.executeTask(mockTask);
        assertThat("Incorrect number of tokens 1.", throttle.availableTokens(), is(MAX_TOKENS - 1));

        // New tokens are added every 5 seconds.
        // Wait 3 seconds and check available tokens. There should still be 1 less than the max.
        executor.schedule(() -> assertThat("Incorrect number of tokens.", throttle.availableTokens(),
                is(MAX_TOKENS - 1)), 3000, TimeUnit.MILLISECONDS).get();

        // Wait another 2 seconds (a total wait equal to the new token cycle) and check again. A new token should have been added.
        executor.schedule(() -> assertThat("Incorrect number of tokens.", throttle.availableTokens(), is(MAX_TOKENS))
                , 2000, TimeUnit.MILLISECONDS).get();

        verify(mockTask, times(1)).tokensAvailableTask();
    }

    @Test
    public void shouldEnabledTimeoutForExpectedTime() throws Exception {
        //this.throttle.kill();
        //this.throttle = new Throttle(MAX_TOKENS, 1000, (msg) -> System.out.println(msg));

        for (int i = 0; i <= (MAX_TOKENS + 1); i++) {
            throttle.executeTask(mockTask);
        }
        DateTime start = DateTime.now();
        assertThat("Timeout should be enabled.", throttle.timeoutEnabled(), is(true));

        while (throttle.timeoutEnabled()) {
            Thread.sleep(500);
        }

        System.out.println("Timeout removed after " + (DateTime.now().getMillis() - start.getMillis()));
        executor.schedule(() -> assertThat("Timeout should be enabled.", throttle.timeoutEnabled(), is(false)), 25000, TimeUnit.MILLISECONDS).get();
    }
}
