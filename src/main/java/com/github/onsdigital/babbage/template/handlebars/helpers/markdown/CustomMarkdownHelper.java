package com.github.onsdigital.babbage.template.handlebars.helpers.markdown;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.MarkdownHelper;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.*;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.vladsch.flexmark.ext.gfm.strikethrough.SubscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util.MapTagReplacer.MapType.SVG;

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

        String path;
        try {
            path = ((LinkedHashMap<String, Object>)options.context.parent().model()).get("uri").toString();
        } catch (Exception e) {
            RequestUtil.Location location = (RequestUtil.Location)ThreadContext.getData("location");
            path = location.getPathname();
        }

        String markdown = context.toString();

        MutableDataSet flexOptions = new MutableDataSet();

        // Set optional markdown extensions
        // Available options… https://github.com/vsch/flexmark-java/wiki/Extensions
        flexOptions.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                SuperscriptExtension.create(),
                SubscriptExtension.create()
        ));

        Parser parser = Parser.builder(flexOptions).build();
        HtmlRenderer renderer = HtmlRenderer.builder(flexOptions).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(markdown);
        String html = renderer.render(document);

        html = processCustomMarkdownTags(path, html);

        return new Handlebars.SafeString(html) ;
    }

    protected String processCustomMarkdownTags(String path, String markdown) throws IOException {
        markdown = new ChartTagReplacer(path, "partials/highcharts/chart").replaceCustomTags(markdown);
        markdown = new TableTagReplacer(path, "partials/table").replaceCustomTags(markdown);
        markdown = new TableTagV2Replacer(path, "partials/table-v2").replaceCustomTags(markdown);
        markdown = new MapTagReplacer(path, "partials/map", SVG).replaceCustomTags(markdown);
        markdown = new ImageTagReplacer(path, "partials/image").replaceCustomTags(markdown);
        markdown = new MathjaxTagReplacer(path, "partials/equation").replaceCustomTags(markdown);
        markdown = new InteractiveTagReplacer(path, "partials/interactive").replaceCustomTags(markdown);
        markdown = new QuoteTagReplacer(path, "partials/quote").replaceCustomTags(markdown);
        markdown = new PulloutBoxTagReplacer(path, "partials/pullout-box").replaceCustomTags(markdown);
        markdown = new WarningBlockTagReplacer(path, "partials/warning-block").replaceCustomTags(markdown);
        return markdown;
    }

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper(HELPER_NAME, this);
    }
}
