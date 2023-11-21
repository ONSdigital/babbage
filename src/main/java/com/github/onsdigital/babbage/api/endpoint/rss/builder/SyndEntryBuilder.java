package com.github.onsdigital.babbage.api.endpoint.rss.builder;

import com.github.onsdigital.babbage.search.model.field.Field;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Map;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.search.model.field.Field.metaDescription;
import static com.github.onsdigital.babbage.search.model.field.Field.releaseDate;
import static com.github.onsdigital.babbage.search.model.field.Field.title;
import static com.github.onsdigital.babbage.search.model.field.Field.uri;
import static com.github.onsdigital.babbage.util.RequestUtil.LOCATION_KEY;
import static com.github.onsdigital.babbage.util.RequestUtil.Location;
import static java.util.Objects.requireNonNull;

/**
 * Class provides a user friendly way to create {@link SyndEntry} objects (required by
 * {@link com.github.onsdigital.babbage.api.endpoint.rss.Rss}).
 */
public class SyndEntryBuilder {

    private static final DateFormat DATE_FORMAT = appConfig().contentAPI().defaultContentDateFormat();
    private static final String DESCRIPTION_TYPE = "text/plain";
    static final String HTTPS_SCHEME = "https://";

    private Map<String, Object> map;

    public SyndEntry build(Map<String, Object> map) {
        requireNonNull(map, "map is a required and cannot be null");
        this.map = map;

        String _title = get(title);
        String _uri = getUri();
        String _metaDescription = get(metaDescription);
        String _releaseDate = get(releaseDate);

        SyndEntry syndEntry = new SyndEntryImpl();
        syndEntry.setTitle(_title);
        syndEntry.setLink(_uri);

        try {
            syndEntry.setPublishedDate(DATE_FORMAT.parse(_releaseDate));
        } catch (ParseException e) {
            String msg = String.format("Unexpected error: Invalid release date could not parse '%s'.", _releaseDate);
            throw new IllegalArgumentException(msg, e);
        }

        SyndContent content = new SyndContentImpl();
        content.setType(DESCRIPTION_TYPE);
        content.setValue(_metaDescription);

        syndEntry.setDescription(content);
        return syndEntry;
    }

    private String getUri() {
        return HTTPS_SCHEME + ((Location) ThreadContext.getData(LOCATION_KEY)).getHostname() + get(uri);
    }

    private String get(Field field) {
        Map nestedMap = this.map;
        String result = null;

        for (String node : field.fieldName().split("\\.")) {
            if (!nestedMap.containsKey(node)) {
                break;
            }
            if (nestedMap.get(node) instanceof Map) {
                nestedMap = (Map<String, Object>) nestedMap.get(node);
                continue;
            }
            result = (String) nestedMap.get(node);
            break;
        }
        return result;
    }
}
