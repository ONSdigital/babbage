package com.github.onsdigital.babbage.search.helpers;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.error.BadRequestException;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.search.helpers.dates.PublishDates;
import com.github.onsdigital.babbage.search.helpers.dates.PublishDatesException;
import com.github.onsdigital.babbage.search.input.SortBy;
import com.github.onsdigital.babbage.search.input.TypeFilter;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import static com.github.onsdigital.babbage.search.helpers.dates.PublishDates.publishedDates;
import static com.github.onsdigital.babbage.search.helpers.dates.PublishDates.updatedWithinPeriod;
import static com.github.onsdigital.babbage.util.RequestUtil.getParam;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.upperCase;

/**
 * Common utilities to manipulate search request query and extracting common search parameters
 */

public class SearchRequestHelper {

    static final String FROM_DAY_PARAM = "fromDateDay";
    static final String FROM_MONTH_PARAM = "fromDateMonth";
    static final String FROM_YEAR_PARAM = "fromDateYear";
    static final String TO_DAY_PARAM = "toDateDay";
    static final String TO_MONTH_PARAM = "toDateMonth";
    static final String TO_YEAR_PARAM = "toDateYear";
    static final String UPDATED_PARAM = "updated";
    static final String UPCOMING_PARAM = "upcoming";
    static final String VIEW_PARAM = "view";

    static BiFunction<HttpServletRequest, String, String> extractDayOrMonthDateValue = (request, parameterName) ->
            StringUtils.isEmpty(request.getParameter(parameterName)) ? "" : request.getParameter(parameterName) + "/";

    static BiFunction<HttpServletRequest, String, String> extractYearDateValue = (request, parameterName) ->
            StringUtils.isEmpty(request.getParameter(parameterName)) ? "" : request.getParameter(parameterName);

    static SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yy");

    private SearchRequestHelper() {
        // contains only static methods - hide constructor.
    }

    public static PublishDates extractPublishDates(HttpServletRequest request) throws PublishDatesException {
        String updatedPeriod = request.getParameter(UPDATED_PARAM);
        PublishDates publishDates;

        if (StringUtils.isEmpty(updatedPeriod)) {
            publishDates = parseDates(request);
        } else {
            publishDates = updatedWithinPeriod(updatedPeriod);
        }
        return publishDates;
    }

    public static PublishDates parseDates(HttpServletRequest req) throws PublishDatesException {
        String fromDateStr = extractDateStr(req, FROM_DAY_PARAM, FROM_MONTH_PARAM, FROM_YEAR_PARAM);
        String toDateStr = extractDateStr(req, TO_DAY_PARAM, TO_MONTH_PARAM, TO_YEAR_PARAM);
        return publishedDates(fromDateStr, toDateStr, allowFutureAfterDate(req));
    }

    private static String extractDateStr(HttpServletRequest req, String dayKey, String monthKey, String yearKey) {
        StringBuilder stringDate = new StringBuilder();
        stringDate.append(extractDayOrMonthDateValue.apply(req, dayKey));
        stringDate.append(extractDayOrMonthDateValue.apply(req, monthKey));
        stringDate.append(extractYearDateValue.apply(req, yearKey));
        return stringDate.toString();
    }

    /**
     * Extracts filter parameters requested, including only filter if given default filters, otherwise ignores.
     * <p>
     * If there are no valid filters will return default filters
     *
     * @param request
     * @param defaultFilters
     * @return
     */
    public static Set<TypeFilter> extractSelectedFilters(HttpServletRequest request, Set<TypeFilter> defaultFilters, Boolean ignoreFilters) {
        String[] filters = request.getParameterValues("filter");
        if (ignoreFilters || filters == null) {
            return defaultFilters;
        }

        HashSet<TypeFilter> selectedFilters = new HashSet<>();
        for (int i = 0; i < filters.length; i++) {
            TypeFilter typeFilter = getEnum(TypeFilter.class, upperCase(filters[i]));
            if (defaultFilters.contains(typeFilter)) {
                selectedFilters.add(typeFilter);
            }
        }
        return selectedFilters.isEmpty() ? defaultFilters : selectedFilters;
    }


    public static SortBy extractSortBy(HttpServletRequest request, SortBy defaultSort) {
        String sortBy = getParam(request, "sortBy");
        if (isEmpty(sortBy)) {
            return defaultSort;
        }
        try {
            return SortBy.valueOf(sortBy.toLowerCase());
        } catch (IllegalArgumentException e) {
            return defaultSort;
        }
    }

    /**
     * Extract the page size from a request - for paged results.
     *
     * @return
     */
    public static int extractSize(HttpServletRequest request) {
        String size = request.getParameter("size");

        if (isEmpty(size)) {
            return 10;
        }
        try {
            int pageSize = Integer.parseInt(size);
            if (pageSize < 1) {
                throw new ResourceNotFoundException();
            }
            return pageSize;
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    /**
     * Extract the page number from a request - for paged results.
     *
     * @return
     */
    public static int extractPage(HttpServletRequest request) {
        String page = request.getParameter("page");

        if (isEmpty(page)) {
            return 1;
        }
        try {
            int pageNumber = Integer.parseInt(page);
            if (pageNumber < 1) {
                throw new ResourceNotFoundException();
            }
            return pageNumber;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Extracts search term, checks for parameter "q" if it does not exist looks for parameter "query"
     *
     * @param request
     * @return
     */
    public static String extractSearchTerm(HttpServletRequest request) {
        String query = getParam(request, "q", getParam(request, "query"));//get query if q not given, list pages use query
        if (StringUtils.isEmpty(query)) {
            return null;
        }
        if (query.length() > 200) {
            throw new BadRequestException("Search query contains too many characters");
        }
        return query;
    }

    public static boolean extractConceptualSearch(HttpServletRequest request) {
        if (Configuration.SEARCH_SERVICE.isConceptualSearchServiceEnabled()) {
            String val = getParam(request, "conceptual");
            if (StringUtils.isEmpty(val)) {
                return true; // default to true
            } else {
                return val.toLowerCase().equals("true");
            }
        }
        return false;
    }

    private static boolean allowFutureAfterDate(HttpServletRequest request) {
        return UPCOMING_PARAM.equalsIgnoreCase(request.getParameter(VIEW_PARAM));
    }


}
