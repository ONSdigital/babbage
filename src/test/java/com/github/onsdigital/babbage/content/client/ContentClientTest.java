package com.github.onsdigital.babbage.content.client;

import com.github.onsdigital.babbage.util.TestsUtil;
import com.github.onsdigital.babbage.util.http.PooledHttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ContentClientTest {

    //  Test target.
    @InjectMocks
    @Spy
    private ContentClient contentClient;

    @Mock
    private static PooledHttpClient clientMock;

    @Mock
    private CloseableHttpResponse closeableHttpResponseMock;

    @Mock
    private HttpEntity httpEntityMock;

    private int maxAgeSeconds = 900;
    private int postPublishMaxAgeSeconds = 10;
    private int postPublishExpiryOffsetSeconds = 180;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        contentClient = ContentClient.getInstance();

        TestsUtil.setPrivateStaticField(contentClient, "client", clientMock);
        TestsUtil.setPrivateStaticField(contentClient, "cacheEnabled", true);
        TestsUtil.setPrivateStaticField(contentClient, "maxAge", maxAgeSeconds);
        TestsUtil.setPrivateStaticField(contentClient, "postPublishCacheMaxAge", postPublishMaxAgeSeconds);
        TestsUtil.setPrivateStaticField(contentClient, "postPublishCacheExpiryOffset", postPublishExpiryOffsetSeconds);
    }
}
