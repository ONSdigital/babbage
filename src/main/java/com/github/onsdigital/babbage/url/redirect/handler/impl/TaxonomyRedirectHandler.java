package com.github.onsdigital.babbage.url.redirect.handler.impl;

import com.github.onsdigital.babbage.url.redirect.RedirectCategory;
import com.github.onsdigital.babbage.url.redirect.RedirectException;
import com.github.onsdigital.babbage.url.redirect.handler.RedirectHandler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Redirect Handler implementation that dealS with {@link RedirectCategory#TAXONOMY_REDIRECT} classified redirects.
 */
public final class TaxonomyRedirectHandler extends RedirectHandler {

	private static final String NSCL_KEY = "nscl";
	private static final String URL_ENCODING = "UTF-8";
	private static final String HOME_REDIRECT = "/";

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws RedirectException {
		validate(request, response);

		Optional<Map.Entry<String, String[]>> nsclParam = request
				.getParameterMap()
				.entrySet()
				.stream()
				.filter(entry -> ((Map.Entry<String,String>)entry).getKey().equalsIgnoreCase(NSCL_KEY))
				.findFirst();

		String redirect;
		String paramValue;

		if (!nsclParam.isPresent() || StringUtils.isEmpty(paramValue = getNscl(nsclParam.get()))) {
			redirect = HOME_REDIRECT;
		} else {
			redirect = urlRedirectService.taxonomyRedirect(paramValue);
		}
		sendRedirect(response, redirect);
	}

	private String getNscl(Map.Entry<String, String[]> params) throws RedirectException {
		try {
			List<String> paramList;
			if (params == null || (paramList = Arrays.asList(params.getValue())).isEmpty()) {
				return null;
			}

			String value = URLEncoder.encode(paramList.get(0).toLowerCase(), URL_ENCODING);
			if (value.contains("+")) {
				value = URLEncoder.encode(value, URL_ENCODING);
			}

			return value;
		} catch (UnsupportedEncodingException e) {
			throw new RedirectException(e, RedirectException.ErrorType.REQUIRED_PARAM_MISSING);
		}
	}
}
