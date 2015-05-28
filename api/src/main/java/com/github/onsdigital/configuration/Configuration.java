package com.github.onsdigital.configuration;

import com.github.davidcarboni.cryptolite.*;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;

public class Configuration {

    private static final String DEFAULT_TAXONOMY_ROOT = "api/target/taxonomy";

    private static final String DEFAULT_ZEBEDEE_URL = "http://localhost:8082";

    /**
     * Mongo is currently only used to provide feedback on the search terms
     * users are typing in.
     */
    private static final String DEFAULT_MONGO_URI = "mongodb://<dbuser>:<dbpassword>@ds055990.mongolab.com:55990/ons";

    /**
     * David Carboni: This token relates to a Prerender.io accout I set up. If
     * necessary this account can be transferred, or a new one set up for ONS.
     */
    private static final String DEFAULT_PRERENDER_TOKEN = "cCc113eXWWV2TbRcnoMV";


    private static final String DEFAULT_TEMPLATES_DIR = "web/templates/handlebars";

    private static final String DEFAULT_TEMPLATES_SUFFIX = ".handlebars";

    private static String templatesDir;

    private static String templatesSuffix;


    public static String getZebedeeUrl() {
        return StringUtils.defaultIfBlank(getValue("ZEBEDEE_URL"), DEFAULT_ZEBEDEE_URL);
    }

    public static String getTaxonomyPath() {
        return StringUtils.defaultIfBlank(getValue("TAXONOMY_DIR"), DEFAULT_TAXONOMY_ROOT);
    }

    public static String getPrerenderToken() {
        return StringUtils.defaultIfBlank(getValue("PRERENDER_TOKEN"), DEFAULT_PRERENDER_TOKEN);
    }

    public static String getTemplatesDirectory() {
        if (templatesDir == null) {
            synchronized (DEFAULT_TEMPLATES_DIR) {
                if (templatesDir == null) {
                    templatesDir = getValue("TEMPLATES_DIR");
                    templatesDir = StringUtils.defaultIfBlank(templatesDir, DEFAULT_TEMPLATES_DIR);
                }
            }
        }

        return templatesDir;
    }

    public static String getTemplatesSuffix() {
        if (templatesSuffix == null) {
            synchronized (DEFAULT_TEMPLATES_SUFFIX) {
                if (templatesSuffix == null) {
                    templatesSuffix = getValue("TEMPLATES_DIR");
                    templatesSuffix = StringUtils.defaultIfBlank(templatesSuffix, DEFAULT_TEMPLATES_SUFFIX);
                }
            }
        }
        return templatesSuffix;
    }

    /**
     * @return The Mongo URI. When deployed, credentials are set in Heroku
     * config. In development, a shared database-as-a-service is used,
     * so we need to decrypt credentials provided in the run script.
     */
    public static String getMongoDbUri() {

        // Normal operation:
        String mongolabUri = getValue("MONGOLAB_URI");
        if (StringUtils.isNotBlank(mongolabUri)) {
            return mongolabUri;
        }

        // Just for development.
        // This is not actually secure, but it's just a development database so
        // it's "good enough":
        String user = getValue("mongo.user");
        String keyPassword = new String(ByteArray.fromBase64String("S3VJNlNrb0U="));
        String salt = "LZNEwAt8CtB664Xw3ml3aA==";
        SecretKey key = new KeyWrapper(keyPassword, salt).unwrapSecretKey("RopLZk7YVsZpOIid6RuZxdqDdeaXIRMr");
        String password = getValue("mongo.password");
        try {
            password = new Crypto().decrypt(password, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        // Not
        mongolabUri = DEFAULT_MONGO_URI;
        mongolabUri = StringUtils.replace(mongolabUri, "<dbuser>", user);
        mongolabUri = StringUtils.replace(mongolabUri, "<dbpassword>", password);
        return mongolabUri;
    }

    /**
     * Gets a configured value for the given key from either the system
     * properties or an environment variable.
     * <p>
     * Copied from {@link com.github.davidcarboni.restolino.Configuration}.
     *
     * @param key The name of the configuration value.
     * @return The system property corresponding to the given key (e.g.
     * -Dkey=value). If that is blank, the environment variable
     * corresponding to the given key (e.g. EXPORT key=value). If that
     * is blank, {@link StringUtils#EMPTY}.
     */
    static String getValue(String key) {
        return StringUtils.defaultIfBlank(System.getProperty(key), System.getenv(key));
    }

    /**
     * Use this method to generate new credentials.
     *
     * @param args Not used
     * @throws InvalidKeyException
     */
    public static void main(String[] args) throws InvalidKeyException {

        // Encrypt password:
        String password = "insert password here";
        String base64 = ByteArray.toBase64String(password.getBytes());
        String salt = Random.salt();
        SecretKey key = Keys.newSecretKey();
        String wrappedKey = new KeyWrapper(password, salt).wrapSecretKey(key);

        // Print out the values you'll need to update above:
        System.out.println("base64 key password: " + base64);
        System.out.println("salt: " + salt);
        System.out.println("Wrapped key: " + wrappedKey);

        // And in the run script:
        System.out.println("Encrypted password: " + new Crypto().encrypt("tr3degaR", key));
    }

}
