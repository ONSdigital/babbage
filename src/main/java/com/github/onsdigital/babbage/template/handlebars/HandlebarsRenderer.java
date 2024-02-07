package com.github.onsdigital.babbage.template.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HumanizeHelper;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.AssignHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by bren on 28/05/15.
 * <p/>
 * HandlebarsRenderer renders data in Map Value structure
 */
public class HandlebarsRenderer {

    private Handlebars handlebars;

    public HandlebarsRenderer(String templatesDirectory, String templatesSuffix, boolean reload) {
        handlebars = new Handlebars(new FileTemplateLoader(templatesDirectory, templatesSuffix)).with(new HighConcurrencyTemplateCache().setReload(reload));
        initializeHelpers();
    }

    private void initializeHelpers() {
        // String helpers
        StringHelpers.register(handlebars);
        // Humanize helpers
        HumanizeHelper.register(handlebars);
        //Assign helper
        handlebars.registerHelper(AssignHelper.NAME, AssignHelper.INSTANCE);
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
        registerHelpers();
    }


    /**
     * Renders content with given template name, array of data is combined to a single context
     *
     * @param data           Main data to render template with
     * @param additionalData Additional data to add to context for rendering
     * @return
     * @throws IOException
     */
    public String render(String templateName, Object data, Map<String, Object> additionalData) throws IOException {
        Template template = getTemplate(templateName);

        Context.Builder builder = Context
                .newBuilder(data)
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE);

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

    private void registerHelpers() {
        try {

            List<String> helpersList = new ArrayList<>();
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().addUrls(HandlebarsRenderer.class.getProtectionDomain().getCodeSource().getLocation());
            configurationBuilder.addClassLoaders(HandlebarsRenderer.class.getClassLoader());
            Set<Class<? extends BabbageHandlebarsHelper>> classes = new Reflections(configurationBuilder).getSubTypesOf(BabbageHandlebarsHelper.class);

            for (Class<? extends BabbageHandlebarsHelper> helperClass : classes) {
                String className = helperClass.getSimpleName();
                boolean _abstract = Modifier.isAbstract(helperClass.getModifiers());
                if (_abstract && !helperClass.isEnum()) {
                    continue;
                }

                if (helperClass.isEnum()) {
                    BabbageHandlebarsHelper[] helpers = helperClass.getEnumConstants();
                    for (BabbageHandlebarsHelper helper : helpers) {

                        helpersList.add(helperClass.getSimpleName() + "." + helper.toString());
                        helper.register(handlebars);
                    }
                } else {
                    //enum constant classes are anonymous classes that are already registered above by getting constants above
                    if (helperClass.isAnonymousClass()) {
                        continue;
                    }
                    BabbageHandlebarsHelper helperInstance = helperClass.newInstance();
                    helpersList.add(helperInstance.getClass().getSimpleName());
                    helperInstance.register(handlebars);
                }
            }

            info().data("helpers", helpersList).log("registered handlebars helpers");

        } catch (Exception e) {
            throw error().logException(new RuntimeException(
                    "Failed initializing request handlers"), "failed initializing handlebars helpers");
        }

    }
}
