package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Options;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StringHelperTest {

    @Mock
    private Options options;

    @Before
    public void setup() throws Exception {
        when(options.isFalsy(any())).thenReturn(false);
    }

    @Test
    public void testWordCount_WithHTMLString_ReturnsCorrectCount() throws Exception {
        String result = StringHelper.wordCount.apply("<h1>some title</h1>", options).toString();
        assertThat(result, equalTo("2"));
    }

    @Test
    public void testWordCount_WithNoHTML_ReturnsCorrectCount() throws Exception {
        String result = StringHelper.wordCount.apply("some more test words", options).toString();
        assertThat(result, equalTo("4"));
    }

    @Test
    public void testWordCount_WithSymbol_ReturnsCorrectCount() throws Exception {
        String result = StringHelper.wordCount.apply("with punctuation symbol!", options).toString();
        assertThat(result, equalTo("3"));
    }

    @Test
    public void testWordCount_WithWhiteSpace_ReturnsZero() throws Exception {
        String result = StringHelper.wordCount.apply(" ", options).toString();
        assertThat(result, equalTo("0"));
    }

    @Test
    public void testWordCount_WithEmptyString_ReturnsNull() throws Exception {
        when(options.isFalsy(any())).thenReturn(true);
        CharSequence result = StringHelper.wordCount.apply("", options);
        assertThat(result, equalTo(null));
    }
}