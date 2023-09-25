package com.github.onsdigital.babbage.template.handlebars.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.helper.EachHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.base.BabbageHandlebarsHelper;
import com.github.onsdigital.babbage.template.handlebars.helpers.util.HelperUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.reverse;

/**
 * Created by bren on 02/07/15.
 * <p/>
 * Repeat content fixed number of times, or array sorted by a field or in reverse order, does not work with set or map objects
 */

public class LoopHelper extends EachHelper implements BabbageHandlebarsHelper<Object> {

    public static final String HELPER_NAME = "loop";

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (options.isFalsy(context)) {
            return options.inverse();
        }

        //Delegate everything other than repeat number to each helper
        if (context instanceof Number == false) {
            List list = (List) context;
            boolean reverse = Boolean.TRUE.equals(options.hash("reverse"));
            int limit = resolveLimit(list, options);
            String field = options.hash("orderBy");
            sort(list, reverse, field);
            return apply(list, options, limit);
        }
        //Create an integer list with given number and delegate to each helper to be able to use all features of each helper (index, last , first etc)
        return super.apply(buildList((Number) context), options);
    }

    private CharSequence apply(final List list, final Options options, int limit)
            throws IOException {
        return render(list, options, limit);
    }

    private CharSequence render(final List<Object> context, final Options options, int limit)
            throws IOException {
        StringBuilder buffer = new StringBuilder();
        int index = -1;
        Context parent = options.context;
        while (index < limit - 1) {
            index += 1;
            Object element = context.get(index);
            boolean first = index == 0;
            boolean even = index % 2 == 0;
            boolean last = (limit == index + 1);
            Context current = Context.newBuilder(parent, element)
                    .combine("@index", index)
                    .combine("@first", first ? "first" : "")
                    .combine("@last", last ? "last" : "")
                    .combine("@odd", even ? "" : "odd")
                    .combine("@even", even ? "even" : "")
                            // 1-based index
                    .combine("@index_1", index + 1)
                    .build();
            buffer.append(options.fn(current));
            current.destroy();
        }
        return buffer.toString();
    }

    private List<Integer> buildList(Number repeatNumber) {
        ArrayList<Integer> integers = new ArrayList<Integer>();
        int numberOfRepeat = repeatNumber.intValue();
        for (int i = 0; i < numberOfRepeat; i++) {
            integers.add(i);
        }
        return integers;
    }

    /**
     * Number of elements to show
     *
     * @return
     */
    private int resolveLimit(List list, Options options) {
        int limit = options.hash("limit", 0);
        if (limit <= 0 || limit > list.size()) {
            return list.size();
        } else {
            return limit;
        }
    }

    @Override
    public void register(Handlebars handlebars) {
        handlebars.registerHelper(HELPER_NAME, this);
    }


    private void sort(List list, boolean reverse, String field) {
        if (field == null) {
            if (reverse) {
                reverse(list);
            } else {
                return; // no sorting if no field is set and reversed not set
            }
        } else {
            Collections.sort(list, new MapFieldComparator<>(field, reverse));
        }

        return;
    }

    /**
     * Compares two map fields. If given object is not map it will fail,
     * not using reflection to get field names. It is sufficient since Babbage deserialise json objects to map for rendering
     *
     * @param <Object>
     */
    private class MapFieldComparator<Object> implements Comparator {

        boolean reverse;
        String field;

        /**
         * Field can be in dot notation e.g. description.releaseDate
         *
         * @param field
         * @param reverse
         */
        private MapFieldComparator(String field, boolean reverse) {
            this.field = field;
            this.reverse = reverse;
        }

        @Override
        public int compare(java.lang.Object o1, java.lang.Object o2) {
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }

            Map m1 = (Map) o1;
            Map m2 = (Map) o2;

            Comparable val1 = getField(m1, this.field);
            Comparable val2 = getField(m2, this.field);

            int result = HelperUtils.compare(val1, val2);

            if (val1 == null || val2 == null) {
                return result;//nulls should always be last, reverse or not
            }
            return reverse ? result * -1 : result;

        }


        private Comparable getField(Map m, String field) {
            if (StringUtils.isEmpty(field)) {
                return null;
            }

            int i = field.indexOf(".");
            if (i < 0) {
                Comparable comparable = (Comparable) m.get(field);
                return comparable;
            } else {
                String firstField = field.substring(0, i);
                return getField((Map) m.get(firstField), field.substring(i + 1));
            }

        }
    }

}

