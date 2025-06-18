package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.url.redirect.RedirectException;
import com.github.onsdigital.babbage.url.shortcut.ShortcutUrl;
import com.github.onsdigital.babbage.url.shortcut.ShortcutUrlService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * URL Shortcut filter: allows shortened url to be redirected to their actual location.
 */
public class ShortUrlFilter implements Filter {

    private static final String ERROR_MSG = "Unexpected error while attempting to find a shortcut url redirect for '%s'.";

    private static Optional<List<ShortcutUrl>> shortcuts = Optional.empty();

    private ShortcutUrlService shortcutUrlService = ShortcutUrlService.getInstance();

    @Override
    public boolean filter(HttpServletRequest req, HttpServletResponse res) {
        try {
            String uri = req.getRequestURI().toLowerCase();
            Optional<ShortcutUrl> temp = get(uri);

            if (temp.isPresent()) {
                res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                res.setHeader(HttpHeaders.LOCATION, temp.get().getRedirect());
                return false;
            }
        } catch (IOException | RedirectException ex) {
            String msg = String.format(ERROR_MSG, req.getRequestURI());
            throw new RuntimeException(msg, ex);
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
}
