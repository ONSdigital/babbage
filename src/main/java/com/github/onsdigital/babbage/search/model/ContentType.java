package com.github.onsdigital.babbage.search.model;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by bren on 08/09/15.
 * <p/>
 * Content types.
 */
public enum ContentType {
    home_page,
    home_page_census,
    taxonomy_landing_page,
    product_page,
    bulletin(1.55f),
    article(1.30f),
    article_download(1.30f),
    timeseries(1.2f),
    data_slice,
    compendium_landing_page(1.30f),
    compendium_chapter,
    compendium_data,
    static_landing_page,
    static_article,
    static_methodology,
    static_methodology_download,
    static_page,
    static_qmi,
    static_foi,
    static_adhoc(1.25f),
    dataset,
    dataset_landing_page(1.35f),
    timeseries_dataset,
    release,
    reference_tables,
    chart,
    table,
    equation,
    departments, //departments type is index under departments index, not a part of ons index which has usual content
    feedback_received;


    //Content type boost in search results
    private Float weight;

    ContentType(float weight) {
        this.weight = weight;
    }

    ContentType() {

    }


    public Float getWeight() {
        return weight;
    }

    public static String[] typeNames(ContentType... contentTypes) {
        String[] types = new String[0];
        for (ContentType type : contentTypes) {
            types = ArrayUtils.addAll(types, type.name());
        }
        return types;
    }
}
