package com.github.onsdigital.request.handler;

import com.github.onsdigital.configuration.Configuration;
import com.github.onsdigital.content.link.PageReference;
import com.github.onsdigital.content.page.base.Page;
import com.github.onsdigital.content.page.list.ListPage;
import com.github.onsdigital.content.partial.SearchResult;
import com.github.onsdigital.content.service.ContentNotFoundException;
import com.github.onsdigital.content.util.ContentUtil;
import com.github.onsdigital.data.zebedee.ZebedeeClient;
import com.github.onsdigital.data.zebedee.ZebedeeRequest;
import com.github.onsdigital.request.handler.base.RequestHandler;
import com.github.onsdigital.request.response.BabbageResponse;
import com.github.onsdigital.request.response.BabbageStringResponse;
import com.github.onsdigital.template.TemplateService;
import com.github.onsdigital.util.NavigationUtil;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Render a list page for the given URI.
 */
public class PreviousReleasesRequestHandler implements RequestHandler {

    private static final String REQUEST_TYPE = "previousreleases";

    public static final String CONTENT_TYPE = "text/html";

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request) throws Exception {
        return get(requestedUri, request, null);
    }

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request, ZebedeeRequest zebedeeRequest) throws Exception {

        // read the previous releases by looking at the file system. To be replaced with a search engine query.
        ListPage page = new ListPage();
        List<PageReference> pageReferences;

        if (zebedeeRequest != null) {
            pageReferences = readFromZebedee(requestedUri, zebedeeRequest);
        } else {
            pageReferences = readFromLocal(requestedUri);
        }

        // build the search result page dynamically instead of from a json file.
        SearchResult result = new SearchResult();
        result.setResults(pageReferences);
        result.setNumberOfResults(pageReferences.size());
        page.setContentSearchResult(result);

        //TODO: Read navigaton from zebedee if zebedee request ????
        page.setNavigation(NavigationUtil.getNavigation());

        // populate the page reference descriptions.
        DataRequestHandler dataRequestHandler = new DataRequestHandler();
        for (PageReference pageReference : pageReferences) {
            Page referencedPage = dataRequestHandler.readAsPage(pageReference.getUri().toString(), false, zebedeeRequest);
            pageReference.setDescription(referencedPage.getDescription());
        }

        String html = TemplateService.getInstance().renderPage(page);
        return new BabbageStringResponse(html, CONTENT_TYPE);
    }

    private List<PageReference> readFromZebedee(String uri, ZebedeeRequest zebedeeRequest) throws ContentNotFoundException, IOException {
        List<PageReference> pages = new ArrayList<>();

        // make request to browse api
        ZebedeeClient zebedeeClient = new ZebedeeClient(zebedeeRequest);
        try {
            DirectoryListing directoryListing = ContentUtil.deserialise(zebedeeClient.get("browse", uri, false), DirectoryListing.class);

            for (String folder : directoryListing.folders.keySet()) {
                pages.add(new PageReference(URI.create(uri + "/" + folder)));
            }
        } finally {
            zebedeeClient.closeConnection();
        }

        return pages;
    }


    private List<PageReference> readFromLocal(String requestedUri) throws IOException {
        Path taxonomyPath = FileSystems.getDefault().getPath(Configuration.getContentPath());

        requestedUri = StringUtils.removeStart(requestedUri, "/");
        Path path = taxonomyPath.resolve(requestedUri);

        List<PageReference> pages = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    pages.add(new PageReference(path.toUri()));
                }
            }
            return pages;
        } catch (DOMException | MalformedURLException e) {
            throw new IOException("Error iterating taxonomy", e);
        }
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

    class DirectoryListing {
        public Map<String, String> folders = new HashMap<>();
        public Map<String, String> files = new HashMap<>();
    }
}
