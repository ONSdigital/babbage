package com.github.onsdigital.babbage.configuration;

import org.apache.poi.util.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 *
 */
public class FeedbackEncryptionConfiguration extends StartUpConfiguration {

    private static PublicKey publicKey;
    private static Path publicKeyPath;

    @Override
    public void initialize() throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        publicKeyPath = Paths.get(getValue("public_key"));

        byte[] fileBytes;
        try (InputStream fi = new FileInputStream(publicKeyPath.toFile())) {
            fileBytes = IOUtils.toByteArray(fi);
        }

        byte[] publicEncodedBytes = Base64.getDecoder().decode(fileBytes);
        EncodedKeySpec publicEncodedKeySpec = new X509EncodedKeySpec(publicEncodedBytes);
        publicKey = factory.generatePublic(publicEncodedKeySpec);
    }

    public static PublicKey getPublicKey() {
        return publicKey;
    }

    public static Path getPublicKeyPath() {
        return publicKeyPath;
    }
}
