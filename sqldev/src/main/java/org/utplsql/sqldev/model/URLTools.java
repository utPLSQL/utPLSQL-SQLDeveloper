/*
 * Copyright 2018 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLTools {

    // do not instantiate this class
    private URLTools() {
        super();
    }
    
    public static String replaceHexChars(final String input) {
        String output = input;
        final Pattern p = Pattern.compile("%([0-9A-F]{2})");
        final Matcher m = p.matcher(input);
        while (m.find()) {
            final String what = m.group(0);
            final int decimal = Integer.parseInt(m.group(1), 16);
            final String with = String.valueOf((char) decimal);
            output = output.replace(what, with);
        }
        return output;
    }

    public static String getConnectionName(final URL url) {
        final Pattern p = Pattern.compile("(sqldev.nav:)([^/]+)(//)?");
        final Matcher m = p.matcher(url.toString());
        if (m.find()) {
            return replaceHexChars(m.group(2)
                    .replace("IdeConnections%2523", "IdeConnections%23")) // remove connection prefix
                    .replace("+", " "); // spaces are encoded als plus signs, fix that, see #118
        } else {
            return "";
        }
    }

    public static String getSchema(final URL url) {
        final Pattern p = Pattern.compile("(//)([^/]+)");
        final Matcher m = p.matcher(url.toString());
        if (m.find()) {
            return m.group(2);
        } else {
            return "";
        }
    }

    public static String getObjectType(final URL url) {
        final Pattern p = Pattern.compile("(//)([^/]+)(/)([^/]+)");
        final Matcher m = p.matcher(url.toString());
        if (m.find()) {
            return m.group(4);
        } else {
            return "";
        }
    }

    public static String getMemberObject(final URL url) {
        final Pattern p = Pattern.compile("(/)([^/]+)(#MEMBER)");
        final Matcher m = p.matcher(url.toString());
        if (m.find()) {
            return m.group(2);
        } else {
            return "";
        }
    }
}
