package com.github.onsdigital.babbage.search.external.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedHashMap;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

public class UserInterestUpdateRequest extends AbstractSearchRequest {

    private final String term;
    private final Sentiment sentiment;

    public UserInterestUpdateRequest(HttpServletRequest babbageRequest, String host) {
        super(babbageRequest, host);
        this.term = extractSearchTerm(babbageRequest);
        this.sentiment = Sentiment.POSITIVE;
    }

    public UserInterestUpdateRequest(HttpServletRequest babbageRequest, String host, Sentiment sentiment) {
        super(babbageRequest, host);
        this.term = extractSearchTerm(babbageRequest);
        this.sentiment = sentiment;
    }

    public LinkedHashMap<String, Object> update() throws IOException {
        String entityString = super.execute();
        LinkedHashMap<String, Object> data = MAPPER.readValue(entityString, new TypeReference<LinkedHashMap<String, Object>>() {
        });
        return data;
    }

    @Override
    public HttpRequestBase generateRequest() {
        // Build query url
        String path = this.getQueryUrl();
        System.out.println(String.format("Recommend request to %s", path));

        HttpPost post = new HttpPost(path);

        return post;
    }

    private final String getQueryUrl() {
        String path = new StringBuilder(super.host)
                .append(Endpoints.RECOMMEND.path)
                .append("/")
                .append(this.sentiment.sentiment)
                .append("/")
                .append(this.term)
                .toString();
        return path;
    }

    public enum Sentiment {
        POSITIVE("positive"),
        NEGATIVE("negative");

        String sentiment;

        Sentiment(String sentiment) {
            this.sentiment = sentiment;
        }
    }
}

enum UserInterestRequestParameters {
    USER_ID("_ga"),
    SESSION_ID("_gid");

    String parameter;

    UserInterestRequestParameters(String parameter) {
        this.parameter = parameter;
    }
}
