package com.github.onsdigital.search;

import com.github.onsdigital.configuration.Configuration;
import com.github.onsdigital.content.page.base.PageType;
import com.github.onsdigital.search.error.IndexingInProgressException;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class Indexer {

	private static final Lock indexingLock = new ReentrantLock();

	public static void loadIndex(Client client) throws IOException {

		if (indexingLock.tryLock()) {
			try {
				List<String> absoluteFilePaths = LoadIndexHelper.getAbsoluteFilePaths(Configuration.getContentPath());
				if (absoluteFilePaths.isEmpty()) {
					throw new IllegalStateException("No items were found for indexing");
				}
				try {
					deleteIndex(client);
					createIndex(client);
					indexDocuments(client, absoluteFilePaths);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} finally {
				indexingLock.unlock();
			}
		} else {
			throw new IndexingInProgressException();
		}
	}

	private static void createIndex(Client client) throws IOException {

		System.out.println("Creating index ons");
		// Set up the synonyms
		CreateIndexRequestBuilder indexBuilder = client.admin().indices().prepareCreate("ons").setSettings(buildSettings());
		System.out.println("Adding document mappings");

		indexBuilder.addMapping(PageType.dataset.toString(), getMappingProperties(PageType.dataset.toString()));
		indexBuilder.addMapping(PageType.taxonomy_landing_page.toString(), getMappingProperties(PageType.taxonomy_landing_page.toString()));
		indexBuilder.addMapping(PageType.product_page.toString(), getMappingProperties(PageType.product_page.toString()));
		indexBuilder.addMapping(PageType.bulletin.toString(), getMappingProperties(PageType.bulletin.toString()));
		indexBuilder.addMapping(PageType.article.toString(), getMappingProperties(PageType.article.toString()));
//		indexBuilder.addMapping(PageType.methodology.toString(), getMappingProperties(PageType.methodology.toString()));

		System.out.println("Adding timeseries mappings");
		indexBuilder.addMapping(PageType.timeseries.toString(), getTimeseriesMappingProperties(PageType.timeseries.toString()));

		indexBuilder.execute();
	}

	private static void deleteIndex(Client client){
		System.out.println("Deleting index ons");
		DeleteIndexRequestBuilder deleteIndexRequestBuilder = client.admin().indices().prepareDelete("ons");
		deleteIndexRequestBuilder.execute();
	}

	// Mapping for field properties. To decide which field will be indexed
	private static XContentBuilder getMappingProperties(String type) throws IOException {

		XContentBuilder builder = jsonBuilder().startObject().startObject(type).startObject("properties");
		try {
			builder.startObject("releaseDate").field("type", "string").field("index", "no").endObject();
			builder.startObject("summary").field("type", "string").field("index", "no").endObject();
			builder.startObject("title").field("type", "string").field("index", "analyzed").endObject();
			builder.startObject("edition").field("type", "string").field("index", "analyzed").endObject();
			builder.startObject("path").field("type", "string").field("index", "analyzed").endObject();
			builder.startObject("uri").field("type", "string").field("index", "analyzed").endObject();
			builder.endObject().endObject().endObject();
			return builder;
		} finally {
			if (builder != null) {
				builder.close();
			}
		}
	}

	// Mapping for timeseries field properties.
	private static XContentBuilder getTimeseriesMappingProperties(String type) throws IOException {
		XContentBuilder builder = jsonBuilder().startObject().startObject(type).startObject("properties");
		try {
			// cdid not analyzed for exact match
			builder.startObject("cdid").field("type", "string").field("index", "not_analyzed").endObject();
			builder.startObject("title").field("type", "string").field("index", "analyzed").endObject();
			builder.startObject("uri").field("type", "string").field("index", "analyzed").endObject();
			builder.startObject("path").field("type", "string").field("index", "analyzed").endObject();
			builder.endObject().endObject().endObject();
			System.out.println(builder.string() + "\n\n");
			return builder;
		} finally {
			if (builder != null) {
				builder.close();
			}
		}
	}

	private static void indexDocuments(Client client, List<String> absoluteFilePaths) throws IOException {
		AtomicInteger idCounter = new AtomicInteger();
		for (String absoluteFilePath : absoluteFilePaths) {

			Map<String, String> documentMap = LoadIndexHelper.getDocumentMap(absoluteFilePath);
			if (documentMap != null && StringUtils.isNotEmpty(documentMap.get("title"))) {
				if (documentMap.get("type").equals(PageType.timeseries.toString())) {
					buildTimeseries(client, documentMap, idCounter.getAndIncrement());
				} else {
					buildDocument(client, documentMap, idCounter.getAndIncrement());
				}
			}
		}
	}

	private static void buildTimeseries(Client client, Map<String, String> documentMap, int idCounter) throws IOException {
		XContentBuilder source = jsonBuilder().startObject().field("title", documentMap.get("title")).field("uri", documentMap.get("uri")).field("path", documentMap.get("tags"))
				.field("cdid", documentMap.get("cdid")).endObject();
		try {
			build(client, documentMap, idCounter, source);
		} finally {
			if (source != null) {
				source.close();
			}
		}
	}

	private static void buildDocument(Client client, Map<String, String> documentMap, int idCounter) throws IOException {

		XContentBuilder source = jsonBuilder().startObject().field("title", documentMap.get("title")).field("uri", documentMap.get("uri")).field("path", documentMap.get("tags"))
				.field("releaseDate", documentMap.get("releaseDate")).field("edition", documentMap.get("edition")).field("summary", documentMap.get("summary")).endObject();
		try {
			build(client, documentMap, idCounter, source);
		} finally {
			if (source != null) {
				source.close();
			}
		}
	}

	private static void build(Client client, Map<String, String> documentMap, int idCounter, XContentBuilder source) {
		String name = "ons";
		String type = StringUtils.lowerCase(documentMap.get("type"));
		String id = String.valueOf(idCounter);
		IndexRequestBuilder index = client.prepareIndex(name, type, id);
		index.setSource(source);
		ListenableActionFuture<IndexResponse> execution = index.execute();
		execution.actionGet();
	}

	private static Map<String, String> buildSettings() throws IOException {
		ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();

		List<String> synonymList = getSynonyms(settingsBuilder);
		getSettingsBuilder(settingsBuilder, synonymList);

		return settingsBuilder.build().getAsMap();
	}

	private static void getSettingsBuilder(ImmutableSettings.Builder settingsBuilder, List<String> synonymList) {
		String[] synonyms = new String[synonymList.size()];
		synonymList.toArray(synonyms);

		settingsBuilder.putArray("analysis.filter.ons_synonym_filter.synonyms", synonyms);

		Map<String, String> settings = new HashMap<>();
		// default analyzer
		settings.put("analysis.analyzer.default_index.tokenizer", "ons_search_tokenizer");
		settings.put("analysis.analyzer.default_index.filter", "lowercase");
		settings.put("analysis.analyzer.ons_synonyms.tokenizer", "standard");
		settings.put("analysis.filter.ons_synonym_filter.type", "synonym");

		// edgeNGram tokenizer
		settings.put("analysis.tokenizer.ons_search_tokenizer.type", "edgeNGram");
		settings.put("analysis.tokenizer.ons_search_tokenizer.max_gram", "15");
		settings.put("analysis.tokenizer.ons_search_tokenizer.min_gram", "2");
		String[] tokenChars = { "letter", "digit" };
		settingsBuilder.putArray("analysis.tokenizer.ons_search_tokenizer.token_chars", tokenChars);

		settingsBuilder.put(settings);
	}

	private static List<String> getSynonyms(ImmutableSettings.Builder settingsBuilder) throws IOException {
		String[] filters = { "lowercase", "ons_synonym_filter" };
		settingsBuilder.putArray("analysis.analyzer.ons_synonyms.filter", filters);

		// java 7 try-with-resources automatically closes streams after use
		try (InputStream inputStream = Indexer.class.getResourceAsStream("/synonym.txt"); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

			List<String> synonymList = new ArrayList<String>();
			String contents = null;
			while ((contents = reader.readLine()) != null) {
				if (!contents.startsWith("#")) {
					synonymList.add(contents);
				}
			}
			return synonymList;
		}
	}
}
