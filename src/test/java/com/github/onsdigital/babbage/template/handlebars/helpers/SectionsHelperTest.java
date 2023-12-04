package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class SectionsHelperTest {

    private Options options;

    @Before
    public void setup() throws Exception {
        Map<String, String> uriMock = new LinkedHashMap<>();
        uriMock.put("uri", "/economy/bulletin/sometitle");

        Handlebars handlebars = Mockito.mock(Handlebars.class);
        TagType tagType = Mockito.mock(TagType.class);
        Template template = Mockito.mock(Template.class);

        Context.Builder parentContext = Context.newBuilder(uriMock);
        Context.Builder contextBuilder = Context.newBuilder(parentContext.build(), "test");

        Options.Builder builder = new Options.Builder(
                handlebars,
                "SectionsHelper",
                tagType,
                contextBuilder.build(),
                template);
        options = builder.build();
    }

    @Test
    public void testTotalWordCount_WithMarkdownSection_ReturnsCorrectCount() throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();

        Map<String, String> section1 = new HashMap<>();
        section1.put("markdown", "- some test text");
        section1.put("title", "title example");
        sections.add(section1);

        String result = SectionsHelper.totalWordCount.apply(sections, options).toString();
        assertThat(result, equalTo("5"));
    }

    @Test
    public void testTotalWordCount_WithPlainTextSection_ReturnsCorrectCount() throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();

        Map<String, String> section1 = new HashMap<>();
        section1.put("markdown", "some different test text");
        section1.put("title", "title example 2");
        sections.add(section1);

        String result = SectionsHelper.totalWordCount.apply(sections, options).toString();
        assertThat(result, equalTo("7"));
    }

    @Test
    public void testTotalWordCount_WithoutTitle_ReturnsCorrectCount() throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();

        Map<String, String> section1 = new HashMap<>();
        section1.put("markdown", "some more test text");
        section1.put("title", "");
        sections.add(section1);

        String result = SectionsHelper.totalWordCount.apply(sections, options).toString();
        assertThat(result, equalTo("4"));
    }

    @Test
    public void testTotalWordCount_WithoutMarkdownText_ReturnsCorrectCount() throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();

        Map<String, String> section1 = new HashMap<>();
        section1.put("markdown", "");
        section1.put("title", "some example title");
        sections.add(section1);

        String result = SectionsHelper.totalWordCount.apply(sections, options).toString();
        assertThat(result, equalTo("3"));
    }

    @Test
    public void testTotalWordCount_WithMultipleSections_ReturnsCorrectCount() throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();

        Map<String, String> section1 = new HashMap<>();
        section1.put("markdown", "- some test text");
        section1.put("title", "title example 1");
        sections.add(section1);

        Map<String, String> section2 = new HashMap<>();
        section2.put("markdown", "some more test text");
        section2.put("title", "title example 2");
        sections.add(section2);

        String result = SectionsHelper.totalWordCount.apply(sections, options).toString();
        assertThat(result, equalTo("13"));
    }

    @Test
    public void testTotalWordCount_WithNoSections_ReturnsNull() throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();

        CharSequence result = SectionsHelper.totalWordCount.apply(sections, options);
        assertThat(result, equalTo(null));
    }

    @Test
    public void testTotalWordCount_WithBlankSpace_NoErrors() throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();

        Map<String, String> section1 = new HashMap<>();
        section1.put("markdown", " ");
        section1.put("title", "");
        sections.add(section1);

        String result = SectionsHelper.totalWordCount.apply(sections, options).toString();
        assertThat(result, equalTo("0"));
    }
}
