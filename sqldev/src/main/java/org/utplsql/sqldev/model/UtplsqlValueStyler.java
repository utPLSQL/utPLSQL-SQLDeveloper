/*
 * Copyright 2020 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.utplsql.sqldev.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.core.style.DefaultValueStyler;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

public class UtplsqlValueStyler extends DefaultValueStyler {
    private static final String EMPTY = "[[empty]]";
    private static final String COLLECTION = "collection";
    private static final String SET = "set";
    private static final String LIST = "list";
    private static final String MAP = "map";
    private static final String EMPTY_MAP = MAP + EMPTY;
    private static final String ARRAY = "array";
    
    @Override
    public String style(@Nullable Object value) {
        if (value == null) {
            return super.style(value);
        } else if (value instanceof Map) {
            return styleMap((Map<?, ?>) value);
        } else if (value instanceof Map.Entry) {
            return styleMapEntry((Map.Entry<?, ?>) value);
        } else if (value instanceof Collection) {
            return styleCollection((Collection<?>) value);
        } else if (value.getClass().isArray()) {
            return styleArray(ObjectUtils.toObjectArray(value));
        } else {
            return super.style(value);
        }
    }

    private <K, V> String styleMap(Map<K, V> value) {
        if (value.isEmpty()) {
            return EMPTY_MAP;
        }

        StringJoiner result = new StringJoiner(",\n", "[", "]");
        for (Map.Entry<K, V> entry : value.entrySet()) {
            result.add(styleMapEntry(entry));
        }
        return MAP + result;
    }

    private String styleMapEntry(Map.Entry<?, ?> value) {
        return style(value.getKey()) + " -> " + style(value.getValue());
    }

    private String styleCollection(Collection<?> value) {
        String collectionType = getCollectionTypeString(value);

        if (value.isEmpty()) {
            return collectionType + EMPTY;
        }

        StringJoiner result = new StringJoiner(",\n", "[", "]");
        for (Object o : value) {
            result.add(style(o));
        }
        return collectionType + result;
    }

    private String getCollectionTypeString(Collection<?> value) {
        if (value instanceof List) {
            return LIST;
        } else if (value instanceof Set) {
            return SET;
        } else {
            return COLLECTION;
        }
    }

    private String styleArray(Object[] array) {
        if (array.length == 0) {
            return ARRAY + '<' + ClassUtils.getShortName(array.getClass().getComponentType()) + '>' + EMPTY;
        }

        StringJoiner result = new StringJoiner(",\n ", "[", "]");
        for (Object o : array) {
            result.add(style(o));
        }
        return ARRAY + '<' + ClassUtils.getShortName(array.getClass().getComponentType()) + '>' + result;
    }

}
