package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.response.base.BabbageResponse;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    @Override
    public void apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // https://acunetix.com/vulnerabilities/web/clickjacking--x-frame-options-header-missing
        addHeader("X-Frame-Options", "SAMEORIGIN");
        super.apply(request, response);
        writeData(response);
    }

    public void applyEmbedded(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super.apply(request, response);
        writeData(response);
    }

    protected void writeData( HttpServletResponse response) throws IOException {
        IOUtils.write(getData(), response.getOutputStream(), StandardCharsets.UTF_8);
    }

    public String getData() {
        return data;
    }

}
