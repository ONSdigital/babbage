package com.github.onsdigital.babbage.api.util;


import com.github.onsdigital.babbage.search.ElasticSearchClient;
import com.github.onsdigital.babbage.search.builders.ONSFilterBuilders;
import com.github.onsdigital.babbage.search.builders.ONSQueryBuilders;
import com.github.onsdigital.babbage.search.helpers.ONSQuery;
import com.github.onsdigital.babbage.search.helpers.ONSSearchResponse;
import com.github.onsdigital.babbage.search.helpers.SearchHelper;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.SearchResult;
import com.github.onsdigital.babbage.search.model.field.Field;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.advancedSearchQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.contentQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.departmentQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.listQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.onsQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.typeBoostedQuery;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractPage;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSearchTerm;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSelectedFilters;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSize;
import static com.github.onsdigital.babbage.search.helpers.SearchRequestHelper.extractSortBy;
import static com.github.onsdigital.babbage.search.input.TypeFilter.contentTypes;
import static com.github.onsdigital.babbage.search.model.field.Field.cdid;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.search.suggest.SuggestBuilders.phraseSuggestion;

//Commons search functionality for search, search publications and search data pages.

public class SearchUtils {

    private static final String DEPARTMENTS_INDEX = "departments";

    /*
     * Performs search for requested search term against filtered content types and counts contents types.
     * Content results are serialised into json with key "result" and document counts are serialised as "counts".
     */
    public static Map<String, SearchResult> search(String searchTerm, SearchQueries queries, boolean searchDepartments) throws IOException {
        Map<String, SearchResult> results = null;
        if (searchTerm != null) {
            results = searchAll(queries);
            if (searchDepartments) {
                searchDeparments(searchTerm, results);
            }
        }
        return results;
    }

    // Search time series for given search term and if found return the URI to redirect to.
    public static String getRedirectWhenTimeSeries(HttpServletRequest request, String searchTerm) {
        String timeSeriesUri = null;
        if (searchTerm != null && !isFiltered(request)) { //only search for time series when new search made through search input
            //search time series by cdid, return url of the time series page if found
            timeSeriesUri = searchTimeSeriesUri(searchTerm);
            if (timeSeriesUri != null) {
                try {
                    timeSeriesUri = new URIBuilder(timeSeriesUri)
                            .addParameter("referrer", "search")
                            .addParameter("searchTerm", searchTerm.toLowerCase())
                            .build().toString();
                } catch (URISyntaxException e) {
                    error().exception(e).log("unable to encode referrer in timeSeriesUri");
                }
            }
        }
        return timeSeriesUri;
    }

    // Execute the given search queries with internal service.
    public static Map<String, SearchResult> searchAll(SearchQueries searchQueries) {
        List<ONSQuery> queries = searchQueries.buildQueries();
        return doSearch(queries);
    }

    public static ONSQuery buildSearchQuery(HttpServletRequest request, String searchTerm, Set<TypeFilter> defaultFilters) {
        boolean advanced = isAdvancedSearchQuery(searchTerm);
        SortBy sortBy = extractSortBy(request, SortBy.relevance);
        QueryBuilder contentQuery;
        contentQuery = advanced ? advancedSearchQuery(searchTerm) : contentQuery(searchTerm);
        ONSQuery query = buildONSQuery(request, contentQuery, sortBy, null, contentTypes(extractSelectedFilters(request, defaultFilters, false)));
        if (!advanced) {
            query.suggest(phraseSuggestion("search_suggest").field(Field.title_no_synonym_no_stem.fieldName()).text(searchTerm));
        }
        return query;
    }

    private static boolean isAdvancedSearchQuery(String searchTerm) {
        return StringUtils.containsAny(searchTerm, '+', '|', '-', '"', '*', '~');
    }

    public static ONSQuery buildAdvancedSearchQuery(HttpServletRequest request, String searchTerm, Set<TypeFilter> defaultFilters) {
        SortBy sortBy = extractSortBy(request, SortBy.relevance);
        ONSQuery query = buildONSQuery(request, advancedSearchQuery(searchTerm), sortBy, null, contentTypes(extractSelectedFilters(request, defaultFilters, false)));
        return query;
    }

    public static ONSQuery buildListQuery(HttpServletRequest request, Set<TypeFilter> defaultFilters, SearchFilter filter, Boolean ignoreFilters) {
        return buildListQuery(request, defaultFilters, filter, null, ignoreFilters);
    }

    public static ONSQuery buildListQuery(HttpServletRequest request, SearchFilter filter) {
        return buildListQuery(request, null, filter, null, false);
    }

    public static ONSQuery buildListQuery(HttpServletRequest request, SearchFilter filter, SortBy defaultSort) {
        return buildListQuery(request, null, filter, defaultSort, false);
    }

    public static ONSQuery buildListQuery(HttpServletRequest request) {
        return buildListQuery(request, null, null, null, false);
    }

    public static ONSQuery buildListQuery(HttpServletRequest request, Set<TypeFilter> defaultFilters) {
        return buildListQuery(request, defaultFilters, null, null, false);
    }


    private static ONSQuery buildListQuery(HttpServletRequest request, Set<TypeFilter> defaultFilters, SearchFilter filter, SortBy defaultSort, Boolean ignoreFilters) {
        String searchTerm = extractSearchTerm(request);
        boolean hasSearchTerm = isNotEmpty(searchTerm);
        SortBy sortBy;
        if (hasSearchTerm) {
            sortBy = SortBy.relevance;
        } else {
            sortBy = defaultSort == null ? SortBy.release_date : defaultSort;
        }

        QueryBuilder query = buildBaseListQuery(searchTerm);
        ContentType[] contentTypes = defaultFilters == null ? null : contentTypes(extractSelectedFilters(request, defaultFilters, ignoreFilters));
        return buildONSQuery(request, query, sortBy, filter, contentTypes);
    }

    public static QueryBuilder buildBaseListQuery(String searchTerm) {
        QueryBuilder query;
        if (isNotEmpty(searchTerm)) {
            query = listQuery(searchTerm);
        } else {
            query = matchAllQuery();
        }
        return query;
    }

    private static ONSQuery buildONSQuery(HttpServletRequest request, QueryBuilder builder, SortBy defaultSort, SearchFilter filter, ContentType... contentTypes) {
        int page = extractPage(request);
        SortBy sort = extractSortBy(request, defaultSort);
        return onsQuery(typeBoostedQuery(builder), filter)
                .types(contentTypes)
                .page(page)
                .sortBy(sort)
                .name("result")
                .size(extractSize(request))
                .highlight(true);
    }

    static Map<String, SearchResult> doSearch(List<ONSQuery> searchQueries) {
        List<ONSSearchResponse> responseList = SearchHelper.searchMultiple(searchQueries);
        Map<String, SearchResult> results = new LinkedHashMap<>();
        for (int i = 0; i < responseList.size(); i++) {
            ONSSearchResponse response = responseList.get(i);
            results.put(searchQueries.get(i).name(), response.getResult());

        }
        return results;
    }

    private static String searchTimeSeriesUri(String searchTerm) {
        ONSSearchResponse search = SearchHelper.
                search(onsQuery(boolQuery().filter(termQuery(cdid.fieldName(), searchTerm.toLowerCase())))
                        .types(ContentType.timeseries)
                        .sortBy(SortBy.release_date)
                        .size(1)
                        .fetchFields(Field.uri));
        if (search.getNumberOfResults() == 0) {
            return null;
        }
        Map<String, Object> timeSeries = search.getResult().getResults().iterator().next();
        return (String) timeSeries.get(Field.uri.fieldName());
    }

    private static void searchDeparments(String searchTerm, Map<String, SearchResult> results) {
        QueryBuilder departmentsQuery = departmentQuery(searchTerm);
        SearchRequestBuilder departmentsSearch = ElasticSearchClient.getElasticsearchClient().prepareSearch(DEPARTMENTS_INDEX);
        departmentsSearch.setQuery(departmentsQuery);
        departmentsSearch.setSize(1);
        departmentsSearch.addHighlightedField("terms", 0, 0);
        departmentsSearch.setHighlighterPreTags("<strong>");
        departmentsSearch.setHighlighterPostTags("</strong>");
        SearchResponse response = departmentsSearch.get();
        ONSSearchResponse onsSearchResponse = new ONSSearchResponse(response);
        if (onsSearchResponse.getNumberOfResults() == 0) {
            return;
        }
        Map<String, Object> hit = onsSearchResponse.getResult().getResults().get(0);
        Text[] highlightedFragments = response.getHits().getAt(0).getHighlightFields().get("terms").getFragments();
        if (highlightedFragments != null && highlightedFragments.length > 0) {
            hit.put("match", highlightedFragments[0].toString());
        }
        results.put("departments", onsSearchResponse.getResult());
    }


    public static Map<String, SearchResult> searchTimeseriesForUri(String uriString) {
        QueryBuilder builder = QueryBuilders.matchAllQuery();
        SortBy sortByReleaseDate = SortBy.release_date;

        SearchFilter filter = boolQueryBuilder -> {
            if (isNotEmpty(uriString)) {
                ONSFilterBuilders.filterUriPrefix(uriString, boolQueryBuilder);
            }
        };

        ONSQuery query = onsQuery(typeBoostedQuery(builder), filter)
                .types(ContentType.timeseries)
                .sortBy(sortByReleaseDate)
                .name("result")
                .highlight(true);

        SearchQueries queries = () -> ONSQueryBuilders.toList(query);
        return SearchUtils.searchAll(queries);
    }

    private static boolean isFiltered(HttpServletRequest request) {
        String[] filter = request.getParameterValues("filter");
        if (extractPage(request) == 1 && isEmpty(filter)) {
            return false;
        }
        return true;
    }


}
