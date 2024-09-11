package com.github.onsdigital.babbage.content.client;

import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.github.onsdigital.babbage.util.http.CacheHttpClient;
import com.github.onsdigital.babbage.util.http.ClientConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by Ann on 11/22.
 * <p/>
 * Content service reads content from external server over http.
 * <p/>
 * Using Apache http client with connection pooling.
 */
public class ContentClientCache {
    private static final String BearerPrefix = "Bearer ";
    private static final String TOKEN_HEADER = "X-Florence-Token";

    private static CacheHttpClient client;
    private static ContentClientCache instance;
    private static final String TAXONOMY_ENDPOINT = "/taxonomy";
    private static final String NAVIGATION_ENDPOINT = "/navigation";

    public static ContentClientCache getInstance() {

        if (instance == null) {
            synchronized (ContentClientCache.class) {
                if (instance == null) {
                    info().log("initialising ContentClientCache instance");
                    instance = new ContentClientCache();
                    info().log("initialising CacheHttpClient for ContentClientCache instance");
                    client = new CacheHttpClient(appConfig().contentAPI().serverURL(), createConfiguration());
                }
            }
        }
        return instance;
    }

    protected static ClientConfiguration createConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxTotalConnection(appConfig().contentAPI().maxConnections());
        configuration.setDisableRedirectHandling(true);
        return configuration;
    }

    public ContentResponse getTaxonomy(Map<String, String[]> queryParameters) throws ContentReadException {
        return sendGet(TAXONOMY_ENDPOINT,getParameters(queryParameters));
    }
    public ContentResponse getNavigation(Map<String, String[]> queryParameters) throws ContentReadException {
        return sendGet(NAVIGATION_ENDPOINT,getParameters(queryParameters));
    }

    /**
     * @param path string path
     * @param getParameters query parameters
     * @return ContentResponse  response
     * @throws ContentReadException exceptions
     */
    ContentResponse sendGet(String path, List<NameValuePair> getParameters) throws ContentReadException {
        CloseableHttpResponse response = null;
        try {
            return new ContentResponse(client.sendGet(path, getHeaders(), getParameters));
        } catch (HttpResponseException e) {
            //noinspection deprecation
            IOUtils.closeQuietly(response);
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                info().data("uri", path).log("ContentClientCache requested uri not found");
                throw new ResourceNotFoundException(e.getMessage());
            }

            throw wrapException( e);

        } catch (IOException e) {
            //noinspection deprecation
            IOUtils.closeQuietly(response);
            info().data("uri", path).log("ContentClientCache "  + e.getMessage());
            throw wrapException( e);
        }
    }

    List<NameValuePair> getParameters(Map<String, String[]> parameters) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("lang", (String) ThreadContext.getData(RequestUtil.LANG_KEY)));
        nameValuePairs.addAll(toNameValuePair(parameters));
        return nameValuePairs;
    }

    private List<NameValuePair> toNameValuePair(Map<String, String[]> parameters) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        if (parameters != null) for (Entry<String, String[]> entry : parameters.entrySet()) {
            String[] values = entry.getValue();
            if (ArrayUtils.isEmpty(values)) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), null));
                continue;
            }
            for (String value : values) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), value));
            }
        }
        return nameValuePairs;
    }

    private ContentReadException wrapException(IOException e) {
        error().exception(e).log("ContentClientCache request returned error");
        return new ContentReadException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed reading from content service", e);
    }

    //Reads collection cookie saved in thread context
    private Map<String, String> getHeaders() {
        Map<String, String> cookies = (Map<String, String>) ThreadContext.getData("cookies");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", BearerPrefix+appConfig().babbage().getServiceAuthToken());
        if (cookies != null) {
            headers.put(TOKEN_HEADER, cookies.get("access_token"));
        }
        return headers;

    }
}



