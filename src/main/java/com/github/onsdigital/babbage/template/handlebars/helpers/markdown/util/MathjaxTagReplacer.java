package com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.template.TemplateService;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

/**
 * Render equations in text by calling the external rendering service.
 */
public class MathjaxTagReplacer extends TagReplacementStrategy {

    private static final Pattern pattern = Pattern.compile("<ons-equation\\spath=\"([-A-Za-z0-9+&@#/%?=~_|!:,.;()*$]+)\"?\\s?/>");
    private final String template;

    public MathjaxTagReplacer(String path, String template) {
        super(path);
        this.template = template;
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

    @Override
    public String replace(Matcher matcher) throws IOException {

        String tagPath = matcher.group(1);
        String figureUri = resolveFigureUri(this.getPath(), Paths.get(tagPath));

        try {
            ContentResponse contentResponse = ContentClient.getInstance().getContent(figureUri);
            return TemplateService.getInstance().renderTemplate(template, contentResponse.getDataStream());
        } catch (ResourceNotFoundException e) {
            error().exception(e).data("uri", figureUri).log("failed to find figure data for equation");
            return TemplateService.getInstance().renderTemplate(figureNotFoundTemplate);
        } catch (ContentReadException e) {
            return matcher.group();
        }
    }
}
