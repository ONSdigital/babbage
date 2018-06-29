package com.github.onsdigital.babbage.content.client;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.babbage.publishing.PublishingManager;
import com.github.onsdigital.babbage.publishing.model.PublishInfo;
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

import static com.github.onsdigital.babbage.configuration.Configuration.CONTENT_SERVICE.*;

/**
 * Created by bren on 23/07/15.
 * <p/>
 * Content service reads content from deprecated server over http.
 * <p/>
 * Using Apache http client with connection pooling.
 */
//TODO: Get http client use cache headers returned from content service
public class ContentClient {

    private static final String TOKEN_HEADER = "X-Florence-Token";


    private static PooledHttpClient client;
    private static ContentClient instance;

    //singleton
    private ContentClient() {
    }

    public static ContentClient getInstance() {
        if (instance == null) {
            synchronized (ContentClient.class) {
                if (instance == null) {
                    instance = new ContentClient();
                    System.out.println("Initializing content service http client");
                    client = new PooledHttpClient(getServerUrl(), createConfiguration());
                }
            }
        }
        return instance;
    }

    private static ClientConfiguration createConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxTotalConnection(getMaxContentServiceConnection());
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
        System.out.println("getContent(): Reading content from content server, uri:" + uri);
        return resolveMaxAge(uri, sendGet(getPath(getDataEndpoint()), addUri(uri, getParameters(queryParameters))));
    }

    public ContentResponse getResource(String uri) throws ContentReadException {
        System.out.println("getResource(): Reading resource from content server, uri:" + uri);
        return resolveMaxAge(uri, sendGet(getPath(getResourceEndpoint()), addUri(uri, new ArrayList<>())));
    }

    public ContentResponse getFileSize(String uri) throws ContentReadException {
        return resolveMaxAge(uri, sendGet(getPath(getFileSizeEndpoint()), addUri(uri, new ArrayList<>())));
    }

    public ContentResponse getTaxonomy(Map<String, String[]> queryParameters) throws ContentReadException {
        return sendGet(getPath(getTaxonomyEndpoint()), getParameters(queryParameters));
    }

    public ContentResponse getTaxonomy() throws ContentReadException {
        return sendGet(getPath(getTaxonomyEndpoint()), null);
    }

    public ContentResponse getParents(String uri) throws ContentReadException {
        return sendGet(getPath(getParentsEndpoint()), addUri(uri, new ArrayList<>()));
    }

    public ContentResponse getGenerator(String uri, Map<String, String[]> queryParameters) throws ContentReadException {
        return resolveMaxAge(uri, sendGet(getPath(getGeneratorEndpoint()), addUri(uri, getParameters(queryParameters))));
    }


    private ContentResponse resolveMaxAge(String uri, ContentResponse response) {
        if (!Configuration.GENERAL.isCacheEnabled()) {
            return response;
        }

        try {
            PublishInfo nextPublish = PublishingManager.getInstance().getNextPublishInfo(uri);
            Date nextPublishDate = nextPublish == null ? null : nextPublish.getPublishDate();
            int maxAge = Configuration.GENERAL.getDefaultContentCacheTime();
            Integer timeToExpire = null;
            if (nextPublishDate != null) {
                Long time = (nextPublishDate.getTime() - new Date().getTime()) / 1000;
                timeToExpire = time.intValue();
            }

            if (timeToExpire == null) {
                response.setMaxAge(maxAge);
            } else if (timeToExpire > 0) {
                response.setMaxAge(timeToExpire < maxAge ? timeToExpire : maxAge);
            } else if (timeToExpire < 0 && Math.abs(timeToExpire) > Configuration.GENERAL.getPublishCacheTimeout()) {
                //if publish is due but there is still a publish date record after an hour drop it
                System.out.println("Dropping publish date record due to publish wait timeout for " + uri);
                PublishingManager.getInstance().dropPublishDate(nextPublish);
                return resolveMaxAge(uri, response);//resolve for next publish date if any
            }
        } catch (Exception e) {
            System.err.println("!!!!!!!!!!!!Warning: Managing publish date failed  for uri " + uri + ". Skipping setting cache times");
            e.printStackTrace();
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
            for (int i = 0; i < uriList.length; i++) {
                parameters.add(new BasicNameValuePair("uri", uriList[i]));
            }
        }
        return sendPost(getPath(getExportEndpoint()), parameters);
    }

    public ContentResponse reIndex(String key, String uri) throws ContentReadException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("key", key));
        parameters.add(new BasicNameValuePair("uri", uri));
        return sendPost(getReindexEndpoint(), parameters);
    }

    public ContentResponse deleteIndex(String key, String uri, String contentType) throws ContentReadException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("key", key));
        parameters.add(new BasicNameValuePair("uri", uri));
        parameters.add(new BasicNameValuePair("pageType", contentType));
        return sendDelete(getReindexEndpoint(), parameters);
    }

    public ContentResponse reIndexAll(String key) throws ContentReadException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("key", key));
        parameters.add(new BasicNameValuePair("all", "1"));
        return sendPost(getReindexEndpoint(), parameters);
    }

    private ContentResponse sendGet(String path, List<NameValuePair> getParameters) throws ContentReadException {
        CloseableHttpResponse response = null;
        try {
            return new ContentResponse(client.sendGet(path, getHeaders(), getParameters));
        } catch (HttpResponseException e) {
            IOUtils.closeQuietly(response);

            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND)
                throw new ResourceNotFoundException(e.getMessage());

            throw wrapException(e);

        } catch (IOException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(e);
        }
    }

    private ContentResponse sendPost(String path, List<NameValuePair> postParameters) throws ContentReadException {
        CloseableHttpResponse response = null;
        try {
            return new ContentResponse(client.sendPost(path, getHeaders(), postParameters));
        } catch (HttpResponseException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(e);
        } catch (IOException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(e);
        }
    }

    private ContentResponse sendDelete(String path, List<NameValuePair> postParameters) throws ContentReadException {
        CloseableHttpResponse response = null;
        try {
            return new ContentResponse(client.sendDelete(path, getHeaders(), postParameters));
        } catch (HttpResponseException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(e);
        } catch (IOException e) {
            IOUtils.closeQuietly(response);
            throw wrapException(e);
        }
    }

    private List<NameValuePair> getParameters(Map<String, String[]> parametes) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();

        nameValuePairs.add(new BasicNameValuePair("lang", (String) ThreadContext.getData("lang")));
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


    private ContentReadException wrapException(HttpResponseException e) {
        return new ContentReadException(e.getStatusCode(), "Failed reading from content service", e);
    }

    private ContentReadException wrapException(IOException e) {
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
