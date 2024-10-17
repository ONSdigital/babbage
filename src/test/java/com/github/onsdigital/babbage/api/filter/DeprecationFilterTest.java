package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.configuration.Babbage;
import com.github.onsdigital.babbage.configuration.DeprecationItem;
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

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Test the deprecation filter works as expected in each scenario.
 */
public class DeprecationFilterTest {

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Babbage babbage;

	private DeprecationFilter filter;

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void shouldNotAddAnyHeadersWhenNotConfigured() {
		filter = new DeprecationFilter(new ArrayList<>());

		when(request.getRequestURI())
				.thenReturn("/timeseriestool/data");

		boolean result = filter.filter(request, response);

		assertEquals(true, result);
		assertEquals(response.getHeader("Sunset"), null);
	}

	@Test
	public void shouldAddSunsetHeadersWhenMatched() {
		List<DeprecationItem> config = new ArrayList<>();

		String testSunsetDate = "2100-01-01T05:00";
		String testDeprecationDate = "2100-01-01T05:00";
		String testLink = "link";
		String testMatchPattern = "^/timeseriestool/data$";

		config.add(new DeprecationItem(testDeprecationDate, testSunsetDate, testLink, testMatchPattern));

		filter = new DeprecationFilter(config);

		when(request.getRequestURI())
				.thenReturn("/timeseriestool/data");

		boolean result = filter.filter(request, response);

		info().data("responseHeaders", response.getHeaderNames()).log("response");

		String expectedLink = String.format("<%s>; rel=\"sunset\"", testLink);

		assertEquals(true, result);
		verify(response, times(1)).addHeader(HttpHeaders.SUNSET, testSunsetDate);
		verify(response, times(1)).addHeader(HttpHeaders.DEPRECATION, testDeprecationDate);
		verify(response, times(1)).addHeader(HttpHeaders.LINK, expectedLink);
	}

	@Test
	public void shouldSetStatusCodeAs404WhenSunsetPassed() {
		List<DeprecationItem> config = new ArrayList<>();

		String testSunsetDate = "2015-01-01T05:00";
		String testDeprecationDate = "2015-01-01T05:00";
		String testLink = "link";
		String testMatchPattern = "^/timeseriestool/data$";

		config.add(new DeprecationItem(testDeprecationDate, testSunsetDate, testLink, testMatchPattern));

		filter = new DeprecationFilter(config);

		when(request.getRequestURI())
				.thenReturn("/timeseriestool/data");

		boolean result = filter.filter(request, response);

		info().data("responseHeaders", response.getHeaderNames()).log("response");

		String expectedLink = String.format("<%s>; rel=\"sunset\"", testLink);

		assertEquals(false, result);
		verify(response, times(1)).addHeader(HttpHeaders.SUNSET, testSunsetDate);
		verify(response, times(1)).addHeader(HttpHeaders.DEPRECATION, testDeprecationDate);
		verify(response, times(1)).addHeader(HttpHeaders.LINK, expectedLink);
		verify(response, times(1)).setStatus(404);
	}
}
