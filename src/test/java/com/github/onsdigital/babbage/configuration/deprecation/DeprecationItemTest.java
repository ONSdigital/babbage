package com.github.onsdigital.babbage.configuration.deprecation;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class DeprecationItemTest {

    @Test
    public void testDeprecationItemBlank() {
        DeprecationItem itemBlank = new DeprecationItem("", "", "", "", "", "");
        assertEquals(null, itemBlank.getDeprecationDate());
        assertEquals(null, itemBlank.getDeprecationDateTimeStampString());
        assertEquals(null, itemBlank.getLink());
        assertEquals(null, itemBlank.getMatchPattern());
        assertEquals(null, itemBlank.getMessage());
        assertEquals(null, itemBlank.getPageTypes());
    }

    @Test
    public void testDeprecationItemFull() {

        DeprecationItem itemFull = new DeprecationItem("2011-11-30T23:59:59", "2011-11-30T23:59:59", "ons.gov.uk",
                "/timeseriestool", "bulletin", "This page has been decommissioned");

        assertEquals("@1322697599", itemFull.getDeprecationDateTimeStampString());
        assertEquals("2011-11-30T23:59:59", itemFull.getSunsetDate().toString());
        assertEquals("<ons.gov.uk>; rel=\"sunset\"", itemFull.getLink());
        assertEquals("This page has been decommissioned", itemFull.getMessage());

        List<String> expectedPageTypes = Arrays.asList("bulletin");
        assertEquals(expectedPageTypes, itemFull.getPageTypes());
    }

    @Test
    public void testRequestMatch() {
        DeprecationItem item = new DeprecationItem("2011-11-30T23:59:59", "2011-11-30T23:59:59", "", "/timeseriestool",
                "", "");
        assertEquals(true, item.requestMatch("/timeseriestool"));
        assertEquals(true, item.requestMatch("/economy/timeseriestool"));
        assertEquals(true, item.requestMatch("/economy/timeseriestool/data"));
        assertEquals(false, item.requestMatch("/economy/timeserietool/data"));
        assertEquals(false, item.requestMatch("timeseriestool/data"));
    }

    @Test
    public void testRequestMatch_complicated() {
        DeprecationItem item = new DeprecationItem("2011-11-30T23:59:59", "2011-11-30T23:59:59", "",
                "/(alladhocs|allmethodologies|publishedrequests)/data$", "", "");
        assertEquals(false, item.requestMatch("/alladhocs"));
        assertEquals(true, item.requestMatch("/alladhocs/data"));
        assertEquals(true, item.requestMatch("/economy/publishedrequests/data"));
    }

}
