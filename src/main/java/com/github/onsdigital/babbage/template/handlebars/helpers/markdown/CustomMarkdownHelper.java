package com.github.onsdigital.babbage.template.handlebars.helpers.markdown;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.MarkdownHelper;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.ChartTagReplacer;
import com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.ImageTagReplacer;
import com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.TableTagReplacer;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.ThreadContext;

import java.io.IOException;

/**
 * Created by bren on 28/07/15.
 * <p/>
 * Extending functionality of Handlebars java markdown helper
 */
public class CustomMarkdownHelper extends MarkdownHelper implements BabbageHandlebarsHelper<Object> {

    private final String HELPER_NAME = "md";

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (options.isFalsy(context)) {
            return "";
        }

        RequestUtil.Location location = (RequestUtil.Location)ThreadContext.getData("location");
        String path = location.getPathname();
        System.out.println("uri:" + location.getPathname());

        String markdown = context.toString();
        markdown = super.apply(markdown, options).toString();
        markdown = SubscriptHelper.doSubscript(markdown);
        markdown = SuperscriptHelper.doSuperscript(markdown);
        markdown = new ChartTagReplacer(path).replaceCustomTags(markdown);
        markdown = new TableTagReplacer(path).replaceCustomTags(markdown);
        markdown = new ImageTagReplacer(path).replaceCustomTags(markdown);
        //markdown = MathjaxRenderer.render(markdown);
        return new Handlebars.SafeString(markdown) ;
    }

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper(HELPER_NAME, this);
    }
}
