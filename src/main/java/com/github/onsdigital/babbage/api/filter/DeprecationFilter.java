package com.github.onsdigital.babbage.api.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

import java.io.IOException;
import java.util.List;

import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.configuration.deprecation.DeprecationConfiguration;
import com.github.onsdigital.babbage.configuration.deprecation.DeprecationItem;
import com.github.onsdigital.babbage.configuration.deprecation.DeprecationItem.DeprecationType;
import com.github.onsdigital.babbage.response.util.HttpHeaders;

public class DeprecationFilter implements Filter {

    private final List<DeprecationItem> config;

    public DeprecationFilter(DeprecationConfiguration deprecationConfig) {
        this.config = deprecationConfig.getDeprecationItems(DeprecationType.FILTER);
    }

    @Override
    public boolean filter(HttpServletRequest request, HttpServletResponse response) {
        for (DeprecationItem deprecationItem : config) {
            String uri = request.getRequestURI();

            if (deprecationItem.requestMatch(uri)) {
                addSunsetHeaders(response, deprecationItem);
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
        }

        return true; // let the request continue through the pipeline.
    }

    private void addSunsetHeaders(HttpServletResponse response, DeprecationItem config) {
        response.addHeader(HttpHeaders.DEPRECATION, config.getDeprecationDate().toString());
        response.addHeader(HttpHeaders.LINK, config.getLink());
        response.addHeader(HttpHeaders.SUNSET, config.getSunsetDate().toString());
    }
}
