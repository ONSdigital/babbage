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
 * Defines the format of the custom markdown tags for maps and defines how to replace them.
 */
public class MapTagReplacer extends TagReplacementStrategy {

    private static final Pattern pattern = Pattern.compile("<ons-map\\spath=\"([-A-Za-z0-9+&@#/%?=~_|!:,.;()*$]+)\"?\\s?/>");
    private static final String RENDERER_HOST = appConfig().mapRenderer().host();
    private static final String RENDERER_SVG_PATH = appConfig().mapRenderer().svgPath();
    private static final String RENDERER_PNG_PATH = appConfig().mapRenderer().pngPath();
    private static final PooledHttpClient HTTP_CLIENT = new PooledHttpClient(RENDERER_HOST, createHttpConfiguration());
    private static final Map<String, String> HEADERS = Collections.singletonMap("Content-Type", "application/json;charset=utf-8");
    private static final String CHARSET = StandardCharsets.UTF_8.name();

    /**
     * the type of map to render - svg or png.
     */
    public enum MapType {
        PNG, SVG
    }

    private final String template;
    private final ContentClient contentClient;
    private final TemplateService templateService;
    private final PooledHttpClient httpClient;
    private final String rendererPath;

    public MapTagReplacer(String path, String template, MapType mapType) {
        this(path, template, ContentClient.getInstance(), TemplateService.getInstance(), HTTP_CLIENT, mapType);
    }

    public MapTagReplacer(String path, String template, ContentClient contentClient, TemplateService templateService, PooledHttpClient httpClient, MapType mapType) {
        super(path);
        this.template = template;
        this.contentClient = contentClient;
        this.templateService = templateService;
        this.httpClient = httpClient;
        if (mapType == MapType.PNG) {
            this.rendererPath = RENDERER_PNG_PATH;
        } else {
            this.rendererPath = RENDERER_SVG_PATH;
        }
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
            String mapJson = contentResponse.getAsString();
            String html = invokeMapRenderer(mapJson);
            Map<String, Object> context = JsonUtil.toMap(mapJson);
            String result = templateService.renderTemplate(template, context, Collections.singletonMap("mapHtml", html));
            return result;
        } catch (ResourceNotFoundException e) {
            error().exception(e).data("uri", figureUri).log("failed to find figure data for map");
            return templateService.renderTemplate(figureNotFoundTemplate);
        } catch (ContentReadException | MapRendererException e) {
            error().exception(e).data("uri", figureUri).log("failed rendering map");
            return matcher.group();
        }
    }

    private String invokeMapRenderer(String postBody) throws MapRendererException {
        try (CloseableHttpResponse response = httpClient.sendPost(rendererPath, HEADERS, postBody, CHARSET)) {
            return IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            throw new MapRendererException(e);
        }
    }

    private static ClientConfiguration createHttpConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxTotalConnection(appConfig().mapRenderer().maxConnections());
        configuration.setDisableRedirectHandling(true);
        return configuration;
    }

    /**
     * MapRendererException is thrown and caught internally in this class only.
     */
    private static class MapRendererException extends Exception {
        public MapRendererException(Throwable cause) {
            super(cause);
        }
    }
}
