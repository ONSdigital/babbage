package com.github.onsdigital.babbage.feedback.slack;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 8/23/16.
 */
public class Attachment {

    private String fallback;
    private String color;
    private List<Map<String, Object>> fields;
    private String text;
    private List<String> mrkdwn_in;


    public Attachment() {
        this.fields = new ArrayList<>();
        this.mrkdwn_in = new ArrayList<>();
    }

    public String getFallback() {
        return fallback;
    }

    public Attachment setFallback(String fallback) {
        this.fallback = fallback;
        return this;
    }

    public String getColor() {
        return color;
    }

    public Attachment setColor(String color) {
        this.color = color;
        return this;
    }

    public List<Map<String, Object>> getFields() {
        return fields;
    }

    public Attachment addField(String title, Object value, boolean isShort) {
        if (value != null) {
            this.fields.add(createField(title, value, isShort));
        }
        return this;
    }

    public String getText() {
        return text;
    }

    public Attachment setText(String text) {
        this.text = text;
        return this;
    }

    public Attachment addMarkDown(String field) {
        this.mrkdwn_in.add(field);
        return this;
    }

    private Map<String, Object> createField(String title, Object value, boolean isShort) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("value", value);
        map.put("short", isShort);
        return map;
    }
}
