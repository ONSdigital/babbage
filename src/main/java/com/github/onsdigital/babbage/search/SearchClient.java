package com.github.onsdigital.babbage.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

/**
 * @author sullid (David Sullivan) on 17/04/2018
 * @project babbage
 *
 * A single search client to replace all search functionality in Babbage with an external HTTP
 * request to a dedicated search service.
 */
public class SearchClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String TEST_ADDRESS = "http://localhost:5000/search/ons?q=";

    private static SearchClient INSTANCE = new SearchClient(TEST_ADDRESS);

    private final String address;

    private SearchClient(String address) {
        this.address = address;
    }

    public static SearchClient getInstance() {
        return INSTANCE;
    }

    public LinkedHashMap<String, SearchResult> search(HttpServletRequest request) throws IOException {
        String searchTerm = extractSearchTerm(request);

        Cookie[] cookies = request.getCookies();

        HttpPost post = new HttpPost(this.getQueryUrl(searchTerm));

        CookieStore cookieStore = new BasicCookieStore();

        for (Cookie cookie : cookies) {
            BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
            clientCookie.setDomain(request.getServerName());
            clientCookie.setPath(request.getContextPath());
            cookieStore.addCookie(clientCookie);
        }

        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        String entityString = EntityUtils.toString(entity);
        LinkedHashMap<String, SearchResult> data = MAPPER.readValue(entityString, new TypeReference<LinkedHashMap<String, SearchResult>>() {
        });

        // Consume the entity
        EntityUtils.consume(entity);

        return data;
    }

    private String getQueryUrl(String searchTerm) {
        return this.getAddress() + URLEncoder.encode(searchTerm);
    }

    public String getAddress() {
        return address;
    }
}
