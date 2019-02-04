package com.github.onsdigital.babbage.api.endpoint.rss;

import com.github.onsdigital.babbage.search.input.TypeFilter;
import com.github.onsdigital.babbage.util.URIUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * POJO for for storing/manipulating RSS request queries. Provides convenience methods for commonly accessed parameters
 * values while generating the RSS feed.
 */
public class RssSearchFilter {

	private static final String DATA_LIST_URI = "/datalist";
	private static final String PUBLICATIONS_URI = "/publications";

	private String uri;
	private Set<TypeFilter> typeFilters = TypeFilter.getAllFilters();
	private HttpServletRequest request;
	private Optional<String[]> filters;
	private Optional<String> keyword = Optional.empty();
	private LinkedHashMap<String, String> providedParams;


	public static class Builder {

		public RssSearchFilter build(HttpServletRequest request) {
			requireNonNull(request, "request cannot be null.");
			return new RssSearchFilter(request);
		}
	}

	private RssSearchFilter(final HttpServletRequest request) {
		this.providedParams = new LinkedHashMap<>();
		this.request = request;
		setTopicAndFilterType(request);
		setFilters(request);
		setKeyword(request);
	}

	public Map<String, String> getProvidedParams() {
		return providedParams;
	}

	private void setTopicAndFilterType(HttpServletRequest request) {
			String uriString = request.getRequestURI();

			int lastSlashIndex = StringUtils.lastIndexOf(uriString, "/");
			String endSegment = StringUtils.substring(uriString, lastSlashIndex, uriString.length());

			if (DATA_LIST_URI.equalsIgnoreCase(endSegment) || PUBLICATIONS_URI.equalsIgnoreCase(endSegment)) {
				uriString = URIUtil.removeLastSegment(uriString);
			}

			uri = uriString;
			providedParams.put("rss.uri", uri);

			switch (endSegment) {
				case DATA_LIST_URI:
					this.typeFilters = TypeFilter.getDataFilters();
					providedParams.put("rss.resultstype", "data");
					break;
				case PUBLICATIONS_URI:
					this.typeFilters = TypeFilter.getPublicationFilters();
					providedParams.put("rss.resultstype", "publications");
					break;
			}
	}

	private void setFilters(HttpServletRequest request) {
		this.filters = Optional.ofNullable((String[])request.getParameterMap().get("filter"));

		if (filters.isPresent()) {
				StringBuilder sb = new StringBuilder();
				Iterator<String> iterator = Arrays.asList(filters.get()).iterator();
				while (iterator.hasNext()) {
					sb.append(iterator.next());
					if (iterator.hasNext()) {
						sb.append(", ");
					}
				}
				providedParams.put("rss.filters", sb.toString());
		}
	}

	private void setKeyword(HttpServletRequest request) {
		this.keyword = Optional.ofNullable(request.getParameter("query"));
		if (keyword.isPresent()) {
			providedParams.put("rss.keyword", this.keyword.get());
		}
	}

	public String getUri() {
		return uri;
	}

	public Set<TypeFilter> getTypeFilters() {
		return typeFilters;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public Optional<String[]> getFilters() {
		return filters;
	}

	public Optional<String> getKeyword() {
		return keyword;
	}


}
