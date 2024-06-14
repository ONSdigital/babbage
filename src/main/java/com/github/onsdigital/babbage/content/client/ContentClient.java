package com.github.onsdigital.babbage.content.client;

import com.github.onsdigital.babbage.error.ResourceNotFoundException;
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
import java.util.*;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;
import static org.apache.commons.lang3.StringUtils.isEmpty;

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
    private static int maxAge = appConfig().babbage().getDefaultContentCacheTime();
    private static boolean postPublishMicroCacheEnabled = appConfig().babbage().isPostPublishMicroCacheEnabled();
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
        return sendGet(getPath(DATA_ENDPOINT), addUri(uri, getParameters(queryParameters)));
    }


    public ContentResponse getDatasetSummaries(String uri) throws ContentReadException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair(URI_PARAM, uri));
        return sendGet(getPath(RESOLVE_DATASETS_ENDPOINT), addUri(uri, nameValuePairs));
    }


    public ContentResponse getResource(String uri) throws ContentReadException {
        return sendGet(getPath(RESOURCE_ENDPOINT), addUri(uri, new ArrayList<>()));
    }

    public ContentResponse getFileSize(String uri) throws ContentReadException {
        return sendGet(getPath(FILE_SIZE_ENDPOINT), addUri(uri, new ArrayList<>()));
    }

    public ContentResponse getParents(String uri) throws ContentReadException {
        return sendGet(getPath(PARENTS_ENDPOINT), addUri(uri, new ArrayList<>()));
    }

    public ContentResponse getGenerator(String uri, Map<String, String[]> queryParameters) throws ContentReadException {
        return sendGet(getPath(GENERATOR_ENDPOINT), addUri(uri, getParameters(queryParameters)));
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
        Optional<NameValuePair> uriPair = Optional.empty();
        if (path.equals("/data")) {
            uriPair = getParameters.parallelStream().filter(nameValuePair -> nameValuePair.getName().equals("uri")).findFirst();
        }
        try {
            if (uriPair.isPresent() && Objects.equals(getSecondLastSegment(uriPair.get().getValue()), "latest")) {
                throw new InvalidURIException(HttpStatus.SC_BAD_REQUEST, "invalid uri");
            }
            return new ContentResponse(client.sendGet(path, getHeaders(), getParameters));
        } catch (InvalidURIException e) {
            info().data("uri", path).log("ContentClient requested uri invalid");
            throw wrapException(path, e);

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

    public static String getSecondLastSegment(String uri) {
        if (isEmpty(uri)) {
            return null;
        }
        String[] segments = getSegments(uri);
        if (ArrayUtils.isEmpty(segments)) {
            return null;
        }

        return segments[segments.length - 2];
    }

    public static String[] getSegments(String uri) {
        return uri.split("/");
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
        uri = isEmpty(uri) ? "/" : uri;
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

    private ContentReadException wrapException(String uri, InvalidURIException e) {
        error().exception(e).log("ContentClient request returned error");
        return new ContentReadException(e.getStatusCode(), e.getMessage(), e);
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
