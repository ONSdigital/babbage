package com.github.onsdigital.babbage.util.http;

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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by ann on 22/11/22
 * <p/>
 * http client to a single host with connection pool and cache functionality.
 */
public class CacheHttpClient extends BabbageHttpClient {
    protected final CloseableHttpClient httpClient;
    private final HttpCacheContext context;

    public CacheHttpClient(String host, ClientConfiguration configuration) {
        super(host, configuration);

        CachingHttpClientBuilder cacheClientBuilder = CachingHttpClients.custom();
        CacheConfig cacheConfig = CacheConfig.custom()
                .setMaxCacheEntries(3000)
                .setMaxObjectSize(50000) // 10MB
                .build();

        this.httpClient = cacheClientBuilder
                .setCacheConfig(cacheConfig)
                .build();

        this.context = HttpCacheContext.create();



    }

    /**
     * @param path            path, should not contain any query string, only path info
     * @param headers         key-value map to to be added to request as headers
     * @param queryParameters query parameters to be sent as get query string
     * @return
     * @throws IOException             All exceptions thrown are IOException implementations
     * @throws ClientProtocolException for protocol related exceptions, HttpResponseExceptions are a subclass of this exception type
     * @throws HttpResponseException   exception for http status code > 300, HttpResponseException is a subclass of IOException
     *                                 catch HttpResponseException for  status code
     */
    public CloseableHttpResponse sendGet(String path, Map<String, String> headers, List<NameValuePair> queryParameters) throws IOException {
        URI uri = buildGetUri(path, queryParameters);
        HttpGet request = new HttpGet(uri);
        addHeaders(headers, request);

        CloseableHttpResponse response = executeRequest(request);
        return validateResponse(response);
    }

    private CloseableHttpResponse executeRequest(HttpRequestBase request) throws IOException {
        info().beginHTTP(request).log("PooledHttpClient executing request");
        CloseableHttpResponse response = httpClient.execute(request);
        CacheResponseStatus responseStatus = context.getCacheResponseStatus();
        System.out.println("responseStatus " + responseStatus);

        info().endHTTP(request, response).log("PooledHttpClient execute request completed");
        return response;
    }

    private void addHeaders(Map<String, String> headers, HttpRequestBase request) {
        if (headers != null) {

            for (Map.Entry<String, String> next : headers.entrySet()) {
                request.addHeader(next.getKey(), next.getValue());
            }

        }
    }

    private URIBuilder newUriBuilder(String path) {
        URIBuilder uriBuilder = new URIBuilder(host);
        String fullPath = "/" + path;
        String prefix = uriBuilder.getPath();
        if (prefix != null) {
            fullPath = prefix + fullPath;
        }
        uriBuilder.setPath(fullPath.replaceAll("//+", "/"));

        return uriBuilder;
    }

    URI buildGetUri(String path, List<NameValuePair> queryParameters) {
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

    /**
     * Throws appropriate errors if response is not successful
     */
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
            String s = EntityUtils.toString(entity);
            return s;
        } catch (Exception e) {
            error().exception(e).log("Failed reading content service:");
        }
        return null;
    }


}