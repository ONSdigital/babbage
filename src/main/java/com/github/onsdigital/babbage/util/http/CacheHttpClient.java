package com.github.onsdigital.babbage.util.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by ann on 22/11/22
 * <p/>
 * http client to a single host with connection pool and cache functionality.
 */
public class CacheHttpClient extends PooledHttpClient {
    protected final CloseableHttpClient httpClient;

    private static final String TAXONOMY_ENDPOINT = "/taxonomy";
    private static final String NAVIGATION_ENDPOINT = "/navigation";

    public CacheHttpClient(String host, ClientConfiguration configuration) {
        super(host, configuration);
        CachingHttpClientBuilder cacheClientBuilder = CachingHttpClients.custom();
        CacheConfig cacheConfig = CacheConfig.custom()
                .setMaxCacheEntries(appConfig().babbage().getMaxCacheEntries())
                .setMaxObjectSize(appConfig().babbage().getMaxCacheObjectSize())
                .build();

        this.httpClient = cacheClientBuilder
                .setCacheConfig(cacheConfig)
                .build();
    }

    public CloseableHttpResponse sendGet(String path, Map<String, String> headers, List<NameValuePair> queryParameters) throws IOException {
        URI uri = buildGetUri(path, queryParameters);
        HttpGet request = new HttpGet(uri);
        addHeaders(headers, request);
        CloseableHttpResponse response = null;
        try {
            response = executeRequest(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return validateResponse(response);
    }

    private CloseableHttpResponse executeRequest(HttpRequestBase request) throws IOException {
        HttpCacheContext context = HttpCacheContext.create();
        info().beginHTTP(request).log("CacheHttpClient executing request");
        CloseableHttpResponse response = httpClient.execute(request,context);
        if (appConfig().babbage().isDevEnvironment()) {
            validateCache(request, context, response);
        }
        info().endHTTP(request, response).log("CacheHttpClient execute request completed");
        return response;
    }

    private static void validateCache(HttpRequestBase request, HttpCacheContext context, CloseableHttpResponse response) {
        try {
            CacheResponseStatus responseStatus = context.getCacheResponseStatus();
            switch (responseStatus) {
                case CACHE_HIT:
                    info().endHTTP(request, response).log(responseStatus + " A response was generated from the cache with no requests sent upstream" );
                    break;

                case CACHE_MODULE_RESPONSE:
                    info().endHTTP(request, response).log(responseStatus + " The response was generated directly by the caching module" );
                    break;

                case CACHE_MISS:
                    info().endHTTP(request, response).log(responseStatus + " The response came from an upstream server" );
                    break;

                case VALIDATED:
                    info().endHTTP(request, response).log(responseStatus + " The response was generated from the cache after validating the entry with the origin server" );
                    break;

            }
        } finally {
            info().endHTTP(request, response).log(Arrays.toString(response.getHeaders("Cache-Control")) + " the header cache-control" );
        }
    }

    private URIBuilder newUriBuilder(String path) throws URISyntaxException {
        URI cacheHost = host;
        if (appConfig().babbage().isNavigationEnabled() && Paths.get(path).getNameCount() == 1){
            path = NAVIGATION_ENDPOINT;
            cacheHost = URI.create(appConfig().babbage().getApiRouterURL());
        }
        URIBuilder uriBuilder = new URIBuilder(cacheHost);
        String fullPath = "/" + path;
        String prefix = uriBuilder.getPath();
        if (prefix != null) {
            fullPath = prefix + fullPath;
        }
        uriBuilder.setPath(fullPath.replaceAll("//+", "/"));
        return uriBuilder;
    }

    private URI buildGetUri(String path, List<NameValuePair> queryParameters) {
        try {
            URIBuilder uriBuilder = newUriBuilder(path);
            if (queryParameters != null) {
                uriBuilder.setParameters(queryParameters);
            }
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid uri! " + host + path);
        }
    }

    private void addHeaders(Map<String, String> headers, HttpRequestBase request) {
        if (headers != null) {
            Iterator<Map.Entry<String, String>> headerIterator = headers.entrySet().iterator();
            while (headerIterator.hasNext()) {
                Map.Entry<String, String> next = headerIterator.next();
                request.addHeader(next.getKey(), next.getValue());
            }
        }
    }

    private CloseableHttpResponse validateResponse(CloseableHttpResponse response)
            throws ClientProtocolException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();

        if (statusLine.getStatusCode() > 302) {
            String errorMessage = getErrorMessage(entity);
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    errorMessage == null ? statusLine.getReasonPhrase() : errorMessage);
        }

        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }

        return response;
    }

    private String getErrorMessage(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            error().exception(e).log("Failed reading content service:");
        }
        return null;
    }

}
