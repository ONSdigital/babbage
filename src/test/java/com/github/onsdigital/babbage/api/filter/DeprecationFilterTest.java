package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.configuration.deprecation.DeprecationConfiguration;
import com.github.onsdigital.babbage.configuration.deprecation.DeprecationItem;
import com.github.onsdigital.babbage.response.util.HttpHeaders;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;

/**
 * Test the deprecation filter works as expected in each scenario.
 */
public class DeprecationFilterTest {

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	private DeprecationFilter filter;

	@Mock
	private DeprecationConfiguration config;

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void shouldNotAddAnyHeadersWhenNotConfigured() {
		List<DeprecationItem> items = new ArrayList<>();
		doReturn(items).when(config).getDeprecationItems();

		filter = new DeprecationFilter(config);

		when(request.getRequestURI())
				.thenReturn("/timeseriestool/data");

		boolean result = filter.filter(request, response);

		assertTrue(result);
		assertEquals(response.getHeader("Sunset"), null);
	}

	@Test
	public void shouldAddSunsetHeadersWhenMatched() {
		// Given the deprecation config is set up for a path to have been deprecated but
		// sunset is in future
		List<DeprecationItem> items = new ArrayList<>();

		String testSunsetDate = "2100-01-01T05:00";
		String testDeprecationDate = "2100-01-01T05:00";
		String testLink = "link";
		String testMatchPattern = "^/timeseriestool/data$";

		items.add(new DeprecationItem(testDeprecationDate, testSunsetDate, testLink, testMatchPattern, "", ""));
		doReturn(items).when(config).getDeprecationItems(any());

		filter = new DeprecationFilter(config);

		when(request.getRequestURI())
				.thenReturn("/timeseriestool/data");

		// When a path that matches is requested
		boolean result = filter.filter(request, response);

		// Then the filter should let processing continue
		assertTrue(result);

		// And the expected headers should be applied
		String expectedLink = String.format("<%s>; rel=\"sunset\"", testLink);
		verify(response, times(1)).addHeader(HttpHeaders.SUNSET, testSunsetDate);
		verify(response, times(1)).addHeader(HttpHeaders.DEPRECATION, testDeprecationDate);
		verify(response, times(1)).addHeader(HttpHeaders.LINK, expectedLink);
	}

	@Test
	public void shouldSetStatusCodeAs404WhenSunsetPassed() {
		// Given the deprecation config is set up for a path to have been deprecated and
		// sunset is in the paste
		List<DeprecationItem> items = new ArrayList<>();

		String testSunsetDate = "2015-01-01T05:00";
		String testDeprecationDate = "2015-01-01T05:00";
		String testLink = "link";
		String testMatchPattern = "^/timeseriestool/data$";

		items.add(new DeprecationItem(testDeprecationDate, testSunsetDate, testLink, testMatchPattern, "", ""));
		doReturn(items).when(config).getDeprecationItems(any());

		filter = new DeprecationFilter(config);

		when(request.getRequestURI())
				.thenReturn("/timeseriestool/data");

		// When a matching path is requested
		boolean result = filter.filter(request, response);

		// Then the processing is stopped
		assertFalse(result);

		// And the sunset headers are applied
		String expectedLink = String.format("<%s>; rel=\"sunset\"", testLink);
		verify(response, times(1)).addHeader(HttpHeaders.SUNSET, testSunsetDate);
		verify(response, times(1)).addHeader(HttpHeaders.DEPRECATION, testDeprecationDate);
		verify(response, times(1)).addHeader(HttpHeaders.LINK, expectedLink);

		// And a 404 is served
		verify(response, times(1)).setStatus(404);
	}
}
