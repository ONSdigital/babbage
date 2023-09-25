package com.github.onsdigital.babbage.template.handlebars.helpers.web;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;

import java.io.IOException;
import java.util.Map;

/**
 * Created by bren on 18/09/15.
 */
public enum WebHelper implements BabbageHandlebarsHelper<Object> {

    query_string {
        @Override
        public CharSequence apply(Object context, Options options) throws IOException {
            Map<String, Object> queryParameters = (Map<String, Object>) options.context.get("parameters");
            String exclude = options.hash("exclude");

            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> params : queryParameters.entrySet()) {
                String key = params.getKey();
                Object[] values = (Object[]) params.getValue();
                if (key.equals(exclude)) {
                    continue;
                }
                for (int i = 0; i < values.length; i++) {
                    Object value = values[i];
                    builder.append(key).append("=").append(value).append("&");
                }
            }

            String queryString = builder.toString();
            queryString = "?" + queryString;
            return queryString;
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    }

}
