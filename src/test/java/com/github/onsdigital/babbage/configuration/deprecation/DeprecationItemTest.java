package com.github.onsdigital.babbage.configuration.deprecation;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertNull;

public class DeprecationItemTest {

    @Test
    public void testDeprecationItemBlank() {
        DeprecationItem itemBlank = new DeprecationItem("", "", "", "", "", "");
        assertNull(itemBlank.getDeprecationDate());
        assertNull(itemBlank.getDeprecationDateTimeStampString());
        assertNull(itemBlank.getLink());
        assertNull(itemBlank.getMatchPattern());
        assertNull(itemBlank.getMessage());
        assertNull(itemBlank.getPageTypes());
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
        assertTrue(item.requestMatch("/timeseriestool"));
        assertTrue(item.requestMatch("/economy/timeseriestool"));
        assertTrue(item.requestMatch("/economy/timeseriestool/data"));
        assertFalse(item.requestMatch("/economy/timeserietool/data"));
        assertFalse(item.requestMatch("timeseriestool/data"));
    }

    @Test
    public void testRequestMatch_complicated() {
        DeprecationItem item = new DeprecationItem("2011-11-30T23:59:59", "2011-11-30T23:59:59", "",
                "/(alladhocs|allmethodologies|publishedrequests)/data$", "", "");
        assertFalse(item.requestMatch("/alladhocs"));
        assertTrue(item.requestMatch("/alladhocs/data"));
        assertTrue(item.requestMatch("/economy/publishedrequests/data"));
    }

}
