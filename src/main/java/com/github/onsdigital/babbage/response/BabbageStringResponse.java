package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.response.base.BabbageResponse;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BabbageStringResponse extends BabbageResponse {

    private String data;

    public BabbageStringResponse(String data) {
        this.data = data;
    }

    public BabbageStringResponse(String data, String mimeType) {
        super(mimeType);
        this.data = data;
    }

    public BabbageStringResponse(String data, String mimeType, String charEncoding) {
        this(data, mimeType);
        setCharEncoding(charEncoding);
    }

    public BabbageStringResponse(String data, String mimeType, Long maxAge) {
        this(data, mimeType);
        setMaxAge(maxAge);
    }

    @Override
    public void apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // https://acunetix.com/vulnerabilities/web/clickjacking--x-frame-options-header-missing
        addHeader("X-Frame-Options", "SAMEORIGIN"); // DENY | SAMEORIGIN
        //addHeader("X-Frame-Options", "ALLOW-FROM https://blog.ons.digital"); // DENY | SAMEORIGIN
        super.apply(request, response);
        writeData(response);
    }

    protected void writeData( HttpServletResponse response) throws IOException {
        IOUtils.write(getData(), response.getOutputStream());
    }

    public String getData() {
        return data;
    }

}
