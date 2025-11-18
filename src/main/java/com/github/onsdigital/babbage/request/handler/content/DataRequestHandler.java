package com.github.onsdigital.babbage.request.handler.content;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.BabbageStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.configuration.deprecation.DeprecationItem;

import org.apache.hc.core5.http.HttpStatus;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.onsdigital.babbage.util.RequestUtil.getQueryParameters;
import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;

/**
 * Handle data requests. Proxies data requests to content service
 */
public class DataRequestHandler extends BaseRequestHandler {

    public static final String REQUEST_TYPE = "data";
    private Map<String, DeprecationItem> deprecationItemMap;

    private final static Pattern latestTimeseriesRequest = Pattern.compile(".*/timeseries/[^/]+/latest(/data)?/?");

    public DataRequestHandler() {
        this(getDeprecationConfig());
    }

    // This abstracts creation to allow mocking
    protected DataRequestHandler(Map<String, DeprecationItem> config) {
        this.deprecationItemMap = config;
    }

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request) throws Exception {
        Matcher rejectRequest = latestTimeseriesRequest.matcher(requestedUri);
        if (rejectRequest.matches()) {
            // Return page not found for timeseries/cdid/latest/data
            throw new ResourceNotFoundException();
        }
        return getData(requestedUri, request);
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

    public BabbageResponse getData(String uri, HttpServletRequest request) throws Exception {
        ContentResponse contentResponse = getContent(uri, getQueryParameters(request));
        String pageType = contentResponse.getPageType();

        BabbageContentBasedStringResponse babbageResponse = new BabbageContentBasedStringResponse(contentResponse,
                contentResponse.getAsString());

        if (deprecationItemMap.containsKey(pageType)) {
            DeprecationItem deprecationItem = deprecationItemMap.get(pageType);

            if (deprecationItem.isSunsetPassed()) {
                BabbageStringResponse deprecatedResponse = new BabbageStringResponse(deprecationItem.getMessage());
                deprecatedResponse.setStatus(HttpStatus.SC_NOT_FOUND);
                deprecatedResponse.addSunsetHeaders(deprecationItem);
                return deprecatedResponse;
            } else {
                babbageResponse.addSunsetHeaders(deprecationItem);
                return babbageResponse;
            }
        }
        return babbageResponse;
    }

    /**
     * This is an abstraction to allow mocking for unit tests.
     */
    protected ContentResponse getContent(String uri, Map<String, String[]> queryParams) throws ContentReadException {
        return ContentClient.getInstance().getContent(uri, queryParams);
    }

    /**
     * This is an abstraction to allow mocking for unit tests.
     */
    protected static Map<String, DeprecationItem> getDeprecationConfig() {
        return appConfig().babbage().getDeprecationConfig().getDeprecationItemsByPageType();
    }
}
