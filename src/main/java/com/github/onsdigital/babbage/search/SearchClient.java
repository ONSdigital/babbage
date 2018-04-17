package com.github.onsdigital.babbage.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.search.model.SearchResult;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

/**
 * @author sullid (David Sullivan) on 17/04/2018
 * @project babbage
 *
 * A single search client to replace all search functionality in Babbage with an external HTTP
 * request to a dedicated search service.
 */
public class SearchClient implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String TEST_ADDRESS = "http://localhost:5000/search/ons?q=";

    private static SearchClient INSTANCE = new SearchClient(TEST_ADDRESS);

    private final String address;

    private final CloseableHttpClient httpClient;

    private SearchClient(String address) {
        this.address = address;
        this.httpClient = HttpClients.createDefault();
    }

    public static SearchClient getInstance() {
        return INSTANCE;
    }

    public LinkedHashMap<String, SearchResult> search(String searchTerm) throws IOException {
        HttpPost post = new HttpPost(this.getQueryUrl(searchTerm));

        LinkedHashMap<String, SearchResult> data;

        try (CloseableHttpResponse response = this.httpClient.execute(post)) {
            HttpEntity entity = response.getEntity();
            String entityString = EntityUtils.toString(entity);
            data = MAPPER.readValue(entityString, new TypeReference<LinkedHashMap<String, SearchResult>>(){});
        }

        return data;
    }

    private String getQueryUrl(String searchTerm) {
        return this.getAddress() + URLEncoder.encode(searchTerm);
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void close() throws Exception {
        this.httpClient.close();
    }
}
