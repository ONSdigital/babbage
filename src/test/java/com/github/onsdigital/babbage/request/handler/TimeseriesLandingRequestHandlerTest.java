package com.github.onsdigital.babbage.request.handler;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class TimeseriesLandingRequestHandlerTest {

    TimeseriesLandingRequestHandler handler = new TimeseriesLandingRequestHandler();

    @Test
    public void canHandleRequestShouldReturnTrueForTimeseriesLanding(){
        assertTrue(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et", ""));
    }

    @Test
    public void canHandleRequestShouldReturnTrueForTimeseriesLandingData(){
        assertTrue(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/data", ""));
    }

    @Test
    public void canHandleRequestShouldReturnTrueForTimeseriesLandingWithTrailingSlash(){
        assertTrue(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/", ""));
    }

    @Test
    public void canHandleRequestShouldReturnTrueForTimeseriesLandingDataWithTrailingSlash(){
        assertTrue(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/data/", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForSpecificTimeSeries(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/mm23", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForSpecificTimeSeriesData(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/mm23/data", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForSpecificTimeSeriesVersion(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/mm23/previous/v1", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForSpecificTimeSeriesWithTrailingSlash(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/mm23/", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForSpecificTimeSeriesDataWithTrailingSlash(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/mm23/data/", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForSpecificTimeSeriesVersionWithTrailingSlash(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/mm23/previous/v1/", ""));
    }

    @Test
    public void isTimeseriesLandingDataUriShouldReturnFalseForTimeseriesLanding(){
        assertFalse(handler.isTimeseriesLandingDataUri("/economy/inflationandpriceindices/timeseries/a9et"));
    }

    @Test
    public void isTimeseriesLandingDataUriShouldReturnTrueForTimeseriesLandingData(){
        assertTrue(handler.isTimeseriesLandingDataUri("/economy/inflationandpriceindices/timeseries/a9et/data"));
    }

    @Test
    public void isTimeseriesLandingDataUriShouldReturnFalseForSpecificTimeSeries(){
        assertFalse(handler.isTimeseriesLandingDataUri("/economy/inflationandpriceindices/timeseries/a9et/mm23"));
    }

    @Test
    public void isTimeseriesLandingDataUriShouldReturnFalseForSpecificTimeSeriesData(){
        assertFalse(handler.isTimeseriesLandingDataUri("/economy/inflationandpriceindices/timeseries/a9et/mm23/data"));
    }

    @Test
    public void getLatestTimeseriesUriShouldReturnLatestTimeseriesUri() {
        String latestUri = handler.getLatestTimeseriesUri("/economy/grossdomesticproductgdp/timeseries/ihyq");
        assertEquals("/economy/grossdomesticproductgdp/timeseries/ihyq/latest", latestUri);
    }

    @Test
    public void getLatestTimeseriesUriShouldReturnLatestTimeseriesUriWithTrailingSlash() {
        String latestUri = handler.getLatestTimeseriesUri("/economy/grossdomesticproductgdp/timeseries/ihyq/");
        assertEquals("/economy/grossdomesticproductgdp/timeseries/ihyq/latest", latestUri);
    }

    @Test
    public void canHandleRequestShouldReturnFalseForTimeseriesLandingWithLatest(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/latest", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForTimeseriesLandingWithLatestData(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/latest/data", ""));
    }

    @Test
    public void canHandleRequestShouldReturnFalseForDatasetWithLatest(){
        assertFalse(handler.canHandleRequest("/economy/inflationandpriceindices/timeseries/a9et/mm23/latest", ""));
    }
}
