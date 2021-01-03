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
package org.utplsql.sqldev.test;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.StringTools;

public class StringToolsTest {

    @Test
    public void one_entry_as_CSV() {
        final List<String> list = new ArrayList<>();
        list.add("hello");
        Assert.assertEquals("     'hello'\n", StringTools.getCSV(list, 5));
    }

    @Test
    public void two_entries_as_CSV() {
        final List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");
        Assert.assertEquals("     'hello',\n     'world'\n", StringTools.getCSV(list, 5));
    }

    @Test
    public void one_entry_as_simpleCSV() {
        final List<String> list = new ArrayList<>();
        list.add("hello");
        Assert.assertEquals("hello", StringTools.getSimpleCSV(list));
    }

    @Test
    public void two_entries_as_simpleCSV() {
        final List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");
        Assert.assertEquals("hello, world", StringTools.getSimpleCSV(list));
    }
    
    @Test
    public void earliest_date_from__millis() {
        long zoneDiff = TimeZone.getDefault().getRawOffset();
        Assert.assertEquals("1970-01-01T00:00:00.000000", StringTools.millisToDateTimeString(-zoneDiff));
    }
    
    @Test
    public void date_from__millis_with_millis() {
        long zoneDiff = TimeZone.getDefault().getRawOffset();
        Assert.assertEquals("1970-01-01T00:00:42.123000", StringTools.millisToDateTimeString(42123 - zoneDiff));
    }


}
