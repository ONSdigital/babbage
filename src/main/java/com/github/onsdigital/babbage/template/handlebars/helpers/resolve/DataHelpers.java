package com.github.onsdigital.babbage.template.handlebars.helpers.resolve;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentFilter;
import com.github.onsdigital.babbage.content.client.ContentStream;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.util.URIUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.github.onsdigital.babbage.content.client.ContentClient.depth;
import static com.github.onsdigital.babbage.content.client.ContentClient.filter;
import static com.github.onsdigital.babbage.util.json.JsonUtil.toList;
import static com.github.onsdigital.babbage.util.json.JsonUtil.toMap;

/**
 * Created by bren on 11/08/15.
 */
public enum DataHelpers implements BabbageHandlebarsHelper<Object> {

    /**
     * usage: {{#resolve "uri" [filter=]}}
     * <p>
     * If variableName is not empty data is assigned to given variable name
     */
    resolve {
        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            ContentStream contentStream = null;
            try {
                validateUri(uri);
                String uriString =  (String)uri;

                ContentFilter filter = null;
                String filterVal = options.<String>hash("filter");
                if (filterVal != null) {
                    filter = ContentFilter.valueOf(filterVal.toUpperCase());
                }
                contentStream = ContentClient.getInstance().getContentStream(uriString, filter(filter));
                InputStream data = contentStream.getDataStream();
                Map<String, Object> context = toMap(data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    //Resolve latest article or bulletin with given uri
    resolveLatest {
        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            ContentStream contentStream = null;
            try {
                validateUri(uri);
                String uriString = (String)uri;
                String s = URIUtil.removeLastSegment(uriString) + "/latest";

                ContentFilter filter = null;
                String filterVal = options.<String>hash("filter");
                if (filterVal != null) {
                    filter = ContentFilter.valueOf(filterVal.toUpperCase());
                }
                contentStream = ContentClient.getInstance().getContentStream(s, filter(filter));
                InputStream data = contentStream.getDataStream();
                Map<String, Object> context = toMap(data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }

        }


        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    },

    /**
     * usage:  {{#resolveTaxonomy [depth=depthvalue] [assign=variableName]}
     * <p>
     * If assign is not empty data is assigned to given variable name
     */
    resolveTaxonomy {
        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            ContentStream stream = null;
            try {
                Integer depth = options.<Integer>hash("depth");
                stream = ContentClient.getInstance().getTaxonomy(depth(depth));
                InputStream data = stream.getDataStream();
                List<Map<String, Object>> context = toList(data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    /**
     * usage:  {{#resolveParents "variableName" "uri"}}
     * <p>
     * If variableName is not empty data is assigned to given variable name
     */
    resolveParents {
        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            ContentStream stream = null;
            try {
                validateUri(uri);
                String uriString =  (String)uri;
                stream = ContentClient.getInstance().getParents(uriString);
                InputStream data = stream.getDataStream();
                List<Map<String, Object>> context = toList(data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    /**
     * File size helper reads file size from content service
     */
    fs {

        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            if (options.isFalsy(uri)) {
                return null;
            }
            String uriString = (String) uri;
            try(ContentStream stream = ContentClient.getInstance().getFileSize(uriString)) {
                Map<String, Object> size = toMap(stream.getDataStream());
                return humanReadableByteCount(((Number) size.get("fileSize")).longValue(), true);
            } catch (Exception e) {
                System.err.printf("Failed reading file size from content service, uri: %s cause: %s", uri, e.getMessage());
                return null;
            }
        }

        // Taken from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        private String humanReadableByteCount(Long bytes, boolean si) {
            int unit = si ? 1000 : 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    };


    //gets first parameter as uri, throws exception if not valid
    private static void validateUri(Object uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("Data Helpers: No uri given for resolving");
        }
    }


    /**
     * Assigns data to current context if assign parameter given
     *
     * @param options
     * @param data
     */
    private static void assign(Options options, Object data) {
        String variableName = options.hash("assign");
        if (StringUtils.isNotEmpty(variableName)) {
            options.context.data(variableName, data);
        }
    }

    private static void logResolveError(Object uri, Exception e) {
        System.err.printf("Failed resolving data, uri: %s cause: %s", uri, e.getMessage());

    }

}
