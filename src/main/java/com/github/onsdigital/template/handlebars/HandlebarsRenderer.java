package com.github.onsdigital.template.handlebars;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.onsdigital.configuration.Configuration;
import com.github.onsdigital.template.TemplateRenderer;
import com.github.onsdigital.template.handlebars.helpers.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.onsdigital.configuration.Configuration.HANDLEBARS.getMainContentTemplateName;

/**
 * Created by bren on 28/05/15.
 */
public class HandlebarsRenderer implements TemplateRenderer {

    private Handlebars handlebars;

    public HandlebarsRenderer(String templatesDirectory, String templatesSuffix) {
        handlebars = new Handlebars(new FileTemplateLoader(templatesDirectory, templatesSuffix)).with(new HighConcurrencyTemplateCache());
        initializeHelpers();
    }

    private void initializeHelpers() {
        handlebars.registerHelper("md", new CustomMarkdownHelper());
        handlebars.registerHelper("df", new DateFormatHelper());
        registerFileHelpers();
        registerConditionHelpers();
        handlebars.registerHelper(ArrayHelpers.contains.name(), ArrayHelpers.contains);
        handlebars.registerHelper(MathHelper.increment.name(), MathHelper.increment);
        handlebars.registerHelper(MathHelper.decrement.name(), MathHelper.decrement);
        handlebars.registerHelper(LoopHelper.NAME, new LoopHelper());
        handlebars.registerHelper(PathHelper.rootpath.name(), PathHelper.rootpath);
        // String helpers
        StringHelpers.register(handlebars);
        // Humanize helpers
        HumanizeHelper.register(handlebars);
    }

    @Override
    public String renderTemplate(Object data) throws IOException {
        return renderTemplate(getMainContentTemplateName(), data, null);
    }

    @Override
    public String renderTemplate(Object data, Map<String, Object> additionalData) throws IOException {
        return renderTemplate(getMainContentTemplateName(), data, additionalData);
    }

    @Override
    public String renderTemplate(String templateName, Object data) throws IOException {
        return renderTemplate(templateName, data, null);
    }

    @Override
    public String renderTemplate(String templateName, Object data, Map<String, Object> additionalData) throws IOException {
        Template template = getTemplate(templateName);

        Context.Builder builder = Context
                .newBuilder(data)
                .resolver(FieldValueResolver.INSTANCE, MapValueResolver.INSTANCE);

        if (additionalData != null) {
            for (Map.Entry<String, Object> entry : additionalData.entrySet()) {
                builder.combine(entry.getKey(), entry.getValue());
            }
        }
        return template.apply(builder.build());
    }


    private Template getTemplate(String templateName) throws IOException {
        return compileTemplate(templateName);
    }

    private Template compileTemplate(String templateName) throws IOException {
        return handlebars.compile(templateName);
    }

    private void registerConditionHelpers() {
        ConditionHelper[] values = ConditionHelper.values();
        for (int i = 0; i < values.length; i++) {
            ConditionHelper value = values[i];
            handlebars.registerHelper(value.name(), value);
        }
    }

    private void registerFileHelpers() {
        FileHelpers[] values = FileHelpers.values();
        for (int i = 0; i < values.length; i++) {
            FileHelpers value = values[i];
            handlebars.registerHelper(value.name(), value);
        }
    }

}
