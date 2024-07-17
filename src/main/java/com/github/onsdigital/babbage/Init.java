package com.github.onsdigital.babbage;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.babbage.configuration.ApplicationConfiguration;
import com.github.onsdigital.babbage.publishing.PublishingManager;
import com.github.onsdigital.babbage.search.ElasticSearchClient;
import com.github.onsdigital.logging.v2.DPLogger;
import com.github.onsdigital.logging.v2.Logger;
import com.github.onsdigital.logging.v2.LoggerImpl;
import com.github.onsdigital.logging.v2.LoggingException;
import com.github.onsdigital.logging.v2.config.Builder;
import com.github.onsdigital.logging.v2.serializer.JacksonLogSerialiser;
import com.github.onsdigital.logging.v2.serializer.LogSerialiser;
import com.github.onsdigital.logging.v2.storage.LogStore;
import com.github.onsdigital.logging.v2.storage.MDCLogStore;

import java.io.IOException;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by bren on 13/12/15.
 * <p/>
 * Startup steps for Babbage
 */
public class Init implements Startup {

    @Override
    public void init() {
        LogSerialiser serialiser = new JacksonLogSerialiser();
        LogStore store = new MDCLogStore(serialiser);
        Logger logger = new LoggerImpl("babbage");

        try {
            DPLogger.init(new Builder()
                    .serialiser(serialiser)
                    .logStore(store)
                    .logger(logger)
                    .create());
        } catch (LoggingException ex) {
            System.err.println(ex);
            System.exit(1);
        }

        info().log("starting application babbage initialisation");

        ApplicationConfiguration.init();

        ElasticSearchClient.init();

        try {
            PublishingManager.init();
        } catch (IOException e) {
            logErrorAndExit(e, "error initializing publishing manager exiting application");
        }

        info().log("application babbage initialisation completed successfully");
    }

    private void logErrorAndExit(Throwable t, String message) {
        error().exception(t).log(message);
        System.exit(1);
    }
}
