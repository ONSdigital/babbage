package com.github.onsdigital.babbage.configuration.deprecation;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThrows;

public class DeprecationConfigurationTest {

    @Test
    public void testParseDeprecationConfig() {
        DeprecationConfiguration config = new DeprecationConfiguration();
        String testConfig = "[{\"deprecationDate\":\"2024-12-25T10:00\", \"sunsetDate\":\"2024-12-03T11:00:00\", \"link\":\"testlink\", \"matchPattern\":\"^/timeseriestool$\"}]";
        List<DeprecationItem> deprecationConfig = config.parseDeprecationConfig(testConfig);
        assertEquals(1, deprecationConfig.size());
        assertEquals("2024-12-25T10:00", deprecationConfig.get(0).getDeprecationDate().toString());
        assertEquals("2024-12-03T11:00", deprecationConfig.get(0).getSunsetDate().toString());
        assertEquals("<testlink>; rel=\"sunset\"", deprecationConfig.get(0).getLink());
        assertEquals("^/timeseriestool$", deprecationConfig.get(0).getMatchPattern().pattern());
    }

    @Test
    public void testParseDeprecationConfig_testMultipleEntries() {
        DeprecationConfiguration config = new DeprecationConfiguration();
        String testConfig = "[{\"deprecationDate\":\"2024-12-25T10:00\", \"sunsetDate\":\"2024-12-03T11:00:00\", \"link\":\"testlink\", \"matchPattern\":\"^/timeseriestool$\"}, {\"deprecationDate\":\"2024-11-25T10:00\", \"sunsetDate\":\"2024-11-03T11:00:00\", \"link\":\"testlink2\", \"matchPattern\":\"^/economy$\"}]";
        List<DeprecationItem> deprecationConfig = config.parseDeprecationConfig(testConfig);
        assertEquals(2, deprecationConfig.size());
        assertEquals("2024-11-25T10:00", deprecationConfig.get(1).getDeprecationDate().toString());
        assertEquals("2024-11-03T11:00", deprecationConfig.get(1).getSunsetDate().toString());
        assertEquals("<testlink2>; rel=\"sunset\"", deprecationConfig.get(1).getLink());
        assertEquals("^/economy$", deprecationConfig.get(1).getMatchPattern().pattern());

    }

    @Test
    public void testParseDeprecationConfig_invalidJSON() {
        DeprecationConfiguration config = new DeprecationConfiguration();
        String testConfig = "[";

        assertThrows(RuntimeException.class,
                () -> config.parseDeprecationConfig(testConfig));
    }

    @Test
    public void testParseDeprecationConfig_emptyJSON() {
        DeprecationConfiguration config = new DeprecationConfiguration();
        String testConfig = "[]";

        List<DeprecationItem> deprecationConfig = config.parseDeprecationConfig(testConfig);
        assertEquals(0, deprecationConfig.size());
    }

    @Test
    public void testParseDeprecationConfig_blankJSON() {
        DeprecationConfiguration config = new DeprecationConfiguration();
        String testConfig = "";

        List<DeprecationItem> deprecationConfig = config.parseDeprecationConfig(testConfig);
        assertEquals(0, deprecationConfig.size());
    }

}
