package com.github.onsdigital.babbage.api.util;

import com.github.onsdigital.babbage.error.ValidationError;
import com.github.onsdigital.babbage.response.BabbageStringResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.search.model.SearchResult;
import com.github.onsdigital.babbage.template.TemplateService;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.github.onsdigital.babbage.util.json.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.util.URIUtil.isDataRequest;

/*
 * SearchRendering contains common methods used to render a list of search results into a
 * BabbageResponse. These were originally part of the SearchUtils package but have now been
 * decoupled.
 */
public class SearchRendering {
  private static final String ERRORS_KEY = "errors";

  // Send result back to client
  public static BabbageResponse buildResponse(
      HttpServletRequest request, String listType, Map<String, SearchResult> results)
      throws IOException {
    BabbageResponse response;
    if (isDataRequest(request.getRequestURI())) {
      response = buildDataResponse(listType, results);
    } else {
      response = buildPageResponse(listType, results);
    }
    return response;
  }

  public static BabbageResponse buildDataResponse(
      String listType, Map<String, SearchResult> results) {
    LinkedHashMap<String, Object> data = buildResults(listType, results);
    return new BabbageStringResponse(
        JsonUtil.toJson(data),
        MediaType.APPLICATION_JSON);
  }

  public static BabbageResponse buildPageResponse(
      String listType, Map<String, SearchResult> results) throws IOException {
    LinkedHashMap<String, Object> data = buildResults(listType, results);
    return new BabbageStringResponse(
        TemplateService.getInstance().renderContent(data),
        MediaType.TEXT_HTML);
  }

  public static BabbageResponse buildPageResponseWithValidationErrors(
      String listType, Map<String, SearchResult> results, Optional<List<ValidationError>> errors)
      throws IOException {
    LinkedHashMap<String, Object> data = buildResults(listType, results);
    if (errors.isPresent() && !errors.get().isEmpty()) {
      data.put(ERRORS_KEY, errors.get());
    }
    return new BabbageStringResponse(
        TemplateService.getInstance().renderContent(data),
        MediaType.TEXT_HTML);
  }

  public static LinkedHashMap<String, Object> buildResults(
      String listType, Map<String, SearchResult> results) {
    LinkedHashMap<String, Object> data = getBaseListTemplate(listType);
    if (results != null) {
      for (Map.Entry<String, SearchResult> result : results.entrySet()) {
        data.put(result.getKey(), result.getValue());
      }
    }
    return data;
  }

  private static LinkedHashMap<String, Object> getBaseListTemplate(String listType) {
    LinkedHashMap<String, Object> baseData = new LinkedHashMap<>();
    baseData.put("type", "list");
    baseData.put("listType", listType.toLowerCase());
    baseData.put("uri", ((RequestUtil.Location) ThreadContext.getData("location")).getPathname());
    return baseData;
  }
}
