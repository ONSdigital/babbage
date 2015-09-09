package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.util.HelperUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by bren on 09/09/15.
 */
public class NumberFormatHelper implements BabbageHandlebarsHelper<Object> {

    private final String HELPER_NAME = "nf";

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper(HELPER_NAME, this);
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (options.isFalsy(context)) {
            return null;
        }
        String pattern = resolvePattern(options.params);
        NumberFormat numberFormat = pattern == null ? new DecimalFormat() : new DecimalFormat(pattern);
        return new Handlebars.SafeString(numberFormat.format(HelperUtils.toNumber(context)));
    }

    private String resolvePattern(Object[] params) {
        if (params == null || params.length == 0) {
            return null;
        }
        return (String) params[0];
    }
}
