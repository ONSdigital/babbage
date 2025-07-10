package com.github.onsdigital.babbage.configuration.deprecation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.onsdigital.babbage.configuration.deprecation.DeprecationItem.DeprecationType;
import com.github.onsdigital.babbage.util.json.JsonUtil;

import static com.github.onsdigital.babbage.configuration.EnvVarUtils.getValueOrDefault;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

public class DeprecationConfiguration {

    private static final String DEPRECATION_CONFIG_KEY = "DEPRECATION_CONFIG";

    private List<DeprecationItem> deprecationItems;
    private static DeprecationConfiguration INSTANCE;

    public static DeprecationConfiguration getInstance() {
        if (INSTANCE == null) {
            synchronized (DeprecationConfiguration.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DeprecationConfiguration();
                }
            }
        }
        return INSTANCE;
    }

    public DeprecationConfiguration() throws RuntimeException {
        deprecationItems = parseDeprecationConfig(getValueOrDefault(DEPRECATION_CONFIG_KEY, "[]"));
    }

    public List<DeprecationItem> parseDeprecationConfig(String jsonConfig) throws RuntimeException {
        try {
            DeprecationItem[] configArray = JsonUtil.fromJson(jsonConfig, DeprecationItem[].class);

            if (configArray != null){
                return new ArrayList<>(Arrays.asList(configArray));
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            error().data("config", jsonConfig).data("e", e).log("failed to parse deprecation config");
            // If the deprecation config is invalid, application shouldn't start.
            throw new RuntimeException(e);
        }
    }

    public List<DeprecationItem> getDeprecationItems() {
        return deprecationItems;
    }

    public List<DeprecationItem> getDeprecationItems(DeprecationType type) {
        List<DeprecationItem> items = deprecationItems
                .stream()
                .filter(deprecationItem -> deprecationItem.getDeprecationType() == type)
                .collect(Collectors.toList());

        info().data("items", items).log("getting deprecation items by type");

        return items;
    }

    public Map<String, DeprecationItem> getDeprecationItemsByPageType() {
        Map<String, DeprecationItem> items = new HashMap<>();

        getDeprecationItems(DeprecationType.DATA)
                .stream()
                .forEach(d -> {
                    d.getPageTypes().forEach(t -> items.put(t, d));
                });

        return items;
    }

}
