package com.github.onsdigital.babbage.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import static com.github.onsdigital.babbage.util.EncryptionFilesUtil.readBase64File;

/**
 *
 */
public class FeedbackEncryptionConfiguration extends StartUpConfiguration {

    private static PublicKey publicKey;
    private static Path publicKeyPath;

    @Override
    public void initialize() throws Exception {
        publicKeyPath = Paths.get(getValue("public_key"));

        byte[] bytes = readBase64File(publicKeyPath.toFile());

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(keySpec);
    }


    public static PublicKey getPublicKey() {
        return publicKey;
    }

    public static Path getPublicKeyPath() {
        return publicKeyPath;
    }
}
