package com.github.onsdigital.babbage.pdf;

import com.github.onsdigital.babbage.content.client.ContentClient;
import com.github.onsdigital.babbage.content.client.ContentReadException;
import com.github.onsdigital.babbage.content.client.ContentResponse;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

public class HtmlImageReplacedElementFactory implements ReplacedElementFactory {

    private final ReplacedElementFactory superFactory;
    private static final Logger log = getLogger(HtmlImageReplacedElementFactory.class);

    public HtmlImageReplacedElementFactory(ReplacedElementFactory superFactory) {
        this.superFactory = superFactory;
    }

    private final double scale = 2.5;
    private final int maxWidth = 2000;

    @Override
    public ReplacedElement createReplacedElement(
            LayoutContext layoutContext, BlockBox blockBox,
            UserAgentCallback userAgentCallback, int cssWidth, int cssHeight
    ) {

        Element element = blockBox.getElement();
        if (element == null) {
            return null;
        }

        String tagName = element.getTagName();

        // if we find an img tag
        if ("img".equals(tagName)) {

            // get the URL from the src attribute.
            String src = element.getAttribute("src");
            try {


                if (src != null && src.length() > 0) {

                    // check if its a relative url - if so get the resource from the content service (zebedee)
                    URI uri = new URI(src);
                    if (!uri.isAbsolute()) {
                        ContentResponse contentResponse = ContentClient.getInstance().getResource(src);

                        try (InputStream input = contentResponse.getDataStream())
                        {
                            ReplacedElement fsImage = getReplacedImage(cssWidth, cssHeight, input);
                            if (fsImage != null) return fsImage;
                        }
                    }

                    // if the url is absolute, go get it using HTTP client.
                    HttpClient client = HttpClientBuilder.create().build();
                    ClassicHttpResponse response = (ClassicHttpResponse) client.execute(new HttpGet(src));

                    try (InputStream input = response.getEntity().getContent()) {
                        ReplacedElement fsImage = getReplacedImage(cssWidth, cssHeight, input);
                        if (fsImage != null) return fsImage;
                    }
                }

            } catch (URISyntaxException | ContentReadException | IOException | BadElementException e) {
                log.error(e.getMessage());
            }


        }

        return superFactory.createReplacedElement(layoutContext, blockBox, userAgentCallback, cssWidth, cssHeight);
    }

    private ReplacedElement getReplacedImage(int cssWidth, int cssHeight, InputStream input) throws IOException, BadElementException {
        byte[] bytes = IOUtils.toByteArray(input);

        Image image = Image.getInstance(bytes);
        ITextFSImage fsImage = new ITextFSImage(image);

        if (fsImage != null) {
            if ((cssWidth != -1) || (cssHeight != -1)) {
                fsImage.scale(cssWidth, cssHeight);
            } else {

                if ((fsImage.getWidth() * scale) > maxWidth) {
                    // pass -1 as height to maintain aspect ratio when setting width.
                    fsImage.scale(maxWidth, -1);
                } else {
                    fsImage.scale(new Double(fsImage.getWidth() * scale).intValue(), new Double(fsImage.getHeight() * scale).intValue());
                }
            }
            return new ITextImageElement(fsImage);
        }
        return null;
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
