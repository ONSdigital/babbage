package com.github.onsdigital.babbage.search;

import com.github.onsdigital.babbage.api.util.SearchUtils;
import com.github.onsdigital.babbage.error.ValidationError;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.helpers.SearchRequestHelper;
import com.github.onsdigital.babbage.search.helpers.base.SearchQueries;
import com.github.onsdigital.babbage.search.helpers.dates.PublishDates;
import com.github.onsdigital.babbage.search.helpers.dates.PublishDatesException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Encapsulates some of the static search methods making it easier to test.
 * Deprecated by David on 31/05/17.
 */
@Deprecated
public class SearchService {

    private static final SearchService service = new SearchService();

    public static SearchService get() {
        return service;
    }

    public PublishDates extractPublishDates(HttpServletRequest request) throws PublishDatesException {
        return SearchRequestHelper.extractPublishDates(request);
    }

    public BabbageResponse listPageWithValidationErrors(String listType, SearchQueries queries,
                                                        List<ValidationError> errors) throws IOException {
        if (errors == null || errors.isEmpty()) {
            return listPage(listType, queries);
        }
        return SearchUtils.listPageWithValidationErrors(listType, queries, errors);
    }

    public BabbageResponse listPage(String listType, SearchQueries queries) throws IOException {
        return SearchUtils.listPage(listType, queries);
    }

    public BabbageResponse listJson(String listType, SearchQueries queries) throws IOException {
        return SearchUtils.listJson(null, listType, queries);
    }
}
