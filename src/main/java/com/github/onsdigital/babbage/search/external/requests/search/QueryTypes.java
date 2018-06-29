package com.github.onsdigital.babbage.search.external.requests.search;

public enum QueryTypes {
    CONTENT("content", "result"),
    TYPE_COUNTS("counts", "counts"),
    FEATURED("featured", "featuredResult");

    private String queryType;
    private String resultKey;

    QueryTypes(String queryType, String resultKey) {
        this.queryType = queryType;
        this.resultKey = resultKey;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getResultKey() {
        return resultKey;
    }
}
