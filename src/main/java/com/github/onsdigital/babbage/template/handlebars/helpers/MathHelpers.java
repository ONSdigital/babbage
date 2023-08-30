package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;

import java.io.IOException;

import static com.github.onsdigital.babbage.template.handlebars.helpers.util.HelperUtils.toNumber;

/**
 * Created by bren on 02/07/15.
 */
public enum MathHelpers implements BabbageHandlebarsHelper<Object> {

    increment {
        @Override
        public Object apply(Object context, Options options) throws IOException {
            Double num = toNumber(context);
            if (num == null) {
                return null;
            }
            num++;
            return new Handlebars.SafeString(String.valueOf(num.longValue()));
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },
    decrement {
        @Override
        public Object apply(Object context, Options options) throws IOException {
            Double num = toNumber(context);
            if (num == null) {
                return null;
            }
            num--;
            return new Handlebars.SafeString(String.valueOf(num.longValue()));
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    },


    add {
        @Override
        public Object apply(Object context, Options options) throws IOException {
            Double result = toNumber(context);
            Object[] params = options.params;
            for (Object param : params) {
                Double currentNumber = toNumber(param);
                if (currentNumber != null) {
                    result = result == null ? currentNumber : (result + currentNumber);
                }
            }
            return result == null ? null : new Handlebars.SafeString(String.valueOf(result));
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    };
}
