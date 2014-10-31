package com.github.onsdigital.json.timeseries;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.onsdigital.generator.Folder;
import com.github.onsdigital.json.ContentType;
import com.github.onsdigital.json.DataItem;
import com.github.onsdigital.json.TaxonomyHome;

public class Timeseries extends DataItem {

	// Spreadsheet headings
	private String cdid;
	public String seasonalAdjustment;
	public String units;
	public String mainMeasure;
	public String description;
	public String note1;
	public String note2;

	// Additional information
	public String number;
	public String unit;
	// A unit that needs to go before the number, e.g. £
	public String preUnit;
	public String date;
	public String lastUpated;
	public String nextUpdate;

	// e.g. "Not a national statistic" or "2005 = 100. Not seasonally adjusted"
	public String note;

	// The nectar, the goodness, the very juice of the fireflower: data.
	public List<TimeseriesValue> data = new ArrayList<>();

	// The URI of this timeseries.
	// This is useful when it is referenced from more than one place in the
	// taxonomy. It helps the generator to ensure it only gets created in one
	// place.
	public URI uri;

	public Timeseries() {
		type = ContentType.timeseries;
		name = "People not in Work";
	}

	public void setBreadcrumb(TaxonomyHome t3) {
		breadcrumb = new ArrayList<>(t3.breadcrumb);
		Folder folder = new Folder();
		folder.name = t3.name;
		TaxonomyHome extra = new TaxonomyHome(folder);
		breadcrumb.add(extra);
	}

	public String cdid() {
		return cdid;
	}

	public void setCdid(String cdid) {
		if (StringUtils.isBlank(cdid)) {
			throw new IllegalArgumentException("Blank CDID");
		}
		this.cdid = StringUtils.trim(cdid);
		fileName = StringUtils.lowerCase(this.cdid.toLowerCase());
	}

	@Override
	public String toString() {
		return cdid;
	}

	@Override
	public int hashCode() {
		if (cdid != null) {
			return cdid.toLowerCase().hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!Timeseries.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		return StringUtils.equalsIgnoreCase(((Timeseries) obj).cdid, cdid);
	}
}
