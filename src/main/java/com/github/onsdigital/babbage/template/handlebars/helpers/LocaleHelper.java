package com.github.onsdigital.babbage.template.handlebars.helpers;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.ThreadContext;

public enum LocaleHelper implements BabbageHandlebarsHelper<String> {

    label {
        @Override
        public CharSequence apply(String key, Options options) throws IOException {
            return MessageFormat.format(getLocalisedMessage(key), options.params);
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    },

    typeLabel {
        @Override
        public CharSequence apply(String typeCode, Options options) throws IOException {
            return getLocalisedMessage(getDictionaryKeyForType(typeCode));
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    };

    private static String getLocalisedMessage(String key) {
        return ((Map<String, String>) ThreadContext.getData(RequestUtil.LABELS_KEY)).get(key);
    }

    private static String getDictionaryKeyForType(String typeCode) {
        switch (typeCode) {
        case "bulletin":
            return "statistical-bulletin";
        case "article":
        case "article_download":
        case "static_article":
            return "article";
        case "timeseries":
            return "time-series";
        case "data_slice":
            return "data-slice";
        case "compendium_landing_page":
            return "compendium";
        case "compendium_chapter":
            return "chapter";
        case "compendium_data":
        case "reference_tables":
            return "datasets";
        case "dataset":
        case "dataset_landing_page":
        case "timeseries_dataset":
            return "dataset";
        case "static_landing_page":
        case "static_page":
            // Empty string?
            return "";
        case "static_methodology":
        case "static_methodology_download":
            return "methodology";
        case "static_qmi":
            return "quality-and-methodology-information";
        case "static_adhoc":
            return "user-requested-data";
        case "static_foi":
            return "freedom-of-information";
        case "release":
            return "release";
        default:
            return "";
        }
    }
}
