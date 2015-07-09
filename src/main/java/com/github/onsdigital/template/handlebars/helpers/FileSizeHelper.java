package com.github.onsdigital.template.handlebars.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.onsdigital.template.handlebars.helpers.util.HelperUtils;

import java.io.IOException;

/**
 * Created by bren on 08/07/15.
 */
public class FileSizeHelper implements Helper<String> {
    @Override
    public CharSequence apply(String uri, Options options) throws IOException {
        if (options.isFalsy(uri)) {
            return null;
        }
        try {
            Long size = HelperUtils.getFileSize(uri);
            return humanReadableByteCount(size, true);
        } catch (IOException e) {
            return null;
        }
    }


    // Taken from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
