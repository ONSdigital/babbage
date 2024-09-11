package com.github.onsdigital.babbage.util.http;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by bren on 22/07/15.
 * <p/>
 * http client to a single host with connection pool and  cache functionality.
 */
//TODO: SSL support for https? not needed currently, configure java for ssl
//Add post,put,etc. functionality if needed
public class PooledHttpClient extends BabbageHttpClient {

    public PooledHttpClient(String host, ClientConfiguration configuration) {
        super(host, configuration);
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


    public CloseableHttpResponse sendDelete(String path, Map<String, String> headers, List<NameValuePair> queryParameters) throws IOException {
        URI uri = buildGetUri(path, queryParameters);
        HttpDelete request = new HttpDelete(uri);
        addHeaders(headers, request);

        CloseableHttpResponse response = executeRequest(request);
        return validateResponse(response);
    }

    /**
     * @param path           path, should not contain any query string, only path info
     * @param headers        key-value map to to be added to request as headers
     * @param postParameters query parameters to be sent as get query string
     * @return
     * @throws IOException             All exceptions thrown are IOException implementations
     * @throws ClientProtocolException for protocol related exceptions, HttpResponseExceptions are a subclass of this exception type
     * @throws HttpResponseException   exception for http status code > 300, HttpResponseException is a subclass of IOException
     *                                 catch HttpResponseException for  status code
     */
    public CloseableHttpResponse sendPost(String path, Map<String, String> headers, List<NameValuePair> postParameters) throws IOException {
        URI uri = buildPath(path);
        HttpPost request = new HttpPost(uri);
        addHeaders(headers, request);
        if (postParameters != null) {
            request.setEntity(new UrlEncodedFormEntity(postParameters, StandardCharsets.UTF_8));
        }

        CloseableHttpResponse response = executeRequest(request);
        return validateResponse(response);
    }

    public CloseableHttpResponse sendPost(String path, Map<String, String> headers, String content, String charset) throws IOException {
        URI uri = buildPath(path);
        HttpPost request = new HttpPost(uri);
        addHeaders(headers, request);

        request.setEntity(new StringEntity(content,Charset.forName(charset)));

        CloseableHttpResponse response = executeRequest(request);
        return validateResponse(response);
    }

    private CloseableHttpResponse executeRequest(HttpUriRequestBase request) throws IOException {
        info().beginHTTP(request).log("PooledHttpClient executing request");

        CloseableHttpResponse response = httpClient.execute(request);

        info().endHTTP(request, response).log("PooledHttpClient execute request completed");
        return response;
    }

    private void addHeaders(Map<String, String> headers, HttpUriRequestBase request) {
        if (headers != null) {
            Iterator<Map.Entry<String, String>> headerIterator = headers.entrySet().iterator();
            while (headerIterator.hasNext()) {
                Map.Entry<String, String> next = headerIterator.next();
                request.addHeader(next.getKey(), next.getValue());
            }

        }
    }

    private URI buildPath(String path) {
        URIBuilder uriBuilder = newUriBuilder(path);
        try {
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid uri! " + host + path);
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

    /**
     * Throws appropriate errors if response is not successful
     */
    private CloseableHttpResponse validateResponse(CloseableHttpResponse response)
            throws ClientProtocolException {
        int statusCode = response.getCode();
        String reasonPhrase = response.getReasonPhrase();
        HttpEntity entity = response.getEntity();

        if (statusCode > 302) {
            String errorMessage = getErrorMessage(entity);
            throw new HttpResponseException(
                    statusCode,
                    errorMessage == null ? reasonPhrase : errorMessage);
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
