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

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.runner.Run;
import org.utplsql.sqldev.model.ut.OutputLines;

@SuppressWarnings("StringBufferReplaceableByString")
public class JsonToStringStylerTest {

    @Test
    public void outputLinesWithTreeLines() {
        final OutputLines o = new OutputLines();
        o.setLines(new String[] { "line \"1\"", "line2", "line3" });
        o.setNumlines(3);

        final String actual = o.toString();

        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"className\": \"OutputLines\",\n");
        sb.append("    \"lines\": [\n");
        sb.append("        \"line \\\"1\\\"\",\n");
        sb.append("        \"line2\",\n");
        sb.append("        \"line3\"\n");
        sb.append("    ],\n");
        sb.append("    \"numlines\": 3\n");
        sb.append("}");
        Assert.assertEquals(sb.toString(), actual);
    }

    @Test
    public void outputLinesWithoutLines() {
        final OutputLines o = new OutputLines();
        o.setLines(new String[] {});
        o.setNumlines(0);

        final String actual = o.toString();

        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"className\": \"OutputLines\",\n");
        sb.append("    \"lines\": [],\n");
        sb.append("    \"numlines\": 0\n");
        sb.append("}");
        Assert.assertEquals(sb.toString(), actual);
    }

    @Test
    public void emptyRun() {
        final Run r = new Run("1", "MyConnection", Collections.emptyList());

        final String actual = r.toString();

        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"className\": \"Run\",\n");
        sb.append("    \"reporterId\": \"1\",\n");
        sb.append("    \"connectionName\": \"MyConnection\",\n");
        sb.append("    \"pathList\": [],\n");
        sb.append("    \"currentTestNumber\": null,\n");
        sb.append("    \"currentTest\": null,\n");
        sb.append("    \"totalNumberOfTests\": null,\n");
        sb.append("    \"startTime\": null,\n");
        sb.append("    \"endTime\": null,\n");
        sb.append("    \"executionTime\": null,\n");
        sb.append("    \"counter\": {\n");
        sb.append("        \"className\": \"Counter\",\n");
        sb.append("        \"disabled\": 0,\n");
        sb.append("        \"success\": 0,\n");
        sb.append("        \"failure\": 0,\n");
        sb.append("        \"error\": 0,\n");
        sb.append("        \"warning\": 0\n");
        sb.append("    },\n");
        sb.append("    \"infoCount\": null,\n");
        sb.append("    \"errorStack\": null,\n");
        sb.append("    \"serverOutput\": null,\n");
        sb.append("    \"tests\": [],\n");
        sb.append("    \"status\": null,\n");
        sb.append("    \"start\": null,\n");
        sb.append("    \"endTime\": null,\n");
        sb.append("    \"totalNumberOfCompletedTests\": 0\n");
        sb.append("}");
        Assert.assertEquals(sb.toString(), actual);
    }

    @Test
    public void runWithTests() {
        final Run r = new Run("1", "MyConnection", Collections.emptyList());
        final org.utplsql.sqldev.model.runner.Test t1 = new org.utplsql.sqldev.model.runner.Test();
        t1.setId("1");
        t1.setName("Test One");
        r.getTests().put(t1.getId(), t1);
        final org.utplsql.sqldev.model.runner.Test t2 = new org.utplsql.sqldev.model.runner.Test();
        t2.setId("2");
        t2.setName("Test Two");
        r.getTests().put(t2.getId(), t2);

        final String actual = r.toString();

        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("    \"className\": \"Run\",\n");
        sb.append("    \"reporterId\": \"1\",\n");
        sb.append("    \"connectionName\": \"MyConnection\",\n");
        sb.append("    \"pathList\": [],\n");
        sb.append("    \"currentTestNumber\": null,\n");
        sb.append("    \"currentTest\": null,\n");
        sb.append("    \"totalNumberOfTests\": null,\n");
        sb.append("    \"startTime\": null,\n");
        sb.append("    \"endTime\": null,\n");
        sb.append("    \"executionTime\": null,\n");
        sb.append("    \"counter\": {\n");
        sb.append("        \"className\": \"Counter\",\n");
        sb.append("        \"disabled\": 0,\n");
        sb.append("        \"success\": 0,\n");
        sb.append("        \"failure\": 0,\n");
        sb.append("        \"error\": 0,\n");
        sb.append("        \"warning\": 0\n");
        sb.append("    },\n");
        sb.append("    \"infoCount\": null,\n");
        sb.append("    \"errorStack\": null,\n");
        sb.append("    \"serverOutput\": null,\n");
        sb.append("    \"tests\": [\n");
        sb.append("        {\n");
        sb.append("            \"className\": \"Test\",\n");
        sb.append("            \"id\": \"1\",\n");
        sb.append("            \"startTime\": null,\n");
        sb.append("            \"endTime\": null,\n");
        sb.append("            \"executionTime\": null,\n");
        sb.append("            \"counter\": {\n");
        sb.append("                \"className\": \"Counter\",\n");
        sb.append("                \"disabled\": 0,\n");
        sb.append("                \"success\": 0,\n");
        sb.append("                \"failure\": 0,\n");
        sb.append("                \"error\": 0,\n");
        sb.append("                \"warning\": 0\n");
        sb.append("            },\n");
        sb.append("            \"errorStack\": null,\n");
        sb.append("            \"serverOutput\": null,\n");
        sb.append("            \"warnings\": null,\n");
        sb.append("            \"executableType\": null,\n");
        sb.append("            \"ownerName\": null,\n");
        sb.append("            \"objectName\": null,\n");
        sb.append("            \"procedureName\": null,\n");
        sb.append("            \"disabled\": null,\n");
        sb.append("            \"name\": \"Test One\",\n");
        sb.append("            \"description\": null,\n");
        sb.append("            \"testNumber\": null,\n");
        sb.append("            \"failedExpectations\": null,\n");
        sb.append("            \"statusIcon\": null,\n");
        sb.append("            \"warningIcon\": null,\n");
        sb.append("            \"infoIcon\": null\n");
        sb.append("        },\n");
        sb.append("        {\n");
        sb.append("            \"className\": \"Test\",\n");
        sb.append("            \"id\": \"2\",\n");
        sb.append("            \"startTime\": null,\n");
        sb.append("            \"endTime\": null,\n");
        sb.append("            \"executionTime\": null,\n");
        sb.append("            \"counter\": {\n");
        sb.append("                \"className\": \"Counter\",\n");
        sb.append("                \"disabled\": 0,\n");
        sb.append("                \"success\": 0,\n");
        sb.append("                \"failure\": 0,\n");
        sb.append("                \"error\": 0,\n");
        sb.append("                \"warning\": 0\n");
        sb.append("            },\n");
        sb.append("            \"errorStack\": null,\n");
        sb.append("            \"serverOutput\": null,\n");
        sb.append("            \"warnings\": null,\n");
        sb.append("            \"executableType\": null,\n");
        sb.append("            \"ownerName\": null,\n");
        sb.append("            \"objectName\": null,\n");
        sb.append("            \"procedureName\": null,\n");
        sb.append("            \"disabled\": null,\n");
        sb.append("            \"name\": \"Test Two\",\n");
        sb.append("            \"description\": null,\n");
        sb.append("            \"testNumber\": null,\n");
        sb.append("            \"failedExpectations\": null,\n");
        sb.append("            \"statusIcon\": null,\n");
        sb.append("            \"warningIcon\": null,\n");
        sb.append("            \"infoIcon\": null\n");
        sb.append("        }\n");
        sb.append("    ],\n");
        sb.append("    \"status\": null,\n");
        sb.append("    \"start\": null,\n");
        sb.append("    \"endTime\": null,\n");
        sb.append("    \"totalNumberOfCompletedTests\": 0\n");
        sb.append("}");
        Assert.assertEquals(sb.toString(), actual);
    }
}
