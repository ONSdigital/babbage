package com.github.onsdigital.babbage.api.endpoint.search;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.babbage.api.util.SearchUtils;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.ONSSearchResponse;
import com.github.onsdigital.babbage.search.helpers.SearchHelper;
import com.github.onsdigital.babbage.search.helpers.SearchRequestHelper;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import com.github.onsdigital.babbage.search.model.field.Field;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.onsdigital.babbage.api.util.SearchUtils.buildBaseListQuery;
import static com.github.onsdigital.babbage.api.util.SearchUtils.buildListQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.firstLetterCounts;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.onsQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;
import static com.github.onsdigital.babbage.util.RequestUtil.getParam;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by bren on 19/11/15.
 */
@Api
public class AtoZ {

    @GET
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String firstLetter = getFirstLetter(request);
        ONSQuery countsByFirstLetter = firstLetterCounts(onsQuery(buildBaseListQuery(extractSearchTerm(request)), filterLatest()).types(ContentType.bulletin));
        ONSSearchResponse countResults = SearchHelper.search(countsByFirstLetter);
        Map<String, SearchResult> results;
        Long count = countResults.getResult().getDocCounts().get(firstLetter);

        if (isNotEmpty(firstLetter) && (count == null)) {//no result for selected letter
            //search all not just that letter
            results = list(request, null);
        } else {
            results = list(request, firstLetter);
        }
        results.put("counts", countResults.getResult());

        SearchUtils.buildResponse(request, getClass().getSimpleName(), results).apply(request, response);

    }

    private LinkedHashMap<String, SearchResult> list(HttpServletRequest request, String firstLetter) throws IOException {
        return SearchUtils.searchAll(request, queries(request, firstLetter));
    }

    private SearchQueries queries(HttpServletRequest request, String firstLetter) {
        return () -> toList(
                buildListQuery(request, filters(request, firstLetter)).types(ContentType.bulletin).sortBy(SortBy.first_letter)
        );
    }

    private SearchFilter filters(HttpServletRequest request, String firstLetter) {
        return (query) -> {
            query.filter(termQuery(Field.latestRelease.fieldName(), true));
            if (isNotEmpty(firstLetter)) {
                query.filter(termQuery(Field.title_first_letter.fieldName(), firstLetter));
            }
        };

    }

    private SearchFilter filterLatest() {
        return (query) -> query.filter(termQuery(Field.latestRelease.fieldName(), true));
    }

    private String getFirstLetter(HttpServletRequest request) {
        String prefix = StringUtils.trim(getParam(request, "az"));
        if (!StringUtils.isEmpty(prefix)) {
            return prefix.toLowerCase();
        }
        return null;

    }

}
