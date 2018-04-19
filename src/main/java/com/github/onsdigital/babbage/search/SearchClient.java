package com.github.onsdigital.babbage.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.*;

/**
 * @author sullid (David Sullivan) on 17/04/2018
 * @project babbage
 *
 * A single search client to replace all search functionality in Babbage with an external HTTP
 * request to a dedicated search service.
 */
public class SearchClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String TEST_ADDRESS = "http://localhost:5000/";

    private static SearchClient INSTANCE = new SearchClient(TEST_ADDRESS);

    private final String host;

    private SearchClient(String host) {
        this.host = host;
    }

    public static SearchClient getInstance() {
        return INSTANCE;
    }

    private HttpPost post(HttpServletRequest request) throws UnsupportedEncodingException {
        String searchTerm = extractSearchTerm(request);
        Set<TypeFilter> typeFilters = extractSelectedFilters(request, TypeFilter.getAllFilters(), false);
        int page = extractPage(request);
        int pageSize = extractSize(request);

        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair("page", String.valueOf(page)));
        postParams.add(new BasicNameValuePair("size", String.valueOf(pageSize)));

        for (TypeFilter typeFilter : typeFilters) {
            for (ContentType contentType : typeFilter.getTypes()) {
                postParams.add(new BasicNameValuePair("filter", contentType.name()));
            }
        }

        String path = this.getQueryUrl(searchTerm);

        HttpPost post = new HttpPost(path);
        post.setEntity(new UrlEncodedFormEntity(postParams, CharEncoding.UTF_8));

        return post;
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest request) throws IOException {
        HttpPost post = this.post(request);

        Cookie[] cookies = request.getCookies();

        HttpClient client;

        if (null != cookies) {
            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : cookies) {
                BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                clientCookie.setDomain(request.getServerName());
                clientCookie.setPath(request.getContextPath());
                cookieStore.addCookie(clientCookie);
            }
            client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        } else {
            client = HttpClientBuilder.create().build();
        }

        try (CloseableResponse response = new CloseableResponse(client.execute(post))) {
            int statusCode = response.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String entityString = EntityUtils.toString(entity);
                LinkedHashMap<String, SearchResult> data = MAPPER.readValue(entityString, new TypeReference<LinkedHashMap<String, SearchResult>>() {
                });

                return data;
            } else {
                throw new IOException(String.format("Got status code %d from search client", statusCode));
            }
        }
    }

    private String getQueryUrl(String searchTerm) throws UnsupportedEncodingException {
        String path = this.host + String.format("search/ons?q=%s", URLEncoder.encode(searchTerm, CharEncoding.UTF_8));
        return path;
    }

    public String getHost() {
        return this.host;
    }

    private static class CloseableResponse implements AutoCloseable {

        private final HttpResponse response;

        public CloseableResponse(HttpResponse response) {
            this.response = response;
        }

        public int getStatusCode() {
            return this.response.getStatusLine().getStatusCode();
        }

        public HttpEntity getEntity() {
            return this.response.getEntity();
        }

        @Override
        public void close() throws IOException {
            EntityUtils.consume(this.response.getEntity());
        }
    }
}
