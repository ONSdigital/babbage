package com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util;

import com.github.onsdigital.babbage.template.TemplateService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InteractiveTagReplacer extends TagReplacementStrategy {

    private static final Pattern pattern = Pattern.compile("<ons-interactive\\surl=\"([-A-Za-z0-9+&@#/%?=~_|!:,.;()*$]+)\"\\s?(?:\\s?full-width=\"([a-zA-Z]*)\")\\s?(?:\\s?url2=\"([-A-Za-z0-9+&@#/%?=~_|!:,.;()*$]+)\")?/>");
    private final String template;

    /**
     * Create an instance of a tag replacement strategy with the given page path.
     * <p>
     * The page path is the path of the page having tags replaced. It is used to resolve links that are relative to the page.
     *
     * @param path
     */
    public InteractiveTagReplacer(String path, String template) {
        super(path);
        this.template = template;
    }

    @Override
    Pattern getPattern() {
        return pattern;
    }

    @Override
    String replace(Matcher matcher) throws IOException {
        String tagPath = matcher.group(1);
        String fullWidth = matcher.group(2);
        String tagPath2 = matcher.group(3);
        LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
        additionalData.put("id", UUID.randomUUID().toString().substring(10));
        additionalData.put("url", tagPath);
        additionalData.put("full-width", fullWidth);
        additionalData.put("url2", tagPath2);
        return TemplateService.getInstance().renderTemplate(template, null, additionalData);
    }
}
