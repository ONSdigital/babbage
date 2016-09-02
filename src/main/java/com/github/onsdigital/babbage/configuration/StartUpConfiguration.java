package com.github.onsdigital.babbage.configuration;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * Created by dave on 9/1/16.
 */
public abstract class StartUpConfiguration {

    static final String MSG = "  [{0}]";
    static final String DEBUG = "      > {1}";
    static final String SET_PROP = "      > Setting property {1}={2}";

    public void init() {
        System.out.println(format(MSG, this.getClass().getSimpleName()));
        initialize();
        System.out.println(format(MSG, this.getClass().getSimpleName()));
    }

    public abstract void initialize();

    protected void configDebug(String msg) {
        System.out.println(format(DEBUG, this.getClass().getSimpleName(), msg));
    }

    protected String debugSetProperty(String name, String value) {
        System.out.println(format(SET_PROP, this.getClass().getSimpleName(), name, value));
        return value;
    }

    protected String getValue(String key) {
        String value = defaultIfBlank(System.getProperty(key), System.getenv(key));
        return debugSetProperty(key, value);
    }

    protected String getValue(String key, String defaultValue) {
        String value = defaultIfBlank(System.getProperty(key), System.getenv(key));
        value = defaultIfBlank(value, defaultValue);
        return debugSetProperty(key, value);
    }
}
