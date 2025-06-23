package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.url.redirect.RedirectException;
import com.github.onsdigital.babbage.url.shortcut.ShortcutUrl;
import com.github.onsdigital.babbage.url.shortcut.ShortcutUrlService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.hc.core5.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

/**
 * URL Shortcut filter: allows shortened url to be redirected to their actual location.
 */
public class ShortUrlFilter implements Filter {

    private static final String ERROR_MSG = "unexpected error while attempting to find a shortcut url redirect.";

    private static Optional<List<ShortcutUrl>> shortcuts = Optional.empty();

    private ShortcutUrlService shortcutUrlService = ShortcutUrlService.getInstance();

    @Override
    public boolean filter(HttpServletRequest req, HttpServletResponse res) {
        try {
            String uri = req.getRequestURI().toLowerCase();
            Optional<ShortcutUrl> temp = get(uri);

            if (temp.isPresent()) {
                res.setStatus(HttpStatus.SC_PERMANENT_REDIRECT);
                res.setHeader(HttpHeaders.LOCATION, temp.get().getRedirect());
                return false;
            }
        } catch (IOException | RedirectException ex) {
            handleError(req, res, ex);
        }
        return true;
    }


    private Optional<ShortcutUrl> get(String uri) throws IOException {
        if (!shortcuts.isPresent()) {
            shortcuts = Optional.of(shortcutUrlService.shortcuts());
        }
        return shortcuts.get()
                .stream()
                .filter(shortcutUrl -> shortcutUrl.getShortcut().equalsIgnoreCase(uri))
                .findFirst();
    }

    private void handleError(HttpServletRequest req, HttpServletResponse res, Exception ex) {
        try {
            ErrorHandler.handle(req, res, ex);
        } catch (IOException e) {
            error().exception(e).data("requestURI", req.getRequestURI()).log(ERROR_MSG);
        }
    }

}
