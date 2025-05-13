package com.github.onsdigital.babbage.configuration;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.github.onsdigital.babbage.configuration.EnvVarUtils.defaultIfBlank;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getNumberValue;
import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getValueOrDefault;

public class ContentAPI implements AppConfig {

    // Configuration environment var keys

    private SimpleDateFormat defaultSimpleDataFormat;
    private final String contentAPIHost;
    private final int maxConnections;
    private final int pooledConnectionsTimeout;
    private final int idleConnectionsTimeout;
    private static ContentAPI INSTANCE;
    private static final String DEFAULT_CONTENT_API_HOST = "http://localhost:8082";
    private static final String CONTENT_API_HOST_KEY = "CONTENT_SERVICE_URL";
    private static final String MAX_CONNECTIONS_KEY = "CONTENT_SERVICE_MAX_CONNECTION";
    private static final String POOLED_CONNECTION_TIMEOUT = "POOLED_CONNECTION_TIMEOUT";
    private static final String IDLE_CONNECTION_TIMEOUT = "IDLE_CONNECTION_TIMEOUT";
    private static final String DEFAULT_CONTENT_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";



    static ContentAPI getInstance() {
        if (INSTANCE == null) {
            synchronized (ContentAPI.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ContentAPI();
                }
            }
        }
        return INSTANCE;
    }

    private ContentAPI() {
        contentAPIHost = StringUtils.removeEnd(getValueOrDefault(CONTENT_API_HOST_KEY, DEFAULT_CONTENT_API_HOST), "/");
        maxConnections = defaultIfBlank(getNumberValue(MAX_CONNECTIONS_KEY), 50);
        pooledConnectionsTimeout = defaultIfBlank(getNumberValue(POOLED_CONNECTION_TIMEOUT), 5000);
        idleConnectionsTimeout = defaultIfBlank(getNumberValue(IDLE_CONNECTION_TIMEOUT), 60);
        defaultSimpleDataFormat = new SimpleDateFormat(DEFAULT_CONTENT_DATE_PATTERN);
    }

    public String serverURL() {
        return contentAPIHost;
    }
    public int maxConnections() {

        return maxConnections;
    }
    public int pooledConnectionsTimeout() {

        return pooledConnectionsTimeout;
    }
    public int idleConnectionsTimeout() {

        return idleConnectionsTimeout;
    }
    public String defaultContentDatePattern() {
        return DEFAULT_CONTENT_DATE_PATTERN;
    }
    public SimpleDateFormat defaultContentDateFormat() {
        return defaultSimpleDataFormat;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(CONTENT_API_HOST_KEY, contentAPIHost);
        config.put(MAX_CONNECTIONS_KEY, maxConnections);
        config.put(POOLED_CONNECTION_TIMEOUT, pooledConnectionsTimeout);
        config.put(IDLE_CONNECTION_TIMEOUT, idleConnectionsTimeout);
        config.put("defaultContentDatePattern", DEFAULT_CONTENT_DATE_PATTERN);
        return config;
    }
}
