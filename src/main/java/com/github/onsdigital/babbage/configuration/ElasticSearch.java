package com.github.onsdigital.babbage.configuration;

import com.github.onsdigital.babbage.logging.LogBuilder;
import com.github.onsdigital.babbage.util.URIUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.github.onsdigital.babbage.configuration.Utils.getValue;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class ElasticSearch {

    private static ElasticSearch INSTANCE = null;

    private static final String SERVER_KEY = "ELASTIC_SEARCH_SERVER";
    private static final String PORT_KEY = "ELASTIC_SEARCH_PORT";
    private static final String INDEX_ALIAS_KEY = "ELASTIC_SEARCH_INDEX_ALIAS";
    private static final String CLUSTER_KEY = "ELASTIC_SEARCH_CLUSTER";
    private static final String HIGHLIGHTS_FILE_KEY = "HIGHLIGHT_URL_BLACKLIST_FILE";

    private final String elasticSearchServer;
    private final String elasticSearchIndexAlias;
    private final Integer elasticSearchPort;
    private final String elasticSearchCluster;
    private final String highlightURLBlacklistFile;
    private final List<String> highlightBlacklist;

    private ElasticSearch() {
        elasticSearchServer = defaultIfBlank(getValue(SERVER_KEY), "localhost");
        elasticSearchPort = Integer.parseInt(defaultIfBlank(getValue(PORT_KEY), "9500"));
        elasticSearchIndexAlias = defaultIfBlank(getValue(INDEX_ALIAS_KEY), "ons");
        elasticSearchCluster = defaultIfBlank(getValue(CLUSTER_KEY), "");
        highlightURLBlacklistFile = defaultIfBlank(getValue(HIGHLIGHTS_FILE_KEY), "highlight-url-blacklist");
        highlightBlacklist = loadHighlightBlacklist(highlightURLBlacklistFile);
    }

    public List<String> highlightBlacklist() {
        return highlightBlacklist;
    }

    public String host() {
        return elasticSearchServer;
    }

    public Integer port() {
        return elasticSearchPort;
    }

    public String indexAlias() {
        return elasticSearchIndexAlias;
    }

    public String cluster() {
        return elasticSearchCluster;
    }

    public void logConfiguration() {
        LogBuilder.Log()
                .parameter(SERVER_KEY, elasticSearchServer)
                .parameter(PORT_KEY, elasticSearchPort)
                .parameter(INDEX_ALIAS_KEY, elasticSearchIndexAlias)
                .parameter(CLUSTER_KEY, elasticSearchCluster)
                .parameter(HIGHLIGHTS_FILE_KEY, highlightURLBlacklistFile)
                .info("elastic search configuration");
    }

    static ElasticSearch getInstance() {
        if (INSTANCE == null) {
            synchronized (ElasticSearch.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ElasticSearch();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Method to load the list of retired product pages to be hidden
     *
     * @return List of url strings containing the black listed urls
     */
    private static List<String> loadHighlightBlacklist(String highlightURLBlacklistFile) {
        ClassLoader classLoader = Configuration.class.getClassLoader();
        URL fileUrl = classLoader.getResource(highlightURLBlacklistFile);

        List<String> urls = new ArrayList<>();
        if (null != fileUrl) {
            File file = new File(fileUrl.getFile());
            try (BufferedReader bw = new BufferedReader(new FileReader(file))) {
                String blacklistedUrl;

                while ((blacklistedUrl = bw.readLine()) != null) {
                    urls.add(URIUtil.cleanUri(blacklistedUrl));
                }
            } catch (IOException e) {
                // Print additional info out to stderr
                String message = "Error while attempting to load highlight blacklist file.";
                System.out.println(message);
                e.printStackTrace();
                // Unable to load the file, so return an empty ArrayList (won't black list any urls)
                return new ArrayList<>();
            }
        }
        return urls;
    }
}
