package com.github.onsdigital.babbage.configuration;

import java.util.HashMap;
import java.util.Map;

import static com.github.onsdigital.babbage.configuration.EnvVarUtils.defaultIfBlank;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getNumberValue;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getStringAsBool;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getValue;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getValueOrDefault;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

public class Babbage implements AppConfig {

    // env var keys
    private static final String ENABLE_CACHE_KEY = "ENABLE_CACHE";
    private static final String DEV_ENVIRONMENT_KEY = "DEV_ENVIRONMENT";
    private static final String IS_PUBLISHING_KEY = "IS_PUBLISHING";
    private static final String REDIRECT_SECRET_KEY = "REDIRECT_SECRET";
    private static final String HIGHCHARTS_EXPORT_SERVER_KEY = "HIGHCHARTS_EXPORT_SERVER";
    private static final String MATHJAX_EXPORT_SERVER_KEY = "MATHJAX_EXPORT_SERVER";
    private static final String ENABLE_METRICS_KEY = "ENABLE_METRICS";

    private static Babbage INSTANCE;

    static Babbage getInstance() {
        if (INSTANCE == null) {
            synchronized (Babbage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Babbage();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * cache timeout in seconds, to be set as HTTP max age header
     */
    private final int defaultCacheTime;

    /**
     * If content that should be published is more than an hour due delete publish date to get it caching again
     **/
    private final int publishCacheTimeout;

    /**
     * search results max age header in seconds
     **/
    private final long searchResponseCacheTime;

    private final int maxVisiblePaginatorLink;
    private final int resultsPerPage;
    private final int maxResultsPerPage;
    private final boolean cacheEnabled;
    private final boolean isDevEnv;
    private final boolean isPublishing;
    private final String redirectSecret;
    private final int maxHighchartsServerConnections;
    private final String exportSeverUrl;
    private final String mathjaxExportServer;
    private final int metricsPort;
    private final boolean metricsEnabled;

    private Babbage() {
        maxVisiblePaginatorLink = 5;
        resultsPerPage = 10;
        maxResultsPerPage = 250;
        defaultCacheTime = 15 * 60;
        publishCacheTimeout = 60 * 60;
        searchResponseCacheTime = 5;
        cacheEnabled = getStringAsBool(ENABLE_CACHE_KEY, "N");

        isDevEnv = getStringAsBool(DEV_ENVIRONMENT_KEY, "N");

        isPublishing = getStringAsBool(IS_PUBLISHING_KEY, "N");

        redirectSecret = getValueOrDefault(REDIRECT_SECRET_KEY, "secret");

        maxHighchartsServerConnections = defaultIfBlank(getNumberValue("HIGHCHARTS_EXPORT_MAX_CONNECTION"), 50);

        exportSeverUrl = getValueOrDefault(HIGHCHARTS_EXPORT_SERVER_KEY, "http://localhost:9999/");

        mathjaxExportServer = getValue(MATHJAX_EXPORT_SERVER_KEY);

        metricsPort = 1234;

        metricsEnabled = getStringAsBool(ENABLE_METRICS_KEY, "Y");
    }

    public int getDefaultContentCacheTime() {
        return defaultCacheTime;
    }

    public long getSearchResponseCacheTime() {
        return searchResponseCacheTime;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public int getMaxVisiblePaginatorLink() {
        return maxVisiblePaginatorLink;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public int getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    public String getRedirectSecret() {
        return redirectSecret;
    }

    public boolean isDevEnvironment() {
        return isDevEnv;
    }

    public boolean isPublishing() {
        return isPublishing;
    }

    public int getPublishCacheTimeout() {
        return publishCacheTimeout;
    }

    public int getDefaultCacheTime() {
        return defaultCacheTime;
    }

    public boolean isDevEnv() {
        return isDevEnv;
    }

    public int getMaxHighchartsServerConnections() {
        return maxHighchartsServerConnections;
    }

    public String getExportSeverUrl() {
        return exportSeverUrl;
    }

    public String getMathjaxExportServer() {
        return mathjaxExportServer;
    }

    public int getMetricsPort() {
        return metricsPort;
    }

    public boolean getMetricsEnabled() {
        return metricsEnabled;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxVisiblePaginatorLink", maxVisiblePaginatorLink);
        config.put("resultsPerPage", resultsPerPage);
        config.put("maxResultsPerPage", maxResultsPerPage);
        config.put("defaultCacheTime", defaultCacheTime);
        config.put("publishCacheTimeout", publishCacheTimeout);
        config.put("searchResponseCacheTime", searchResponseCacheTime);
        config.put("cacheEnabled", cacheEnabled);
        config.put("isDevEnv", isDevEnv);
        config.put("isPublishing", isPublishing);
        config.put("maxHighchartsServerConnections", maxHighchartsServerConnections);
        config.put("exportSeverUrl", exportSeverUrl);
        config.put("mathjaxExportServer", mathjaxExportServer);
        config.put("metricsPort", metricsPort);
        config.put("metricsEnabled", metricsEnabled);
        return config;
    }
}
