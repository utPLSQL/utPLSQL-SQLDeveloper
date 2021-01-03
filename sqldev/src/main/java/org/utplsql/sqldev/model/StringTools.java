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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.utplsql.sqldev.exception.GenericRuntimeException;

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
    
    public static String millisToDateTimeString(long millis) {
        final Date dateTime = new Date(millis);
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000'");
        return df.format(dateTime);
    }

    public static String getSysdate() {
        return millisToDateTimeString(System.currentTimeMillis());
    }
    
    public static long dateTimeStringToMillis(final String dateTime) {
        // handle milliseconds separately since they get lost (rounded) when converted to date
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date;
        try {
            date = df.parse(dateTime.substring(0, 20));
        } catch (ParseException e) {
            throw new GenericRuntimeException("cannot parse datetime string " + dateTime + ".", e);
        }
        long millis = Long.parseLong(dateTime.substring(20, 23));
        return date.getTime() + millis;
    }
    
    public static double elapsedTime(String startDateTime, String endDateTime) {
        double start = (double) dateTimeStringToMillis(startDateTime);
        double end = (double) dateTimeStringToMillis(endDateTime);
        return (end - start) / 1000;
    }
    
}
