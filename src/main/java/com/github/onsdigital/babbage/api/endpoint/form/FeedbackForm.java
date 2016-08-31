package com.github.onsdigital.babbage.api.endpoint.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dave on 8/18/16.
 */
public class FeedbackForm extends PostForm {

    private String comments;
    private boolean optionOne;
    private boolean optionTwo;

    public FeedbackForm(String comments, boolean optionOne, boolean optionTwo) {
        this.comments = comments;
        this.optionOne = optionOne;
        this.optionTwo = optionTwo;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isOptionOne() {
        return optionOne;
    }

    public void setOptionOne(boolean optionOne) {
        this.optionOne = optionOne;
    }

    public boolean isOptionTwo() {
        return optionTwo;
    }

    public void setOptionTwo(boolean optionTwo) {
        this.optionTwo = optionTwo;
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
