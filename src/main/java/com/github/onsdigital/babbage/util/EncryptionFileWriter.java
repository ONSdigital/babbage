package com.github.onsdigital.babbage.util;

import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.function.Function;

import static com.github.onsdigital.babbage.configuration.FeedbackEncryptionConfiguration.getPublicKey;

/**
 * Writes a file encrypted to the specified location.
 */
public class EncryptionFileWriter {

    private static final String AES_ALGORITHM = "AES";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String KEY_PREFIX = "key-";

    private final PublicKey publicKey;

    private static class InstanceHolder {
        static final EncryptionFileWriter INSTANCE = new EncryptionFileWriter(getPublicKey());
    }

    public static EncryptionFileWriter getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Generate a new {@link SecretKey} to encrypt the data with. {@link KeyGenerator#generateKey()} returns a different
     * value each time. Wrapping the call inside a function means that it can be replaced with a mock during tests.
     */
    private Function<KeyGenerator, SecretKey> secretKeyGenerator = (keyGen) -> keyGen.generateKey();

    EncryptionFileWriter(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Encrypt the string and write it to a file.
     */
    public void writeEncrypted(Path location, String message) {
        try {
            SecretKey secretKey = encryptFile(location, message);
            encryptSecretKey(secretKey, location);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypt the file content.
     */
    private SecretKey encryptFile(Path location, String message) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(128);

        SecretKey secretKey = secretKeyGenerator.apply(keyGen);
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);

        writeFile(location, aesCipher.doFinal(message.getBytes()));
        return secretKey;
    }

    /**
     * Write to a file at the specified path.
     */
    private void writeFile(Path location, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(location.toFile())) {
            IOUtils.write(data, fos);
        }
    }

    /**
     * Encrypt the {@link SecretKey} used to encrypt the file.
     */
    private void encryptSecretKey(SecretKey secretKey, Path location) throws Exception {
        Path filename = location.getFileName();
        Path dir = location.getParent();
        Path keyLocation = dir.resolve(KEY_PREFIX + filename.toString());

        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        writeFile(keyLocation, rsaCipher.doFinal(secretKey.getEncoded()));
    }
}
