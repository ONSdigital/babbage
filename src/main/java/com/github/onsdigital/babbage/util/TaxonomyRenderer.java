package com.github.onsdigital.babbage.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;


public class TaxonomyRenderer {
    static List<String> topicsToIgnore = Arrays.asList("Census", "Survey");

    public static List<Map<String, Object>> navigationToTaxonomy(Object items) throws IOException, URISyntaxException {

        List<Map<String, Object>> context = new ArrayList<>();
        List<Map<String, Object>> itemsArray = new ArrayList();

        try {
            itemsArray = (ArrayList) items;
            for (Map<String, Object> item : itemsArray) {

                if (!((topicsToIgnore.contains(item.get("title"))) || (item.isEmpty()))) {
                    Map<String, Object> itemMap = new HashMap<String, Object>();
                    Map<String, Object> itemDescriptionMap = new HashMap<String, Object>();

                    itemMap.put("uri", item.get("uri"));
                    itemDescriptionMap.put("title", item.get("label"));
                    itemMap.put("description", itemDescriptionMap);
                    itemMap.put("type", "taxonomy_landing_page");

                    List<Map<String, Object>> childArray = new ArrayList();
                    childArray = (ArrayList) item.get("subtopics");

                    List<Map<String, Object>> children = new ArrayList();
                    if (childArray != null) {
                        for (Map<String, Object> child : childArray) {
                            Map<String, Object> childMap = new HashMap<String, Object>();
                            Map<String, Object> childDescriptionMap = new HashMap<String, Object>();
                            childMap.put("uri", child.get("uri"));
                            childDescriptionMap.put("title", child.get("label"));
                            childMap.put("description", childDescriptionMap);
                            childMap.put("type", "taxonomy_landing_page");
                            children.add(childMap);
                        }
                    }
                    itemMap.put("children", children);
                    context.add(itemMap);
                }
            }


        } catch (Exception e) {
            logTaxonomyRendererError(items, e);
//            return context;
        }
        return context;
    }

    private static void logTaxonomyRendererError(Object items, Exception e) {
        error().exception(e).data("items", items).log("TaxonomyRenderer items");
    }
}
