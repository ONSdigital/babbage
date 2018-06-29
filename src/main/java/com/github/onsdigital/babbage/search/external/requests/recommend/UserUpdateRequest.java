package com.github.onsdigital.babbage.search.external.requests.recommend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.babbage.search.external.Endpoint;
import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class UserUpdateRequest extends AbstractSearchRequest<Map<String, Object>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String pageUri;

    public UserUpdateRequest(HttpServletRequest babbageRequest, String host, String pageUri) {
        super(babbageRequest, host);

        this.pageUri = pageUri.startsWith("/") ? pageUri.substring(1) : pageUri;
    }

    final String queryString() {
        return Endpoint.RECOMMEND.getUri() + API.UPDATE_BY_PAGE.uri + this.pageUri;
    }

    /**
     * Builds the content query url using the given search term.
     * @return URL to query external search service.
     */
    private String getPageUpdateUrl() {
        return this.host + this.queryString();
    }

    @Override
    protected HttpRequestBase generateRequest() {
        // Build query url
        String path = this.getPageUpdateUrl();

        return new HttpPost(path);
    }

    @Override
    public Map<String, Object> call() throws Exception {
        String entityString = super.execute();

        Map<String, Object> result = MAPPER.readValue(entityString, new TypeReference<Map<String, Object>>() {});
        return result;
    }

    enum API {
        UPDATE_BY_PAGE("update/page/");

        String uri;

        API(String uri) {
            this.uri = uri;
        }
    }
}
