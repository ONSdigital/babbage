package com.github.onsdigital.babbage.highcharts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import com.github.onsdigital.babbage.util.TestsUtil;
import org.apache.commons.lang3.CharEncoding;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.onsdigital.babbage.highcharts.ChartConfigBuilder.TITLE_PARAM;
import static com.github.onsdigital.babbage.highcharts.ChartConfigBuilder.URI_PARAM;
import static com.github.onsdigital.babbage.highcharts.ChartConfigBuilder.WIDTH_PARAM;
import static com.github.onsdigital.babbage.highcharts.ChartRenderer.*;
import static java.text.MessageFormat.format;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dave on 12/04/2017.
 */
public class ChartRendererTest {

    private static final String CHART_ID = UUID.randomUUID().toString();
    private static final String URI =
            "/economy/economicoutputandproductivity/output/datasets/outputoftheproductionindustries/" + CHART_ID;
    private static final Integer WIDTH = 500;
    private static ObjectMapper OBJ_MAPPER = new ObjectMapper();

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private ContentClient contentClientMock;
    @Mock
    private ContentResponse contentResponseMock;
    @Mock
    private TemplateService templateServiceMock;
    @Mock
    private HighChartsExportClient highChartsExportClientMock;
    @Mock
    private ServletOutputStream outputStreamMock;

    // Test target.
    private ChartRenderer renderer;
    private Map<String, Object> chartConfigMap;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        renderer = ChartRenderer.getInstance();
        chartConfigMap = new HashMap<>();

        TestsUtil.setPrivateField(renderer, "contentClient", contentClientMock);
        TestsUtil.setPrivateField(renderer, "templateService", templateServiceMock);
        TestsUtil.setPrivateField(renderer, "highChartsExportClient", highChartsExportClientMock);
    }

    @Test
    public void shouldSetContentDispositionHeaderToFileName() throws Exception {
        // Given
        chartConfigMap.put(TITLE_PARAM, "Test title");
        ChartConfigBuilder ccb = new ChartConfigBuilder();
        ccb.getMap().put(WIDTH_PARAM, WIDTH);
        setMockBehaviours(ccb);

        // When
        renderer.renderChartImage(requestMock, responseMock);

        // Then
        assertions(format(CONTENT_DISPOSITION_HEADER_FMT, "Test title"));
    }

    @Test
    public void shouldUseDefaultContentDispositionHeaderIfTitleNull() throws Exception {
        // Given...
        ChartConfigBuilder ccb = new ChartConfigBuilder();
        ccb.getMap().put(WIDTH_PARAM, WIDTH);
        setMockBehaviours(ccb);

        //...When
        renderer.renderChartImage(requestMock, responseMock);

        // Then
        assertions(format(DEFAULT_CONTENT_DISPOSITION_HEADER_FMT, CHART_ID));
    }

    @Test
    public void shouldUseDefaultContentDispositionHeaderIfTitleEmpty() throws Exception {
        // Given...
        chartConfigMap.put(TITLE_PARAM, "");
        ChartConfigBuilder ccb = new ChartConfigBuilder();
        ccb.getMap().put(WIDTH_PARAM, WIDTH);
        setMockBehaviours(ccb);

        //...When
        renderer.renderChartImage(requestMock, responseMock);

        // Then
        assertions(format(DEFAULT_CONTENT_DISPOSITION_HEADER_FMT, CHART_ID));
    }

    @Test
    public void shouldUseDefaultContentDispositionHeaderIfTitleIsFlorenceDefault() throws Exception {
        // Given...
        chartConfigMap.put(TITLE_PARAM, DEFAULT_TITLE_VALUE);
        ChartConfigBuilder ccb = new ChartConfigBuilder();
        ccb.getMap().put(WIDTH_PARAM, WIDTH);
        setMockBehaviours(ccb);

        //...When
        renderer.renderChartImage(requestMock, responseMock);

        // Then
        assertions(format(DEFAULT_CONTENT_DISPOSITION_HEADER_FMT, CHART_ID));
    }

    private void setMockBehaviours(ChartConfigBuilder ccb) throws Exception {
        String chartConfigstr = OBJ_MAPPER.writeValueAsString(chartConfigMap);

        when(requestMock.getParameter(URI_PARAM))
                .thenReturn(URI);
        when(requestMock.getParameter(HIDE_SOURCE_PARAM))
                .thenReturn("true");
        when(contentClientMock.getContent(URI))
                .thenReturn(contentResponseMock);
        when(requestMock.getParameter(WIDTH_PARAM))
                .thenReturn(String.valueOf(WIDTH));
        when(contentResponseMock.getDataStream())
                .thenReturn(new ByteArrayInputStream(chartConfigstr.getBytes()));
        when(templateServiceMock.renderChartConfiguration(any(InputStream.class), eq(ccb.getMap())))
                .thenReturn(chartConfigstr);
        when(highChartsExportClientMock.getImage(chartConfigstr, Integer.valueOf(WIDTH)))
                .thenReturn(new ByteArrayInputStream(chartConfigstr.getBytes()));
        when(responseMock.getOutputStream())
                .thenReturn(outputStreamMock);
    }

    private void assertions(String expectedContentDispositionHeader) throws Exception {
        verify(responseMock, times(1)).setHeader(CONTENT_DISPOSITION_HEADER, expectedContentDispositionHeader);
        verify(responseMock, times(1)).setContentType(PNG_MIME_TYPE);
        verify(responseMock, times(1)).setCharacterEncoding(CharEncoding.UTF_8);
        verify(responseMock, times(1)).setStatus(HttpServletResponse.SC_OK);
    }
}
