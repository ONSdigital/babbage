package com.github.onsdigital.babbage;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.babbage.configuration.ApplicationConfiguration;
import com.github.onsdigital.babbage.logging.LogBuilder;
import com.github.onsdigital.babbage.publishing.PublishingManager;
import com.github.onsdigital.babbage.search.ElasticSearchClient;
import com.github.onsdigital.babbage.search.external.SearchClient;

import java.io.IOException;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.logging.LogBuilder.logEvent;

/**
 * Created by bren on 13/12/15.
 * <p/>
 * Startup steps for Babbage
 */
public class Init implements Startup {

    @Override
    public void init() {
        LogBuilder.logEvent().info("starting application babbage initialisation");

        ApplicationConfiguration.init();

        try {
            ElasticSearchClient.init();
        } catch (IOException e) {
            logErrorAndExit(e, "error initializing publish dates index for caching exiting application");
        }

        try {
            PublishingManager.init();
        } catch (IOException e) {
            logErrorAndExit(e, "error initializing publishing manager exiting application");
        }

        if (appConfig().externalSearch().isEnabled()) {
            try {
                SearchClient.getInstance();
            } catch (Exception e) {
                logErrorAndExit(e, "error initializing external search client existing application");
            }
        }

        logEvent().info("application babbage initalisation compeleted successfully");
    }

    private void logErrorAndExit(Throwable t, String message) {
        logEvent(t).error(message);
        System.exit(1);
    }
}
