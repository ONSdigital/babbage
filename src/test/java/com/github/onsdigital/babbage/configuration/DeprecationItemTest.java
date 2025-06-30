package com.github.onsdigital.babbage.configuration;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class DeprecationItemTest {

    @Test
    public void testRequestMatch() {
        DeprecationItem item = new DeprecationItem("2011-11-30T23:59:59", "2011-11-30T23:59:59", "", "/timeseriestool","");
        assertEquals(true, item.requestMatch("/timeseriestool"));
        assertEquals(true, item.requestMatch("/economy/timeseriestool"));
        assertEquals(true, item.requestMatch("/economy/timeseriestool/data"));
        assertEquals(false, item.requestMatch("/economy/timeserietool/data"));
        assertEquals(false, item.requestMatch("timeseriestool/data"));
    }

    @Test
    public void testRequestMatch_complicated() {
        DeprecationItem item = new DeprecationItem("2011-11-30T23:59:59", "2011-11-30T23:59:59", "", "/(alladhocs|allmethodologies|publishedrequests)/data$","");
        assertEquals(false, item.requestMatch("/alladhocs"));
        assertEquals(true, item.requestMatch("/alladhocs/data"));
        assertEquals(true, item.requestMatch("/economy/publishedrequests/data"));
    }

}
