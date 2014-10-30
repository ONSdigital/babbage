package com.github.onsdigital.generator.data;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.onsdigital.generator.Folder;
import com.github.onsdigital.json.timeseries.TimeSeries;

/**
 * Handles the {@value #resourceName} CSV file.
 * <p>
 * This class and its members are package private (default visibility) because
 * the API doesn't need to be exposed to the rest of the application.
 * 
 * @author david
 *
 */
class AlphaContentCSV {

	static final String resourceName = "/Alpha content master.xlsx";

	static String THEME = "Theme";
	static String LEVEL2 = "Level 2";
	static String LEVEL3 = "Level 3";
	static String NAME = "Name";
	static String KEY = "Key";
	static String PREUNIT = "Pre unit";
	static String UNITS = "Units";
	static String FIGURE = "Figure";
	static String PERIOD = "Period";
	static String CDID = "CDID";
	static String[] columns = { THEME, LEVEL2, LEVEL3, NAME, KEY, PREUNIT, UNITS, FIGURE, PERIOD, CDID };

	static Csv csv;

	/**
	 * Parses the CSV and validates headings.
	 * 
	 * @throws IOException
	 */
	public static void parse() throws IOException {

		// Read the first worksheet - "Data":
		csv = new Csv(resourceName);
		csv.read(0);
		String[] headings = csv.getHeadings();

		// Verify the headings:
		for (String column : columns) {
			if (!ArrayUtils.contains(headings, column)) {
				throw new RuntimeException("Expected a " + column + " column in " + resourceName);
			}
		}

		// Process the rows
		for (Map<String, String> row : csv) {

			// There are blank lines in the CSV that separate theme sections:
			if (StringUtils.isBlank(row.get(THEME))) {
				continue;
			}

			String cdid = row.get(CDID);

			// Get the timeseries to work with:
			TimeSeries timeseries = Data.timeseries(cdid);
			if (timeseries == null) {
				// We haven't seen this one before, so add it:
				System.out.println(resourceName + ": new CDID found - " + cdid);
				timeseries = Data.addTimeseries(cdid);

			}

			// Set the URI if necessary:
			Folder folder = Data.getNode(row.get(THEME), row.get(LEVEL2), row.get(LEVEL3));
			if (timeseries.uri == null) {
				timeseries.uri = toUri(folder, timeseries);
			}

			// Set the other properties:
			timeseries.name = row.get(NAME);
			if (BooleanUtils.toBoolean(row.get(KEY))) {
				folder.headline = timeseries;
			}
			folder.timeserieses.add(timeseries);
			timeseries.preUnit = row.get(PREUNIT);
			timeseries.unit = row.get(UNITS);
			timeseries.number = row.get(FIGURE);
			timeseries.date = row.get(PERIOD);
		}
	}

	private static URI toUri(Folder folder, TimeSeries timeseries) {
		URI result = null;

		if (timeseries != null) {
			if (timeseries.uri == null) {
				String baseUri = "/" + folder.filename();
				Folder parent = folder.parent;
				while (parent != null) {
					baseUri = "/" + parent.filename() + baseUri;
					parent = parent.parent;
				}
				baseUri += "/timeseries";
				timeseries.uri = URI.create(baseUri + "/" + StringUtils.trim(timeseries.fileName));
			}
			result = timeseries.uri;
		}

		return result;
	}
}
