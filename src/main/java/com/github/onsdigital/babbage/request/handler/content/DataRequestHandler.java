package com.github.onsdigital.babbage.request.handler.content;

import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.configuration.DeprecationItem;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.util.URIUtil;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.github.onsdigital.babbage.util.RequestUtil.getQueryParameters;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;


/**
 * Handle data requests. Proxies data requests to content service
 */
public class DataRequestHandler extends BaseRequestHandler {

    public static final String REQUEST_TYPE = "data";
    private static Map<String, ListRequestHandler> listPageHandlers = new HashMap<>();

    private Map<String, DeprecationItem> deprecationItemMap;

    public DataRequestHandler() {
        Map<String, DeprecationItem> dm = new HashMap<>();
        appConfig().babbage().getDeprecationConfig().stream()
                .filter(deprecationItem -> deprecationItem.deprecationType() == DeprecationItem.DeprecationType.DATA)
                .forEach(d -> {
                    d.pageTypes().forEach(t -> {
                        dm.put(t, d);
                    });
                });
        this.deprecationItemMap = dm;
    }

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request) throws Exception {
        return getData(requestedUri, request);
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

    public BabbageResponse getData(String uri, HttpServletRequest request) throws Exception {

        String requestType = URIUtil.resolveRequestType(uri);

        if (listPageHandlers.containsKey(requestType)) {
            return listPageHandlers.get(requestType).getData(URIUtil.removeLastSegment(uri), request);
        }

        ContentResponse contentResponse = ContentClient.getInstance().getContent(uri, getQueryParameters(request));
        String pageType = contentResponse.getPageType();
        if (deprecationItemMap.containsKey(pageType)) {
            responseaddSunsetHeaders(response, deprecationItem);
            if (deprecationItem.isSunsetPassed()) {
                try {
                    ErrorHandler.renderErrorPage(404, response);
                    return false;
                } catch (IOException e) {
                    error().data("uri", uri).log("error rendering error page for uri");
                    return false;
                }

            }
        }
        return new BabbageContentBasedStringResponse(contentResponse, contentResponse.getAsString());
    }

    static {
        registerListHandlers();
    }

    private synchronized static void registerListHandlers() {
        try {
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().addUrls(BaseRequestHandler.class.getProtectionDomain().getCodeSource().getLocation());
            configurationBuilder.addClassLoaders(BaseRequestHandler.class.getClassLoader());
            Set<Class<? extends ListRequestHandler>> requestHandlerClasses = new Reflections(configurationBuilder).getSubTypesOf(ListRequestHandler.class);

            for (Class<? extends ListRequestHandler> handlerClass : requestHandlerClasses) {
                if (!Modifier.isAbstract(handlerClass.getModifiers())) {
                    String className = handlerClass.getSimpleName();
                    ListRequestHandler handlerInstance = handlerClass.newInstance();
                    listPageHandlers.put(handlerInstance.getRequestType(), handlerInstance);
                }
            }

            info().data("handlers", Arrays.toString(listPageHandlers.entrySet()
                    .stream()
                    .map((e) -> e.getValue().getClass().getName()).toArray()))
                    .log("registered list page request handlers");

        } catch (Exception ex) {
            error().exception(ex).log("error failed to initialize request handler");
            throw new RuntimeException(ex);
        }
    }
}
