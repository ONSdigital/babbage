package com.github.onsdigital.babbage.search.external.requests.recommend.requests;

import com.github.onsdigital.babbage.search.external.requests.base.AbstractSearchRequest;
import com.github.onsdigital.babbage.search.external.requests.recommend.models.UserSession;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpScheme;

import java.net.HttpCookie;

public class UpdateUserRecommendations extends AbstractSearchRequest<UserSession> {

    private static final String RECOMMEND_BY_PAGE_URI = "/recommend/update/page/";
    public static final String USER_ID_COOKIE = "_ga";
    public static final String SESSION_ID_COOKIE = "_gid";

    private final String pageUri;
    private final String userId;
    private final String sessionId;

    public UpdateUserRecommendations(String pageUri, String userId, String sessionId) {
        super(UserSession.class);
        this.pageUri = pageUri.startsWith("/") ? pageUri.substring(1) : pageUri;
        this.userId = userId;
        this.sessionId = sessionId;
    }

    @Override
    public URIBuilder targetUri() {
        String path = RECOMMEND_BY_PAGE_URI +
                this.pageUri;

        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(HttpScheme.HTTP.asString())
                .setHost(HOST)
                .setPath(path);

        return uriBuilder;
    }

    @Override
    protected Request getRequest() throws Exception {
        return super.post()
                .cookie(new HttpCookie(USER_ID_COOKIE, this.userId))
                .cookie(new HttpCookie(SESSION_ID_COOKIE, this.sessionId));
    }
}
