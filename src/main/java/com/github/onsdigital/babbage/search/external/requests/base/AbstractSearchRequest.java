package com.github.onsdigital.babbage.search.external.requests.base;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.Callable;

public abstract class AbstractSearchRequest<T> implements Callable<T> {

    protected final HttpServletRequest babbageRequest;
    protected final String host;

    public AbstractSearchRequest(HttpServletRequest babbageRequest, String host) {
        this.babbageRequest = babbageRequest;
        this.host = host.endsWith("/") ? host : host + "/";
    }

    protected abstract HttpRequestBase generateRequest() throws IOException;

    protected final String execute() throws IOException {
        CookieStore cookieStore = this.extractCookies();
        HttpRequestBase requestBase = this.generateRequest();

        try (CloseableHttpClient client = httpClient(cookieStore)) {
            HttpClientContext context = httpClientContext(cookieStore);
            try (CloseableHttpResponse response = client.execute(requestBase, context)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    String entityString = EntityUtils.toString(entity);

                    return entityString;
                } else {
                    throw new IOException(String.format("Got status code %d from search client: %s", statusCode, requestBase));
                }
            } finally {
                requestBase.releaseConnection();
            }
        }
    }

    protected final CookieStore extractCookies() {
        // Initialise empty cookie store
        CookieStore cookieStore = new BasicCookieStore();

        if (null != this.babbageRequest) {
            Cookie[] cookies = this.babbageRequest.getCookies();

            // Forward cookies
            if (null != cookies) {
                for (Cookie cookie : cookies) {
                    BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                    clientCookie.setDomain(this.babbageRequest.getServerName());
                    clientCookie.setPath(this.babbageRequest.getContextPath());
                    cookieStore.addCookie(clientCookie);
                }
            }
        }

        return cookieStore;
    }

    private static HttpClientContext httpClientContext(CookieStore cookieStore) {
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        return context;
    }

    private static CloseableHttpClient httpClient(CookieStore cookieStore) {
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();

        return client;
    }

}
