package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.util.HelperUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by bren on 26/11/15.
 */
public class LookupBlockHelper implements BabbageHandlebarsHelper {

    /**
     * Block version of Lookuphelper, renders child context with found element
     * <p/>

     */
    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (options.params.length <= 0) {
            return options.inverse();
        }
        if (context == null) {
            return options.inverse();
        }

        Object lookup = null;
        if (context instanceof Map) {
            lookup = ((Map) context).get(options.param(0));
        } else if (context instanceof List) {
            int index = HelperUtils.toNumber(options.param(0)).intValue();
            lookup = ((List) context).get(index);
        } else {
            throw new RuntimeException("Object property resolution is not supported");
        }
        if (lookup == null) {
            return options.inverse() ;
        }
        return options.fn(lookup);
    }

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper("getBlock", this);
    }

}
