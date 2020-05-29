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
package org.utplsql.sqldev.test.runner;

import java.sql.Connection;
import java.util.Collections;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.runner.UtplsqlRunner;
import org.utplsql.sqldev.test.AbstractJdbcTest;

@SuppressWarnings("all")
public class UtplsqlRunnerTest extends AbstractJdbcTest {
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
    _builder.append("   ");
    _builder.append("/* tags annotation without parameter will raise a warning */");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%tags");
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
    _builder.append("--%test(test 3 - disabled)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%disabled");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_3_disabled;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 4 - errored)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_4_errored;");
    _builder.newLine();
    _builder.append("   ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 5 - warnings)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_5_warnings;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%endcontext");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%afterall");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("procedure print_and_raise;");
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
    _builder_1.append("dbms_output.put_line(\'start test 1\');");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("dbms_session.sleep(1);");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("ut.expect(1).to_equal(1);");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("dbms_output.put_line(\'end test 1\');");
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
    _builder_1.append("dbms_output.put_line(\'start test 2\');");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("dbms_session.sleep(2);");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("ut.expect(1, \'first assert.\').to_equal(2);");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("ut.expect(1, \'second assert.\').to_equal(2);");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("dbms_output.put_line(\'end test 2\');");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_3_disabled IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("NULL;");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_4_errored IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("EXECUTE IMMEDIATE \'bla bla\';");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_5_warnings IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("COMMIT; -- will raise a warning");
    _builder_1.newLine();
    _builder_1.append("\t  ");
    _builder_1.append("ut.expect(1).to_equal(1);");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE print_and_raise IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("dbms_output.put_line(\'Now, a no_data_found exception is raised\');");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("dbms_output.put_line(\'dbms_output and error stack is reported for this suite.\');");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("dbms_output.put_line(\'A runtime error in afterall is counted as a warning.\');");
    _builder_1.newLine();
    _builder_1.append("      ");
    _builder_1.append("RAISE no_data_found;");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.append("END;");
    _builder_1.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_1.toString());
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
  }
  
  @Test
  public void runTestsWithMaxTime() {
    try {
      SingleConnectionDataSource ds1 = new SingleConnectionDataSource();
      ds1.setDriverClassName("oracle.jdbc.OracleDriver");
      ds1.setUrl(AbstractJdbcTest.dataSource.getUrl());
      ds1.setUsername(AbstractJdbcTest.dataSource.getUsername());
      ds1.setPassword(AbstractJdbcTest.dataSource.getPassword());
      SingleConnectionDataSource ds2 = new SingleConnectionDataSource();
      ds2.setDriverClassName("oracle.jdbc.OracleDriver");
      ds2.setUrl(AbstractJdbcTest.dataSource.getUrl());
      ds2.setUsername(AbstractJdbcTest.dataSource.getUsername());
      ds2.setPassword(AbstractJdbcTest.dataSource.getPassword());
      Connection _connection = ds1.getConnection();
      Connection _connection_1 = ds2.getConnection();
      UtplsqlRunner runner = new UtplsqlRunner(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(":a")), _connection, _connection_1);
      runner.runTestAsync();
      runner.getProducerThread().join(200000);
      runner.getConsumerThread().join(200000);
      Thread.sleep((4 * 1000));
      runner.dispose();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
