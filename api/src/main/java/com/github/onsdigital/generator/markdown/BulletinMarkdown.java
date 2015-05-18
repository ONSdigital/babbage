package com.github.onsdigital.generator.markdown;

import com.github.onsdigital.generator.Folder;
import com.github.onsdigital.generator.data.Data;
import com.github.onsdigital.json.markdown.Bulletin;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BulletinMarkdown {

    static final String resourceName = "/bulletins";
    public static Map<String, Bulletin> bulletins = new HashMap<>();

    public static void parse() throws IOException {
        Collection<Path> files = Markdown.getFiles(resourceName);

        for (Path file : files) {

            // Read the bulletin:
            Bulletin bulletin = readBulletin(file);

            // Set the URI if necessary:
            Folder folder = Data.getFolder(bulletin.theme, bulletin.level2, bulletin.level3);
            if (bulletin.uri == null) {
                bulletin.uri = toUri(folder, bulletin);
            }

            // Add it to the taxonomy:
            folder.bulletins.add(bulletin);
            bulletins.put(bulletin.name, bulletin);
            if (StringUtils.isNotBlank(bulletin.headline1)) {
                folder.headlineBulletin = bulletin;
            }
        }
    }

    static Bulletin readBulletin(Path file) throws IOException {

        // Read the file
        System.out.println("Processing bulletin from: " + file);
        Markdown markdown = new Markdown(file);

        // Set up the bulletin
        Bulletin bulletin = new Bulletin();
        bulletin.name = markdown.title;
        bulletin.title = markdown.title;
        setProperties(bulletin, markdown);
        bulletin.sections.clear();
        bulletin.sections.addAll(markdown.sections);
        bulletin.accordion.addAll(markdown.accordion);
        bulletin.fileName = markdown.toFilename();

        return bulletin;
    }

    /**
     * Reads the "header" information about the bulletin. Information is
     * expected in the form "key : value" and the header block should be
     * terminated with an empty line. The recognised keys are as follows.
     * <ul>
     * <li>Theme</li>
     * <li>Level 2</li>
     * <li>Level 3</li>
     * <li>Summary</li>
     * <li>Headline 1</li>
     * <li>Headline 2</li>
     * <li>Headline 3</li>
     * <li>Contact name</li>
     * <li>Contact email</li>
     * <li>Phone</li>
     * <li>Search keywords</li>
     * <li>National statistic</li>
     * <li>Language</li>
     * <li>Release Date</li>
     * </ul>
     *
     * @param bulletin The {@link Bulletin} containing the data.
     * @param markdown The parsed {@link Markdown}.
     */
    private static void setProperties(Bulletin bulletin, Markdown markdown) {

        Map<String, String> properties = markdown.properties;

        // Location
        bulletin.theme = StringUtils.defaultIfBlank(properties.remove("theme"), bulletin.theme);
        bulletin.level2 = StringUtils.defaultIfBlank(properties.remove("level 2"), bulletin.level2);
        bulletin.level3 = StringUtils.defaultIfBlank(properties.remove("level 3"), bulletin.level3);

        // Additional details
        bulletin.summary = StringUtils.defaultIfBlank(properties.remove("summary"), bulletin.summary);
        bulletin.headline1 = StringUtils.defaultIfBlank(properties.remove("headline 1"), bulletin.headline1);
        bulletin.headline2 = StringUtils.defaultIfBlank(properties.remove("headline 2"), bulletin.headline2);
        bulletin.headline3 = StringUtils.defaultIfBlank(properties.remove("headline 3"), bulletin.headline3);
        bulletin.contact.name = StringUtils.defaultIfBlank(properties.remove("contact name"), bulletin.contact.name);
        bulletin.contact.email = StringUtils.defaultIfBlank(properties.remove("contact email"), bulletin.contact.email);
        bulletin.releaseDate = StringUtils.defaultIfBlank(properties.remove("release date"), bulletin.releaseDate);

        // Additional fields for migration:
        bulletin.phone = StringUtils.defaultIfBlank(properties.remove("phone"), bulletin.phone);
        bulletin.nationalStatistic = BooleanUtils.toBoolean(StringUtils.defaultIfBlank(properties.remove("national statistic"), BooleanUtils.toString(bulletin.nationalStatistic, "yes", "no")));
        bulletin.language = StringUtils.defaultIfBlank(properties.remove("language"), bulletin.language);
        // Split keywords by commas:
        String searchKeywordsString = StringUtils.defaultIfBlank(properties.remove("search keywords"), StringUtils.join(bulletin.searchKeywords, ','));
        bulletin.searchKeywords = StringUtils.split(searchKeywordsString, ',');
        if (bulletin.searchKeywords != null) {
            for (int i = 0; i < bulletin.searchKeywords.length; i++) {
                bulletin.searchKeywords[i] = StringUtils.trim(bulletin.searchKeywords[i]);
            }
        }

        // Note any unexpected information
        for (String property : properties.keySet()) {
            System.out.println("Bulletin key not recognised: '" + property + "' (length " + property.length() + " for value '" + properties.get(property) + "')");
        }

    }

    static URI toUri(Folder folder, Bulletin bulletin) {
        URI result = null;

        if (bulletin != null) {
            if (bulletin.uri == null) {
                String baseUri = "/" + folder.filename();
                Folder parent = folder.parent;
                while (parent != null) {
                    baseUri = "/" + parent.filename() + baseUri;
                    parent = parent.parent;
                }
                baseUri += "/bulletins";
                bulletin.uri = URI.create(baseUri + "/" + StringUtils.trim(bulletin.fileName));
            }
            result = bulletin.uri;
        }

        return result;
    }


}
