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

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.annotation.Nullable;

import org.springframework.core.style.ToStringStyler;
import org.springframework.core.style.ValueStyler;

public class JsonToStringStyler implements ToStringStyler, ValueStyler{
    public static final ToStringStyler INSTANCE = new JsonToStringStyler();
    public static final String INDENT_SPACES = "    ";
    private int indent = 0;
    
    private void newLine(StringBuilder buffer) {
        buffer.append('\n');
        buffer.append(getIndentSpaces(0));
    }
    
    private String getIndentSpaces(int indentOffset) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<indent+indentOffset; i++) {
            sb.append(INDENT_SPACES);
        }
        return sb.toString();
    }

    private String getStringStyle(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        sb.append(value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", ""));
        sb.append('"');
        return sb.toString();
    }

    private String getArrayStyle(Object[] array) {
        if (array.length == 0) {
            return "[]";
        }

        StringJoiner result = new StringJoiner(",\n" + getIndentSpaces(1), "[\n" + getIndentSpaces(1) , "\n" + getIndentSpaces(0) + "]");
        indent++;
        for (Object o : array) {
            result.add(style(o));
        }
        indent--;
        return result.toString();
    }

    private String getListStyle(List<?> list) {
        if (list.isEmpty()) {
            return "[]";
        }
        
        StringJoiner result = new StringJoiner(",\n" + getIndentSpaces(1), "[\n" + getIndentSpaces(1) , "\n" + getIndentSpaces(0) + "]");
        indent++;
        for (Object o : list) {
            result.add(style(o));
        }
        indent--;
        return result.toString();
    }    
    
    private String getMapStyle(Map<?, ?> map) {
        if (map.isEmpty()) {
            return "[]";
        }
        
        StringJoiner result = new StringJoiner(",\n" + getIndentSpaces(1), "[\n" + getIndentSpaces(1) , "\n" + getIndentSpaces(0) + "]");
        indent++;
        for (Object o : map.values()) {
            result.add(style(o));
        }
        indent--;
        return result.toString();
    }    
    
    private String getDefaultStyle(Object value) {
        return String.valueOf(value);
    }
    
    @Override
    public void styleStart(StringBuilder buffer, Object obj) {
        indent++;
        if (!obj.getClass().isArray()) {
            buffer.append("{");
            newLine(buffer);
            buffer.append("\"className\": ");
            buffer.append('"');
            buffer.append(obj.getClass().getSimpleName());
            buffer.append('"');
            buffer.append(',');
        } else {
            buffer.append('[');
            styleValue(buffer, obj);
        }
    }
    
    @Override
    public void styleEnd(StringBuilder buffer, Object obj) {
        indent--;
        newLine(buffer);
        if (!obj.getClass().isArray()) {
            buffer.append('}');
        } else {
            buffer.append(']');
        }
    }

    @Override
    public void styleField(StringBuilder buffer, String fieldName, @Nullable Object value) {
        newLine(buffer);
        buffer.append('"');
        buffer.append(fieldName);
        buffer.append('"');
        buffer.append(": ");
        styleValue(buffer, value);
    }

    @Override
    public void styleValue(StringBuilder buffer, Object value) {
        buffer.append(style(value));
    }
    
    @Override
    public void styleFieldSeparator(StringBuilder buffer) {
        buffer.append(",");
    }

    @Override
    public String style(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return getStringStyle((String) value);
        } else if (value instanceof Object[]) {
            return getArrayStyle((Object[]) value);
        } else if (value instanceof List<?>) {
            return getListStyle((List<?>) value);
        } else if (value instanceof Map) {
            return getMapStyle((Map<?, ?>) value);
        } else {
            return getDefaultStyle(value);
        }
    }
}
