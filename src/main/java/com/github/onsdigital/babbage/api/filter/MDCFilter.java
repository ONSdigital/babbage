package com.github.onsdigital.babbage.api.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.onsdigital.logging.util.RequestLogUtil;

/*
Filter to add the X-Request-Id and remote address to the {@link org.slf4j.MDC} for logging.
*/
public class MDCFilter implements Filter {

    @Override
    public boolean filter(HttpServletRequest req, HttpServletResponse res) {
        RequestLogUtil.extractDiagnosticContext(req);
        return true;
    }
}