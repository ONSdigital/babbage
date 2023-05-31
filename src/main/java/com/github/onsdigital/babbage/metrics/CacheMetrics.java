package com.github.onsdigital.babbage.metrics;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.Counter;

import java.io.IOException;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;

public class CacheMetrics implements Metrics {

    private final Counter publishDatePresent;
    private final Counter publishDateNotPresent;
    private final Counter publishDateInFuture;
    private final Counter publishDateTooFarInPast;
    private final Counter publishDateTooFarInFuture;
    private final Gauge cacheExpiryTime;

    public CacheMetrics() throws IOException {
        new HTTPServer.Builder().withPort(appConfig().babbage().getMetricsPort()).build();

        this.publishDatePresent = Counter.build()
                .name("publish_date_present").help("Total requests for uris that have a past or future publishing date").register();
        this.publishDateNotPresent = Counter.build()
                .name("publish_date_not_present").help("Total requests for uris that have no publishing date found").register();
        this.publishDateInFuture = Counter.build()
                .name("publish_date_in_future").help("Total requests for uris that have a future publishing date").register();
        this.publishDateTooFarInPast = Counter.build()
                .name("publish_date_too_far_in_past").help("Total requests for uris that have a past publishing date too long ago (outside a given time span)").register();
        this.publishDateTooFarInFuture = Counter.build().name("publish_date_too_far_in_future").help("Total requests for uris that have a future publishing date later than that calculated by the default expiry time").register();
        this.cacheExpiryTime = Gauge.build()
                .name("cache_expiry_time").help("The time until the cache expires and will be refreshed by another call to the server.").labelNames("is_greater_than_default").register();
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

    public void incPublishDateTooFarInFuture() { publishDateTooFarInFuture.inc(); }

    public void setCacheExpiryTime(Double expiryTime) {
        boolean isGreaterThanDefault = false;
        if (expiryTime.intValue() > appConfig().babbage().getDefaultContentCacheTime()) {
            isGreaterThanDefault = true;
        }
        cacheExpiryTime.labels(String.valueOf(isGreaterThanDefault)).set(expiryTime);
    }
}
