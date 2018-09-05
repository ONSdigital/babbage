package com.github.onsdigital.babbage.search.external.requests.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.external.SearchClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;

import javax.servlet.http.Cookie;
import java.net.HttpCookie;
import java.util.concurrent.Callable;

public abstract class AbstractSearchRequest<T> implements Callable<T> {

    protected static final String HOST = Configuration.SEARCH_SERVICE.getExternalSearchAddress();

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    private final Class<T> returnClass;
    private Cookie[] cookies;

    public AbstractSearchRequest(Class<T> returnClass) {
        this(returnClass, null);
    }

    public AbstractSearchRequest(Class<T> returnClass, Cookie[] cookies) {
        this.returnClass = returnClass;
        this.cookies = cookies;
    }

    /**
     * Abstract method for building/returning the target URI for HTTP requests
     * @return
     */
    public abstract URIBuilder targetUri();

    /**
     * Builds a simple HTTP GET request with the target URI
     * @return
     * @throws Exception
     */
    protected Request get() throws Exception {
        return SearchClient.getInstance().get(this.targetUri().toString());
    }

    /**
     * Builds a HTTP POST request with mime-type application/json
     * @return
     */
    protected Request post() throws Exception {
        return SearchClient.getInstance().post(this.targetUri().toString())
                .header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    }

    /**
     * Abstract method for executing requests
     * @return
     * @throws Exception
     */
    protected abstract Request getRequest() throws Exception;

    @Override
    public T call() throws Exception {

        Request request = this.getRequest();

        if (null != this.cookies) {
            for (Cookie cookie : this.cookies) {
                request = request.cookie(new HttpCookie(cookie.getName(), cookie.getValue()));
            }
        }

        String response = request.send().getContentAsString();
        return MAPPER.readValue(response, this.returnClass);
    }

}
