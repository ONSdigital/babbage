package com.github.onsdigital.babbage.publishing;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.publishing.model.ContentDetail;
import com.github.onsdigital.babbage.publishing.model.FilePublishType;
import com.github.onsdigital.babbage.publishing.model.PublishInfo;
import com.github.onsdigital.babbage.publishing.model.PublishNotification;
import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.util.ElasticSearchUtils;
import com.github.onsdigital.babbage.util.json.JsonUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.search.ElasticSearchClient.getElasticsearchClient;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;
import static org.apache.commons.lang.StringUtils.removeEnd;

/**
 * Created by bren on 16/12/15.
 * <p/>
 * Handles notifications sent by Florence to manage publish dates, hence the caching
 */
public class PublishingManager {

    private static PublishingManager instance = new PublishingManager();
    private static ElasticSearchUtils searchUtils;
    private static final String PUBLISH_DATES_INDEX = "publish";
    private final TimeValue scrollKeepAlive = new TimeValue(120, TimeUnit.SECONDS);

    private PublishingManager() {
        searchUtils = new ElasticSearchUtils(getElasticsearchClient());
    }

    public void notifyUpcoming(PublishNotification notification) throws IOException {
        initPublishDatesIndex();
        try (BulkProcessor bulkProcessor = createBulkProcessor()) {

            // Add files to update.
            for (String uri : notification.getUrisToUpdate()) {
                uri = cleanUri(uri);
                IndexRequestBuilder indexRequestBuilder = getElasticsearchClient().prepareIndex(PUBLISH_DATES_INDEX, notification.getCollectionId(), uri);
                PublishInfo publishInfo = new PublishInfo(uri, notification.getCollectionId(), notification.getDate(), FilePublishType.UPDATE);
                indexRequestBuilder.setSource(JsonUtil.toJson(publishInfo));
                bulkProcessor.add(indexRequestBuilder.request());
            }

            for (ContentDetail contentDetail : notification.getUrisToDelete()) {
                String uri = cleanUri(contentDetail.uri);
                IndexRequestBuilder indexRequestBuilder = getElasticsearchClient().prepareIndex(PUBLISH_DATES_INDEX, notification.getCollectionId(), uri);
                PublishInfo publishInfo = new PublishInfo(uri, notification.getCollectionId(), notification.getDate(), FilePublishType.DELETE, ContentType.valueOf(contentDetail.type));
                indexRequestBuilder.setSource(JsonUtil.toJson(publishInfo));
                bulkProcessor.add(indexRequestBuilder.request());
            }
        }
    }

    /**
     * Deletes collection uris from the index and triggers reindex for the uri
     *
     * @param notification
     */
    public void notifyPublished(PublishNotification notification) throws IOException {
        deletePublishDates(notification, true);
    }

    public void notifyPublishCancel(PublishNotification notification) throws IOException {
        deletePublishDates(notification, false);
    }

    private void deletePublishDates(PublishNotification notification, boolean triggerReindex) throws IOException {
        initPublishDatesIndex();
        try (BulkProcessor bulkProcessor = createBulkProcessor()) {
            SearchResponse response = readUriList(notification.getCollectionId());
            while (true) {
                for (SearchHit hit : response.getHits().getHits()) {
                    String uri = (String) hit.getSource().get("uri");


                    bulkProcessor.add(prepareDeleteRequest(notification, uri));
                    if (triggerReindex) {
                        //only index uri to the page which hash data.json at the end and is stripped out when saving the uri to upcoming publish uris
                        if (StringUtils.isEmpty(FilenameUtils.getExtension(uri)) && !isVersionedUri(uri)) {

                            FilePublishType filePublishType = FilePublishType.UPDATE; // default to update for existing entries with no type
                            String storedFilePublishType = (String) hit.getSource().get("filePublishType");

                            if (storedFilePublishType != null) ;
                            filePublishType = FilePublishType.valueOf(storedFilePublishType);

                            switch (filePublishType) {
                                case UPDATE:
                                    triggerReindex(notification.getKey(), uri);
                                    break;
                                case DELETE:
                                    String contentType = (String) hit.getSource().get("contentType");
                                    deleteIndex(notification.getKey(), uri, contentType);
                                    break;
                            }
                        }
                    }
                }
                response = getElasticsearchClient().prepareSearchScroll(response.getScrollId()).setScroll(scrollKeepAlive).execute().actionGet();
                //Break condition: No hits are returned
                if (response.getHits().getHits().length == 0) {
                    break;
                }
            }
        } catch (Exception e) {
            error().exception(e).data("collectionID", notification.getCollectionId()).log("reindexing collection failed");
        }
    }

    private boolean isVersionedUri(String uri) {
        return uri.contains("/previous/v");
    }

    private DeleteRequest prepareDeleteRequest(PublishNotification notification, String uri) {
        return getElasticsearchClient().prepareDelete(PUBLISH_DATES_INDEX, notification.getCollectionId(), uri).request();
    }

    private void triggerReindex(String key, String uri) {
        try {
            ContentClient.getInstance().reIndex(key, uri);
        } catch (ContentReadException e) {
            error().exception(e).data("uri", uri).log("error reindexing uri");
        }
    }

    private void deleteIndex(String key, String uri, String contentType) {
        try {
            ContentClient.getInstance().deleteIndex(key, uri, contentType);
        } catch (ContentReadException e) {
            error().exception(e).data("uri", uri).log("error deleting index for uri");
        }
    }

    public PublishInfo getNextPublishInfo(String uri) {

        BoolQueryBuilder builder = QueryBuilders.boolQuery();

        // Only return dates that are in future
        builder.should(QueryBuilders.rangeQuery("publishDate").gte(System.currentTimeMillis()));

        // if the uri is the homepage then cache until the next publish time.
        if (uri != null && uri.length() > 1) {
            builder.filter(QueryBuilders.termQuery("uri", uri));
        }

        SearchRequestBuilder searchRequestBuilder = getElasticsearchClient().prepareSearch(PUBLISH_DATES_INDEX);
        searchRequestBuilder.setSize(1);
        searchRequestBuilder.setQuery(builder).addSort(new FieldSortBuilder("publishDate").unmappedType("date"));

        SearchResponse response = searchRequestBuilder.get();
        if (response.getHits().getTotalHits() > 0) {
            Long publishDate = (Long) response.getHits().getAt(0).getSource().get("publishDate");
            String collectionId = (String) response.getHits().getAt(0).getSource().get("collectionId");
            String publishType = (String) response.getHits().getAt(0).getSource().get("filePublishType");
            FilePublishType filePublishType = publishType == null ? FilePublishType.UPDATE : FilePublishType.valueOf(publishType);
            return new PublishInfo(uri, collectionId, publishDate == null ? null : new Date(publishDate), filePublishType);
        }
        return null;
    }

    private SearchResponse readUriList(String collectionId) {
        return getElasticsearchClient().prepareSearch(PUBLISH_DATES_INDEX)
                .setTypes(collectionId).setSize(100).setScroll(scrollKeepAlive).get();

    }

    public void dropPublishDate(PublishInfo info) {
        getElasticsearchClient().prepareDelete(PUBLISH_DATES_INDEX, info.getCollectionId(), info.getUri()).get();
    }

    public static PublishingManager getInstance() {
        return instance;
    }

    public static void init() throws IOException {
        info().log("initialising search service");
        if (!appConfig().babbage().isPublishing()) {
            initPublishDatesIndex();
        }
        info().log("initialising search service completed successfully");
    }

    private static void initPublishDatesIndex() throws IOException {
        info().data("index", PUBLISH_DATES_INDEX).log("checking status of publish dates index");
        if (!searchUtils.isIndexAvailable(PUBLISH_DATES_INDEX)) {
            info().data("index", PUBLISH_DATES_INDEX).log("Publish dates index not available, creating");
            searchUtils.createIndex(PUBLISH_DATES_INDEX, buildPublishDatesIndexSettings());
        }
    }

    private static Settings buildPublishDatesIndexSettings() {
        Map<String, String> settings = new HashMap<>();
        // default analyzer
        settings.put("analysis.analyzer.default_index.tokenizer", "keyword"); //no analyzing
        settings.put("analysis.analyzer.default_index.filter", "lowercase");
        return Settings.builder().put(settings).build();
    }

    //Clears data.json and .json at the end of uri
    public static String cleanUri(String uri) {
        return removeEnd(removeEnd(removeEnd(uri, "/data.json"), "/data_cy.json"), ".json");
    }

    private BulkProcessor createBulkProcessor() {
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                getElasticsearchClient(),
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {
                        info().data("batch_size", request.numberOfActions()).log("bulk indexing publish dates");
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        if (response.hasFailures()) {
                            BulkItemResponse[] items = response.getItems();
                            for (BulkItemResponse item : items) {
                                if (item.isFailed()) {
                                    info().data("failed_item_id", item.getFailure().getId())
                                            .data("failure_details", item.getFailureMessage())
                                            .log("bulk processor after bulk failure");
                                }
                            }
                        }
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {
                        error().exception(failure).log("bulk processor after bulk failure");
                    }
                })
                .setBulkActions(10000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                .setConcurrentRequests(1)
                .build();

        return bulkProcessor;
    }
}
