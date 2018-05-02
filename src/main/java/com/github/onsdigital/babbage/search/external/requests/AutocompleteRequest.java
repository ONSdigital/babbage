package com.github.onsdigital.babbage.search.external.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.onsdigital.babbage.search.model.Suggestions;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

public class AutocompleteRequest extends AbstractRequest {

    private final String searchTerm;

    public AutocompleteRequest(HttpServletRequest babbageRequest, String host) {
        super(babbageRequest, host);
        this.searchTerm = extractSearchTerm(babbageRequest);
    }

    @Override
    public HttpRequestBase generateRequest() throws IOException {
        HttpGet httpGet = new HttpGet(this.getAutocompleteUrl(this.searchTerm));
        return httpGet;
    }

    private String getAutocompleteUrl(String searchTerm) throws UnsupportedEncodingException {
        String path = new StringBuilder(this.host)
                .append(Endpoints.AUTOCOMPLETE.getQueryPath(searchTerm))
                .toString();
        return path;
    }

    public LinkedHashMap<String, LinkedHashMap<String, Suggestions>> autocomplete() throws IOException {
        long start = System.currentTimeMillis();

        String entityString = super.execute();
        LinkedHashMap<String, LinkedHashMap<String, Suggestions>> data = MAPPER.readValue(entityString, new TypeReference<LinkedHashMap<String, LinkedHashMap<String, Suggestions>>>() {
        });
        long end = System.currentTimeMillis();

        System.out.println(String.format("Autocomplete GET request took %d ms", (end - start)));

        return data;
    }
}
