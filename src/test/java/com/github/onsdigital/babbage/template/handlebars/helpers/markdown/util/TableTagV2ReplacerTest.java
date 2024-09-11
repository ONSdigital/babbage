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

import java.util.regex.Matcher;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class TableTagV2ReplacerTest {


    private final String tableHtml = "<table></table>";
    private final String path = "/myPath/";
    private final String template = "myTemplate";
    private final String markdownContent = "<ons-table-v2 path=\"tableid\" />";
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
    private TableTagV2Replacer testObj;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        testObj = new TableTagV2Replacer(path, template, contentClientMock, templateServiceMock, httpClientMock);

        when(contentClientMock.getResource(path + "tableid.json")).thenReturn(contentResponseMock);

        matcher = testObj.getPattern().matcher(markdownContent);
        matcher.find();
    }

    @Test
    public void replaceShouldGetContentAndinvokeTableRenderer() throws Exception {
        String json = "{\"foo\": \"bar\"}";
        when(contentResponseMock.getAsString()).thenReturn(json);
        when(httpClientMock.sendPost(appConfig().tableRenderer().htmlPath(),
                singletonMap("Content-Type", "application/json;charset=utf-8"), json, "UTF-8"))
                .thenReturn(responseMock);
        when(responseMock.getEntity().getContent()).thenReturn(IOUtils.toInputStream(tableHtml));
        when(templateServiceMock.renderTemplate(template, singletonMap("foo", "bar"), singletonMap("tableHtml", tableHtml))).thenReturn(renderedTemplate);

        String result = testObj.replace(matcher);

        assertThat(result, equalTo(renderedTemplate));
    }

    @Test
    public void replaceShouldRenderFigureNotFoundTemplateIfResourceNotFound() throws Exception {
        when(contentClientMock.getResource(anyString())).thenThrow(new ResourceNotFoundException());
        when(templateServiceMock.renderTemplate(TagReplacementStrategy.figureNotFoundTemplate)).thenReturn(TagReplacementStrategy.figureNotFoundTemplate);

        String result = testObj.replace(matcher);

        assertThat(result, equalTo(TagReplacementStrategy.figureNotFoundTemplate));
    }

    @Test
    public void replaceShouldRenderOriginalContentWhenContentNotFound() throws Exception {
        ContentReadException ex = new ContentReadException(404, "");
        when(contentClientMock.getResource(anyString())).thenThrow(ex);

        String result = testObj.replace(matcher);

        assertThat(result, equalTo(markdownContent));
    }
}
