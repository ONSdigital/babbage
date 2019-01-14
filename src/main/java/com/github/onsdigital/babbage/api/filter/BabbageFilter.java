package com.github.onsdigital.babbage.api.filter;

import com.google.common.collect.ImmutableList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * A single implementation of the Restolino filter interface allowing the run order of filters to be controlled.
 * <p>
 * Be sure to implement the com.github.onsdigital.babbage.api.filter.Filter interface and add it into the list
 * contained in this class to specify when it is run.
 */
public class BabbageFilter implements com.github.davidcarboni.restolino.framework.Filter {

    private static final List<Filter> filters = new ImmutableList.Builder<Filter>()
            .add(
                    new RequestContextFilter(),
                    new MDCFilter(),
                    new CorsFilter(),
                    new UrlRedirectFilter(),
                    new ShortUrlFilter(),
                    new StaticFilesFilter())
            .build();

    @Override
    public boolean filter(HttpServletRequest request, HttpServletResponse response) {

        for (Filter filter : filters) {
            if (!filter.filter(request, response)) {
                return false;
            }
        }

        return true;
    }
}
