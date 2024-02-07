package com.github.onsdigital.babbage.api.endpoint.rss.builder;

import com.github.onsdigital.babbage.search.model.field.Field;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.rometools.rome.feed.synd.SyndEntry;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.github.onsdigital.babbage.api.endpoint.rss.builder.SyndEntryBuilder.HTTPS_SCHEME;
import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.search.model.field.Field.metaDescription;
import static com.github.onsdigital.babbage.search.model.field.Field.releaseDate;
import static com.github.onsdigital.babbage.search.model.field.Field.title;
import static com.github.onsdigital.babbage.search.model.field.Field.uri;
import static com.github.onsdigital.babbage.util.RequestUtil.LOCATION_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Tests verify the builder behaves as expected when given valid & invalid inputs.
 */
public class SyndEntryBuilderTest {

    private static final String TITLE = "Title";
    private static final String META_DESC = "Meta-Description";
    private static final String RELEASE_DATE = "2015-11-27T00:00:00.000Z";
    private static final String URI = "you/are/eye";

    private Map<String, Object> masterMap;
    private Map<String, Object> description;
    private RequestUtil.Location loc;

    private SyndEntryBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new SyndEntryBuilder();
        masterMap = new HashMap<>();
        description = new HashMap<>();
        loc = new RequestUtil.Location();
        loc.setHostname("host");
        ThreadContext.addData(LOCATION_KEY, loc);
    }

    /**
     * Test verifies that builder throws a {@link NullPointerException} when null is passed in.
     */
    @Test(expected = NullPointerException.class)
    public void testBuilderNullParam() throws Exception {
        try {
            builder.build(null);
        } catch (Exception ex) {
            assertThat("Incorrect exception message", ex.getMessage(), equalTo("map is a required and cannot be null"));
            throw ex;
        }
    }

    /**
     * Test case: verify builder's behaviour when no uri is provided.
     */
    //@Test(expected = NullPointerException.class)
    public void testBuilderNoUri() throws Exception {
        testMissingFields(map(title), uri);
    }

    /**
     * Test case: verify builder's behaviour when no metaDescription is provided.
     */
    //@Test(expected = NullPointerException.class)
    public void testBuilderNoMetaDesc() throws Exception {
        testMissingFields(map(title, uri), metaDescription);
    }

    /**
     * Test case: verify builder's behaviour when no releaseDate is provided.
     */
    //@Test(expected = NullPointerException.class)
    public void testBuilderNoReleaseDate() throws Exception {
        testMissingFields(map(title, uri, metaDescription), releaseDate);
    }

    /**
     * Test case: verify the builder behaves as expected when the released date string is invalid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderInvalidReleaseDate() throws Exception {
        Map<String, Object> args = map(title, uri, metaDescription);
        ((Map<String, Object>) args.get("description")).put("releaseDate", "£$%^&*()");

        try {
            builder.build(args);
        } catch (Exception ex) {
            assertThat("Incorrect exception message", ex.getMessage(),
                    equalTo("Unexpected error: Invalid release date could not parse '£$%^&*()'."));
            throw ex;
        }
    }

    /**
     * Test case: Verify that the created {@link SyndEntry} contains the expected values when builder is supplied with
     * all the valid necessary params.
     */
    @Test
    public void testBuilderSuccess() throws Exception {
        SyndEntry entry = builder.build(map(title, uri, metaDescription, releaseDate));

        assertThat("SyndEntry.title not as expected.", entry.getTitle(), equalTo(TITLE));
        assertThat("SyndEntry.link not as expected.", entry.getLink(), equalTo(HTTPS_SCHEME + loc.getHostname() + URI));
        assertThat("SyndEntry.description.value not as expected.", entry.getDescription().getValue(), equalTo(META_DESC));
        assertThat("SyndEntry.description.type not as expected.", entry.getDescription().getType(), equalTo("text/plain"));
        assertThat("SyndEntry.publishedDate not as expected.", entry.getPublishedDate(),
                equalTo(new SimpleDateFormat(appConfig().contentAPI().defaultContentDatePattern()).parse(RELEASE_DATE)));
    }

    public void testMissingFields(Map<String, Object> map, Field field) throws Exception {
        try {
            builder.build(map);
        } catch (Exception ex) {
            assertThat("Incorrect exception message", ex.getMessage(), equalTo(errorMsg(field)));
            throw ex;
        }
    }

    private Map<String, Object> map(Field... fields) {
        for (Field f : fields) {
            switch (f) {
                case title:
                    description.put("title", TITLE);
                    break;
                case metaDescription:
                    description.put("metaDescription", META_DESC);
                    break;
                case releaseDate:
                    description.put("releaseDate", RELEASE_DATE);
                    break;
                case uri:
                    masterMap.put("uri", URI);
            }
            if (!description.isEmpty()) {
                masterMap.put("description", description);
            }
        }
        return masterMap;
    }

    private String errorMsg(Field field) {
        return String.format("Expected field '%s' in search results but none not found.", field.fieldName());
    }
}
