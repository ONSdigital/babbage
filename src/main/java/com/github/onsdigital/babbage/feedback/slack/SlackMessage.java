package com.github.onsdigital.babbage.feedback.slack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 8/23/16.
 */
public class SlackMessage {

    private String text;
    private List<Attachment> attachments;

    public SlackMessage() {
        this.attachments = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public SlackMessage setText(String text) {
        this.text = text;
        return this;
    }

    public SlackMessage addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public SlackMessage setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }
}
