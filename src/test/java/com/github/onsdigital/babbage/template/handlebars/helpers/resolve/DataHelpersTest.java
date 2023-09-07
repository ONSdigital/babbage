package com.github.onsdigital.babbage.template.handlebars.helpers.resolve;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataHelpersTest {
    @Mock
    DataHelpers mockDH;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public static Map<String, Object> ItemMapBusinessSubtopics () {
        Map<String, Object> testItemMap = new HashMap<>();
        testItemMap.put("description", "Activities of businesses and industry in the UK, including data on the production and trade of goods and services, sales by retailers, characteristics of businesses, the construction and manufacturing sectors, and international trade.");
        testItemMap.put("label", "Business, industry and trade");
        testItemMap.put("links", "{self={href=/topics/businessindustryandtrade, id=businessindustryandtrade}}");
        testItemMap.put("name", "business-industry-and-trade");
        testItemMap.put("title", "Business, industry and trade");
        testItemMap.put("uri", "/businessindustryandtrade");
        testItemMap.put("subtopics", ItemListBusinessSubtopics());
        return testItemMap;
    }
    public static List<Map<String, Object>> ItemListBusinessSubtopics () {
        List<Map<String, Object>> subtopicsList = new ArrayList<>();
        Map<String, Object> testSubtopicsMap = new HashMap<>();
        testSubtopicsMap.put("description", "UK businesses registered for VAT and PAYE with regional breakdowns, including data on size (employment and turnover) and activity (type of industry), research and development, and business services.");
        testSubtopicsMap.put("label", "Business");
        testSubtopicsMap.put("links", "{self={href=/topics/business, id=business}}");
        testSubtopicsMap.put("name", "business");
        testSubtopicsMap.put("title", "Business");
        testSubtopicsMap.put("uri", "/businessindustryandtrade/business");
        subtopicsList.add(testSubtopicsMap);
        return subtopicsList;
    }

    @Test
    public void testDataHelpers_resolveTaxonomy_nullOption_thrownException() {
        Object uri = "/navigation";

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> DataHelpers.resolveTaxonomy.apply(uri, null));
        org.junit.Assert.assertEquals(NullPointerException.class, exception.getClass());

    }

    @Test
    public void testDataHelpers_resolveTaxonomy_Navigation() throws Exception {
        //        build options
        Object uri = "/navigation";
        Options options = setNavigationOptions();
        org.junit.Assert.assertEquals(options.helperName, "resolveTaxonomy");
        assertEquals(2, (int) options.<Integer>hash("depth"));

        List<Map<String, Object>> testItemList = new java.util.ArrayList<>();
        testItemList.add(ItemMapBusinessSubtopics());
        mockDH.isPublication = false;
        mockDH.isNavigation = true;

        Object resolve = DataHelpers.resolveTaxonomy.apply(uri, options);
        assertTrue(resolve.toString().isEmpty());
    }
    @Test
    public void testDataHelpers_resolveTaxonomy_Taxonomy() throws Exception {
        //        build options
        Object uri = "/navigation";
        Options options = setNavigationOptions();
        org.junit.Assert.assertEquals(options.helperName, "resolveTaxonomy");
        assertEquals(2, (int) options.<Integer>hash("depth"));

        List<Map<String, Object>> testItemList = new java.util.ArrayList<>();
        testItemList.add(ItemMapBusinessSubtopics());
        mockDH.isPublication = true;
        mockDH.isNavigation = true;
        Object resolve = DataHelpers.resolveTaxonomy.apply(uri, options);
        assertTrue(resolve.toString().isEmpty());
    }

    private Options setNavigationOptions() {
        Map<String, String> uriMock = new LinkedHashMap<>();
        uriMock.put("uri", "/navigation");
        Map<String, Object> optionMap = new HashMap<>();
        optionMap.put("depth", 2);

        Handlebars handlebars = mock(Handlebars.class);
        TagType tagType = mock(TagType.class);
        Template template = mock(Template.class);

        Context.Builder parentContext = Context.newBuilder(uriMock);

        Context.Builder contextBuilder = Context.newBuilder(parentContext.build(), "test");

        Options.Builder builder = new Options.Builder(
                handlebars,
                "resolveTaxonomy",
                tagType,
                contextBuilder.build(),
                template);
        builder.setHash(optionMap);
        return builder.build();
    }

}
