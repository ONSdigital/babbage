package com.github.onsdigital.babbage.request.handler;

import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.content.DataRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.util.URIUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// only handle requests that specify the CDID and not source dataset.
// ie.
// handle
//    /timeseries/cdid
// and
//    /timeseries/cdid/data
// do not handle
//    /timeseries/cdid/datasetId*
public class TimeseriesLandingRequestHandler extends BaseRequestHandler {

    // regex breakdown:
    // .*/timeseries/  - match anything with /timeseries/
    // [^/]*           - match any number of characters that are not '/' (if there is a forward slash without /data, there is an extra segment of url and implies its not a landing page)
    // (/data)?        - match zero or one instances of '/data'
    // /?              - match zero or one forward slash's on the end of the url
    private static final String landingPageRegex = ".*/timeseries/[^/]*(/data)?/?";

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {



        // use the original request uri, as the uri passed in has the last segment removed.
        uri = URIUtil.cleanUri(request.getRequestURI());

        boolean isDataRequest = false;
        if (URIUtil.isDataRequest(uri)) {
            isDataRequest = true;
            uri = URIUtil.removeLastSegment(uri);
        }

        String latestUri = getLatestTimeseriesUri(uri);

        if (isDataRequest) {
            return new DataRequestHandler().getData(latestUri, request);
        } else {
            return new PageRequestHandler().getPage(latestUri, request);
        }
    }

    public static String getLatestTimeseriesUri(String uri) {
        if (uri.endsWith("/")) {
            return uri + "latest";
        } else {
            return uri + "/latest";
        }
    }

    @Override
    public String getRequestType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean canHandleRequest(String uri, String requestType) {
        return isTimeseriesLandingUri(uri);
    }

    public static boolean isTimeseriesLandingUri(String uri) {
        Pattern r = Pattern.compile(landingPageRegex);
        Matcher match = r.matcher(uri);
        return match.matches();
    }

    public static boolean isTimeseriesLandingDataUri(String uri) {
        Pattern r = Pattern.compile(landingPageRegex);
        Matcher match = r.matcher(uri);
        if (match.find()) {
            String group = match.group(1);
            if (group != null) {
                return true;
            }
        }
        return false;
    }
}
