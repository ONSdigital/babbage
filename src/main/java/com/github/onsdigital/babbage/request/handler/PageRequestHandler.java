package com.github.onsdigital.babbage.request.handler;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.request.handler.base.BaseRequestHandler;
import com.github.onsdigital.babbage.response.BabbageContentBasedStringResponse;
import com.github.onsdigital.babbage.response.BabbageRedirectResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import com.github.onsdigital.babbage.util.RequestUtil;
import com.github.onsdigital.babbage.util.URIUtil;
import javax.servlet.http.HttpServletRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;

import static com.github.onsdigital.babbage.configuration.ApplicationConfiguration.appConfig;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Created by bren on 28/05/15.
 * <p>
 * Serves rendered html output
 */
public class PageRequestHandler extends BaseRequestHandler {

    private static final String REQUEST_TYPE = "/";
    private static final String PDF = "pdf";
    private static final String PDF_STYLE = "pdf_style";
    private static final String ENABLE_COVID19_FEATURE = "EnableCovid19Feature";

    private static final List<String> serialisedContentTypes = Arrays.asList("bulletin", "article", "compendium_landing_page");

    @Override
    public BabbageResponse get(String uri, HttpServletRequest request) throws IOException, ContentReadException {
        return getPage(uri, request);
    }

    public BabbageResponse getPage(String uri, HttpServletRequest request) throws ContentReadException, IOException {
        ContentResponse contentResponse = getContent(uri);

        JsonObject jsonResponse = JsonParser.parseString(contentResponse.getAsString()).getAsJsonObject();

        JsonElement migrationLink = jsonResponse.getAsJsonObject("description").get("migrationLink");
        JsonElement contentType = jsonResponse.get("type");

        if (migrationLink != null && contentType != null && shouldRedirect(uri, contentType.getAsString(), migrationLink.getAsString())) {
            return new BabbageRedirectResponse(migrationLink.getAsString());
        }

        try (InputStream dataStream = contentResponse.getDataStream()) {
            LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
            if (RequestUtil.getQueryParameters(request).containsKey(PDF)) {
                additionalData.put(PDF_STYLE, true);
            }
            additionalData.put(ENABLE_COVID19_FEATURE, appConfig().handlebars().isEnableCovid19Feature());
            String html = getTemplateService().renderContent(dataStream, additionalData);
            return new BabbageContentBasedStringResponse(contentResponse, html, TEXT_HTML);
        }
    }

    /**
     * Should redirect assess if this particular page requested should be redirected
     * based on
     *
     * @param uri - uri received in Babbage
     * @param contentType - content type from zebedee
     * @param migrationLink - place to redirect to
     */
    public boolean shouldRedirect(String uri, String contentType, String migrationLink) {

        boolean matchedSerialisedContentType = serialisedContentTypes.contains(contentType);
        boolean migrationLinkSet = !StringUtils.isBlank(migrationLink);
        boolean isLatestRequest = URIUtil.isLatestRequest(uri);

        // These are two separate if statements as otherwise the logic gets a bit impenetrable.
        if (migrationLinkSet && matchedSerialisedContentType && isLatestRequest) {
            return true;
        }

        if (migrationLinkSet && !matchedSerialisedContentType){
            return true;
        }

        return false;
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

   /**
     * This is an abstraction to allow mocking for unit tests.
     */
    protected ContentResponse getContent(String uri) throws IOException, ContentReadException {
        return ContentClient.getInstance().getContent(uri);
    }

    /**
     * This is an abstraction to allow mocking for unit tests.
     */
    protected TemplateService getTemplateService() {
        return TemplateService.getInstance();
    }
}
