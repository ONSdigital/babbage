package com.github.onsdigital.babbage.template.handlebars.helpers.markdown;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;

import java.io.IOException;

/**
 * Created by bren on 03/11/15.
 * Replaces text wrapped with tilda (~~) with html subscript tags
 */
public class SubscriptHelper implements BabbageHandlebarsHelper<String> {

    private final static String SUBSCRIPT_PATTERN = "~(?=\\S)(\\S*)~";

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper("sub", this);
    }

    @Override
    public CharSequence apply(String text, Options options) throws IOException {
        if (options.isFalsy(text)) {
            return "";
        }
        if(Boolean.TRUE.equals(options.hash("clear")))  {
            return clearSubscript(text);
        }
        return doSubscript(text);
    }

    static String doSubscript(String text) {
        return text.toString().replaceAll(SUBSCRIPT_PATTERN, "<sub>$1</sub>");
    }

    static String clearSubscript(String text) {
        return text.toString().replaceAll(SUBSCRIPT_PATTERN, "");
    }
}
