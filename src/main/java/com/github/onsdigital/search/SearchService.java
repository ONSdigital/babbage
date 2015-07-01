package com.github.onsdigital.search;

import java.net.URI;
import java.util.*;

import com.github.onsdigital.content.link.PageReference;
import com.github.onsdigital.content.page.base.Page;
import com.github.onsdigital.content.page.base.PageDescription;
import com.github.onsdigital.content.page.base.PageType;
import com.github.onsdigital.content.page.statistics.base.StatisticsDescription;
import com.github.onsdigital.content.partial.SearchResult;
import com.github.onsdigital.content.util.ContentUtil;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;

import com.github.onsdigital.search.util.ONSQueryBuilder;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

/**
 * 
 * Centralized service to search
 * 
 * @author brn
 *
 */
public class SearchService {

	private SearchService() {

	}

	/**
	 * Performs the search and returns documents as a list of maps that contains
	 * key-value pairs
	 * 
	 * @param queryBuilder
	 * @return {@link SearchResult}
	 * @throws Exception
	 */
	public static SearchResult search(ONSQueryBuilder queryBuilder) {
		System.out.println("Searcing For:" + ReflectionToStringBuilder.toString(queryBuilder));
		return buildSearchResult(execute(queryBuilder));

	}
	
	public static long count(ONSQueryBuilder queryBuilder) {
		System.out.println("Counting:" + ReflectionToStringBuilder.toString(queryBuilder));
		return executeCount(queryBuilder).getCount();
	}

	/**
	 * Performs multi search and returns results
	 * 
	 * @return
	 */
	public static List<SearchResult> multiSearch(ONSQueryBuilder... queryBuilders) {
		System.out.println("Multiple Searcing For:" + getQueries(queryBuilders));
		List<SearchResult> results = new ArrayList<SearchResult>();

		MultiSearchResponse response = execute(queryBuilders);
		Item[] responses = response.getResponses();
		for (int i = 0; i < responses.length; i++) {
			Item item = responses[i];
			if (!item.isFailure()) {
				results.add(buildSearchResult(item.getResponse()));
			} else {
				System.out.println("Warning: Search failed for " + ReflectionToStringBuilder.toString(queryBuilders));
			}
		}
		
		return results;
	}

	private static String getQueries(ONSQueryBuilder... querybuilders) {
		StringBuilder builder =  new StringBuilder();
		for (ONSQueryBuilder queryBuilder : querybuilders) {
			builder.append(ReflectionToStringBuilder.toString(queryBuilder));
		}
		return builder.toString();
	}

	private static SearchResponse execute(ONSQueryBuilder queryBuilder) {
		return buildRequest(queryBuilder).get();
	}
	
	private static CountResponse executeCount(ONSQueryBuilder queryBuilder) {
		return buildCountRequest(queryBuilder).get();
	}
	
	

	private static MultiSearchResponse execute(ONSQueryBuilder... queryBuilders) {
		return buildMultiSearchRequest(queryBuilders).get();
	}

	private static SearchRequestBuilder buildRequest(ONSQueryBuilder queryBuilder) {
		SearchRequestBuilder searchBuilder = getClient().prepareSearch(queryBuilder.getIndex());
		String[] types = queryBuilder.getTypes();
		searchBuilder.setTypes(types);
		searchBuilder.setExtraSource(queryBuilder.buildQuery());
		return searchBuilder;
	}
	
	private static CountRequestBuilder buildCountRequest(ONSQueryBuilder queryBuilder) {
		CountRequestBuilder countBuilder = getClient().prepareCount(queryBuilder.getIndex());
		String[] types = queryBuilder.getTypes();
		countBuilder.setTypes(types);
		countBuilder.setQuery(queryBuilder.buildCountQuery());
		return countBuilder;
	}

	private static MultiSearchRequestBuilder buildMultiSearchRequest(ONSQueryBuilder... builders) {
		MultiSearchRequestBuilder multiSearchRequestBuilder = getClient().prepareMultiSearch();
		for (int i = 0; i < builders.length; i++) {
			multiSearchRequestBuilder.add(buildRequest(builders[i]));
		}
		return multiSearchRequestBuilder;
	}


	public static SearchResult buildSearchResult(SearchResponse response) {
		SearchResult searchResult = new SearchResult();
		searchResult.setNumberOfResults(response.getHits().getTotalHits());
        searchResult.setResults(resolveReferences(response));
        return searchResult;
	}

	private static List<Map<String, Object>> resolveHits(SearchResponse response) {
        List<Map<String, Object>> results = new ArrayList<>();
        SearchHit hit;
        Iterator<SearchHit> iterator = response.getHits().iterator();
        while (iterator.hasNext()) {
            hit = iterator.next();
            Map<String, Object> item = new HashMap<String, Object>(
                    hit.getSource());
            item.put("type", hit.getType());
            item.put("highlights",
                    extractHihglightedFields(hit));
            results.add(item);
        }
        return results;
    }

	private static List<PageReference> resolveReferences(SearchResponse response) {
        List<Map<String, Object>> results = resolveHits(response);
		List<PageReference> references = new ArrayList<>();
        for (Iterator<Map<String, Object>> iterator = results.iterator(); iterator.hasNext(); ) {
            Map<String, Object> next = iterator.next();
            PageReference reference = new PageReference(URI.create((String) next.get("uri")));
            reference.setType(PageType.valueOf((String) next.get("type")));
            PageDescription pageDescription = ContentUtil.deserialise(ContentUtil.serialise(next), StatisticsDescription.class);
            reference.setDescription(pageDescription);
            references.add(reference);
        }
        return references;
    }

	private static Map<? extends String, ? extends Object> extractHihglightedFields(
			SearchHit hit) {

		HashMap<String, Object> highlightedFields = new HashMap<>();
		for (Map.Entry<String, HighlightField> entry : hit.getHighlightFields()
				.entrySet()) {
			Text[] fragments = entry.getValue().getFragments();
			if (fragments != null) {
				for (Text text : fragments) {
					highlightedFields.put(entry.getKey(), text.toString());
				}
			}
		}
		return highlightedFields;
	}


	private static Client getClient() {
		return ElasticSearchServer.getClient();
	}

}
