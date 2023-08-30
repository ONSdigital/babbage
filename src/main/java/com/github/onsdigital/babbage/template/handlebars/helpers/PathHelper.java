package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.util.URIUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by bren on 14/07/15.
 *
 * Adds / before uri if not available
 */
public enum PathHelper implements BabbageHandlebarsHelper<String> {

    absolute   {
        @Override
        public Object apply(String uri, Options options) throws IOException {
            if(options.isFalsy(uri)) {
                return null;
            }
            return uri.startsWith("/") ? uri : "/" + uri;
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    parentPath {
        @Override
        public Object apply(String uri, Options options) throws IOException {
            if (options.isFalsy(uri)) {
                return null;
            }
            return URIUtil.removeLastSegment(uri);
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    },

    /*File extension*/
    fe {

        @Override
        public Object apply(String uri, Options options) throws IOException {
            if (options.isFalsy(uri)) {
                return null;
            }
            return FilenameUtils.getExtension(uri);
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    /*File name*/
    fn {
        @Override
        public Object apply(String uri, Options options) throws IOException {
            if (options.isFalsy(uri)) {
                return null;
            }
            return Paths.get(uri).getFileName().toString();
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    }
}


