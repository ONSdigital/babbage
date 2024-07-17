package com.github.onsdigital.babbage.response;

import com.github.onsdigital.babbage.response.base.BabbageResponse;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class BabbageBinaryResponse extends BabbageResponse {

    private byte[] data;

    public BabbageBinaryResponse(InputStream data, String mimeType) throws IOException {
        super(mimeType);
        this.data = IOUtils.toByteArray(data);
    }

    @Override
    public void apply(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super.apply(request, response);
        writeData(response);
    }

    private void writeData(HttpServletResponse response) throws IOException {
        IOUtils.write(data, response.getOutputStream());
    }

    public byte[] getData() {
        return data;
    }
}
