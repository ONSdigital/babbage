package com.github.onsdigital.babbage.template.handlebars;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import static com.github.onsdigital.babbage.configuration.Configuration.HANDLEBARS.getMainContentTemplateName;

/**
 * Created by bren on 28/05/15.
 * <p>
 * HandlebarsRenderer renders data in Map Value structure
 */
public class HandlebarsRenderer {

    private Handlebars handlebars;

    public HandlebarsRenderer(String templatesDirectory, String templatesSuffix) {
        handlebars = new Handlebars(new FileTemplateLoader(templatesDirectory, templatesSuffix)).with(new HighConcurrencyTemplateCache());
        initializeHelpers();
    }

    private void initializeHelpers() {
        // String helpers
        StringHelpers.register(handlebars);
        // Humanize helpers
        HumanizeHelper.register(handlebars);
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
        registerHelpers();
    }

    /**
     * Renders content using main handlebars template, array of data is combined into a single context
     *
     * @param data array of data
     * @return
     * @throws IOException
     */
    public String renderContent(Map<String, Object>... data) throws IOException {
        return render(getMainContentTemplateName(), data);
    }


    /**
     * Renders content with given template name, array of data is combined to a single context
     *
     * @param data array of data
     * @return
     * @throws IOException
     */
    public String render(String templateName, Map<String, Object>... data) throws IOException {
        Template template = getTemplate(templateName);

        Context.Builder builder = Context
                .newBuilder(data)
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE);

        if (data != null) {
            for (Map<String, Object> next : data) {
                if (next != null) {
                    for (Map.Entry<String, Object> entry : next.entrySet()) {
                        builder.combine(entry.getKey(), entry.getValue());
                    }
                }

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

    private void registerHelpers() {
        System.out.println("Resolving Handlebars helpers");
        try {

            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().addUrls(HandlebarsRenderer.class.getProtectionDomain().getCodeSource().getLocation());
            configurationBuilder.addClassLoader(HandlebarsRenderer.class.getClassLoader());
            Set<Class<? extends BabbageHandlebarsHelper>> classes = new Reflections(configurationBuilder).getSubTypesOf(BabbageHandlebarsHelper.class);

            for (Class<? extends BabbageHandlebarsHelper> helperClass : classes) {
                String className = helperClass.getSimpleName();
                boolean _abstract = Modifier.isAbstract(helperClass.getModifiers());
                if (_abstract && !helperClass.isEnum()) {
                    System.out.println("Skipping registering abstract handlebars helper " + className);
                    continue;
                }

                if (helperClass.isEnum()) {
                    BabbageHandlebarsHelper[] helpers = helperClass.getEnumConstants();
                    for (BabbageHandlebarsHelper helper : helpers) {
                        System.out.println("Registering Handlebars helper " + helper.getClass().getSimpleName() + ":" + helper);
                        helper.register(handlebars);
                    }
                } else {
                    //enum constant classes are anonymous classes that are already registered above by getting constants above
                    if (helperClass.isAnonymousClass()) {
                        continue;
                    }
                    BabbageHandlebarsHelper helperInstance = helperClass.newInstance();
                    System.out.println("Registering Handlebars helper  " + helperInstance.getClass() + ":" + className);
                    helperInstance.register(handlebars);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed initializing handlebars helpers");
            throw new RuntimeException("Failed initializing request handlers", e);
        }

    }
}
