package com.github.onsdigital.babbage.pdf;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.InputSource;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

/**
 * Created by bren on 08/07/15.
 */
public class PDFGenerator {

    private static final String TEMP_DIRECTORY_PATH = FileUtils.getTempDirectoryPath();
    private static final String URL = "http://localhost:8080";

    public static Path generatePdf(String uri, String fileName, String pdfTable) {

        try {
            ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);
            String html;
            try (InputStream dataStream = contentResponse.getDataStream()) {
                LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
                additionalData.put("pdf_style", true);
                html = TemplateService.getInstance().renderTemplate("pdf/pdf", dataStream, additionalData);

                html = html.replace("\"\"/>", " \"/>"); // img tags from markdown have an extra " at the end of the tag for some reason
                html = html.replace("alt= \"/>", "alt=\"\"/>");//above replacement adds an incorrect alt tag
                html = Jsoup.parse(html, URL, Parser.xmlParser()).toString();
                html = html.replace("&nbsp;", "&#160;");
            }

            String outputFile = TEMP_DIRECTORY_PATH + "/" + fileName + ".pdf";
            InputStream inputStream = new ByteArrayInputStream(html.getBytes());
            createPDF(uri, inputStream, outputFile);

            Path pdfFile = FileSystems.getDefault().getPath(TEMP_DIRECTORY_PATH).resolve(fileName + ".pdf");
            if (!Files.exists(pdfFile)) {
                throw new RuntimeException("Failed generating pdf, file not created");
            }

            addDataTableToPdf(fileName, pdfTable, pdfFile);

            return pdfFile;
        } catch (Exception ex) {
            error().exception(ex)
                    .data("uri", uri)
                    .data("filename", fileName)
                    .log("error generating PDF for uri");
            throw new RuntimeException("Failed generating pdf", ex);
        }
    }

    private static void createPDF(String url, InputStream input, String outputFile)
            throws IOException, DocumentException, com.lowagie.text.DocumentException {

        OutputStream os = null;

        try {
            os = new FileOutputStream(outputFile);
            ITextRenderer renderer = new ITextRenderer(4.1666f, 3);

            // Create a chain of custom classes to manipulate the HTML.
            renderer.getSharedContext().setReplacedElementFactory(
                    new HtmlImageReplacedElementFactory(
                            new EquationImageInserter(
                                    new ChartImageReplacedElementFactory(
                                            renderer.getSharedContext().getReplacedElementFactory()))));

            Document doc = XMLResource.load(new InputSource(input)).getDocument();

            renderer.setDocument(doc, url);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;

        } catch (Exception ex) {
            error().exception(ex)
                    .data("url", url)
                    .data("outputFile", outputFile)
                    .log("error creating PDF");
            throw ex;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static void addDataTableToPdf(String fileName, String pdfTable, Path pdfFile) throws IOException {
        if (pdfTable != null) {

            PDFMergerUtility ut = new PDFMergerUtility();
            ut.addSource(TEMP_DIRECTORY_PATH + "/" + fileName + ".pdf");
            ut.addSource(pdfTable);
            ut.setDestinationFileName(TEMP_DIRECTORY_PATH + "/" + fileName + "-merged.pdf");
            ut.mergeDocuments(null);; // null means unrestricted memory usage
            
            Path mergedPdfFile = FileSystems.getDefault().getPath(TEMP_DIRECTORY_PATH).resolve(fileName + "-merged.pdf");
            if (!Files.exists(mergedPdfFile)) {
                throw new RuntimeException("Failed generating pdf, file not created");
            }

            Files.delete(pdfFile);
            Files.move(mergedPdfFile, pdfFile);
        }
    }
}
