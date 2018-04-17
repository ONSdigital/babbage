package com.github.onsdigital.babbage.util;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;

/**
 * Created by bren on 02/09/15.
 */
@Deprecated
public class ElasticSearchUtils {
    private Client client;

    public ElasticSearchUtils(Client client) {
        this.client = client;
    }

    public CreateIndexResponse createIndex(String index, Settings settings) throws IOException {
        System.out.println("Creating index " + index);
        CreateIndexRequestBuilder createIndexRequest = getIndicesClient().prepareCreate(index);
        createIndexRequest.setSettings(settings);
        return createIndexRequest.get();
    }

    public DeleteIndexResponse deleteIndex(String index) {
        System.out.println("Deleting index " + index);
        DeleteIndexRequestBuilder deleteIndexRequest = getIndicesClient().prepareDelete(index);
        return deleteIndexRequest.get();
    }

    /**
     * Creates given document under given index and given type, if document wit the id already exists overwrites the existing one
     *
     * @param index
     * @param type
     * @param id
     * @param document
     * @return
     */
    public IndexResponse createDocument(String index, String type, String id, String document) {
        IndexRequestBuilder indexRequestBuilder = prepareIndex(index, type, id);
        indexRequestBuilder.setSource(document);
        return indexRequestBuilder.get();
    }

    public DeleteResponse deleteDocument(String index, String type, String id) {
        DeleteRequestBuilder deleteRequestBuilder = client.prepareDelete(index, type, id);
        return deleteRequestBuilder.get();
    }

    public boolean isIndexAvailable(String index) {
        IndicesExistsResponse response = client.admin().indices().prepareExists(index).execute().actionGet();
        return response.isExists();
    }

    public IndexRequestBuilder prepareIndex(String index, String type, String id) {
        return client.prepareIndex(index, type, id);
    }

    private IndicesAdminClient getIndicesClient() {
        return client.admin().indices();
    }

    private ListenableActionFuture execute(ActionRequestBuilder requestBuilder) {
        return requestBuilder.execute();
    }

}

