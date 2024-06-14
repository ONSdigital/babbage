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
    private static final String API_ROUTER_URL = "API_ROUTER_URL";
    private static final String DEV_ENVIRONMENT_KEY = "DEV_ENVIRONMENT";
    private static final String ENABLE_NAVIGATION_KEY = "ENABLE_NAVIGATION";
    private static final String HIGHCHARTS_EXPORT_SERVER_KEY = "HIGHCHARTS_EXPORT_SERVER";
    private static final String IS_PUBLISHING_KEY = "IS_PUBLISHING";
    private static final String MATHJAX_EXPORT_SERVER_KEY = "MATHJAX_EXPORT_SERVER";
    private static final String MAXAGE_SERVICE_KEY = "MAXAGE_SERVER";
    private static final String MAX_CACHE_ENTRIES = "CACHE_ENTRIES";
    private static final String MAX_OBJECT_SIZE = "CACHE_OBJECT_SIZE";
    private static final String POST_PUBLISH_MICRO_CACHE_ENABLED_KEY = "POST_PUBLISH_MICRO_CACHE_ENABLED";
    private static final String REDIRECT_SECRET_KEY = "REDIRECT_SECRET";
    private static final String REINDEX_SERVICE_KEY = "REINDEX_SERVER";
    private static final String SERVICE_AUTH_TOKEN = "SERVICE_AUTH";
    private static final String DEFAULT_CACHE_TIME = "DEFAULT_CACHE_TIME";
    private static final String PUBLISH_CACHE_TIMEOUT = "PUBLISH_CACHE_TIMEOUT";

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
    private final String maxAgeSecret;
    private final String reindexSecret;
    private final String redirectSecret;
    private final String serviceAuthToken;
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

    /**
     * Post publish, how long should we use the postPublishCacheMaxAge before going back to the default cache time
     */
    private final boolean postPublishMicroCacheEnabled;

    private Babbage() {
        apiRouterURL = getValueOrDefault(API_ROUTER_URL, "http://localhost:23200/v1");
        defaultCacheTime = defaultIfBlank(getNumberValue(DEFAULT_CACHE_TIME), 15 * 60);
        exportSeverUrl = getValueOrDefault(HIGHCHARTS_EXPORT_SERVER_KEY, "http://localhost:9999/");
        isDevEnv = getStringAsBool(DEV_ENVIRONMENT_KEY, "N");
        isNavigationEnabled = getStringAsBool(ENABLE_NAVIGATION_KEY, "N");
        isPublishing = getStringAsBool(IS_PUBLISHING_KEY, "N");
        mathjaxExportServer = getValue(MATHJAX_EXPORT_SERVER_KEY);
        maxAgeSecret = getValueOrDefault(MAXAGE_SERVICE_KEY, "mPHbKjCol7ObQ87qKVQgHz6kR3nsYJ3WJHgP7+JYyi5rSJbmbDAcQU8EQilFQ6QQ");
        reindexSecret = getValueOrDefault(REINDEX_SERVICE_KEY, "5NpB6/uAgk14nYwHzMbIQRnuI2W63MrBOS2279YlcUUY2kNOhrL+R5UFR3O066bQ");
        maxCacheEntries = defaultIfBlank(getNumberValue(MAX_OBJECT_SIZE), 3000);
        maxCacheObjectSize = defaultIfBlank(getNumberValue(MAX_CACHE_ENTRIES), 50000);
        maxHighchartsServerConnections = defaultIfBlank(getNumberValue("HIGHCHARTS_EXPORT_MAX_CONNECTION"), 50);
        maxResultsPerPage = 250;
        maxVisiblePaginatorLink = 5;
        postPublishMicroCacheEnabled = getStringAsBool(POST_PUBLISH_MICRO_CACHE_ENABLED_KEY, "N");
        publishCacheTimeout = defaultIfBlank(getNumberValue(PUBLISH_CACHE_TIMEOUT), 60 * 60);
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
    public String getMaxAgeSecret() {
        return maxAgeSecret;
    }
    public String getRedirectSecret() {
        return redirectSecret;
    }

    public String getServiceAuthToken() {
        return serviceAuthToken;
    }
    public String getReindexServiceKey() {
        return reindexSecret;
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

    public boolean isPostPublishMicroCacheEnabled() {
        return postPublishMicroCacheEnabled;
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
        config.put("defaultCacheTime", defaultCacheTime);
        config.put("exportSeverUrl", exportSeverUrl);
        config.put("isDevEnv", isDevEnv);
        config.put("isNavigationEnable", isNavigationEnabled);
        config.put("isPublishing", isPublishing);
        config.put("mathjaxExportServer", mathjaxExportServer);
        config.put("maxAgeSecret", maxAgeSecret);
        config.put("maxCacheEntries", maxCacheEntries);
        config.put("maxCacheObjectSize", maxCacheObjectSize);
        config.put("maxHighchartsServerConnections", maxHighchartsServerConnections);
        config.put("maxResultsPerPage", maxResultsPerPage);
        config.put("maxVisiblePaginatorLink", maxVisiblePaginatorLink);
        config.put("publishCacheTimeout", publishCacheTimeout);
        config.put("reindexSecret", reindexSecret);
        config.put("resultsPerPage", resultsPerPage);
        config.put("searchResponseCacheTime", searchResponseCacheTime);
        return config;
    }
}
