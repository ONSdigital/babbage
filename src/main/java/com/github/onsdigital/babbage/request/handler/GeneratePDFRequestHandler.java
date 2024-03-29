package com.github.onsdigital.babbage.request.handler;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.pdf.PDFGenerator;
import com.github.onsdigital.babbage.response.BabbageBinaryResponse;
import com.github.onsdigital.babbage.response.base.BabbageResponse;
import com.github.onsdigital.babbage.util.json.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Request handler for the generation of pdf files based on page content
 */
public class GeneratePDFRequestHandler extends PDFRequestHeandler {

    private static final String REQUEST_TYPE = "pdf-new";
    private static final String CONTENT_TYPE = "application/pdf";

    @Override
    public BabbageResponse get(String requestedUri, HttpServletRequest request) throws Exception {

        String uriPath = StringUtils.removeStart(requestedUri, "/");
        info().data("uri", uriPath).log("generating PDF for uri");
        String pdfTable = getPDFTables(uriPath);
        if (pdfTable != null) {
            info().data("uri", uriPath).log("generating PDF table for uri");
        }
        Path pdfFile = PDFGenerator.generatePdf(requestedUri, getTitle(requestedUri), pdfTable);
        InputStream fin = Files.newInputStream(pdfFile);
        BabbageBinaryResponse response = new BabbageBinaryResponse(fin, CONTENT_TYPE);
        response.addHeader("Content-Length", Long.toString(FileUtils.sizeOf(pdfFile.toFile())));
        response.addHeader("Content-Disposition", "attachment; filename=\"" + pdfFile.getFileName() + "\"");
        return response;
    }

    @Override
    public String getRequestType() {
        return REQUEST_TYPE;
    }

    private String getPDFTables(String uri) throws IOException, ContentReadException {
        ContentResponse contentResponse = getContent(uri, null);
        Map<String, Object> stringObjectMap = JsonUtil.toMap(contentResponse.getDataStream());

        List<Map<String, Object>> o = (List<Map<String, Object>>) stringObjectMap.get("pdfTable");

        if (o != null && o.size() > 0) {
            String filename = (String) o.get(0).get("file");
            String file = uri + "/" + filename;
            ContentResponse pdfTableContentResponse = ContentClient.getInstance().getResource(file);
            File targetFile = new File(FileUtils.getTempDirectoryPath() + "/" + filename);
            FileUtils.copyInputStreamToFile(pdfTableContentResponse.getDataStream(), targetFile);
            return targetFile.getAbsolutePath();
        }

        return null;
    }

}
