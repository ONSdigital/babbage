package com.github.onsdigital.babbage.api.endpoint.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dave on 8/18/16.
 */
public class FeedbackForm extends PostForm {

    private String uri;
    private String emailAddress;
    private String comments;
    private String questionOne;
    private String questionTwo;

    public String getUri() {
        return uri;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getComments() {
        return comments;
    }

    public String getQuestionOne() {
        return questionOne;
    }

    public String getQuestionTwo() {
        return questionTwo;
    }

    public FeedbackForm uri(String uri) {
        this.uri = uri;
        return this;
    }

    public FeedbackForm emailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public FeedbackForm comments(String comments) {
        this.comments = comments;
        return this;
    }

    public FeedbackForm questionOne(String questionOne) {
        this.questionOne = questionOne;
        return this;
    }

    public FeedbackForm questionTwo(String questionTwo) {
        this.questionTwo = questionTwo;
        return this;
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
