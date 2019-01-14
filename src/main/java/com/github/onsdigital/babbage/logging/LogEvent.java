package com.github.onsdigital.babbage.logging;

import ch.qos.logback.classic.Level;
import com.github.onsdigital.babbage.error.BabbageException;
import com.github.onsdigital.babbage.error.BadRequestException;
import com.github.onsdigital.babbage.error.InternalServerErrorException;
import com.github.onsdigital.babbage.error.ResourceNotFoundException;
import com.github.onsdigital.logging.builder.LogMessageBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.MDC;

public class LogEvent extends LogMessageBuilder {

    private static final String REQ_ID = "X-Request-Id";

    public static LogEvent logEvent() {
        return new LogEvent("");
    }

    public static LogEvent logEvent(Throwable e) {
        return new LogEvent(e);
    }

    public static LogEvent logEvent(Throwable e, String message) {
        return new LogEvent(e, message);
    }

    protected LogEvent(String eventDescription) {
        super(eventDescription);
        setNamespace("babbage"); // TODO should this be configurable?
    }

    protected LogEvent(Throwable e) {
        this(e, "");
    }

    protected LogEvent(Throwable e, String message) {
        super(e, message);
        setNamespace("babbage"); // TODO should this be configurable?
    }

    @Override
    public String getLoggerName() {
        return "babbage";
    }

    public void trace(String message) {
        logLevel = Level.TRACE;
        description = message;
        log();
    }

    public void debug(String message) {
        logLevel = Level.DEBUG;
        description = message;
        log();
    }

    public void info(String message) {
        logLevel = Level.INFO;
        description = message;
        log();
    }

    public void warn(String message) {
        logLevel = Level.WARN;
        description = message;
        log();
    }

    public void error(String message) {
        logLevel = Level.ERROR;
        description = message;
        log();
    }

    public LogEvent httpGET() {
        return addParamSafe("httpMethod", "GET");
    }

    public LogEvent httpPOST() {
        return addParamSafe("httpMethod", "POST");
    }

    public LogEvent httpDELETE() {
        return addParamSafe("httpMethod", "DELETE");
    }

    public LogEvent parameter(String key, Object value) {
        return addParamSafe(key, value);
    }

    public <T, R> LogEvent parameter(String key, Stream<T> s, Function<T, R> valueMapper) {
        String value = s.map(i -> valueMapper.apply(i))
                .collect(Collectors.toList())
                .toString();

        addParamSafe(key, value);
        return this;
    }

    public LogEvent uri(Object uri) {
        if (uri != null) {
            return addParamSafe("uri", uri.toString());
        }
        return this;
    }

    public LogEvent uri(String uri) {
        return addParamSafe("uri", uri);
    }

    public LogEvent host(String host) {
        return addParamSafe("host", host);
    }

    public LogEvent requestParam(List<NameValuePair> params) {
        return addParamSafe("requestParameters", params);
    }

    public LogEvent responseStatus(int status) {
        return addParamSafe("responseStatus", status);
    }

    public LogEvent requestID(HttpRequestBase req) {
        if (req != null) {
            String requestID = MDC.get(REQ_ID);
            addParamSafe("requestID", requestID);
        }
        return this;
    }

    public BabbageException logAndCreateException(int statusCode, Throwable cause) {
        parameter("details", cause.getMessage());
        log();
        switch (statusCode) {
            case 400:
                return new BadRequestException(cause.getMessage());
            case 404:
                return new ResourceNotFoundException(cause.getMessage());
            default:
                return new InternalServerErrorException(cause.getMessage(), cause);
        }
    }

    private LogEvent addParamSafe(String key, Object value) {
        if (StringUtils.isNotEmpty(key) && null != value) {
            super.addParameter(key, value);
        }
        return this;
    }
}
