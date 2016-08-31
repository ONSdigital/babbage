package com.github.onsdigital.babbage.api.endpoint.form;

import java.util.HashMap;
import java.util.Map;

public class PostFormResponse {

    private PostForm postForm;
    private int status;
    private Map<String, String> errors;

    public PostFormResponse(PostForm postForm, int status) {
        this.postForm = postForm;
        this.status = status;
        this.errors = new HashMap<>();
    }

    public PostFormResponse(PostForm postForm, int status, FormError... errors) {
        this(postForm, status);

        for (FormError entry : errors) {
            this.errors.entrySet().add(entry);
        }
    }

    public PostForm getPostForm() {
        return postForm;
    }

    public void setPostForm(PostForm postForm) {
        this.postForm = postForm;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
