package com.github.onsdigital.babbage;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
        import io.prometheus.client.Counter;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;

public class Metrics {
    private static Metrics metrics;

    HTTPServer httpServer;
    Counter publishDatePresent;
    Counter publishDateNotPresent;
    Counter publishDateInFuture;
    Counter publishDateTooFarInPast;
    Gauge cacheExpiryTime;


    public static void init() throws Exception {
        if (metrics != null) {
            throw new Exception("Init already called");
        }

        metrics = new Metrics();

        metrics.httpServer = new HTTPServer.Builder()
                .withPort(1234)
                .build();

        metrics.publishDatePresent = Counter.build()
                .name("publish_date_present").help("Total requests for uris that have a past or future publishing date").register();
        metrics.publishDateNotPresent = Counter.build()
                .name("publish_date_not_present").help("Total requests for uris that currently have no publishing date").register();
        metrics.publishDateInFuture = Counter.build().name("publish_date_in_future").help("Total requests for uris that have a future publishing date").register();
        metrics.publishDateTooFarInPast = Counter.build().name("publish_date_too_far_in_past").help("Total requests for uris that have a past publishing date too long ago to concern").register();

        // The cache_expiry_time will be given by the max-age value being used for the cache-control header
        metrics.cacheExpiryTime = Gauge.build()
                .name("cache_expiry_time").help("The time until the cache expires and will be refreshed by another call to the server.").labelNames("is_greater_than_default").register();
    }

    public static Metrics get(){
        return Metrics.metrics;
    }

    public void incPublishDatePresent() {
        publishDatePresent.inc();
    }

    public void incPublishDateNotPresent() {
        publishDateNotPresent.inc();
    }

    public void incPublishDateInFuture() {
        publishDateInFuture.inc();
    }

    public void incPublishDateTooFarInPast() {
        publishDateTooFarInPast.inc();
    }

    public void setCacheExpiryTime(Double expiryTime) {
        boolean isGreaterThanDefault = false;
        if (expiryTime.intValue() > appConfig().babbage().getDefaultContentCacheTime()) {
            isGreaterThanDefault = true;
        }
        cacheExpiryTime.labels(String.valueOf(isGreaterThanDefault)).set(expiryTime);
    }
}
