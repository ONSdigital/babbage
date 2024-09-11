package com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.template.TemplateService;
import com.github.onsdigital.babbage.util.http.ClientConfiguration;
import com.github.onsdigital.babbage.util.http.PooledHttpClient;
import com.github.onsdigital.babbage.util.json.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

/**
 * Defines the format of the custom markdown tags for charts and defines how to replace them.
 */
public class TableTagV2Replacer extends TagReplacementStrategy {

    private static final Pattern pattern = Pattern.compile("<ons-table-v2\\spath=\"([-A-Za-z0-9+&@#/%?=~_|!:,.;()*$]+)\"?\\s?/>");
    private static final String RENDERER_HOST = appConfig().tableRenderer().host();
    private static final String RENDERER_PATH = appConfig().tableRenderer().htmlPath();
    private static final PooledHttpClient HTTP_CLIENT = new PooledHttpClient(RENDERER_HOST, createHttpConfiguration());
    private static final Map<String, String> HEADERS = Collections.singletonMap("Content-Type", "application/json;charset=utf-8");
    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final String template;
    private final ContentClient contentClient;
    private final TemplateService templateService;
    private final PooledHttpClient httpClient;

    public TableTagV2Replacer(String path, String template) {
        this(path, template, ContentClient.getInstance(), TemplateService.getInstance(), HTTP_CLIENT);
    }

    public TableTagV2Replacer(String path, String template, ContentClient contentClient, TemplateService templateService, PooledHttpClient httpClient) {
        super(path);
        this.template = template;
        this.contentClient = contentClient;
        this.templateService = templateService;
        this.httpClient = httpClient;
    }

    /**
     * Gets the pattern that this strategy is applied to.
     *
     * @return
     */
    @Override
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * The function that generates the replacement text for each match.
     *
     * @param matcher
     * @return
     * @throws IOException
     */
    @Override
    public String replace(Matcher matcher) throws IOException {

        String tagPath = matcher.group(1);
        String figureUri = resolveFigureUri(this.getPath(), Paths.get(tagPath)) + ".json";

        try {
            ContentResponse contentResponse = contentClient.getResource(figureUri);
            String tableJson = contentResponse.getAsString();
            String html = invokeTableRenderer(tableJson);
            Map<String, Object> context = JsonUtil.toMap(tableJson);
            String result = templateService.renderTemplate(template, context, Collections.singletonMap("tableHtml", html));
            return result;
        } catch (ResourceNotFoundException e) {
            error().exception(e).data("uri", figureUri).log("failed to find figure data for table");
            return templateService.renderTemplate(figureNotFoundTemplate);
        } catch (ContentReadException | TableRendererException e) {
            error().exception(e).data("uri", figureUri).log("failed rendering table-v2");
            return matcher.group();
        }
    }

    private String invokeTableRenderer(String postBody) throws TableRendererException {
        try (CloseableHttpResponse response = httpClient.sendPost(RENDERER_PATH, HEADERS, postBody, CHARSET)) {
            return IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            throw new TableRendererException(e);
        }
    }

    private static ClientConfiguration createHttpConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxTotalConnection(appConfig().tableRenderer().maxConnections());
        configuration.setDisableRedirectHandling(true);
        return configuration;
    }

    /**
     * TableRendererException is thrown and caught internally in this class only.
     */
    private static class TableRendererException extends Exception {
        public TableRendererException(Throwable cause) {
            super(cause);
        }
    }
}
