package com.github.onsdigital.babbage.util.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;

/**
 * Class for building Closable Http clients to be used by Babbage
 */
public class BabbageHttpClient implements AutoCloseable {

    protected final CloseableHttpClient httpClient;
    protected final URI host;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final IdleConnectionMonitorThread monitorThread;

    public BabbageHttpClient(String host, ClientConfiguration configuration) {
        this.host = resolveHostUri(host);
        this.connectionManager = new PoolingHttpClientConnectionManager();
        HttpClientBuilder customClientBuilder = HttpClients.custom();
        configure(customClientBuilder, configuration);
        this.httpClient = customClientBuilder.setConnectionManager(connectionManager)
                .build();

        info().log("Starting monitor thread");
        this.monitorThread = new IdleConnectionMonitorThread(connectionManager);
        this.monitorThread.start();
        Runtime.getRuntime().addShutdownHook(new BabbageHttpClient.ShutdownHook());
    }

    private void configure(HttpClientBuilder customClientBuilder, ClientConfiguration configuration) {
        Integer connectionNumber = configuration.getMaxTotalConnection();
        if (connectionNumber != null) {
            connectionManager.setMaxTotal(connectionNumber);
            connectionManager.setDefaultMaxPerRoute(connectionNumber);
        }
        if (configuration.isDisableRedirectHandling()) {
            customClientBuilder.disableRedirectHandling();
        }
    }

    private URI resolveHostUri(String host) {
        URIBuilder builder = new URIBuilder();
        if (StringUtils.startsWithIgnoreCase(host, "http")) {
            URI givenHost = URI.create(host);
            builder.setScheme(givenHost.getScheme());
            builder.setHost(givenHost.getHost());
            builder.setPort(givenHost.getPort());
            builder.setPath(givenHost.getPath());
            builder.setUserInfo(givenHost.getUserInfo());
        } else {
            builder.setScheme("http");
            builder.setHost(host);
        }
        try {
            return builder.build();
        } catch (URISyntaxException e) {
            info().exception(e).log("error building uri");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        info().data("host", host.getHost()).log("Shutting down connection pool");
        httpClient.close();
        info().data("host", host.getHost()).log("Successfully shut down connection pool");
        monitorThread.shutdown();
    }

    private class IdleConnectionMonitorThread extends Thread {

        private boolean shutdown;
        private PoolingHttpClientConnectionManager connMgr;

        public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            info().data("host", host.getHost()).log("Running connection pool monitor");
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(appConfig().contentAPI().pooledConnectionsTimeout());
                        // Close expired connections every x seconds (now configurable)
                        connMgr.closeExpired();
                        // Close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdle(TimeValue.of(appConfig().contentAPI().idleConnectionsTimeout(), TimeUnit.SECONDS));
                    }
                }
            } catch (InterruptedException ex) {
                info().exception(ex).data("host", host.getHost()).log("Connection pool monitor failed");
            }
        }

        public void shutdown() {
            info().data("host", host.getHost()).log("Shutting down connection pool monitor");
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }

    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            try {
                info().data("host", host.getHost()).log("Closing http client");
                if (httpClient != null) {
                    close();
                }
            } catch (Exception e) {
                info().exception(e).data("host", host.getHost()).log("Falied shutting down http client");
            }
        }
    }

}
