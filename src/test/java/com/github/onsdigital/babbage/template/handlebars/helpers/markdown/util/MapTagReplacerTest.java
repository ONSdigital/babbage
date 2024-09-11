package com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.template.TemplateService;
import com.github.onsdigital.babbage.util.http.PooledHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.regex.Matcher;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.MapTagReplacer.MapType.PNG;
import static com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.MapTagReplacer.MapType.SVG;
import static com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.TagReplacementStrategy.figureNotFoundTemplate;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class MapTagReplacerTest {

    private final Map<String, String> headers = singletonMap("Content-Type", "application/json;charset=utf-8");
    private final String mapHtml = "<map></map>";
    private final String path = "/myPath/";
    private final String template = "myTemplate";
    private final String markdownContent = "<ons-map path=\"mapid\" />";
    private final String renderedTemplate = "renderedTemplate";

    @Mock
    private ContentClient contentClientMock;
    @Mock
    private TemplateService templateServiceMock;
    @Mock
    private PooledHttpClient httpClientMock;
    @Mock
    private ContentResponse contentResponseMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CloseableHttpResponse responseMock;
    @Mock
    private ContentReadException readException;

    private Matcher matcher;
    private MapTagReplacer testObj;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        testObj = new MapTagReplacer(path, template, contentClientMock, templateServiceMock, httpClientMock, SVG);

        when(contentClientMock.getResource(path + "mapid.json")).thenReturn(contentResponseMock);

        matcher = testObj.getPattern().matcher(markdownContent);
        matcher.find();
    }

    @Test
    public void replaceShouldGetContentAndInvokeMapRendererForSvg() throws Exception {
        String json = "{\"foo\": \"bar\"}";
        when(contentResponseMock.getAsString()).thenReturn(json);
        when(httpClientMock.sendPost(appConfig().mapRenderer().svgPath(), headers, json, "UTF-8"))
                .thenReturn(responseMock);
        when(responseMock.getEntity().getContent()).thenReturn(IOUtils.toInputStream(mapHtml));
        when(templateServiceMock.renderTemplate(template, singletonMap("foo", "bar"), singletonMap("mapHtml", mapHtml))).thenReturn(renderedTemplate);

        String result = testObj.replace(matcher);

        assertThat(result, equalTo(renderedTemplate));
        verify(httpClientMock, times(1)).sendPost(appConfig().mapRenderer().svgPath(), headers, json, "UTF-8");
        verify(templateServiceMock, times(1)).renderTemplate(template, singletonMap("foo", "bar"), singletonMap("mapHtml", mapHtml));
    }

    @Test
    public void replaceShouldGetContentAndInvokeMapRendererForPng() throws Exception {
        testObj = new MapTagReplacer(path, template, contentClientMock, templateServiceMock, httpClientMock, PNG);
        String json = "{\"foo\": \"bar\"}";
        when(contentResponseMock.getAsString()).thenReturn(json);
        when(httpClientMock.sendPost(appConfig().mapRenderer().pngPath(), headers, json, "UTF-8"))
                .thenReturn(responseMock);
        when(responseMock.getEntity().getContent()).thenReturn(IOUtils.toInputStream(mapHtml));
        when(templateServiceMock.renderTemplate(template, singletonMap("foo", "bar"), singletonMap("mapHtml", mapHtml))).thenReturn(renderedTemplate);

        String result = testObj.replace(matcher);

        assertThat(result, equalTo(renderedTemplate));
        verify(httpClientMock, times(1)).sendPost(appConfig().mapRenderer().pngPath(), headers, json, "UTF-8");
        verify(templateServiceMock, times(1)).renderTemplate(template, singletonMap("foo", "bar"), singletonMap("mapHtml", mapHtml));
    }

    @Test
    public void replaceShouldRenderFigureNotFoundTemplateIfResourceNotFound() throws Exception {
        when(contentClientMock.getResource(anyString())).thenThrow(new ResourceNotFoundException());
        when(templateServiceMock.renderTemplate(figureNotFoundTemplate)).thenReturn(figureNotFoundTemplate);

        String result = testObj.replace(matcher);

        assertThat(result, equalTo(figureNotFoundTemplate));
        verifyZeroInteractions(httpClientMock);
        verify(templateServiceMock, times(1)).renderTemplate(figureNotFoundTemplate);
    }

    @Test
    public void replaceShouldRenderOriginalContentWhenContentNotFound() throws Exception {
        when(contentClientMock.getResource(anyString())).thenThrow(new ContentReadException(0, ""));

        String result = testObj.replace(matcher);

        assertThat(result, equalTo(markdownContent));
        verifyZeroInteractions(httpClientMock, templateServiceMock);
    }
}
