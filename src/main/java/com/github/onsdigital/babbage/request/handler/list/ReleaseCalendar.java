package com.github.onsdigital.babbage.request.handler.list;

import com.github.onsdigital.babbage.api.endpoint.rss.service.RssService;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.request.handler.base.ListRequestHandler;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.SearchService;
import com.github.onsdigital.babbage.search.builders.ONSFilterBuilders;
import com.github.onsdigital.babbage.search.helpers.base.SearchFilter;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.helpers.dates.PublishDatesException;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.field.Field;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

import static com.github.onsdigital.babbage.api.util.SearchUtils.buildListQuery;
import static com.github.onsdigital.babbage.search.builders.ONSQueryBuilders.toList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by bren on 22/09/15.
 */
public class ReleaseCalendar extends BaseRequestHandler implements ListRequestHandler {

    private static final String REQUEST_TYPE = "releasecalendar";
    private static final String VIEW_PARAM = "view";
    private static final String UPCOMING_VIEW = "upcoming";

    private static RssService rssService = RssService.getInstance();
    private static SearchService searchService = SearchService.get();

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws Exception {
        if (rssService.isRssRequest(request)) {
            return rssService.getReleaseCalendarFeedResponse(request, queries(request, false));
        } else {
            try {
                // Not ideal but the lack of cohesion & encapsulation in search makes this very messy to do properly.
                searchService.extractPublishDates(request);
                return searchService.listPage(getClass().getSimpleName(), queries(request, true));
            } catch (PublishDatesException ex) {
                return searchService.listPageWithValidationErrors(getClass().getSimpleName(), queries(request, true),
                        ex.getErrors());
            }
        }
    }

    @Override
    public BabbageResponse getData(String uri, HttpServletRequest request) throws IOException, ContentReadException {
        return searchService.listJson(getClass().getSimpleName(), queries(request, true));
    }

    private SearchQueries queries(HttpServletRequest request, boolean highlight) {
        boolean upcoming = isUpcoming(request);
        SortBy defaultSort = upcoming ? SortBy.release_date_asc : SortBy.release_date;
        return () -> toList(
                buildListQuery(request, filters(request, upcoming), defaultSort)
                        .types(ContentType.release)
                        .highlight(highlight)
        );
    }

    private boolean isUpcoming(HttpServletRequest request) {
        return UPCOMING_VIEW.equalsIgnoreCase(request.getParameter(VIEW_PARAM));
    }

    private SearchFilter filters(HttpServletRequest request, boolean upcoming) {
        return (query) -> {
            ONSFilterBuilders.filterDates(request, query);
            if (upcoming) {
                filterUpcoming(query);
            } else {//published
                filterPublished(query);
            }
        };
    }

    public static void filterUpcoming(BoolQueryBuilder query) {
        QueryBuilder notPublishedNotCancelled = and(not(published()), not(cancelled()));
        QueryBuilder cancelledButNotDue = and(cancelled(), not(due()));
        query.filter(or(notPublishedNotCancelled, cancelledButNotDue));
    }

    public static void filterPublished(BoolQueryBuilder query) {
        QueryBuilder publishedNotCancelled = and(published(), not(cancelled()));
        QueryBuilder cancelledAndDue = and(cancelled(), due());
        query.filter(or(publishedNotCancelled, cancelledAndDue));
    }


    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }


    private static QueryBuilder published() {
        return termQuery(Field.published.fieldName(), true);
    }

    private static QueryBuilder cancelled() {
        return termQuery(Field.cancelled.fieldName(), true);
    }

    private static QueryBuilder due() {
        return rangeQuery(Field.releaseDate.fieldName()).to(new Date());
    }

    private static QueryBuilder not(QueryBuilder query) {
        return boolQuery().mustNot(query);
    }

    //Or query for given two queries. Database would help a lot , wouldn't it ?
    private static QueryBuilder or(QueryBuilder q1, QueryBuilder q2) {
        return boolQuery().should(q1).should(q2);
    }

    private static QueryBuilder and(QueryBuilder q1, QueryBuilder q2) {
        return boolQuery().must(q1).must(q2);
    }

}
