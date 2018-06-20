package com.github.onsdigital.babbage.search.external.requests;

import javax.servlet.http.HttpServletRequest;

public class UserPageInterestUpdateRequest extends UserInterestUpdateRequest {

    private final String pageUri;

    public UserPageInterestUpdateRequest(HttpServletRequest babbageRequest, String host, String pageUri) {
        super(babbageRequest, host);
        if (!pageUri.startsWith("/")) {
            pageUri = "/" + pageUri;
        }
        this.pageUri = pageUri;
    }

    @Override
    protected String getQueryUrl() {
        String path = new StringBuilder(super.host)
                .append(Endpoints.RECOMMEND.path)
                .append("/page")
                .append(this.pageUri)
                .toString();
        return path;
    }
}
