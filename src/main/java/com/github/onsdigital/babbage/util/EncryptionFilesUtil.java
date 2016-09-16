package com.github.onsdigital.babbage.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Created by dave on 15/09/2016.
 */
public class EncryptionFilesUtil {

    static final String KEY_PREFIX = "key-";

    /**
     * Write bytes to file in Base64.
     */
    public static void writeBase64ToFile(File file, byte[] bytes) throws Exception {
        try (OutputStream out = new FileOutputStream(file)) {
            IOUtils.write(Base64.getEncoder().encode(bytes), out);
        }
    }

    public static byte[] readBase64File(File file) throws Exception {
        try (InputStream in = new FileInputStream(file)) {
            return Base64.getDecoder().decode(IOUtils.toByteArray(in));
        }
    }

    public static byte[] readFile(File file) throws Exception {
        try (InputStream in = new FileInputStream(file)) {
            return IOUtils.toByteArray(in);
        }
    }

    public static Path getKeyPath(Path filePath) {
        return filePath.getParent().resolve(KEY_PREFIX + filePath.getFileName().toString());
    }
}
