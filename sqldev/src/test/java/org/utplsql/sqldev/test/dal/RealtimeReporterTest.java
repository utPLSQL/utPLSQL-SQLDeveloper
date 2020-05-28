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
package org.utplsql.sqldev.test.dal;

import java.sql.Connection;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.utplsql.sqldev.dal.RealtimeReporterDao;
import org.utplsql.sqldev.model.runner.PostRunEvent;
import org.utplsql.sqldev.model.runner.PostSuiteEvent;
import org.utplsql.sqldev.model.runner.PostTestEvent;
import org.utplsql.sqldev.model.runner.PreRunEvent;
import org.utplsql.sqldev.model.runner.PreSuiteEvent;
import org.utplsql.sqldev.model.runner.PreTestEvent;
import org.utplsql.sqldev.model.runner.RealtimeReporterEvent;
import org.utplsql.sqldev.test.AbstractJdbcTest;
import org.utplsql.sqldev.test.dal.TestRealtimerReporterEventConsumer;

@SuppressWarnings("all")
public class RealtimeReporterTest extends AbstractJdbcTest {
  private static final Logger logger = Logger.getLogger(RealtimeReporterTest.class.getName());
  
  @BeforeClass
  public static void setup() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg is");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%suite(JUnit testing)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%suitepath(a)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%context(test context)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 1 - OK) ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_1_ok;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 2 - NOK)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_2_nok;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%endcontext");
    _builder.newLine();
    _builder.append("END;");
    _builder.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test1_pkg IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_1_ok IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("ut.expect(1).to_equal(1);");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_2_nok IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("ut.expect(1).to_equal(2);");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.append("END;");
    _builder_1.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_1.toString());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("CREATE OR REPLACE PACKAGE junit_utplsql_test2_pkg IS");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("--%suite");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("--%suitepath(b)");
    _builder_2.newLine();
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("--%test ");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("PROCEDURE test_3_ok;");
    _builder_2.newLine();
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("--%test");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("PROCEDURE test_4_nok;");
    _builder_2.newLine();
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("--%test");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("--%disabled");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("PROCEDURE test_5;");
    _builder_2.newLine();
    _builder_2.append("end;");
    _builder_2.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_2.toString());
    StringConcatenation _builder_3 = new StringConcatenation();
    _builder_3.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test2_pkg IS");
    _builder_3.newLine();
    _builder_3.append("   ");
    _builder_3.append("PROCEDURE test_3_ok IS");
    _builder_3.newLine();
    _builder_3.append("   ");
    _builder_3.append("BEGIN");
    _builder_3.newLine();
    _builder_3.append("      ");
    _builder_3.append("ut3.ut.expect(2).to_equal(2);");
    _builder_3.newLine();
    _builder_3.append("   ");
    _builder_3.append("END;");
    _builder_3.newLine();
    _builder_3.newLine();
    _builder_3.append("   ");
    _builder_3.append("PROCEDURE test_4_nok IS");
    _builder_3.newLine();
    _builder_3.append("   ");
    _builder_3.append("BEGIN");
    _builder_3.newLine();
    _builder_3.append("      ");
    _builder_3.append("ut3.ut.expect(2).to_equal(3);");
    _builder_3.newLine();
    _builder_3.append("      ");
    _builder_3.append("ut3.ut.expect(2).to_equal(4);");
    _builder_3.newLine();
    _builder_3.append("   ");
    _builder_3.append("END;");
    _builder_3.newLine();
    _builder_3.newLine();
    _builder_3.append("  ");
    _builder_3.append("PROCEDURE test_5 IS");
    _builder_3.newLine();
    _builder_3.append("  ");
    _builder_3.append("BEGIN");
    _builder_3.newLine();
    _builder_3.append("     ");
    _builder_3.append("null;");
    _builder_3.newLine();
    _builder_3.append("  ");
    _builder_3.append("END;");
    _builder_3.newLine();
    _builder_3.append("END;");
    _builder_3.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_3.toString());
    StringConcatenation _builder_4 = new StringConcatenation();
    _builder_4.append("CREATE OR REPLACE PACKAGE junit_utplsql_test3_pkg IS");
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("--%suite");
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("--%suitepath(b)");
    _builder_4.newLine();
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("--%test ");
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("PROCEDURE test_6_with_runtime_error;");
    _builder_4.newLine();
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("--%test");
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("PROCEDURE test_7_with_serveroutput;");
    _builder_4.newLine();
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("--%afterall");
    _builder_4.newLine();
    _builder_4.append("   ");
    _builder_4.append("PROCEDURE print_and_raise;");
    _builder_4.newLine();
    _builder_4.append("END;");
    _builder_4.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_4.toString());
    StringConcatenation _builder_5 = new StringConcatenation();
    _builder_5.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test3_pkg IS");
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("PROCEDURE test_6_with_runtime_error is");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("l_actual INTEGER;");
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("BEGIN");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("EXECUTE IMMEDIATE \'select 6 from non_existing_table\' INTO l_actual;");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("ut3.ut.expect(6).to_equal(l_actual);");
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("END;");
    _builder_5.newLine();
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("PROCEDURE test_7_with_serveroutput IS");
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("BEGIN");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("dbms_output.put_line(\'before test 7\');");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("ut3.ut.expect(7).to_equal(7);");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("dbms_output.put_line(\'after test 7\');");
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("END;");
    _builder_5.newLine();
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("PROCEDURE print_and_raise IS");
    _builder_5.newLine();
    _builder_5.append("   ");
    _builder_5.append("BEGIN");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("dbms_output.put_line(\'Now, a no_data_found exception is raised\');");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("dbms_output.put_line(\'dbms_output and error stack is reported for this suite.\');");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("dbms_output.put_line(\'A runtime error in afterall is counted as a warning.\');");
    _builder_5.newLine();
    _builder_5.append("      ");
    _builder_5.append("raise no_data_found;");
    _builder_5.newLine();
    _builder_5.append("    ");
    _builder_5.append("END;");
    _builder_5.newLine();
    _builder_5.append("END;");
    _builder_5.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_5.toString());
  }
  
  @AfterClass
  public static void teardown() {
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test1_pkg");
    } catch (final Throwable _t) {
      if (_t instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test2_pkg");
    } catch (final Throwable _t_1) {
      if (_t_1 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_1);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test3_pkg");
    } catch (final Throwable _t_2) {
      if (_t_2 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_2);
      }
    }
  }
  
  @Test
  public void produceAndConsume() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final RealtimeReporterDao dao = new RealtimeReporterDao(_connection);
      final String reporterId = UUID.randomUUID().toString().replace("-", "");
      final TestRealtimerReporterEventConsumer consumer = new TestRealtimerReporterEventConsumer();
      dao.produceReport(reporterId, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(":a", ":b")));
      dao.consumeReport(reporterId, consumer);
      RealtimeReporterTest.logger.fine(consumer.getConsumedList().toString());
      final Function1<RealtimeReporterEvent, Boolean> _function = (RealtimeReporterEvent it) -> {
        return Boolean.valueOf((it instanceof PreRunEvent));
      };
      Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<RealtimeReporterEvent>filter(consumer.getConsumedList(), _function)));
      final Function1<RealtimeReporterEvent, Boolean> _function_1 = (RealtimeReporterEvent it) -> {
        return Boolean.valueOf((it instanceof PostRunEvent));
      };
      Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<RealtimeReporterEvent>filter(consumer.getConsumedList(), _function_1)));
      final Function1<RealtimeReporterEvent, Boolean> _function_2 = (RealtimeReporterEvent it) -> {
        return Boolean.valueOf((it instanceof PreSuiteEvent));
      };
      Assert.assertEquals(6, IterableExtensions.size(IterableExtensions.<RealtimeReporterEvent>filter(consumer.getConsumedList(), _function_2)));
      final Function1<RealtimeReporterEvent, Boolean> _function_3 = (RealtimeReporterEvent it) -> {
        return Boolean.valueOf((it instanceof PostSuiteEvent));
      };
      Assert.assertEquals(6, IterableExtensions.size(IterableExtensions.<RealtimeReporterEvent>filter(consumer.getConsumedList(), _function_3)));
      final Function1<RealtimeReporterEvent, Boolean> _function_4 = (RealtimeReporterEvent it) -> {
        return Boolean.valueOf((it instanceof PreTestEvent));
      };
      Assert.assertEquals(7, IterableExtensions.size(IterableExtensions.<RealtimeReporterEvent>filter(consumer.getConsumedList(), _function_4)));
      final Function1<RealtimeReporterEvent, Boolean> _function_5 = (RealtimeReporterEvent it) -> {
        return Boolean.valueOf((it instanceof PostTestEvent));
      };
      Assert.assertEquals(7, IterableExtensions.size(IterableExtensions.<RealtimeReporterEvent>filter(consumer.getConsumedList(), _function_5)));
      Assert.assertEquals(28, consumer.getConsumedList().size());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
