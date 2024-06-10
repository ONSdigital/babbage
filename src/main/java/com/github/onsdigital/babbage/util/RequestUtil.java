package com.github.onsdigital.babbage.util;

import com.github.onsdigital.babbage.locale.LocaleConfig;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by bren on 10/08/15.
 */
public class RequestUtil {

    public static final String LOCATION_KEY = "location";
    public static final String LABELS_KEY = "labels";
    public static final String LANG_KEY = "lang";
    public static final String IS_DEV_KEY = "is_dev";
    public static final String IS_PUBLISHING = "is_publishing";

    /**
     * Saves Authentication token and collection id to thread context if available when a request is made to babbage,
     * this ensures all data requests from content service can be authorized both  when data is requested for any purpose on Babbage ( rendering page, sending data back etc. )
     */
    public static void saveRequestContext(HttpServletRequest request) {
        ThreadContext.addData("cookies", getAllCookies(request));
        try {
            ThreadContext.addData("parameters", RequestUtil.getQueryParameters(request));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed saving request context");
        }
        Locale locale = resolveLocale(request);
        ThreadContext.addData(LABELS_KEY, LocaleConfig.getLabels(locale));
        ThreadContext.addData(LANG_KEY, locale.getLanguage());
        ThreadContext.addData(LOCATION_KEY, getLocation(request));
        ThreadContext.addData(IS_DEV_KEY, appConfig().babbage().isDevEnvironment());
        ThreadContext.addData(IS_PUBLISHING, appConfig().babbage().isPublishing());
    }

    public static void clearContext() {
        ThreadContext.clear();
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static Map<String, String> getAllCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookiesMap = new HashMap<>();
        if (cookies == null) {
            return cookiesMap;
        }
        for (Cookie cookie : cookies) {
            cookiesMap.put(cookie.getName(), cookie.getValue());
        }
        return cookiesMap;
    }

    /**
     * Resolves locale using cookie, if cookie not available falls back to sub domain name ( e.g. cy.ons.gov.uk )
     *
     * @param request
     * @return language code
     */
    private static Locale resolveLocale(HttpServletRequest request) {

        //Decide language from cookie first, if not there check subdomain
        String languageSegment = getCookieValue(request, LANG_KEY);
        if (StringUtils.isEmpty(languageSegment)) {
            languageSegment = request.getServerName();
        } else {
            languageSegment += ".";
        }

        for (Locale supportedLanguage : LocaleConfig.getSupportedLanguages()) {
            if (StringUtils.startsWithIgnoreCase(languageSegment, supportedLanguage.getLanguage() + ".")) {
                return supportedLanguage;
            }
        }
        return LocaleConfig.getDefaultLocale();
    }

    public static Location getLocation(HttpServletRequest request) {
        String hostName = request.getServerName();
        int port = request.getServerPort();
        String pathName = StringUtils.removeEnd(request.getRequestURI(), "/");
        Location location = new Location();
        location.setHost(hostName + ":" + port);
        location.setHostname(hostName);
        location.setPathname(pathName);
        return location;
    }

    public static String[] getParams(HttpServletRequest request, String name) {
        return request.getParameterValues(name);
    }

    public static String getParam(HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    /**
     * gets request parameter, if value is empty returns given default value
     *
     * @param request
     * @param name
     * @param defaultValue
     * @return
     */
    public static String getParam(HttpServletRequest request, String name, String defaultValue) {
        String param = getParam(request, name);
        if (isEmpty(param)) {
            return defaultValue;
        }
        return param;
    }


    /**
     * Extracts GET parameters from query string
     * <p/>
     * This method matches parameters to query string, if parameters is in query string it is return in the list of parameters.
     * <p/>
     * Note that a post parameters with the same name might also be included. There should not be parameters with same names in both get and post parameters if not wanted to be extracted
     */
    public static Map<String, String[]> getQueryParameters(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, String[]> queryParameters = new TreeMap<>();

        if (request == null || request.getQueryString() == null ||
                request.getQueryString().length() == 0) {
            return queryParameters;
        }

        String queryString = URLDecoder.decode(request.getQueryString(), StandardCharsets.UTF_8.name());

        if (StringUtils.isEmpty(queryString)) {
            return queryParameters;
        }

        String[] parameters = queryString.split("&");

        for (String parameter : parameters) {
            String[] keyValuePair = parameter.split("=");
            String[] values = queryParameters.get(keyValuePair[0]);
            values = ArrayUtils.add(values, keyValuePair.length == 1 ? "" : keyValuePair[1]); //length is one if no value is available.
            queryParameters.put(keyValuePair[0], values);
        }
        return queryParameters;
    }

    /*Converts given request parameters to a get query string*/
    public static String toQueryString(Map<String, String[]> parameters) {
        if(parameters == null) {
            return "";
        }

        Iterator<Map.Entry<String, String[]>> iterator = parameters.entrySet().iterator();
        StringBuilder builder = new StringBuilder();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> next = iterator.next();
            if(next.getValue() == null) {
                builder.append(next.getKey()).append("&");
                continue;
            }
            for (int i = 0; i < next.getValue().length; i++) {
                String[] values = next.getValue();
                builder.append(next.getKey()).append("=").append(values[i]).append("&");
            }
        }

        return builder.toString();
    }


    /**
     * Current location information to be extracted from HTTP request
     */
    public static class Location {
        /**
         * Hostname and port of the current url
         */
        private String host;
        /**
         * Hostname of the current url
         */
        private String hostname;

        /**
         * Path name of the current url with no trailing slash,  empty if root path
         */
        private String pathname;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getPathname() {
            return pathname;
        }

        public void setPathname(String pathname) {
            this.pathname = pathname;
        }
    }
}
