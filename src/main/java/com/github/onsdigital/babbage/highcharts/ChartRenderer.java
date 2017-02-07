package com.github.onsdigital.babbage.highcharts;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.response.BabbageContentBasedBinaryResponse;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.BabbageStringResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * Created by bren on 09/10/15.
 */
public class ChartRenderer {
    private static ChartRenderer instance = new ChartRenderer();
    public static final int DEFAULT_CHART_WIDTH = 600;
    public static final int MAX_CHART_WIDTH = 1600;

    public static ChartRenderer getInstance() {
        return instance;
    }

    private ChartRenderer() {
    }

    /**
     * Fetches configuration from Zebedee Reader and converts it into chart configuration and renders using Handlebars templates for the chart type.
     * <p>
     * Optionally takes a width parameter, width is 600 by default, if width exceeds max, max width will be applied, if it is smaller than min, min width will apply
     */
    public void renderChartConfig(HttpServletRequest request, HttpServletResponse response) throws IOException, ContentReadException {
        String uri = request.getParameter("uri");
        if (assertUri(uri, request, response)) {
            ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);
            LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
            Integer width = getWidth(request);
            if (width == null) {
                width = DEFAULT_CHART_WIDTH;
            }
            additionalData.put("width", width);
            String chartConfig = TemplateService.getInstance().renderChartConfiguration(contentResponse.getDataStream(),
                    additionalData);
            new BabbageContentBasedStringResponse(contentResponse,chartConfig).apply(request, response);
        }
    }

    /**
     * Fetches configuration from Zebedee Reader and renders self contained chart html
     * <p>
     * Optionally takes a width parameter, width is 600 by default, if width exceeds max, max width will be applied, if it is smaller than min, min width will apply
     */
    public void renderEmbeddedChart(HttpServletRequest request, HttpServletResponse response) throws IOException, ContentReadException {
        String uri = request.getParameter("uri");
        if (assertUri(uri, request, response)) {
            ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);
            LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
            additionalData.put("width", getWidth(request));

            Boolean showTitle = true;
            String showTitleInput = request.getParameter("title");
            if (StringUtils.isNotBlank(showTitleInput)) {
                showTitle = BooleanUtils.toBoolean(showTitleInput);
            }
            additionalData.put("showTitle", showTitle);

            Boolean showSubTitle = true;
            String showSubTitleInput = request.getParameter("subtitle");
            if (StringUtils.isNotBlank(showSubTitleInput)) {
                showSubTitle = BooleanUtils.toBoolean(showSubTitleInput);
            }
            additionalData.put("showSubTitle", showSubTitle);

            Boolean showSource = true;
            String showSourceInput = request.getParameter("source");
            if (StringUtils.isNotBlank(showSourceInput)) {
                showSource = BooleanUtils.toBoolean(showSourceInput);
            }
            additionalData.put("showSource", showSource);

            Boolean showNotes = true;
            String showNotesInput = request.getParameter("notes");
            if (StringUtils.isNotBlank(showNotesInput)) {
                showNotes = BooleanUtils.toBoolean(showNotesInput);
            }
            additionalData.put("showNotes", showNotes);

            new BabbageContentBasedStringResponse(contentResponse, TemplateService.getInstance().renderTemplate("partials/highcharts/embeddedchart", contentResponse.getDataStream(), additionalData),
                    MediaType.TEXT_HTML).applyEmbedded(request, response);
        }
    }

    public void renderChartImage(HttpServletRequest request, HttpServletResponse response) throws IOException, ContentReadException {
        String uri = request.getParameter("uri");
        if (assertUri(uri, request, response)) {
            ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);
            Integer width = getWidth(request);
            if (width == null) {
                width = DEFAULT_CHART_WIDTH;
            }
            LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
            additionalData.put("width", width);
            String chartConfig = TemplateService.getInstance().renderChartConfiguration(contentResponse.getDataStream(),
                    additionalData);
            try (InputStream stream = HighChartsExportClient.getInstance().getImage(chartConfig, getWidth(request))) {
                new BabbageContentBasedBinaryResponse(contentResponse,stream, "image/png").apply(request, response);
            }
        }
    }


    /**
     * Converts given data into chart configuration and renders using Handlebars templates for the chart type.
     * <p>
     * Optionally takes a width parameter, width is 600 by default, if width exceeds max, max width will be applied, if it is smaller than min, min width will apply
     */
    public void renderChartConfigFor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String data = request.getParameter("data");
        if (StringUtils.isEmpty(data)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            IOUtils.write("Please specify chart data to get chart configuration for", response.getOutputStream());
        }
        LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
        Integer width = getWidth(request);
        if (width == null) {
            width = DEFAULT_CHART_WIDTH;
        }
        additionalData.put("width", width);
        new BabbageStringResponse(TemplateService.getInstance().renderChartConfiguration(data,
                additionalData)).apply(request, response);
    }

    /**
     * Fetches configuration from Zebedee Reader and renders self contained chart html
     * <p>
     * Optionally takes a width parameter, width is 600 by default, if width exceeds max, max width will be applied, if it is smaller than min, min width will apply
     */
    public void renderChart(HttpServletRequest request, HttpServletResponse response) throws IOException, ContentReadException {
        String uri = request.getParameter("uri");
        if (assertUri(uri, request, response)) {
            ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);
            LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
            additionalData.put("width", getWidth(request));
            new BabbageContentBasedStringResponse(contentResponse, TemplateService.getInstance().renderTemplate("highcharts/chart", contentResponse.getDataStream(), additionalData),
                    MediaType.TEXT_HTML).apply(request, response);
        }
    }

    private boolean assertUri(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isEmpty(uri)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            IOUtils.write("Please specify uri of the chart", response.getOutputStream());
            return false;
        }
        return true;
    }


    public static Integer getWidth(HttpServletRequest request) {
        try {
            String width = request.getParameter("width");
            if (StringUtils.isNotEmpty(width)) {
                return calculateWidth(Integer.parseInt(width));
            }
        } catch (NumberFormatException e) {
        }
        return DEFAULT_CHART_WIDTH;
    }

    private static int calculateWidth(Integer width) {
        int w = width == null ? DEFAULT_CHART_WIDTH : width;
        if (w < 0) {
            w = DEFAULT_CHART_WIDTH;
        } else if (w > MAX_CHART_WIDTH) {
            w = MAX_CHART_WIDTH;
        }
        return w;
    }

}
