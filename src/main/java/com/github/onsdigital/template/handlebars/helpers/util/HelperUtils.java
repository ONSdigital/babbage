package com.github.onsdigital.template.handlebars.helpers.util;

import com.github.onsdigital.configuration.Configuration;
import com.github.onsdigital.error.ResourceNotFoundException;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by bren on 06/07/15.
 */
public class HelperUtils {

    //Compare generic number values ignoring data type of number
    public static boolean isEqualNumbers(Number no1, Number no2) {
        return new BigDecimal(no1.toString()).compareTo(new BigDecimal(no2.toString())) == 0;
    }


    public static boolean isEqual(Object o1, Object o2) {
        if(o1 instanceof Number && o2 instanceof Number) {
            return isEqualNumbers((Number) o1, (Number) o2);
        }
        return String.valueOf(o1).equals(String.valueOf(o2));
    }

    public static Long getFileSize(String uri) throws IOException {
        Path file = getFile(uri);

        return Files.size(file);
    }

    private static Path getFile(String uri) throws FileNotFoundException {
        // Standardise the path:
        String uriPath = StringUtils.removeStart(uri, "/");
        Path path = FileSystems.getDefault().getPath(
                Configuration.getContentPath());

        Path file = path.resolve(uriPath);
        if (!Files.exists(file)) {
            throw new FileNotFoundException();
        }
        return file;
    }

}

