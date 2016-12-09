package com.github.onsdigital.babbage.url;

import au.com.bytecode.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a {@link CSVReader} for the specified resource.
 */
public abstract class AbstractCSVFactory {

    private static final char DELIMITER = ',';

    /**
     * Creates a new {@link CSVReader} for the resource specified. Make sure you either explicitly call
     * {@link CSVReader#close()} or use it in a try-with-resources block to ensure the unused resources are closed.
     *
     * @param resourcePath the resource the reader will be used to read.
     * @return the reader.
     * @throws IOException unexpected error while creating the CSVReader.
     */
    protected CSVReader createInstance(final String resourcePath) {
        // Closing these is the responsibility of the calling code.
        InputStream in = AbstractCSVFactory.class.getResourceAsStream(resourcePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return new CSVReader(reader, DELIMITER);
    }
}
