package com.github.onsdigital.babbage.template.handlebars;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HandlebarsRendererTest {

    @Rule
    public TemporaryFolder templates = new TemporaryFolder();

    @Test
    public void testRenderTemplate() throws Exception {
        new HandlebarsRenderer("","", false);
    }

    @Test
    public void rendersPartialWithMapContext() throws Exception {
        writeTemplate("partial", "{{type}}");
        assertEquals("chart", render("{{> partial}}", Collections.singletonMap("type", "chart")));
    }

    @Test
    public void resolvesMapSizeWithoutExposingInternalFields() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "chart");
        assertEquals("1|chart|", render("{{size}}|{{type}}|{{threshold}}", data));
    }

    @Test
    public void mapKeysTakePrecedenceOverContainerSize() throws Exception {
        assertEquals("custom", render("{{size}}", Collections.singletonMap("size", "custom")));
    }

    @Test
    public void resolvesCollectionSizeForDifferentImplementations() throws Exception {
        for (Object collection : Arrays.asList(
                new ArrayList<>(Arrays.asList("a", "b")),
                Arrays.asList("a", "b"),
                Collections.unmodifiableList(Arrays.asList("a", "b")))) {
            assertEquals("2", render("{{items.size}}", Collections.singletonMap("items", collection)));
        }
        assertEquals("1", render("{{items.size}}",
                Collections.singletonMap("items", Collections.singleton("a"))));
        assertEquals("0", render("{{items.size}}",
                Collections.singletonMap("items", Collections.emptyList())));
    }

    @Test
    public void preservesObjectFieldAndArrayLengthResolution() throws Exception {
        assertEquals("title|summary|2", render("{{title}}|{{summary}}|{{items.length}}", new PageData()));
    }

    private String render(String template, Object data) throws Exception {
        writeTemplate("test", template);
        return new HandlebarsRenderer(templates.getRoot().getAbsolutePath(), ".handlebars", false)
                .render("test", data, null);
    }

    private void writeTemplate(String name, String template) throws Exception {
        Files.write(templates.getRoot().toPath().resolve(name + ".handlebars"),
                template.getBytes(StandardCharsets.UTF_8));
    }

    private static class PageData {
        // These attributes will flag as unused but are necessary for testing template rendering
        // They are accessed via template rendering reflection.
        private String title = "title";
        private String summary = "summary";
        private String[] items = {"a", "b"};

    }

    public void testRenderTemplate1() throws Exception {

    }

    public void testInitialize() {

    }
}