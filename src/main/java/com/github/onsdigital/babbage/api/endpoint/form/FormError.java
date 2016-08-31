package com.github.onsdigital.babbage.api.endpoint.form;

import java.util.Map;

/**
 * Created by dave on 8/18/16.
 */
public class FormError implements Map.Entry<String, String> {

    private String key;
    private String value;

    public FormError(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String setValue(String value) {
        this.value = value;
        return value;
    }
}
