package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;

import java.io.IOException;

public class MinifyBlockHelper implements BabbageHandlebarsHelper {

    /**
     * Block helper to minify the contents and remove any spacing from template spacing
     * e.g.
     * {{#minify}}
     *      foo
     *              bar
     * {{/minify}}
     *
     * Outputs:
     * foobar
     * 
     * <p/>

     */
    @Override
    public Object apply(Object context, Options options) throws IOException {
        return options.fn().toString().trim().replace("\t", "").replace("\n", "");
    }

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper("minify", this);
    }

}
