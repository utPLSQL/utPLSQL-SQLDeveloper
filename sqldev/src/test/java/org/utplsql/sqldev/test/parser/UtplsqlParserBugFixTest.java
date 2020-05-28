/**
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

import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.parser.UtplsqlParser;

@SuppressWarnings("all")
public class UtplsqlParserBugFixTest {
  @Test
  public void issue1MatchingExprInStringLiterals() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("create or replace package body test_expect_not_to_be_null");
    _builder.newLine();
    _builder.append("is");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("gc_object_name constant varchar2(30) := \'t_not_to_be_null_test\';");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("gc_nested_table_name constant varchar2(30) := \'tt_not_to_be_null_test\';");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("gc_varray_name constant varchar2(30) := \'tv_not_to_be_null_test\';");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure cleanup_expectations");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("is");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("begin");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("ut3.ut_expectation_processor.clear_expectations();");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("end;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure create_types");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("is");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("pragma autonomous_transaction;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("begin");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("execute immediate \'create type \'||gc_object_name||\' is object (dummy number)\';");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("execute immediate \' create type \'||gc_nested_table_name||\' is table of number\';");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("execute immediate \'");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("create type \'||gc_varray_name||\' is varray(1) of number\';");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("end;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure drop_types");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("is");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("pragma autonomous_transaction;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("begin");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("execute immediate \'drop type \'||gc_object_name;");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("execute immediate \' drop type \'||gc_nested_table_name;");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("execute immediate \'");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("drop type \'||gc_varray_name;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("end;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure blob_not_null");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("is");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("begin");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("--Act");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("execute immediate expectations_helpers.unary_expectation_block(\'not_to_be_null\', \'blob\', \'to_blob(\'\'abc\'\')\');");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("--Assert");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("ut.expect(anydata.convertCollection(ut3.ut_expectation_processor.get_failed_expectations())).to_be_empty();");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("end;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("--and so on...");
    _builder.newLine();
    _builder.newLine();
    _builder.append("end;");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    Assert.assertEquals("test_expect_not_to_be_null.cleanup_expectations", parser.getPathAt(parser.toPosition(7, 1)));
    Assert.assertEquals("test_expect_not_to_be_null.create_types", parser.getPathAt(parser.toPosition(13, 1)));
    Assert.assertEquals("test_expect_not_to_be_null.drop_types", parser.getPathAt(parser.toPosition(23, 1)));
    Assert.assertEquals("test_expect_not_to_be_null.blob_not_null", parser.getPathAt(parser.toPosition(33, 1)));
  }
  
  @Test
  public void issue7WrongPositionWithWindowsLineSeparator() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("create or replace package test_expect_not_to_be_null");
    _builder.newLine();
    _builder.append("is");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("--%suite(expectations - not_to_be_null)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("--%suitepath(utplsql.core.expectations.unary)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("--%aftereach");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure cleanup_expectations;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("--%beforeall");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure create_types;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("--%afterall");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure drop_types;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("--%test(Gives success for not null blob)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure blob_not_null;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("--%test(Gives success for blob with length 0)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("procedure blob_0_length;");
    _builder.newLine();
    _builder.append("    ");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("-- ...");
    _builder.newLine();
    _builder.append("end test_expect_not_to_be_null;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    Assert.assertEquals("test_expect_not_to_be_null.blob_not_null", parser.getPathAt(parser.toPosition(13, 26)));
  }
}
