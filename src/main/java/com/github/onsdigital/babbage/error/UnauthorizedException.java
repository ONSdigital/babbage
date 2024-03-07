package com.github.onsdigital.babbage.error;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by baratha 05/03/24.
 * <p/>
 * All exceptions extending UnauthorizedException will result in http 401 sent to the client
 */
public class UnauthorizedException extends BabbageException {

    private final static int UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;

    public UnauthorizedException(String message) {
        super(UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        super(UNAUTHORIZED);
    }

}