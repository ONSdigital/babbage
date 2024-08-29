package com.github.onsdigital.babbage.search.input;

import com.github.onsdigital.babbage.search.model.ContentType;
import com.github.onsdigital.babbage.search.model.field.Field;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bren on 16/09/15.
 * <p/>
 * Maps filters on search and list pages to actual content types to query. Most of the filters maps to multiple content types
 */
public enum TypeFilter {
    BULLETIN(ContentType.bulletin),
    ARTICLE(ContentType.article, ContentType.article_download),
    COMPENDIA(ContentType.compendium_landing_page),
    TIME_SERIES(ContentType.timeseries),
    DATASETS(ContentType.dataset_landing_page, ContentType.reference_tables),
    USER_REQUESTED_DATA(ContentType.static_adhoc),
    QMI(ContentType.static_qmi),
    METHODOLOGY(ContentType.static_qmi, ContentType.static_methodology, ContentType.static_methodology_download),
    METHODOLOGY_ARTICLE(ContentType.static_methodology, ContentType.static_methodology_download),
    CORPORATE_INFORMATION(ContentType.static_foi, ContentType.static_page, ContentType.static_landing_page, ContentType.static_article);

    private ContentType[] types;

    private static Set<TypeFilter> allFilters;
    private static Set<TypeFilter> publicationFilters;
    private static Set<TypeFilter> dataFilters;
    private static Set<TypeFilter> methodologyFilters;

    TypeFilter(ContentType... types) {
        this.types = types;
    }

    public static Set<TypeFilter> getAllFilters() {
        return initialize(allFilters, TypeFilter.values());
    }

    public static Set<TypeFilter> getPublicationFilters() {
        return initialize(publicationFilters, BULLETIN, ARTICLE, COMPENDIA);

    }

    public static Set<TypeFilter> getDataFilters() {
        return initialize(dataFilters, TIME_SERIES, DATASETS, USER_REQUESTED_DATA);

    }

    public static Set<TypeFilter> getMethodologyFilters() {
        // QMI is no longer used 
        // It's left here so any old pages are still returned
        return initialize(methodologyFilters, QMI, METHODOLOGY_ARTICLE);
    }

    public ContentType[] getTypes() {
        return types;
    }

    private static Set<TypeFilter> initialize(Set<TypeFilter> filterSet, TypeFilter... types) {
        if (filterSet == null) {
            synchronized (TypeFilter.class) {
                if (filterSet == null) {
                    filterSet = new HashSet<>();
                    Collections.addAll(filterSet, types);
                }
            }
        }
        return filterSet;
    }


    public static String[] typeNames(Set<TypeFilter> filters) {
        String[] types = new String[0];
        for (TypeFilter selectedFilter : filters) {
            ContentType[] contentTypes = selectedFilter.getTypes();
            types = ArrayUtils.addAll(types, ContentType.typeNames(contentTypes));
        }
        return types;
    }

    public static ContentType[] contentTypes(Set<TypeFilter> filters) {
        return resolveContentTypes(filters.toArray(new TypeFilter[filters.size()]));
    }

    public static ContentType[] resolveContentTypes(TypeFilter... filters) {
        ContentType[] contentTypes = new ContentType[0];
        for (TypeFilter filter : filters) {
            contentTypes = ArrayUtils.addAll(contentTypes, filter.getTypes());
        }
        return contentTypes;
    }


}
