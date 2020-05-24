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
package org.utplsql.sqldev.dal;

import java.util.Collections;
import java.util.List;

public class StringUtil {
    // do not instantiate this class
    private StringUtil() {
        super();
    }
    
    public static String getCSV(List<String> list, int indentSpaces) {
        final StringBuilder sb = new StringBuilder();
        final String indent = String.join("", Collections.nCopies(indentSpaces, " "));
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
}