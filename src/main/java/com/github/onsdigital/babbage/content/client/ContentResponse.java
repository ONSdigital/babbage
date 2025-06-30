package com.github.onsdigital.babbage.content.client;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.message.MessageSupport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by bren on 07/08/15.
 */
public class ContentResponse implements Serializable {

    private String mimeType;
    private Charset charset;
    private byte[] data;
    private long size;
    private String name;

    private String hash;

    private String pageType;

    ContentResponse(CloseableHttpResponse response) throws IOException {
        try {
            ContentType contentType = getContentType(response);
            mimeType = contentType.getMimeType();
            charset = contentType.getCharset();
            // IOUtils.toByteArray has been updated to return a null pointer exception
            // in commons-io 2.9.0.  This resulted in some failed tests so unsure how it
            // will affect the behaviour of the system, therefore this if statement has been
            // added to return an empty byte array as previously returned by IOUtils.toByteArray
            // and not transform any content.
            if (response.getEntity().getContent() == null) {
                data = new byte[0];
            }
            else {
                data = IOUtils.toByteArray(response.getEntity().getContent());
            }
            size = response.getEntity().getContentLength();
            name = extractName(response);
            pageType = extractPageType(response);
            Header etag = response.getFirstHeader("Etag");
            hash = etag == null ? null : etag.getValue();
        }finally {
            IOUtils.close(response);
        }
    }

    public String getMimeType() {
        return mimeType;
    }

    public Charset getCharset() {
        return charset;
    }

    public InputStream getDataStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    public String getAsString() throws IOException {
        return IOUtils.toString(getDataStream(), getCharset());
    }

    private ContentType getContentType(ClassicHttpResponse response) {
        return ContentType.parseLenient(response.getEntity().getContentType());
    }

    /**
     *
     * @return size in bytes
     */
    public long getSize() {
        return size;
    }

    public String getHash() {
        return StringUtils.remove(hash, "--gzip");//TODO:Checkout why zip extension is passed back
    }

    public String getName() {
        return name;
    }

    private String extractName(HttpResponse response) {
        Header[] contentDisposition = response.getHeaders("Content-Disposition");
        if (contentDisposition != null && contentDisposition.length > 0) {
            HeaderElement[] elements = MessageSupport.parse(contentDisposition[0]);
            if (elements != null && elements.length > 0) {
                NameValuePair filename = elements[0].getParameterByName("filename");
                return filename == null ? null : filename.getValue();

            }
        }
        return null;
    }

    public String getPageType() {
        return pageType;
    }

    private String extractPageType(HttpResponse response) {
        Header pageTypeHeader = response.getFirstHeader("ONS-Page-Type");
        if (pageTypeHeader != null) {
            return pageTypeHeader.getValue();
        }
        return null;
    }

}
