package com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util;

import org.pegdown.LinkRenderer;
import org.pegdown.ast.MailLinkNode;

public class CustomLinkRenderer extends LinkRenderer{
    // Overriding the MailLinkNode render in order to stop the automatic
    // obfuscation that happens. This is down to the way we generate eTags using rendererd
    // content
    @Override
    public Rendering render(MailLinkNode node) {
        String text = node.getText();
        return new Rendering("mailto:" + text, text);
    }
}
