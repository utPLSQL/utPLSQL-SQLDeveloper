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

import java.util.Collections;
import java.util.List;

public class StringTools {
    // do not instantiate this class
    private StringTools() {
        super();
    }

    public static String getCSV(List<String> list, String indent) {
        final StringBuilder sb = new StringBuilder();
        for (final String item : list) {
            if (sb.length() > 0) {
                sb.append(",\n");
            }
            sb.append(indent);
            sb.append("'");
            sb.append(item);
            sb.append("'");
        }
        sb.append("\n");
        return sb.toString();
    }
    
    public static String getCSV(List<String> list, int indentSpaces) {
        return getCSV(list, repeat(" ", indentSpaces));
    }
    
    public static String getSimpleCSV(List<String> list) {
        final StringBuilder sb = new StringBuilder();
        for (final String item : list) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static String repeat(String s, int times) {
        return String.join("", Collections.nCopies(times, s));
    }

    public static String replaceTabsWithSpaces(final CharSequence input, int indentSpaces) {
        final String spaces = StringTools.repeat(" ", indentSpaces);
        return input.toString().replace("\t", spaces);
    }

    public static String formatDateTime(final String dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            if (dateTime.length() == 26) {
                return dateTime.replace("T", " ").substring(0, 23);
            } else {
                return dateTime;
            }
        }
    }
}
