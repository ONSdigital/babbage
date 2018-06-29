package com.github.onsdigital.babbage.search.external.requests.spelling;

import aQute.lib.io.IO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.search.external.Endpoint;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class SpellCheckerRequest extends AbstractSearchRequest<Map<String, Correction>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String searchTerm;

    public SpellCheckerRequest(HttpServletRequest babbageRequest, String host, String searchTerm) {
        super(babbageRequest, host);

        this.searchTerm = searchTerm;
    }

    protected URI queryUri() {
        URI uri = URI.create(Endpoint.SPELLING.getUri());
        return uri;
    }

    /**
     * Builds the content query url using the given search term.
     * @return URL to query external search service.
     */
    private String getQueryUrl() {
        URIBuilder ub = new URIBuilder(this.queryUri());
        ub.addParameter("q", this.searchTerm);
        return this.host + ub.toString();
    }

    @Override
    protected HttpRequestBase generateRequest() {
        String path = this.getQueryUrl();
        HttpGet get = new HttpGet(path);

        return get;
    }

    @Override
    public Map<String, Correction> call() throws IOException {
        String entityString = super.execute();

        Map<String, Correction> corrections = MAPPER.readValue(entityString, new TypeReference<Map<String, Correction>>(){});

        return corrections;
    }
}
