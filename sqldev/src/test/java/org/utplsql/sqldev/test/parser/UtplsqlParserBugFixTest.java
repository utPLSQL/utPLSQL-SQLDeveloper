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
package org.utplsql.sqldev.test.parser;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.parser.UtplsqlParser;

public class UtplsqlParserBugFixTest {

    // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/1
    @Test
    public void issue1MatchingExprInStringLiterals() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace package body test_expect_not_to_be_null\n");
        sb.append("is\n");
        sb.append("    gc_object_name constant varchar2(30) := 't_not_to_be_null_test';\n");
        sb.append("    gc_nested_table_name constant varchar2(30) := 'tt_not_to_be_null_test';\n");
        sb.append("    gc_varray_name constant varchar2(30) := 'tv_not_to_be_null_test';\n\n");

        sb.append("    procedure cleanup_expectations\n");
        sb.append("    is\n");
        sb.append("    begin\n");
        sb.append("        ut3.ut_expectation_processor.clear_expectations();\n");
        sb.append("    end;\n\n");

        sb.append("    procedure create_types\n");
        sb.append("    is");
        sb.append("        pragma autonomous_transaction;\n");
        sb.append("    begin\n");
        sb.append("        execute immediate 'create type '||gc_object_name||' is object (dummy number)'\n;");
        sb.append("        execute immediate ' create type '||gc_nested_table_name||' is table of number';\n");
        sb.append("        execute immediate '\n");
        sb.append("        create type '||gc_varray_name||' is varray(1) of number';\n");
        sb.append("    end;\n\n");

        sb.append("    procedure drop_types\n");
        sb.append("    is\n");
        sb.append("        pragma autonomous_transaction;\n");
        sb.append("    begin\n");
        sb.append("        execute immediate 'drop type '||gc_object_name;\n");
        sb.append("        execute immediate ' drop type '||gc_nested_table_name;\n");
        sb.append("        execute immediate '\n");
        sb.append("        drop type '||gc_varray_name;\n");
        sb.append("    end;\n\n");

        sb.append("    procedure blob_not_null\n");
        sb.append("    is\n");
        sb.append("    begin\n");
        sb.append("        --Act\n");
        sb.append("        execute immediate expectations_helpers.unary_expectation_block('not_to_be_null', 'blob', 'to_blob(''abc'')');\n");
        sb.append("        --Assert\n");
        sb.append("        ut.expect(anydata.convertCollection(ut3.ut_expectation_processor.get_failed_expectations())).to_be_empty();\n");
        sb.append("    end;\n\n");

        sb.append("--and so on...\n\n");

        sb.append("end;");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        Assert.assertEquals("test_expect_not_to_be_null.cleanup_expectations",
                parser.getPathAt(parser.toPosition(7, 1)));
        Assert.assertEquals("test_expect_not_to_be_null.create_types", parser.getPathAt(parser.toPosition(13, 1)));
        // was: '||gc_varray_name||'.drop_types
        Assert.assertEquals("test_expect_not_to_be_null.drop_types", parser.getPathAt(parser.toPosition(23, 1)));
        // was: '||gc_varray_name||'.blob_not_null
        Assert.assertEquals("test_expect_not_to_be_null.blob_not_null", parser.getPathAt(parser.toPosition(33, 1)));
    }

    @Test
    // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/7
    public void issue7WrongPositionWithWindowsLineSeparator() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace package test_expect_not_to_be_null\n");
        sb.append("is\n");
        sb.append("    --%suite(expectations - not_to_be_null)\n");
        sb.append("    --%suitepath(utplsql.core.expectations.unary)\n\n");

        sb.append("    --%aftereach\n");
        sb.append("    procedure cleanup_expectations;\n\n");

        sb.append("    --%beforeall\n");
        sb.append("    procedure create_types;\n\n");

        sb.append("    --%afterallv");
        sb.append("    procedure drop_types;\n\n");

        sb.append("    --%test(Gives success for not null blob)\n");
        sb.append("    procedure blob_not_null;\n\n");

        sb.append("    --%test(Gives success for blob with length 0)\n");
        sb.append("    procedure blob_0_length;\n\n");

        sb.append("    -- ...\n");
        sb.append("end test_expect_not_to_be_null;\n");
        sb.append("/\n");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        Assert.assertEquals("test_expect_not_to_be_null.blob_not_null", parser.getPathAt(parser.toPosition(13, 26)));
    }
}
