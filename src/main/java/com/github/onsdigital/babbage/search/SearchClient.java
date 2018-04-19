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
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
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

        String path = this.getQueryUrl(searchTerm, typeFilters, page);
        return new HttpPost(path);
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

    private String getQueryUrl(String searchTerm, Set<TypeFilter> typeFilters, int page) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(String.format("search/ons?q=%s", URLEncoder.encode(searchTerm, CharEncoding.UTF_8)));

        sb.append(String.format("&page=%d", page));
        for (TypeFilter typeFilter : typeFilters) {
            for (ContentType contentType : typeFilter.getTypes()) {
                sb.append(String.format("&filter=%s", URLEncoder.encode(contentType.name(), CharEncoding.UTF_8)));
            }
        }
        return this.getHost() + sb.toString();
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
