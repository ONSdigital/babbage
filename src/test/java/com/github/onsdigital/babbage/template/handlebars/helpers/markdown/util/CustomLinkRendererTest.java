package com.github.onsdigital.babbage.template.handlebars.helpers.markdown.util;

import org.junit.Test;
import org.pegdown.LinkRenderer;
import org.pegdown.ast.MailLinkNode;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;



public class CustomLinkRendererTest {
    @Test
    public void shouldNotObfuscateEmailAddresses()  {
        
        String expectedHref = "mailto:test@example.com";
        String expectedText = "test@example.com";

        CustomLinkRenderer renderer = new CustomLinkRenderer();
        MailLinkNode testNode = new MailLinkNode("test@example.com") ;

        LinkRenderer.Rendering result = renderer.render(testNode);

        assertThat(result.href, equalTo(expectedHref));
        assertThat(result.text, equalTo(expectedText));

    }
}
