package com.github.onsdigital.babbage.content.client;

import com.github.onsdigital.babbage.error.BabbageException;

public class InvalidURIException extends BabbageException {


    public InvalidURIException(int statusCode) {
        super(statusCode);
    }

    public InvalidURIException(int statusCode, String message) {
        super(statusCode, message);
    }

    public InvalidURIException(int statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}


