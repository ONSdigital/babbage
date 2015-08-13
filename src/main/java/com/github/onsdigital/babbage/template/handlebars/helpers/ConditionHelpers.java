package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.util.HelperUtils;

import java.io.IOException;

/**
 * Created by bren on 02/07/15.
 */
public enum ConditionHelpers implements BabbageHandlebarsHelper<Object> {

    //evaluates equality of given parameters
    eq {
        @Override
        public CharSequence apply(Object context, Options o) throws IOException {
            //Can not return String value of false, it will not be evaluated as falsy ( null, empty, etc, ) by handlebars java. Instead returning false
            return HelperUtils.isEqual(o, context, o.param(0)) ? valid() : null;
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    //evaluates equality of given parameters
    ne {

        @Override
        public CharSequence apply(Object context, Options o) throws IOException {
            return HelperUtils.isNotEqual(o, context, o.param(0)) ? valid() : null;
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },


    //evaluates equality of given parameters
    if_eq {

        @Override
        public CharSequence apply(Object context, Options o) throws IOException {
            return HelperUtils.isEqual(o, context, o.param(0)) ? o.fn() : o.inverse();
        }


        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    //evaluates equality of given parameters
    if_ne {

        @Override
        public CharSequence apply(Object context, Options o) throws IOException {
            return HelperUtils.isNotEqual(o, context, o.param(0)) ? o.fn() : o.inverse();
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    //Render block if all given params are ok(ok as in resolves as true in javascript)
    if_all {
        @Override
        public CharSequence apply(Object context, Options options) throws IOException {
            if (options.isFalsy(context)) {
                return options.inverse();
            } else {
                Object[] params = options.params;
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (options.isFalsy(param)) {
                        return options.inverse();
                    }
                }
                return options.fn();
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    //Render block if any given params are ok(ok as in resolves as true in javascript)
    if_any {
        @Override
        public CharSequence apply(Object context, Options options) throws IOException {
            if (!options.isFalsy(context)) {
                return options.fn();
            } else {
                Object[] params = options.params;
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (!options.isFalsy(param)) {
                        return options.fn();
                    }
                }
                return options.inverse();
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    };


    private static String valid() {
        return String.valueOf(Boolean.TRUE);
    }


}
