package com.github.onsdigital.babbage.request.handler.highcharts.sparkline;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentFilter;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentStream;
import com.github.onsdigital.babbage.request.handler.base.RequestHandler;
import com.github.onsdigital.babbage.request.response.BabbageResponse;
import com.github.onsdigital.babbage.request.response.BabbageStringResponse;
import com.github.onsdigital.babbage.template.TemplateService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by bren on 18/06/15.
 */
public class SparklineConfigRequestHandler implements RequestHandler {
    public static final String REQUEST_TYPE = "sparklineconfig";

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request) throws Exception {
        return new BabbageStringResponse(getChartConfig(requestedUri).toString());
    }

    String getChartConfig(String requestedUri) throws IOException, ContentReadException {
        ContentStream contentStream = ContentClient.getInstance().getContentStream(requestedUri, ContentClient.filter(ContentFilter.SERIES));
        String config = TemplateService.getInstance().renderTemplate("highcharts/sparklineconfig", contentStream.getAsString());
        return config;
    }


    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

}
