package com.github.onsdigital.babbage.url.redirect;

/**
 * Exception class for any errors related to URL Redirects.
 */
public class RedirectException extends Exception {

	/**
	 * Types for errors that can occur while processing a redirect.
	 */
	public enum ErrorType {

		/**
		 * Expected Redirect properties value not found.
		 */
		PROPERTY_NOT_FOUND_ERROR("Error while attempting to retrieve url-redirect-mapping property. No entry for key = '%s'" +
				" was found."),
		/**
		 * Unexpected error while attempting to load the URL redirect properties file.
		 */
		PROPERTIES_LOAD_ERROR("Unexpected error while attempting to load the URL redirect properties file."),
		/**
		 * Error for null/missing required parameters.
		 */
		REQUIRED_PARAM_MISSING("Mandatory parameter '%s' was null.");

		private final String message;

		ErrorType(String message) {
			this.message = message;
		}
	}

	private ErrorType errorType;

	/**
	 * Constructs a RedirectException.
	 *
	 * @param cause     the {@link Throwable} cause of the error. Gives greater debug info.
	 * @param errorType the {@link RedirectException.ErrorType} for the type of error caused.
	 * @param args      additional information to add to the exception message.
	 */
	public RedirectException(Throwable cause, ErrorType errorType, Object[] args) {
		super(String.format(errorType.message, args), cause);
		this.errorType = errorType;
	}

	/**
	 * Constructs a RedirectException.
	 *
	 * @param errorType the {@link RedirectException.ErrorType} for the type of error caused.
	 * @param args      additional information to add to the exception message.
	 */
	public RedirectException(ErrorType errorType, Object[] args) {
		this(null, errorType, args);
	}

	/**
	 * Constructs a RedirectException.
	 *
	 * @param cause     the {@link Throwable} cause of the error. Gives greater debug info.
	 * @param errorType the {@link RedirectException.ErrorType} for the type of error caused.
	 */
	public RedirectException(Throwable cause, ErrorType errorType) {
		this(cause, errorType, null);
	}

	/**
	 * @return the {@link ErrorType of the exception.}
	 */
	public ErrorType getErrorType() {
		return this.errorType;
	}
}
