package com.github.onsdigital.babbage.configuration;

import java.util.HashMap;
import java.util.Map;

import com.github.onsdigital.babbage.configuration.deprecation.DeprecationConfiguration;

import static com.github.onsdigital.babbage.configuration.EnvVarUtils.defaultIfBlank;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getNumberValue;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getStringAsBool;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getValueOrDefault;

public class Babbage implements AppConfig {

    // env var keys
    private static final String API_ROUTER_URL = "API_ROUTER_URL";
    private static final String DEV_ENVIRONMENT_KEY = "DEV_ENVIRONMENT";
    private static final String ENABLE_NAVIGATION_KEY = "ENABLE_NAVIGATION";
    private static final String HIGHCHARTS_EXPORT_SERVER_KEY = "HIGHCHARTS_EXPORT_SERVER";
    private static final String IS_PUBLISHING_KEY = "IS_PUBLISHING";
    private static final String MAX_CACHE_ENTRIES = "CACHE_ENTRIES";
    private static final String MAX_OBJECT_SIZE = "CACHE_OBJECT_SIZE";
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

    private final String apiRouterURL;
    private final String exportSeverUrl;
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
    private final DeprecationConfiguration deprecationConfig;

    private Babbage() {
        apiRouterURL = getValueOrDefault(API_ROUTER_URL, "http://localhost:23200/v1");
        exportSeverUrl = getValueOrDefault(HIGHCHARTS_EXPORT_SERVER_KEY, "http://localhost:9999/");
        deprecationConfig = DeprecationConfiguration.getInstance();
        isDevEnv = getStringAsBool(DEV_ENVIRONMENT_KEY, "N");
        isNavigationEnabled = getStringAsBool(ENABLE_NAVIGATION_KEY, "N");
        isPublishing = getStringAsBool(IS_PUBLISHING_KEY, "N");
        maxCacheEntries = defaultIfBlank(getNumberValue(MAX_OBJECT_SIZE), 3000);
        maxCacheObjectSize = defaultIfBlank(getNumberValue(MAX_CACHE_ENTRIES), 50000);
        maxHighchartsServerConnections = defaultIfBlank(getNumberValue("HIGHCHARTS_EXPORT_MAX_CONNECTION"), 50);
        maxResultsPerPage = 250;
        maxVisiblePaginatorLink = 5;
        resultsPerPage = 10;
        serviceAuthToken = getValueOrDefault(SERVICE_AUTH_TOKEN,
                "ahyofaem2ieVie6eipaX6ietigh1oeM0Aa1aiyaebiemiodaiJah0eenuchei1ai");
    }

    public String getApiRouterURL() {
        return apiRouterURL;
    }

    public String getExportSeverUrl() {
        return exportSeverUrl;
    }

    public String getServiceAuthToken() {
        return serviceAuthToken;
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

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public DeprecationConfiguration getDeprecationConfig() {
        return deprecationConfig;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("exportSeverUrl", exportSeverUrl);
        config.put("isDevEnv", isDevEnv);
        config.put("isNavigationEnable", isNavigationEnabled);
        config.put("isPublishing", isPublishing);
        config.put("maxCacheEntries", maxCacheEntries);
        config.put("maxCacheObjectSize", maxCacheObjectSize);
        config.put("maxHighchartsServerConnections", maxHighchartsServerConnections);
        config.put("maxResultsPerPage", maxResultsPerPage);
        config.put("maxVisiblePaginatorLink", maxVisiblePaginatorLink);
        config.put("resultsPerPage", resultsPerPage);
        config.put("deprecationConfig", deprecationConfig);
        return config;
    }
}
