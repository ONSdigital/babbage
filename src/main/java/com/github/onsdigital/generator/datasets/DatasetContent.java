package com.github.onsdigital.generator.datasets;

import com.github.onsdigital.content.partial.DownloadSection;
import com.github.onsdigital.content.statistic.Dataset;
import com.github.onsdigital.generator.ContentNode;
import com.github.onsdigital.generator.data.Csv;
import com.github.onsdigital.generator.data.Data;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasetContent {
    static final String resourceName = "/Alpha content master.xlsx";
    private static Csv rows;

    static String THEME = "Theme";
    static String LEVEL2 = "Level 2";
    static String LEVEL3 = "Level 3";
    static String NAME = "Name";
    static String SUMMARY = "Summary";
    static String DATASET_TYPE = "Dataset type";
    static String DESCRIPTION = "Description";
    static String SERIES = "Series";
    static String[] DOWNLOAD = new String[]{"download1", "download2", "download3"};
    static String[] DOWNLOAD_XLS = new String[]{"download1xls", "download2xls", "download3xls"};
    static String[] DOWNLOAD_CSV = new String[]{"download1csv", "download2csv", "download3csv"};
    static String NATIONAL_STATISTIC = "ns";
    static String[] columns = {THEME, LEVEL2, LEVEL3, NAME, SUMMARY, DATASET_TYPE, DESCRIPTION, SERIES, DOWNLOAD[0], DOWNLOAD[1], DOWNLOAD[2], DOWNLOAD_XLS[0], DOWNLOAD_XLS[1], DOWNLOAD_XLS[2],
            DOWNLOAD_CSV[0], DOWNLOAD_CSV[1], DOWNLOAD_CSV[2], NATIONAL_STATISTIC};

    public static void parse() throws IOException {
        if (rows == null) {
            parseCsv();
        }
    }

    private static void parseCsv() throws IOException {
        rows = new Csv(resourceName);
        rows.read(1);
        String[] headings = rows.getHeadings();

        // Verify the headings:
        for (String column : columns) {
            if (!ArrayUtils.contains(headings, column)) {
                throw new RuntimeException("Expected a " + column + " column in " + resourceName);
            }
        }

        for (Map<String, String> row : rows) {

            // Skip empty rows:
            if (StringUtils.isBlank(row.get(NAME))) {
                continue;
            }

            // There are blank rows separating the themes:
            if (StringUtils.isBlank(row.get(THEME))) {
                continue;
            }

            // Get to the folder in question:
            ContentNode folder = Data.getFolder(row.get(THEME), row.get(LEVEL2), row.get(LEVEL3));

            Dataset dataset = new Dataset();
            dataset.title = StringUtils.trim(row.get(NAME));
//			dataset.title = dataset.title; //Title no more there
//			dataset.fileName = sanitise(dataset.title.toLowerCase());
            if (StringUtils.isNotBlank(row.get(SUMMARY))) {
                dataset.summary = row.get(SUMMARY);
            }

            if (StringUtils.isNotBlank(row.get(SERIES))) {

                DownloadSection downloadSection = new DownloadSection();
                downloadSection.title = dataset.title;
                downloadSection.cdids = new ArrayList<String>();
                dataset.downloads.add(downloadSection);

                // Extract CDIDs
                // (four-character sequences of letters and numbers):
                String cdidList = row.get(SERIES);
                Pattern pattern = Pattern.compile("[A-Za-z0-9]{4}");
                Matcher matcher = pattern.matcher(cdidList);
                while (matcher.find()) {
                    downloadSection.cdids.add(matcher.group());
                }
            } else if (StringUtils.isNotBlank(row.get(DOWNLOAD[0]))) {

                for (int s = 0; s < 3; s++) {
                    if (StringUtils.isNotBlank(row.get(DOWNLOAD[s]))) {
                        DownloadSection downloadSection = new DownloadSection();
                        downloadSection.title = row.get(DOWNLOAD[s]);
                        String xls = row.get(DOWNLOAD_XLS[s]);
                        String csv = row.get(DOWNLOAD_CSV[s]);
                        if (StringUtils.isNotBlank(xls)) {
                            downloadSection.xls = xls;
                        }
                        if (StringUtils.isNotBlank(csv)) {
                            downloadSection.csv = csv;
                        }
                        dataset.downloads.add(downloadSection);
                    }
                }

            } else if (StringUtils.isNotBlank(row.get("Link (latest)"))) {
                DownloadSection downloadSection = new DownloadSection();
                downloadSection.title = dataset.title;
                downloadSection.xls = row.get("Link (latest)");
                dataset.downloads.add(downloadSection);
            }

            String nationalStatistic = StringUtils.defaultIfBlank(row.get(NATIONAL_STATISTIC), "yes");
            dataset.nationalStatistic = BooleanUtils.toBoolean(nationalStatistic);

            if (StringUtils.isNotBlank(row.get(DESCRIPTION))) {
                dataset.description = row.get(DESCRIPTION);
            }

            folder.datasets.add(dataset);
            Data.addDataset(dataset);
        }
    }

    /**
     * <a href=
     * "http://stackoverflow.com/questions/1155107/is-there-a-cross-platform-java-method-to-remove-filename-special-chars/13293384#13293384"
     * >http://stackoverflow.com/questions/1155107/is-there-a-cross-platform-
     * java-method-to-remove-filename-special-chars/13293384#13293384</a>
     *
     * @param name
     * @return
     */
    private static String sanitise(String name) {
        StringBuilder result = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (c == '.' || Character.isJavaIdentifierPart(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String cdidList = "chaw, A9ER, cpsk";

        Pattern pattern = Pattern.compile("[A-Za-z0-9]{4}");
        Matcher matcher = pattern.matcher(cdidList);
        while (matcher.find()) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end() + " ");
            System.out.println(matcher.group());
        }
    }
}
