package com.github.onsdigital.babbage.request.handler.highcharts.linechart;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentFilter;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentStream;
import com.github.onsdigital.babbage.request.handler.base.RequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.response.BabbageStringResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import com.github.onsdigital.babbage.util.json.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bren on 18/06/15.
 */
public class LineChartConfigRequestHandler implements RequestHandler {

    public static final String REQUEST_TYPE = "linechartconfig";

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request) throws Exception {
        return new BabbageStringResponse(getChartConfig(requestedUri).toString());
    }

    String getChartConfig(String requestedUri) throws IOException, ContentReadException {

        try (ContentStream series = ContentClient.getInstance().getContentStream(requestedUri, ContentClient.filter(ContentFilter.SERIES));
             ContentStream description = ContentClient.getInstance().getContentStream(requestedUri, ContentClient.filter(ContentFilter.DESCRIPTION))
        ) {
            Map<String, Object> descriptionMap = new HashMap<>();
            descriptionMap.put("fullDescription", JsonUtil.toMap(description.getDataStream()));
            String config = TemplateService.getInstance().renderTemplate("highcharts/config/linechartconfig", series.getDataStream(), descriptionMap);
            return config;
        }

    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }
}
