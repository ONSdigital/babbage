package com.github.onsdigital.babbage.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.function.Function;

import static com.github.onsdigital.babbage.configuration.FeedbackEncryptionConfiguration.getPublicKey;
import static com.github.onsdigital.babbage.util.EncryptionFilesUtil.getKeyPath;
import static com.github.onsdigital.babbage.util.EncryptionFilesUtil.writeBase64ToFile;

/**
 * Provides functionality for encrypting data and writing it to the file system.
 */
public class EncryptionFileWriter {

    static final String AES_ALGORITHM = "AES";
    static final String RSA_WITH_PADDING = "RSA/ECB/PKCS1Padding";

    /**
     * Generate a new {@link SecretKey} to writeEncrypted the data with. {@link KeyGenerator#generateKey()} returns a different
     * value each time. Wrapping the call inside a function means that it can be replaced with a mock during tests.private PublicKey publicKey;
     */
    private Function<KeyGenerator, SecretKey> secretKeyGenerator = (keyGen) -> keyGen.generateKey();
    private PublicKey publicKey;

    private static class InstanceHolder {
        static final EncryptionFileWriter INSTANCE = new EncryptionFileWriter(getPublicKey());
    }

    public static EncryptionFileWriter getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @param publicKey the public key to writeEncrypted the data with.
     */
    EncryptionFileWriter(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * EncryptionFileWriter the data and write to the specified location.
     */
    public void writeEncrypted(Path filePath, String message) throws Exception {
        SecretKey secretKey = encryptDataAndWriteToFile(filePath, message);
        encryptSecretKeyAndWriteToFile(filePath, secretKey);
    }

    private SecretKey encryptDataAndWriteToFile(Path filePath, String data) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(128);
        SecretKey secretKey = secretKeyGenerator.apply(keyGen);

        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] fileAESBytes = aesCipher.doFinal(data.getBytes());
        writeBase64ToFile(filePath.toFile(), fileAESBytes);
        return secretKey;
    }

    private void encryptSecretKeyAndWriteToFile(Path filePath, SecretKey secretKey) throws Exception {
        Path secretKeyEncryptedFilePath = getKeyPath(filePath);

        Cipher rsaCipher = Cipher.getInstance(RSA_WITH_PADDING);
        rsaCipher.init(Cipher.ENCRYPT_MODE, this.publicKey);

        byte[] encrypted = rsaCipher.doFinal(secretKey.getEncoded());
        writeBase64ToFile(secretKeyEncryptedFilePath.toFile(), encrypted);
    }
}
