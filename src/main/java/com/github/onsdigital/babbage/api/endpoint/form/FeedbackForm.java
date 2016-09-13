package com.github.onsdigital.babbage.api.endpoint.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by dave on 8/18/16.
 */
public class FeedbackForm extends PostForm {

    public static final String URI_FIELD = "uri";
    public static final String EMAIL_FIELD = "Email";
    public static final String FEEDBACK_FIELD = "Feedback";
    public static final String FOUND_FIELD = "Found";
    public static final String UNDERSTOOD_FIELD = "Understood";

    private String uri;
    private String emailAddress;
    private String feedback;
    private String found;
    private String understood;

    public String getUri() {
        return uri;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getFound() {
        return found;
    }

    public String getUnderstood() {
        return understood;
    }

    public FeedbackForm uri(String uri) {
        this.uri = uri;
        return this;
    }

    public FeedbackForm emailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public FeedbackForm feedback(String comments) {
        this.feedback = comments;
        return this;
    }

    public FeedbackForm found(String found) {
        this.found = found;
        return this;
    }

    public FeedbackForm understood(String understood) {
        this.understood = understood;
        return this;
    }

    public String toJSON() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FeedbackForm that = (FeedbackForm) o;

        return new EqualsBuilder()
                .append(uri, that.uri)
                .append(emailAddress, that.emailAddress)
                .append(feedback, that.feedback)
                .append(found, that.found)
                .append(understood, that.understood)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uri)
                .append(emailAddress)
                .append(feedback)
                .append(found)
                .append(understood)
                .toHashCode();
    }
}
