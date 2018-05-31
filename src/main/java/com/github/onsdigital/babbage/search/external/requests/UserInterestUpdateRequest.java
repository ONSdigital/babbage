package com.github.onsdigital.babbage.search.external.requests;

import org.apache.commons.lang3.CharEncoding;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;

public class UserInterestUpdateRequest extends AbstractSearchRequest {

    private static final String USER_ID_COOKIE = "_ga";

    private final String term;
    private final Sentiment sentiment;

    public UserInterestUpdateRequest(HttpServletRequest babbageRequest, String host, Sentiment sentiment) {
        super(babbageRequest, host);
        this.term = extractSearchTerm(babbageRequest);
        this.sentiment = sentiment;
    }

    @Override
    public HttpRequestBase generateRequest() throws IOException {
        CookieStore cookieStore = super.extractCookies();

        String userId = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals(USER_ID_COOKIE)) {
                userId = cookie.getValue();
                break;
            }
        }

        if (null == userId) {
            throw new IOException("GA cookie not found");
        }

        // Build form params
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair(UserInterestRequestParameters.USER_ID.parameter, userId));
        postParams.add(new BasicNameValuePair(UserInterestRequestParameters.TERM.parameter, this.term));
        postParams.add(new BasicNameValuePair(UserInterestRequestParameters.SENTIMENT.parameter, this.sentiment.sentiment));

        // Build query url
        String path = this.getQueryUrl();
        System.out.println(String.format("Recommend request to %s", path));

        HttpPost post = new HttpPost(path);
        // Set form params
        post.setEntity(new UrlEncodedFormEntity(postParams, CharEncoding.UTF_8));

        return post;
    }

    private final String getQueryUrl() {
        String path = new StringBuilder(super.host)
                .append(Endpoints.RECOMMEND.path)
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
    USER_ID("user_id"),
    TERM("term"),
    SENTIMENT("sentiment");

    String parameter;

    UserInterestRequestParameters(String parameter) {
        this.parameter = parameter;
    }
}
