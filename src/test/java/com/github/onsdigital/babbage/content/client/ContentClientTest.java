package com.github.onsdigital.babbage.content.client;

import com.github.onsdigital.babbage.publishing.PublishingManager;
import com.github.onsdigital.babbage.publishing.model.PublishInfo;
import com.github.onsdigital.babbage.util.TestsUtil;
import com.github.onsdigital.babbage.util.http.PooledHttpClient;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

public class ContentClientTest {

    //  Test target.
    @InjectMocks
    @Spy
    private ContentClient contentClient;

    @Mock
    private PublishingManager publishingManagerMock;

    @Mock
    private static PooledHttpClient clientMock;

    @Mock
    private CloseableHttpResponse closeableHttpResponseMock;

    @Mock
    private HttpEntity httpEntityMock;

    private String uriStr = "economy/environmentalaccounts/articles/environmentaltaxes/2015-06-01";
    private int maxAgeSeconds = 900;
    private int postPublishMaxAgeSeconds = 10;
    private int postPublishExpiryOffsetSeconds = 180;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        contentClient = ContentClient.getInstance();

        TestsUtil.setPrivateField(contentClient, "publishingManager", publishingManagerMock);
        TestsUtil.setPrivateStaticField(contentClient, "client", clientMock);
        TestsUtil.setPrivateStaticField(contentClient, "cacheEnabled", true);
        TestsUtil.setPrivateStaticField(contentClient, "maxAge", maxAgeSeconds);
        TestsUtil.setPrivateStaticField(contentClient, "postPublishCacheMaxAge", postPublishMaxAgeSeconds);
        TestsUtil.setPrivateStaticField(contentClient, "postPublishCacheExpiryOffset", postPublishExpiryOffsetSeconds);
    }

    //** Provides a PublishingManager response based on secondsUntilPublish */
    private void generateMockPublishManagerResponse(int secondsUntilPublish) throws Exception {
        Date publishDate = DateTime.now().plusSeconds(secondsUntilPublish).toDate();
        PublishInfo nextPublish = new PublishInfo(uriStr, null, publishDate, null);
        when(publishingManagerMock.getNextPublishInfo(uriStr)).thenReturn(nextPublish);
    }

    //** Sets mock responses to allow the request to work correctly. */
    private void generateMockResponse() throws Exception {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("uri", "/" + uriStr));
        Header[] headers = {
                new BasicHeader("Content-type", "application/json")
        };
        when(httpEntityMock.getContentType()).thenReturn(headers[0].toString());
        when(closeableHttpResponseMock.getEntity()).thenReturn(httpEntityMock);

        List<NameValuePair> parameters2 = new ArrayList<>();
        parameters2.add(new BasicNameValuePair("lang", null));
        parameters2.add(new BasicNameValuePair("uri", uriStr));
        when(clientMock.sendGet("/data", null, parameters2)).thenReturn(closeableHttpResponseMock);
    }
}
