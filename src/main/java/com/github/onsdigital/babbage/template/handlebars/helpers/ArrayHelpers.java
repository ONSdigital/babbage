package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.util.HelperUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bren on 06/07/15.
 */
public enum ArrayHelpers implements BabbageHandlebarsHelper<Iterable> {

    contains {
        @Override
        public Object apply(Iterable context, Options options) throws IOException {
            Object value = options.param(0);
            if (options.isFalsy(context)) {
                return options.inverse();
            }

            for (Iterator iterator = context.iterator(); iterator.hasNext(); ) {
                Object next = iterator.next();
                if (HelperUtils.isEqual(next, value)) {
                    return options.fn();
                }
            }

            return options.inverse();
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },


    /**
     * Last element of array
     */
    last {
        @Override
        public Object apply(Iterable context, Options options) throws IOException {
            if (options.isFalsy(context)) {
                return options.inverse();
            }

            if (context instanceof List) {
                List list = (List) context;
                if (list.isEmpty()) {
                    return options.inverse();
                }
                return options.fn(list.get(list.size() - 1));
            }

            return options.fn(getLast(context.iterator()));
        }

        private Object getLast(Iterator iterator) {
            while (true) {
                Object current = iterator.next();
                if (!iterator.hasNext()) {
                    return current;
                }
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    }

}
