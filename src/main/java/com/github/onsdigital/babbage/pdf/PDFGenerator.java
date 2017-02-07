package com.github.onsdigital.babbage.pdf;

import com.github.onsdigital.babbage.configuration.Configuration;
import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.template.TemplateService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.DocumentException;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bren on 08/07/15.
 */
public class PDFGenerator {

    private static final String TEMP_DIRECTORY_PATH = FileUtils.getTempDirectoryPath();
    private static final String URL = "http://localhost:8080";
    private static final String PDF_EXT = ".pdf";

    public static Path generatePdf(String uri, String fileName, Map<String, String> cookies, String pdfTable) {

        try {
            ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);
            String html;
            try (InputStream dataStream = contentResponse.getDataStream()) {
                LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
                additionalData.put("pdf_style", true);
                html = TemplateService.getInstance().renderTemplate("pdf/pdf", dataStream, additionalData);

                html = html.replace("\"\"/>", " \"/>"); // img tags from markdown have an extra " at the end of the tag for some reason
                html = Jsoup.parse(html, URL, Parser.xmlParser()).toString();
                html = html.replace("&nbsp;", "&#160;");
            }

            String outputFile = TEMP_DIRECTORY_PATH + "/" + fileName + PDF_EXT;
            try (
                    InputStream inputStream = new ByteArrayInputStream(html.getBytes());
                    FileSystem fs = FileSystems.getDefault();
                    FileReader fileReader = new FileReader(outputFile);
                    BufferedReader bufferedReader = new BufferedReader(fileReader)
            ) {
                createPDF(uri, inputStream, outputFile);
                Path pdfFile = fs.getPath(TEMP_DIRECTORY_PATH).resolve(fileName + PDF_EXT);

                if (!Files.exists(pdfFile)) {
                    throw new RuntimeException("Failed generating pdf, file not created");
                }
                addDataTableToPdf(fileName, pdfTable, bufferedReader, pdfFile);
                return pdfFile;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed generating pdf", ex);
        }
    }

    public static void createPDF(String url, InputStream input, String outputFile)
            throws IOException, DocumentException, com.lowagie.text.DocumentException {

        OutputStream os = null;

        try {
            os = new FileOutputStream(outputFile);

           /* standard approach
           ITextRenderer renderer = new ITextRenderer();
           renderer.setDocument(url);
           renderer.layout();
           renderer.createPDF(os);
           */

            // ITextRenderer renderer = new ITextRenderer();
            ITextRenderer renderer = new ITextRenderer(4.1666f, 3);

//            ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(renderer.getOutputDevice());
//            callback.setSharedContext(renderer.getSharedContext());
//            renderer.getSharedContext().setUserAgentCallback(callback);
//
//            renderer.getSharedContext().setPrint(true);

//            renderer.getSharedContext().setDotsPerPixel(1);

            // add a custom image replacer
            renderer.getSharedContext().setReplacedElementFactory(
                    new HtmlImageReplacedElementFactory(
                            new ChartImageReplacedElementFactory(
                                    renderer.getSharedContext().getReplacedElementFactory())));

            Document doc = XMLResource.load(new InputSource(input)).getDocument();

            renderer.setDocument(doc, url);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;
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

    private static class ResourceLoaderUserAgent extends ITextUserAgent {
        public ResourceLoaderUserAgent(ITextOutputDevice outputDevice) {
            super(outputDevice);
        }

        protected InputStream resolveAndOpenStream(String uri) {
            InputStream is = super.resolveAndOpenStream(uri);
            System.out.println("IN resolveAndOpenStream() " + uri);
            return is;
        }
    }

    public static Path generatePdfUsingPhantom(String uri, String fileName, Map<String, String> cookies, String pdfTable) {
        String[] command = {
                Configuration.PHANTOMJS.getPhantomjsPath(), "target/web/js/generatepdf.js", URL + uri + "?pdf=1", "" + TEMP_DIRECTORY_PATH + "/" + fileName + ".pdf"
        };

        Iterator<Map.Entry<String, String>> iterator = cookies.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            command = ArrayUtils.add(command, next.getKey());
            command = ArrayUtils.add(command, next.getValue());
        }

        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            try
                    (
                            InputStream inputStream = process.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            FileSystem fs = FileSystems.getDefault();

                    ) {
                // Execute command, redirect error to output to print all in the console
                System.out.println(ArrayUtils.toString(command));
                int exitStatus = process.waitFor();
                String currentLine;
                StringBuilder stringBuilder = new StringBuilder(exitStatus == 0 ? "SUCCESS:" : "ERROR:");
                currentLine = bufferedReader.readLine();
                while (currentLine != null) {
                    stringBuilder.append(currentLine);
                    currentLine = bufferedReader.readLine();
                }
                System.out.println(stringBuilder.toString());
                Path pdfFile = fs.getPath(TEMP_DIRECTORY_PATH).resolve(fileName + ".pdf");
                if (!Files.exists(pdfFile)) {
                    throw new RuntimeException("Failed generating pdf, file not created");
                }

                addDataTableToPdf(fileName, pdfTable, bufferedReader, pdfFile);
                return pdfFile;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed generating pdf", ex);
        }
    }

    private static void addDataTableToPdf(String fileName, String pdfTable, BufferedReader bufferedReader, Path pdfFile) throws IOException, InterruptedException {
        if (pdfTable != null) {
            String[] gsCommand = {
                    Configuration.GHOSTSCRIPT.getGhostscriptPath(),
                    "-dBATCH", "-dNOPAUSE", "-q", "-sDEVICE=pdfwrite", "-dPDFSETTINGS=/prepress",
                    "-sOutputFile=" + TEMP_DIRECTORY_PATH + "/" + fileName + "-merged.pdf",
                    TEMP_DIRECTORY_PATH + "/" + fileName + ".pdf", pdfTable
            };

            Process gsProcess = new ProcessBuilder(gsCommand).redirectErrorStream(true).start();
            System.out.println(ArrayUtils.toString(gsCommand));
            int gsExitStatus = gsProcess.waitFor();

            try (
                    InputStreamReader in = new InputStreamReader(gsProcess.getInputStream());
                    BufferedReader gsBufferedReader = new BufferedReader(in);
                    FileSystem fileSystem = FileSystems.getDefault()
            ) {
                String gsCurrentLine;
                StringBuilder gsStringBuilder = new StringBuilder(gsExitStatus == 0 ? "SUCCESS:" : "ERROR:");
                gsCurrentLine = bufferedReader.readLine();
                while (gsCurrentLine != null) {
                    gsStringBuilder.append(gsCurrentLine);
                    gsCurrentLine = gsBufferedReader.readLine();
                }
                System.out.println(gsStringBuilder.toString());

                Path gsPdfFile = fileSystem.getPath(TEMP_DIRECTORY_PATH).resolve(fileName + "-merged.pdf");
                if (!Files.exists(gsPdfFile)) {
                    throw new RuntimeException("Failed generating pdf, file not created");
                }

                Files.delete(pdfFile);
                Files.move(gsPdfFile, pdfFile);
            }
        }
    }
}
