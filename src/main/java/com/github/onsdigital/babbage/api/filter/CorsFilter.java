package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.util.URIUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Add CORS headers to specific requests.
 */
public class CorsFilter implements Filter {
    @Override
    public boolean filter(HttpServletRequest request, HttpServletResponse response) {

        String requestType = URIUtil.resolveRequestType(request.getRequestURI());

        if (requestType.equals("data") || request.getRequestURI().equals("/chartconfig")) {
            response.addHeader("Access-Control-Allow-Origin", "*"); //request.getHeader("origin"));
            response.addHeader("Access-Control-Allow-Methods", "GET");
        }

        return true; // let the request continue through the pipeline.
    }
}
