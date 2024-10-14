package com.github.onsdigital.babbage.pdf;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.github.onsdigital.babbage.highcharts.HighChartsExportClient;
import com.github.onsdigital.babbage.template.TemplateService;
import com.lowagie.text.Image;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;

public class ChartImageReplacedElementFactory implements ReplacedElementFactory {

    private final ReplacedElementFactory superFactory;

    private final Map<String, Image> chartImageCache;

    public ChartImageReplacedElementFactory(ReplacedElementFactory superFactory) {
        this.superFactory = superFactory;
        this.chartImageCache = new HashMap<>();
    }

    @Override
    public ReplacedElement createReplacedElement(
            LayoutContext layoutContext, BlockBox blockBox,
            UserAgentCallback userAgentCallback, int cssWidth, int cssHeight
    ) {
        Element element = blockBox.getElement();
        if (element == null) {
            return null;
        }

        // The markdown charts get output as a div with a particular class and data attributes for the URI.
        // Here we look for the class name in each DIV to see where charts need to be rendered.
        String nodeName = element.getNodeName();
        String className = element.getAttribute("class");
        if ("div".equals(nodeName) && className.contains("markdown-chart-div")) {

            String uri = element.getAttribute("data-uri");

            InputStream input = null;
            try {
                Image image;
                if (chartImageCache.containsKey(uri)) {
                    image = chartImageCache.get(uri);
                } else {
                    // read the chart JSON from the content service (zebedee reader)
                    ContentResponse contentResponse = ContentClient.getInstance().getContent(uri);

                    // The highcharts configuration is generated from a handlebars template with the chart JSON as input.
                    LinkedHashMap<String, Object> additionalData = new LinkedHashMap<>();
                    additionalData.put("width", 600);
                    String chartConfig = TemplateService.getInstance().renderChartConfiguration(contentResponse.getDataStream(),
                            additionalData);

                    Integer width = null; // do not set the width here as it overrides the scale
                    double scale = 3.5; // we use the scale to increase the size of the chart so that it also increased the font size accordingly.
                    input = HighChartsExportClient.getInstance().getImage(chartConfig, width, scale);

                    byte[] bytes = IOUtils.toByteArray(input);
                    image = Image.getInstance(bytes);
                    chartImageCache.put(uri, image);
                }

                ITextFSImage fsImage = new ITextFSImage(image);
                if (fsImage != null) {
                    if ((cssWidth != -1) || (cssHeight != -1)) {
                        fsImage.scale(cssWidth, cssHeight);
                    }
                    ITextImageElement ite = new ITextImageElement(fsImage);
                    return ite;
                }
            } catch (Exception ex) {
                error().exception(ex)
                        .log("ChartImageReplacedElementFactory.createReplacedElement encountered an unexpected error");
            } finally {
                if (input != null) {
                    IOUtils.closeQuietly(input);
                }
            }
        }

        return superFactory.createReplacedElement(layoutContext, blockBox, userAgentCallback, cssWidth, cssHeight);
    }

    @Override
    public void reset() {
        superFactory.reset();
    }

    @Override
    public void remove(Element e) {
        superFactory.remove(e);
    }

    @Override
    public void setFormSubmissionListener(FormSubmissionListener listener) {
        superFactory.setFormSubmissionListener(listener);
    }
}
