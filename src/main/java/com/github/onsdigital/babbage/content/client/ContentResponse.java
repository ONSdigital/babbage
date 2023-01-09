package com.github.onsdigital.babbage.content.client;

//import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
//import java.util.Date;

/**
 * Created by bren on 07/08/15.
 */
public class ContentResponse implements Serializable {

    private String mimeType;
    private Charset charset;
    private byte[] data;
    private long size;
    private String name;
    private int maxAge;//seconds

    private String hash;

    ContentResponse(CloseableHttpResponse response) throws IOException {
        try {
            ContentType contentType = getContentType(response);
            mimeType = contentType.getMimeType();
            charset = contentType.getCharset();
            data = IOUtils.toByteArray(response.getEntity().getContent());
            size = response.getEntity().getContentLength();
            name = extractName(response);
            Header etag = response.getFirstHeader("Etag");
            hash = etag == null ? null : etag.getValue();
        }finally {
//            IOUtils.closeQuietly(response);
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

    private ContentType getContentType(HttpResponse response) {
        return ContentType.getLenient(response.getEntity());
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

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public String getName() {
        return name;
    }

    private String extractName(HttpResponse response) {
        Header[] contentDisposition = response.getHeaders("Content-Disposition");
        if (contentDisposition != null && contentDisposition.length > 0) {
            HeaderElement[] elements = contentDisposition[0].getElements();
            if (elements != null && elements.length > 0) {
                NameValuePair filename = elements[0].getParameterByName("filename");
                return filename == null ? null : filename.getValue();

            }
        }
        return null;
    }

}
