package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.util.HelperUtils;

import java.io.IOException;

/**
 * Created by bren on 22/10/15.
 * Used to render a number, if given value is not a number will render null instead, used for chart data
 *
 */
public class NumberPrintHelper implements BabbageHandlebarsHelper<Object> {

    private static final String HELPER_NAME= "num";

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper(HELPER_NAME,this);
    }

    @Override
    public Object apply(Object context, Options options) throws IOException {
        Double num = HelperUtils.toNumber(context);
        if (num == null) {
            return "null";
        }
        return num.toString();
    }
}
