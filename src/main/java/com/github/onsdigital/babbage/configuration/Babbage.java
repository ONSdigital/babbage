package com.github.onsdigital.babbage.configuration;

import java.util.HashMap;
import java.util.Map;


import static com.github.onsdigital.babbage.configuration.EnvVarUtils.defaultIfBlank;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getNumberValue;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getStringAsBool;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getValue;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getValueOrDefault;

public class Babbage implements AppConfig {

    // env var keys
    private static final String DEV_ENVIRONMENT_KEY = "DEV_ENVIRONMENT";
    private static final String ENABLE_CACHE_KEY = "ENABLE_CACHE";
    private static final String ENABLE_NAVIGATION_KEY = "ENABLE_NAVIGATION";
    private static final String API_ROUTER_URL = "API_ROUTER_URL";
    private static final String MAX_CACHE_ENTRIES = "CACHE_ENTRIES";
    private static final String MAX_OBJECT_SIZE = "CACHE_OBJECT_SIZE";
    private static final String HIGHCHARTS_EXPORT_SERVER_KEY = "HIGHCHARTS_EXPORT_SERVER";
    private static final String IS_PUBLISHING_KEY = "IS_PUBLISHING";
    private static final String MATHJAX_EXPORT_SERVER_KEY = "MATHJAX_EXPORT_SERVER";
    private static final String REDIRECT_SECRET_KEY = "REDIRECT_SECRET";
    private static final String SERVICE_AUTH_TOKEN = "SERVICE_AUTH";

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
    private final String apiRouterURL;
    private final String exportSeverUrl;
    private final String mathjaxExportServer;
    private final String redirectSecret;
    private final String serviceAuthToken;
    private final boolean cacheEnabled;
    private final boolean isDevEnv;
    private final boolean isNavigationEnabled;
    private final boolean isPublishing;
    private final int maxCacheEntries;
    private final int maxCacheObjectSize;
    private final int maxHighchartsServerConnections;
    private final int maxResultsPerPage;
    private final int maxVisiblePaginatorLink;
    private final int resultsPerPage;
    private final long searchResponseCacheTime;

    private Babbage() {
        apiRouterURL = getValueOrDefault(API_ROUTER_URL, "http://localhost:23200/v1");
        cacheEnabled = getStringAsBool(ENABLE_CACHE_KEY, "N");
        defaultCacheTime = 15 * 60;
        exportSeverUrl = getValueOrDefault(HIGHCHARTS_EXPORT_SERVER_KEY, "http://localhost:9999/");
        isDevEnv = getStringAsBool(DEV_ENVIRONMENT_KEY, "N");
        isNavigationEnabled = getStringAsBool(ENABLE_NAVIGATION_KEY, "N");
        isPublishing = getStringAsBool(IS_PUBLISHING_KEY, "N");
        mathjaxExportServer = getValue(MATHJAX_EXPORT_SERVER_KEY);
        maxCacheEntries = defaultIfBlank(getNumberValue(MAX_OBJECT_SIZE), 3000);
        maxCacheObjectSize = defaultIfBlank(getNumberValue(MAX_CACHE_ENTRIES), 50000);
        maxHighchartsServerConnections = defaultIfBlank(getNumberValue("HIGHCHARTS_EXPORT_MAX_CONNECTION"), 50);
        maxResultsPerPage = 250;
        maxVisiblePaginatorLink = 5;
        publishCacheTimeout = 60 * 60;
        redirectSecret = getValueOrDefault(REDIRECT_SECRET_KEY, "secret");
        resultsPerPage = 10;
        searchResponseCacheTime = 5;
        serviceAuthToken = getValueOrDefault(SERVICE_AUTH_TOKEN, "ahyofaem2ieVie6eipaX6ietigh1oeM0Aa1aiyaebiemiodaiJah0eenuchei1ai");
    }

    public String getApiRouterURL() {
        return apiRouterURL;
    }

    public String getExportSeverUrl() {
        return exportSeverUrl;
    }

    public String getMathjaxExportServer() {
        return mathjaxExportServer;
    }

    public String getRedirectSecret() {
        return redirectSecret;
    }

    public String getServiceAuthToken() {
        return serviceAuthToken;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public boolean isDevEnv() {
        return isDevEnv;
    }

    public boolean isDevEnvironment() {
        return isDevEnv;
    }

    public boolean isNavigationEnabled() {
        return isNavigationEnabled;
    }

    public boolean isPublishing() {
        return isPublishing;
    }

    public int getDefaultCacheTime() {
        return defaultCacheTime;
    }

    public int getDefaultContentCacheTime() {
        return defaultCacheTime;
    }

    public int getMaxCacheEntries() {
        return maxCacheEntries;
    }

    public int getMaxCacheObjectSize() {
        return maxCacheObjectSize;
    }

    public int getMaxHighchartsServerConnections() {
        return maxHighchartsServerConnections;
    }

    public int getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    public int getMaxVisiblePaginatorLink() {
        return maxVisiblePaginatorLink;
    }

    public int getPublishCacheTimeout() {
        return publishCacheTimeout;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public long getSearchResponseCacheTime() {
        return searchResponseCacheTime;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("cacheEnabled", cacheEnabled);
        config.put("defaultCacheTime", defaultCacheTime);
        config.put("exportSeverUrl", exportSeverUrl);
        config.put("isDevEnv", isDevEnv);
        config.put("isNavigationEnable", isNavigationEnabled);
        config.put("isPublishing", isPublishing);
        config.put("mathjaxExportServer", mathjaxExportServer);
        config.put("maxCacheEntries", maxCacheEntries);
        config.put("maxCacheObjectSize", maxCacheObjectSize);
        config.put("maxHighchartsServerConnections", maxHighchartsServerConnections);
        config.put("maxResultsPerPage", maxResultsPerPage);
        config.put("maxVisiblePaginatorLink", maxVisiblePaginatorLink);
        config.put("publishCacheTimeout", publishCacheTimeout);
        config.put("resultsPerPage", resultsPerPage);
        config.put("searchResponseCacheTime", searchResponseCacheTime);
        return config;
    }
}
