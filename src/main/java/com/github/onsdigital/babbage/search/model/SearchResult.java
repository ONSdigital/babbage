package com.github.onsdigital.babbage.search.model;

import com.github.onsdigital.babbage.paginator.Paginator;
import com.github.onsdigital.babbage.search.input.SortBy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bren on 07/09/15.
 */
public class SearchResult {

    private Long numberOfResults;
    private long took;
    private List<Map<String, Object>> results = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private Map<String,Long> docCounts = new LinkedHashMap<>();
    private Paginator paginator;
    private String sortBy;

    public SearchResult() {

    }

    public Long getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(long numberOfResults) {
        //do not set number of results if zero,so it does not appear in json
        if (numberOfResults > 0) {
            this.numberOfResults = numberOfResults;
        }
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, Object>> results) {
        this.results = results;
    }

    public Map<String, Long> getDocCounts() {
        return docCounts;
    }

    public void addResult(Map<String, Object> result) {
        results.add(result);
    }

    public void addDocCount(String key, long number) {
        if (docCounts == null) {
            docCounts = new LinkedHashMap<>();
        }
        docCounts.put(key, number);
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public long getTook() {
        return took;
    }

    public void setTook(long took) {
        this.took = took;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
