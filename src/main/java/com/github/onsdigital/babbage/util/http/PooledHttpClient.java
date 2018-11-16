package com.github.onsdigital.babbage.util.http;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.onsdigital.babbage.logging.LogEvent.logEvent;

/**
 * Created by bren on 22/07/15.
 * <p/>
 * http client to a single host with connection pool and  cache functionality.
 */
//TODO: SSL support for https? not needed currently, configure java for ssl
//Add post,put,etc. functionality if needed
public class PooledHttpClient {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final PoolingHttpClientConnectionManager connectionManager;
    private final CloseableHttpClient httpClient;
    private final IdleConnectionMonitorThread monitorThread;
    private final URI HOST;

    public PooledHttpClient(String host, ClientConfiguration configuration) {
        HOST = resolveHostUri(host);
        this.connectionManager = new PoolingHttpClientConnectionManager();
        HttpClientBuilder customClientBuilder = HttpClients.custom();
        configure(customClientBuilder, configuration);
        httpClient = customClientBuilder.setConnectionManager(connectionManager)
                .build();
        this.monitorThread = new IdleConnectionMonitorThread(connectionManager);
        this.monitorThread.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    private void configure(HttpClientBuilder customClientBuilder, ClientConfiguration configuration) {
        Integer connectionNumber = configuration.getMaxTotalConnection();
        if (connectionNumber != null) {
            connectionManager.setMaxTotal(connectionNumber);
            connectionManager.setDefaultMaxPerRoute(connectionNumber);
        }
        if (configuration.isDisableRedirectHandling()) {
            customClientBuilder.disableRedirectHandling();
        }
    }

    private URI resolveHostUri(String host) {
        URI givenHost = URI.create(host);
        URIBuilder builder = new URIBuilder();
        if (StringUtils.startsWithIgnoreCase(host, "http")) {
            builder.setScheme(givenHost.getScheme());
            builder.setHost(givenHost.getHost());
            builder.setPort(givenHost.getPort());
            builder.setPath(givenHost.getPath());
            builder.setUserInfo(givenHost.getUserInfo());
        } else {
            builder.setScheme("http");
            builder.setHost(host);
        }
        try {
            return builder.build();
        } catch (URISyntaxException e) {
            logEvent(e).error("error building URI object");
            throw new RuntimeException(e);
        }
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

        logEvent().httpGET()
                .uri(path)
                .requestID(request)
                .requestParam(queryParameters)
                .debug("making HTTP request");

        return validate(httpClient.execute(request));
    }


    public CloseableHttpResponse sendDelete(String path, Map<String, String> headers, List<NameValuePair> queryParameters) throws IOException {
        URI uri = buildGetUri(path, queryParameters);
        HttpDelete request = new HttpDelete(uri);
        addHeaders(headers, request);

        logEvent().httpDELETE()
                .uri(path)
                .requestID(request)
                .requestParam(queryParameters)
                .debug("making HTTP request");

        return validate(httpClient.execute(request));
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
            request.setEntity(new UrlEncodedFormEntity(postParameters, Charsets.UTF_8));
        }

        logEvent().httpGET()
                .uri(path)
                .requestID(request)
                .requestParam(postParameters)
                .debug("making HTTP request");

        return validate(httpClient.execute(request));
    }

    public CloseableHttpResponse sendPost(String path, Map<String, String> headers, String content, String charset) throws IOException {
        URI uri = buildPath(path);
        HttpPost request = new HttpPost(uri);
        addHeaders(headers, request);

        request.setEntity(new StringEntity(content, charset));

        logEvent().httpGET()
                .uri(path)
                .requestID(request)
                .debug("making HTTP request");

        return validate(httpClient.execute(request));
    }

    private void addHeaders(Map<String, String> headers, HttpRequestBase request) {
        request.addHeader(REQUEST_ID_HEADER, UUID.randomUUID().toString());

        if (headers != null) {
            Iterator<Map.Entry<String, String>> headerIterator = headers.entrySet().iterator();
            while (headerIterator.hasNext()) {
                Map.Entry<String, String> next = headerIterator.next();
                request.addHeader(next.getKey(), next.getValue());
            }

        }
    }

    public void shutdown() throws IOException {
        logEvent()
                .host(HOST != null ? HOST.toString() : "")
                .info("closing connection pool");

        httpClient.close();

        logEvent()
                .host(HOST != null ? HOST.toString() : "")
                .info("successfully closed connection pool");

        monitorThread.shutdown();
    }

    private URI buildPath(String path) {
        URIBuilder uriBuilder = newUriBuilder(path);
        try {
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            logEvent(e).host(HOST != null ? HOST.toString() : "").uri(path).error("error building uri");
            throw new RuntimeException("Invalid uri! " + HOST + path);
        }
    }

    private URIBuilder newUriBuilder(String path) {
        URIBuilder uriBuilder = new URIBuilder(HOST);
        uriBuilder.setPath((uriBuilder.getPath() + "/" + path).replaceAll("//+", "/"));
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
            logEvent(e).host(HOST != null ? HOST.toString() : "").uri(path).error("error building uri");
            throw new RuntimeException("Invalid uri! " + HOST + path);
        }
    }

    /**
     * Throws appropriate errors if response is not successful
     */
    private CloseableHttpResponse validate(CloseableHttpResponse response) throws ClientProtocolException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() > 302) {
            String errorMessage = getErrorMessage(entity);
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    errorMessage == null ? statusLine.getReasonPhrase() : errorMessage);
        }
        if (entity == null) {
            logEvent().error("expected http response entity but response contains no content");
            throw new ClientProtocolException("Response contains no content");
        }

        return response;
    }

    private String getErrorMessage(HttpEntity entity) {
        try {
            String s = EntityUtils.toString(entity);
            return s;
        } catch (Exception e) {
            logEvent(e).error("error attempting to read HTTPEntity as string");
        }
        return null;
    }

    //Based on tuorial code on https://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e393

    //Http client already tests connection to see if it is stale before making a request, but documents suggests using monitor thread since it is 100% reliable
    private class IdleConnectionMonitorThread extends Thread {

        private boolean shutdown;
        private HttpClientConnectionManager connMgr;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            logEvent().info("running connection pool monitor");
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections every 5 seconds
                        connMgr.closeExpiredConnections();
                        // Close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(60, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                logEvent(ex).error("connection pool monitor failed");
            }
        }

        public void shutdown() {
            logEvent().info("shutting down connection pool monitor");
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }

    }


    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            try {
                if (httpClient != null) {
                    shutdown();
                }
            } catch (IOException e) {
                logEvent(e).host(HOST != null ? HOST.toString() : "")
                        .error("exception while attempting to shutdown HTTP client");
            }
        }
    }
}
