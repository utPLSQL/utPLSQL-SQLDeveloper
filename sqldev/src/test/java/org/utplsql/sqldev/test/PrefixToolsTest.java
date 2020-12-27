/*
 * Copyright 2019 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.PrefixTools;

public class PrefixToolsTest {
    @Test
    public void two() {
        final String actual = PrefixTools.commonPrefix(Arrays.asList("junit.test.a", "junit.test.b"));
        final String expected = "junit.test.";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void oneWithDot() {
        final String actual = PrefixTools.commonPrefix(Collections.singletonList("junit.test.a"));
        final String expected = "junit.test.";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void oneWithoutDot() {
        final String actual = PrefixTools.commonPrefix(Collections.singletonList("junit-test-a"));
        final String expected = "";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void twoOverlapLeft() {
        final String actual = PrefixTools.commonPrefix(Arrays.asList("a.b.c", "a.b.c.d"));
        final String expected = "";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void twoOverlapRight() {
        final String actual = PrefixTools.commonPrefix(Arrays.asList("a.b.c.d", "a.b.c"));
        final String expected = "";
        Assert.assertEquals(expected, actual);
    }
}
