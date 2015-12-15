package com.github.onsdigital.babbage.search;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.search.helpers.CountResponseHelper;
import com.github.onsdigital.babbage.search.helpers.SearchResponseHelper;
import com.github.onsdigital.babbage.util.ElasticSearchUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.TermsFilterBuilder;

import java.io.IOException;
import java.util.*;

import static com.github.onsdigital.babbage.configuration.Configuration.ELASTIC_SEARCH.*;

public class SearchService {

    private static Client client;
    private static SearchService instance;
    private static ElasticSearchUtils searchUtils;
    private static final String PUBLISH_DATES_INDEX = "publish";
    private static final String PUBLISH_DATES_TYPE = "dates";

    private SearchService() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", getElasticSearchCluster()).build();

        client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(getElasticSearchServer(), getElasticSearchPort()));
        Runtime.getRuntime().addShutdownHook(new ShutDownNodeThread(client));
        searchUtils = new ElasticSearchUtils(client);
    }

    public static SearchService getInstance() {
        return instance;
    }

    public SearchResponseHelper search(ONSQuery query) throws IOException {
        SearchRequestBuilder searchRequestBuilder = new QueryRequestBuilder().buildSearchRequest(newSearchRequest(), query);
        System.out.println("Searching: \ntypes:\n" + ArrayUtils.toString(query.getTypes()) + " \nquery:\n" + searchRequestBuilder.internalBuilder());
        return new SearchResponseHelper(searchRequestBuilder.get());
    }

    public CountResponseHelper count(ONSQuery query) {
        CountRequestBuilder countRequestBuilder =
                client.prepareCount(getElasticSearchIndexAlias());
        countRequestBuilder = new QueryRequestBuilder().buildCountRequest(countRequestBuilder, query);
        return new CountResponseHelper(countRequestBuilder.get());
    }

    public List<SearchResponseHelper> searchMultiple(ONSQuery... queries) throws IOException {
        MultiSearchRequestBuilder multiSearchRequestBuilder = client.prepareMultiSearch();
        for (ONSQuery query : queries) {
            SearchRequestBuilder requestBuilder = new QueryRequestBuilder().buildSearchRequest(newSearchRequest(), query);
            System.out.println("Searching: \ntypes:\n" + ArrayUtils.toString(query.getTypes()) + " \nquery:\n" + requestBuilder.internalBuilder());
            multiSearchRequestBuilder.add(requestBuilder);
        }
        List<SearchResponseHelper> helpers = doSearchMultiple(multiSearchRequestBuilder);
        return helpers;
    }


    public Date getNextPublishDate(String uri) {
        GetResponse response = client.prepareGet(PUBLISH_DATES_INDEX, PUBLISH_DATES_TYPE, uri).get();
        Object publish_date = response.getField("publish_date").getValue();
    }

    private List<SearchResponseHelper> doSearchMultiple(MultiSearchRequestBuilder multiSearchRequestBuilder) {
        List<SearchResponseHelper> helpers = new ArrayList<>();
        MultiSearchResponse response = multiSearchRequestBuilder.get();
        for (MultiSearchResponse.Item item : response.getResponses()) {
            if (item.isFailure()) {
                throw new ElasticsearchException(item.getFailureMessage());
            }
            helpers.add(new SearchResponseHelper(item.getResponse()));
        }
        return helpers;
    }





    public static Client getClient() {
        return client;
    }

    public static void init() throws IOException {
        instance = new SearchService();
        if(Configuration.GENERAL.isCacheEnabled()) {
            initPublishDatesIndex();
        }
    }

    private static void initPublishDatesIndex() throws IOException {
        if (!searchUtils.isIndexAvailable(PUBLISH_DATES_INDEX)) {
            searchUtils.createIndex(PUBLISH_DATES_INDEX, buildPublishDatesIndexSettings());
        }
    }

    private static Settings buildPublishDatesIndexSettings() {
        Map<String, String> settings = new HashMap<>();
        // default analyzer
        settings.put("analysis.analyzer.default_index.tokenizer", "keyword"); //no analyzing
        settings.put("analysis.analyzer.default_index.filter", "lowercase");
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder();
        builder.put(settings);
        return builder.build();
    }


    private SearchRequestBuilder newSearchRequest() {
        return client.prepareSearch(getElasticSearchIndexAlias());
    }


    private static class ShutDownNodeThread extends Thread {
        private Client client;

        public ShutDownNodeThread(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.close();
        }
    }

}
