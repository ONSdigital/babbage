package com.github.onsdigital.babbage.api.filter;

import com.github.onsdigital.babbage.api.error.ErrorHandler;
import com.github.onsdigital.babbage.url.shortcut.ShortcutUrl;
import com.github.onsdigital.babbage.url.shortcut.ShortcutUrlService;
import com.github.onsdigital.babbage.util.TestsUtil;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


/**
 * Test the shortcut filter works as expected in each scenario.
 */
public class ShortUrlFilterTest {

	private static final String SHORT_URL = "/shortcut";
	private static final String SHORT_URL_REDIRECT = "/shortcut/redirected/to/here";

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private ShortcutUrlService shortcutUrlService;

	private ShortUrlFilter filter;

	private List<ShortcutUrl> shortcuts = new ImmutableList.Builder<ShortcutUrl>()
			.add(new ShortcutUrl(SHORT_URL, SHORT_URL_REDIRECT))
			.build();

	private Optional<List<ShortcutUrl>> mapOptional = Optional.of(shortcuts);


	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);

		filter = new ShortUrlFilter();

		TestsUtil.setPrivateField(filter, "shortcutUrlService", shortcutUrlService);
	}

	@Test
	public void shouldLoadShortcutMappingAndNotRedirect() throws Exception {
		when(request.getRequestURI())
				.thenReturn("/notAShortcut");
		when(shortcutUrlService.shortcuts())
				.thenReturn(shortcuts);

		TestsUtil.setPrivateStaticField(filter, "shortcuts", Optional.empty());

		boolean result = filter.filter(request, response);

		assertThat("Expected filter to return true but was false. Test failed.", result, equalTo(true));
		verify(shortcutUrlService, times(1)).shortcuts();
		verifyNoInteractions(response);
	}

	@Test
	public void shouldNotLoadShortcutMappingAndNotRedirect() throws Exception {
		when(request.getRequestURI())
				.thenReturn("/notAShortcut");

		TestsUtil.setPrivateStaticField(filter, "shortcuts", mapOptional);

		boolean result = filter.filter(request, response);

		assertThat("Expected filter to return true but was false. Test failed.", result, equalTo(true));
		verifyNoInteractions(shortcutUrlService, response);
	}

	@Test
	public void shouldLoadShortcutMappingAndRedirect() throws Exception {
		when(request.getRequestURI())
				.thenReturn(SHORT_URL);
		when(shortcutUrlService.shortcuts())
				.thenReturn(shortcuts);

		TestsUtil.setPrivateStaticField(filter, "shortcuts", Optional.empty());

		boolean result = filter.filter(request, response);

		assertThat("Expected filter to return false but was true. Test failed.", result, equalTo(false));
		verify(shortcutUrlService, times(1)).shortcuts();
		verify(response, times(1)).setHeader(HttpHeaders.LOCATION, SHORT_URL_REDIRECT);
		verify(response, times(1)).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);

	}

	@Test
	public void shouldNotLoadShortcutMappingAndShouldRedirect() throws Exception {
		when(request.getRequestURI())
				.thenReturn(SHORT_URL);

		TestsUtil.setPrivateStaticField(filter, "shortcuts", mapOptional);

		boolean result = filter.filter(request, response);

		assertThat("Expected filter to return false but was true. Test failed.", result, equalTo(false));
		verifyNoInteractions(shortcutUrlService);
		verify(response, times(1)).setHeader(HttpHeaders.LOCATION, SHORT_URL_REDIRECT);
		verify(response, times(1)).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
	}

	@Test
	public void shouldHandleErrorIfExceptionThrown() throws Exception {
		when(request.getRequestURI())
				.thenReturn(SHORT_URL);

		doThrow(new IOException())
				.when(shortcutUrlService).shortcuts();

		try (MockedStatic<ErrorHandler> errorHandlerMock = mockStatic(ErrorHandler.class)) {
			boolean result = filter.filter(request, response);

			assertEquals(true, result);

			verifyNoInteractions(response);
			errorHandlerMock.verify(() -> ErrorHandler.handle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(IOException.class)));
		}
	}
}
