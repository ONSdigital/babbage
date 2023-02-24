package com.github.onsdigital.babbage.template.handlebars.helpers.resolve;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.babbage.api.util.SearchRendering;
import com.github.onsdigital.babbage.api.util.SearchUtils;
import com.github.onsdigital.babbage.content.client.*;
import com.github.onsdigital.babbage.request.handler.TimeseriesLandingRequestHandler;
import com.github.onsdigital.babbage.search.model.SearchResult;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.util.URIUtil;
import com.google.gson.*;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.github.onsdigital.babbage.content.client.ContentClient.depth;
import static com.github.onsdigital.babbage.content.client.ContentClient.filter;
import static com.github.onsdigital.babbage.util.json.JsonUtil.*;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

/**
 * Created by bren on 11/08/15.
 */
public enum DataHelpers implements BabbageHandlebarsHelper<Object> {

    /**
     * usage: {{#resolve "uri" [filter=] [assign=variableName]}}
     * <p>
     * If variableName is not empty data is assigned to given variable name
     */
    resolve {
        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            ContentResponse contentResponse;
            try {
                validateUri(uri);
                String uriString = (String) uri;

                if (TimeseriesLandingRequestHandler.isTimeseriesLandingUri(uriString)) {
                    uriString = TimeseriesLandingRequestHandler.getLatestTimeseriesUri(uriString);
                }

                ContentFilter filter = null;
                String filterVal = options.hash("filter");
                if (filterVal != null) {
                    filter = ContentFilter.valueOf(filterVal.toUpperCase());
                }
                contentResponse = ContentClient.getInstance().getContent(uriString, filter(filter));

                InputStream data = contentResponse.getDataStream();
                List<Map<String, Object>> context = toList(data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    },


    resolveDatasets {
        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            ContentResponse contentResponse;
            try {
                validateUri(uri);
                String uriString = (String) uri;
                contentResponse = ContentClient.getInstance().getDatasetSummaries(uriString);
                try (InputStream data = contentResponse.getDataStream()) {
                    List<Map<String, Object>> context = toList(data);
                    assign(options, context);
                    return options.fn(context);
                }
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }
    },


    /**
     * usage: {{#resolve "uri" [filter=] [assign=variableName]}}
     * <p>
     * If variableName is not empty data is assigned to given variable name
     */
    resolveTimeSeriesList {
        @Override

        public CharSequence apply(Object uri, Options options) throws IOException {
            try {

                validateUri(uri);
                String uriString = (String) uri;

                Map<String, SearchResult> results = SearchUtils.searchTimeseriesForUri(uriString);
                LinkedHashMap<String, Object> data = SearchRendering.buildResults("list", results);

                assign(options, data);
                return options.fn(data);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
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
            ContentResponse contentResponse = null;
            try {
                validateUri(uri);
                String uriString = (String) uri;
                String s = URIUtil.removeLastSegment(uriString) + "/latest";

                ContentFilter filter = null;
                String filterVal = options.hash("filter");
                if (filterVal != null) {
                    filter = ContentFilter.valueOf(filterVal.toUpperCase());
                }
                contentResponse = ContentClient.getInstance().getContent(s, filter(filter));
                InputStream data = contentResponse.getDataStream();
                Map<String, Object> context = toMap(data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
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
            ContentResponse stream = null;

            try {
                Integer depth = options.<Integer>hash("depth");
                stream = ContentClientCache.getInstance().getTaxonomy(depth(depth));
                InputStream data = stream.getDataStream();
                List<Map<String, Object>> context = null;
                try {
                    context = toList(data);
                } catch (IOException e) {
                    context =  navigationToTaxonomy(stream.getAsString());
                }
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
            }
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    },

    /**
     * Resolves resource file as string, if a file can not be resolved as a string, this will not work
     * usage:  {{#resolveResource [depth=depthvalue] [assign=variableName]}
     * <p>
     * If assign is not empty data is assigned to given variable name
     */
    resolveResource {
        @Override
        public CharSequence apply(Object uri, Options options) throws IOException {
            ContentResponse contentResponse = null;

            try {
                validateUri(uri);
                String uriString = (String) uri;
                contentResponse = ContentClient.getInstance().getResource(uriString);
                String data = contentResponse.getAsString();
                Map<String, Object> context = new LinkedHashMap<>();
                context.put("resource", data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
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
            ContentResponse stream = null;

            try {
                validateUri(uri);
                String uriString = (String) uri;
                stream = ContentClient.getInstance().getParents(uriString);
                InputStream data = stream.getDataStream();
                List<Map<String, Object>> context = toList(data);
                assign(options, context);
                return options.fn(context);
            } catch (Exception e) {
                logResolveError(uri, e);
                return options.inverse();
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
            try {
                ContentResponse stream = ContentClient.getInstance().getFileSize(uriString);
                Map<String, Object> size = toMap(stream.getDataStream());
                return humanReadableByteCount(((Number) size.get("fileSize")).longValue(), true);
            } catch (Exception ex) {
                error().exception(ex).data("uri", uri).log("failed reading file size from content service");
                return null;
            }
        }

        // Taken from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        private String humanReadableByteCount(Long bytes, boolean si) {
            int unit = si ? 1000 : 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }

        @Override
        public void register(Handlebars handlebars) {
            handlebars.registerHelper(this.name(), this);
        }

    };

    private static List<Map<String, Object>> navigationToTaxonomy(String jsondata) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsondata).getAsJsonObject();
        JsonArray items = jsonObject.getAsJsonArray("items");
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> context = new ArrayList<>();

        for (JsonElement item : items) {
            JsonObject item_jsonObject = item.getAsJsonObject();
            Map<String, Object> map = new HashMap<String, Object>();
            JsonObject description = new JsonObject();
//            if( item_jsonObject.get("title") == "Census" ){
//                continue;
//            }
            map.put("uri",item_jsonObject.get("uri"));
            description.add("title", item_jsonObject.get("title"));
            map.put("description",description);
            map.put("type","taxonomy_landing_page");

            List<Map<String, Object>> subtopics_context = new ArrayList<>();
            JsonArray subtopics = item_jsonObject.getAsJsonArray("subtopics");

            if (subtopics != null) {

                for (JsonElement subtopic : subtopics) {
                    JsonObject subtopicJsonObject = subtopic.getAsJsonObject();
                    Map<String, Object> subtopicMap = new HashMap<String, Object>();
                    Map<String, Object> subtopicDescription = new HashMap<String, Object>();

                    subtopicMap.put("uri",subtopicJsonObject.get("uri"));
                    subtopicDescription.put("title",subtopicJsonObject.get("title"));
                    subtopicMap.put("description",subtopicDescription);
                    subtopicMap.put("type","taxonomy_landing_page");
                    subtopics_context.add(subtopicMap);
                }
                map.put("children",subtopics_context);
            }
            context.add(map);
        }
        return context;

    }


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
        error().exception(e).data("uri", uri).log("DataHelpers resolve data for uri");
    }


}
