package com.github.onsdigital.babbage.content.client;

import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.metrics.Metrics;
import com.github.onsdigital.babbage.metrics.MetricsFactory;
import com.github.onsdigital.babbage.publishing.PublishingManager;
import com.github.onsdigital.babbage.publishing.model.PublishInfo;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.ThreadContext;
import com.github.onsdigital.babbage.util.http.ClientConfiguration;
import com.github.onsdigital.babbage.util.http.PooledHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by bren on 23/07/15.
 * <p/>
 * Content service reads content from external server over http.
 * <p/>
 * Using Apache http client with connection pooling.
 */
//TODO: Get http client use cache headers returned from content service
public class ContentClient {

    private static final String TOKEN_HEADER = "X-Florence-Token";
    private static boolean cacheEnabled = appConfig().babbage().isCacheEnabled();
    private static int maxAge = appConfig().babbage().getDefaultContentCacheTime();
    private static int postPublishCacheMaxAge = appConfig().babbage().getPostPublishCacheMaxAge();
    private static int postPublishCacheExpiryOffset = appConfig().babbage().getPostPublishCacheExpiryOffset();

    private static PooledHttpClient client;
    private static ContentClient instance;

    private static final String DATA_ENDPOINT = "/data";
    private static final String PARENTS_ENDPOINT = "/parents";
    private static final String RESOURCE_ENDPOINT = "/resource";
    private static final String FILE_SIZE_ENDPOINT = "/filesize";
    private static final String REINDEX_ENDPOINT = "/reindex";
    private static final String GENERATOR_ENDPOINT = "/generator";
    private static final String EXPORT_ENDPOINT = "/export";
    private static final String RESOLVE_DATASETS_ENDPOINT = "/resolveDatasets";
    private static final String URI_PARAM = "uri";

    private Metrics metrics = MetricsFactory.getMetrics();
    private PublishingManager publishingManager = PublishingManager.getInstance();

    //singleton
    private ContentClient() {

    }

    public static ContentClient getInstance() {
        if (instance == null) {
            synchronized (ContentClient.class) {
                if (instance == null) {
                    info().log("initialising ContentClient instance");
                    instance = new ContentClient();
                    info().log("initialising PooledHttpClient for ContentClient instance");
                    client = new PooledHttpClient(appConfig().contentAPI().serverURL(), createConfiguration());
                }
            }
        }
        return instance;
    }

    private static ClientConfiguration createConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxTotalConnection(appConfig().contentAPI().maxConnections());
        configuration.setDisableRedirectHandling(true);
        return configuration;
    }


    /**
     * Serves requested content data, stream should be closed after use or fully consumed, fully consuming the stream will close the stream automatically
     * Content  client forwards any requests headers and cookies to content service using saved ThreadContext
     * <p/>
     * Any request headers like authentication tokens or collection ids should be saved to ThreadContext before calling content service
     *
     * @param uri uri for requested content.
     *            e.g./economy/inflationandpriceindices for content data requests
     *            e.g./economy/inflationandpriceindices/somecontent.xls  ( no data.json after the uri )
     * @return Json for requested content
     * @throws ContentReadException If content read fails due content service error status is set to whatever error is sent back from content service,
     *                              all other IO Exceptions are rethrown with HTTP status 500
     */
    public ContentResponse getContent(String uri) throws ContentReadException, IOException {
        return getContent(uri, null);
    }

    /**
     * Serves requested content data, stream should be closed after use or fully consumed, fully consuming the stream will close the stream automatically
     * Content  client forwards any requests headers and cookies to content service using saved ThreadContext
     * <p/>
     * Any request headers like authentication tokens or collection ids should be saved to ThreadContext before calling content client
     *
     * @param uri             uri for requested content.
     *                        e.g./economy/inflationandpriceindices for content data requests
     *                        e.g./economy/inflationandpriceindices/somecontent.xls  ( no data.json after the uri )
     * @param queryParameters GET parameters that needs to be passed to content service (e.g. filter parameters)
     * @return Json for requested content
     * @throws ContentReadException If content read fails due content service error status is set to whatever error is sent back from content service,
     *                              all other IO Exceptions are rethrown with HTTP status 500
     */
    public ContentResponse getContent(String uri, Map<String, String[]> queryParameters) throws ContentReadException {
        return resolveMaxAge(uri, sendGet(getPath(DATA_ENDPOINT), addUri(uri, getParameters(queryParameters))));
    }


    public ContentResponse getDatasetSummaries(String uri) throws ContentReadException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair(URI_PARAM, uri));
        return sendGet(getPath(RESOLVE_DATASETS_ENDPOINT), addUri(uri, nameValuePairs));
    }


    public ContentResponse getResource(String uri) throws ContentReadException {
        return resolveMaxAge(uri, sendGet(getPath(RESOURCE_ENDPOINT), addUri(uri, new ArrayList<>())));
    }

    public ContentResponse getFileSize(String uri) throws ContentReadException {
        return resolveMaxAge(uri, sendGet(getPath(FILE_SIZE_ENDPOINT), addUri(uri, new ArrayList<>())));
    }

    public ContentResponse getParents(String uri) throws ContentReadException {
        return sendGet(getPath(PARENTS_ENDPOINT), addUri(uri, new ArrayList<>()));
    }

    public ContentResponse getGenerator(String uri, Map<String, String[]> queryParameters) throws ContentReadException {
        return resolveMaxAge(uri, sendGet(getPath(GENERATOR_ENDPOINT), addUri(uri, getParameters(queryParameters))));
    }


    private ContentResponse resolveMaxAge(String uri, ContentResponse response) {
        if (!cacheEnabled) {
            return response;
        }

        try {
            PublishInfo nextPublish = publishingManager.getNextPublishInfo(uri);
            Date nextPublishDate = nextPublish == null ? null : nextPublish.getPublishDate();
            Integer timeToExpire = null;
            if (nextPublishDate != null) {
                Long time = (nextPublishDate.getTime() - new Date().getTime()) / 1000;
                timeToExpire = time.intValue();
            }

            if (timeToExpire == null) {
                response.setMaxAge(maxAge);
                //increment count of requests where publish date is not present
                metrics.incPublishDateNotPresent();
            } else if (timeToExpire > 0) {
                // requested uri cache expiry is set as either:-
                // 1. the time remaining until the publishing time or
                // 2. the maximum cache expiry time permitted
                if (timeToExpire < maxAge) {
                    //increment count of requests where the cache expiry is set as
                    //the time remaining until the publishing time
                    metrics.incPublishDateInRange();
                    response.setMaxAge(timeToExpire);
                } else {
                    //increment count of requests where the cache expiry is set as
                    //the maximum cache expiry time permitted
                    metrics.incPublishDateTooFarInFuture();
                    response.setMaxAge(maxAge);
                }
            } else if (timeToExpire <= 0 && Math.abs(timeToExpire) < postPublishCacheExpiryOffset) {
                // For a configured timeout after publish, set cache headers to a lower value to prevent
                // caching old content.
                response.setMaxAge(postPublishCacheMaxAge);
            } else if (timeToExpire <= 0 && Math.abs(timeToExpire) >= postPublishCacheExpiryOffset) {
                //if publish is due but there is still a publish date record after an hour drop it
                info().data("uri", uri).log("dropping publish date record due to publish wait timeout for uri");
                publishingManager.dropPublishDate(nextPublish);
                //increment count of requests where publish date is more than an hour ago
                metrics.incPublishDateTooFarInPast();
                return resolveMaxAge(uri, response);//resolve for next publish date if any
            }
        } catch (Exception e) {
            error().exception(e)
                    .data("uri", uri)
                    .log("managing publish date failed for uri, skipping setting cache times");
        }
        return response;
    }

    /**
     * Calls zebedee to export given time series as uri list
     *
     * @param format
     * @param uriList
     * @return
     * @throws ContentReadException
     */
    public ContentResponse export(String format, String[] uriList) throws ContentReadException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("format", format));
        if (uriList != null) {
            for (String s : uriList) {
                parameters.add(new BasicNameValuePair("uri", s));
            }
        }
        return sendPost(getPath(EXPORT_ENDPOINT), parameters);
    }

    public ContentResponse reIndex(String key, String uri) throws ContentReadException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("key", key));
        parameters.add(new BasicNameValuePair("uri", uri));
        return sendPost(REINDEX_ENDPOINT, parameters);
    }

    public ContentResponse deleteIndex(String key, String uri, String contentType) throws ContentReadException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("key", key));
        parameters.add(new BasicNameValuePair("uri", uri));
        parameters.add(new BasicNameValuePair("pageType", contentType));
        return sendDelete(REINDEX_ENDPOINT, parameters);
    }

    public ContentResponse reIndexAll(String key) throws ContentReadException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("key", key));
        parameters.add(new BasicNameValuePair("all", "1"));
        return sendPost(REINDEX_ENDPOINT, parameters);
    }

    private ContentResponse sendGet(String path, List<NameValuePair> getParameters) throws ContentReadException {
        CloseableHttpResponse response = null;
        try {
            return new ContentResponse(client.sendGet(path, getHeaders(), getParameters));
        } catch (HttpResponseException e) {
            IOUtils.closeQuietly(response);

            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                info().data("uri", path).log("ContentClient requested uri not found");
                throw new ResourceNotFoundException(e.getMessage());
            }

            throw wrapException(path, e);

        } catch (IOException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(path, e);
        }
    }

    private ContentResponse sendPost(String path, List<NameValuePair> postParameters) throws ContentReadException {
        ;
        CloseableHttpResponse response = null;
        try {
            return new ContentResponse(client.sendPost(path, getHeaders(), postParameters));
        } catch (HttpResponseException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(path, e);
        } catch (IOException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(path, e);
        }
    }

    private ContentResponse sendDelete(String path, List<NameValuePair> postParameters) throws ContentReadException {
        CloseableHttpResponse response = null;
        try {
            return new ContentResponse(client.sendDelete(path, getHeaders(), postParameters));
        } catch (HttpResponseException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(path, e);
        } catch (IOException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(path, e);
        }
    }

    private List<NameValuePair> getParameters(Map<String, String[]> parametes) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();

        nameValuePairs.add(new BasicNameValuePair("lang", (String) ThreadContext.getData(RequestUtil.LANG_KEY)));
        nameValuePairs.addAll(toNameValuePair(parametes));
        return nameValuePairs;
    }

    private List<NameValuePair> addUri(String uri, List<NameValuePair> parameters) {
        if (parameters == null) {
            return null;
        }
        uri = StringUtils.isEmpty(uri) ? "/" : uri;
        //uris are requested as get parameter from content service
        parameters.add(new BasicNameValuePair("uri", uri));
        return parameters;
    }

    private List<NameValuePair> toNameValuePair(Map<String, String[]> parametes) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        if (parametes != null) {
            for (Iterator<Map.Entry<String, String[]>> iterator = parametes.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String[]> entry = iterator.next();
                String[] values = entry.getValue();
                if (ArrayUtils.isEmpty(values)) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), null));
                    continue;
                }
                for (int i = 0; i < values.length; i++) {
                    String s = entry.getValue()[i];
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), values[i]));
                }
            }
        }
        return nameValuePairs;
    }


    private ContentReadException wrapException(String uri, HttpResponseException e) {
        error().exception(e).log("ContentClient request returned error");
        return new ContentReadException(e.getStatusCode(), "Failed reading from content service", e);
    }

    private ContentReadException wrapException(String uri, IOException e) {
        error().exception(e).log("ContentClient request returned error");
        return new ContentReadException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Failed reading from content service", e);
    }

    //Reads collection cookie saved in thread context
    private String getCollectionId() {
        Map<String, String> cookies = (Map<String, String>) ThreadContext.getData("cookies");
        if (cookies != null) {
            return cookies.get("collection");
        }
        return null;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> cookies = (Map<String, String>) ThreadContext.getData("cookies");
        if (cookies != null) {
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put(TOKEN_HEADER, cookies.get("access_token"));
            return headers;
        }
        return null;
    }

    private String getPath(String endpoint) {
        String collectionId = getCollectionId();
        if (collectionId == null) {
            return endpoint;
        } else {
            return endpoint + "/" + collectionId;
        }
    }

    /**
     * Create query parameters to filter content to be passed to content client
     *
     * @param filter
     * @return
     */
    public static Map<String, String[]> filter(ContentFilter filter) {
        if (filter == null) {
            return Collections.emptyMap();
        }
        return params(filter.name().toLowerCase(), null);
    }

    /**
     * Create query parameters to get depth of taxonomy content request
     *
     * @param depth
     * @return
     */
    public static Map<String, String[]> depth(Integer depth) {
        if (depth == null) {
            return params("depth", 1);
        }
        return params("depth", depth);
    }

    public static Map<String, String[]> params(String key, Object... values) {
        HashMap<String, String[]> parameterMap = new HashMap<>();
        if (values == null) {
            parameterMap.put(key, null);
        } else {
            String[] strings = new String[values.length];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = String.valueOf(values[i]);
            }
            parameterMap.put(key, strings);
        }
        return parameterMap;
    }
}
