package com.github.onsdigital.babbage.api.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.configuration.DeprecationItem;
import com.github.onsdigital.babbage.configuration.DeprecationItem.DeprecationType;

import com.github.onsdigital.babbage.response.util.HttpHeaders;

public class DeprecationFilter implements Filter {

    private final List<DeprecationItem> config;

    public DeprecationFilter(List<DeprecationItem> config) {
        this.config = config.stream()
                .filter(deprecationItem -> deprecationItem.deprecationType() == DeprecationType.FILTER)
                .collect(Collectors.toList());
    }


    @Override
    public boolean filter(HttpServletRequest request, HttpServletResponse response) {
        for (DeprecationItem deprecationItem : config) {
            String uri = request.getRequestURI();

            if (deprecationItem.requestMatch(uri)){
                addSunsetHeaders(response, deprecationItem);
                if(deprecationItem.isSunsetPassed()){
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
        response.addHeader(HttpHeaders.DEPRECATION, config.deprecationDate().toString()); 
        response.addHeader(HttpHeaders.LINK, config.link()); 
        response.addHeader(HttpHeaders.SUNSET, config.sunsetDate().toString()); 
    }
}
