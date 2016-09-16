package com.github.onsdigital.babbage.util;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.onsdigital.babbage.util.TestsUtil.setPrivateField;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class EncryptionFileWriterTest {

    private static final String ENCRYPTED_FILE_NAME = "encryption-test.json";
    private static final String ENCRYPTED_KEY_FILE_NAME = "key-" + ENCRYPTED_FILE_NAME;
    private static final String PLAIN_TEXT = "Woe to you, oh Earth and Sea, for the Devil sends the beast with wrath, " +
            "Because he knows the time is short... Let him who hath understanding reckon the number of the beast " +
            "For it is a human number, Its number is Six hundred and sixty six.";

    private EncryptionFileWriter target;
    private SecretKey secretKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyGenerator keyGen;
    private Path tempDir;
    private Path tempFile;

    @Before
    public void setUp() throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);

        KeyPair keyPair = keyGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        KeyGenerator secretKeyGen = KeyGenerator.getInstance("AES");
        secretKeyGen.init(128);

        this.secretKey = secretKeyGen.generateKey();
        Function<KeyGenerator, SecretKey> secretKeyGeneratorStub = (arg) -> secretKey;

        target = new EncryptionFileWriter(this.publicKey);
        setPrivateField(target, "secretKeyGenerator", secretKeyGeneratorStub);

        this.tempDir = Files.createTempDirectory("encrypted");
        this.tempFile = Files.createFile(tempDir.resolve(ENCRYPTED_FILE_NAME));
    }

    @Test
    public void shouldEncryptData() throws Exception {
        target.writeEncrypted(tempFile, PLAIN_TEXT);

        List<File> files = Arrays.asList(new File(tempDir.toString()).listFiles());
        List<String> fileNames = files.stream().map(file -> file.getName()).collect(Collectors.toList());

        // Check the expected files are created,
        assertThat("Expected 2 files.", files.size(), equalTo(2));
        assertThat("Expected encrypted data file.", fileNames.contains(ENCRYPTED_FILE_NAME), is(true));
        assertThat("Expected encrypted key file.", fileNames.contains(ENCRYPTED_KEY_FILE_NAME), is(true));

        // Attempt to decrypt the files created and verify they match the original plain text.
        File encryptedFile = files.stream().filter(file -> file.getName().equals(ENCRYPTED_FILE_NAME)).findFirst().get();
        File encryptedKey = files.stream().filter(file -> file.getName().equals(ENCRYPTED_KEY_FILE_NAME)).findFirst().get();

        assertThat("File should be encrypted", new String(fileBytes(encryptedFile)), is(not(equalTo(PLAIN_TEXT))));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encyrptedKeyByes = Base64.getDecoder().decode(fileBytes(encryptedKey));

        byte[] decryptedKeyBytes = cipher.doFinal(encyrptedKeyByes);
        SecretKey secretKey = new SecretKeySpec(decryptedKeyBytes, 0, decryptedKeyBytes.length, "AES");

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
        String result = new String(aesCipher.doFinal(Base64.getDecoder().decode(fileBytes(encryptedFile))));

        assertThat("Decrypted value is not as expected.", result, equalTo(PLAIN_TEXT));
    }

    private byte[] fileBytes(File file) throws Exception {
        try (InputStream fi = new FileInputStream(file)) {
            return IOUtils.toByteArray(fi);
        }
    }
}
