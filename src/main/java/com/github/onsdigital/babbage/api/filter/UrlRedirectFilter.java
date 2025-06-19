package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.url.redirect.RedirectCategory;
import com.github.onsdigital.babbage.url.redirect.RedirectException;
import com.github.onsdigital.babbage.url.redirect.handler.RedirectHandler;
import com.github.onsdigital.babbage.url.redirect.handler.impl.DataExplorerRedirectHandler;
import com.github.onsdigital.babbage.url.redirect.handler.impl.GeneralRedirectHandler;
import com.github.onsdigital.babbage.url.redirect.handler.impl.TaxonomyRedirectHandler;
import com.google.common.collect.ImmutableMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.github.onsdigital.babbage.url.redirect.RedirectCategory.DATA_EXPLORER_REDIRECT;
import static com.github.onsdigital.babbage.url.redirect.RedirectCategory.GENERAL_REDIRECT;
import static com.github.onsdigital.babbage.url.redirect.RedirectCategory.TAXONOMY_REDIRECT;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;


public class UrlRedirectFilter implements Filter {

    private static final String ONS_URL_PREFIX = "/ons";

    private Map<RedirectCategory, RedirectHandler> handlers =
            new ImmutableMap.Builder<RedirectCategory, RedirectHandler>()
                    .put(TAXONOMY_REDIRECT, new TaxonomyRedirectHandler())
                    .put(DATA_EXPLORER_REDIRECT, new DataExplorerRedirectHandler())
                    .put(GENERAL_REDIRECT, new GeneralRedirectHandler())
                    .build();

    @Override
    public boolean filter(HttpServletRequest request, HttpServletResponse response) {
        try {
            return handle(request, response);
        } catch (RedirectException | IOException e) {
            handleError(request, response, e);
        }
        return true;
    }

    /**
     * @return true if the request URI starts with '<i>/ons</i>' and the request parameters and headers do not contain
     * a value for '<i>redirected</i>'. Return false otherwise.
     */
    private boolean isRedirectCandidate(HttpServletRequest request) {
        return request.getRequestURI().toLowerCase().startsWith(ONS_URL_PREFIX);
    }

    private boolean handle(HttpServletRequest request, HttpServletResponse response) throws RedirectException, IOException {
        if (isRedirectCandidate(request)) {
            Optional<RedirectCategory> category = RedirectCategory.categorize(request);
            if (category.isPresent()) {
                handlers.get(category.get()).handle(request, response);
                return false;
            }
        }
        return true;
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        try {
            ErrorHandler.handle(request, response, ex);
        } catch (IOException e) {
            error().exception(e).log("UrlRedirectFilter encountered an unexpected error");
        }
    }

}
