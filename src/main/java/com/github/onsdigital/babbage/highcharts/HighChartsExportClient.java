package com.github.onsdigital.babbage.highcharts;

import com.github.onsdigital.babbage.util.http.ClientConfiguration;
import com.github.onsdigital.babbage.util.http.PooledHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static com.github.onsdigital.babbage.logging.LogBuilder.logEvent;

/**
 * Created by bren on 17/06/15.
 */
public class HighChartsExportClient {


    private static PooledHttpClient client;
    private static HighChartsExportClient instance;

    // singleton
    private HighChartsExportClient() {
    }

    public static HighChartsExportClient getInstance() {
        if (instance == null) {
            synchronized (HighChartsExportClient.class) {
                if (instance == null) {
                    instance = new HighChartsExportClient();
                    logEvent().info("initializing Highcharts export server client connection pool");
                    client = new PooledHttpClient(appConfig().babbage().getExportSeverUrl(), createConfiguration());
                }
            }
        }
        return instance;
    }

    private static ClientConfiguration createConfiguration() {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxTotalConnection(appConfig().babbage().getMaxHighchartsServerConnections());
        return configuration;
    }

    public InputStream getImage(String chartConfig, Integer width) throws IOException {
        return getImage(chartConfig, width, null);
    }

    /**
     * Retrived the image from Highcharts. <b>Caller is responsible for closing the returned {@link InputStream}</b>
     */
    public InputStream getImage(String chartConfig, Integer width, Double scale) throws IOException {
        logEvent().debug("making request to Highcharts export server");
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("options", chartConfig));
        postParameters.add(new BasicNameValuePair("type", "png"));
        if (width != null) {
            postParameters.add(new BasicNameValuePair("width", width.toString()));
        }
        if (scale != null) {
            postParameters.add(new BasicNameValuePair("scale", scale.toString()));
        }

        try (CloseableHttpResponse response = client.sendPost("/", null, postParameters)) {
            logEvent().responseStatus(response.getStatusLine().getStatusCode()).debug("Highcharts export response");

            // try with resources block will close the response InputStream when the method returns.
            // take a copy of the bytes and return a new Inputstream which is the callers responsibility to close.
            byte[] content = IOUtils.toByteArray(response.getEntity().getContent());
            return new ByteArrayInputStream(content);
        } catch (IOException ex) {
            throw logEvent(ex, "Unexpected error while requesting highcharts image").logAndCreateException(404, ex);
        }
    }

}
