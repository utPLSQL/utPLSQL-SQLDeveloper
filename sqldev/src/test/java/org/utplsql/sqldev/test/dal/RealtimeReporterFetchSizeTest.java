/**
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
package org.utplsql.sqldev.test.dal;

import java.sql.Connection;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.dal.RealtimeReporterDao;
import org.utplsql.sqldev.test.AbstractJdbcTest;
import org.utplsql.sqldev.test.dal.TestRealtimerReporterEventTimedConsumer;

@SuppressWarnings("all")
public class RealtimeReporterFetchSizeTest extends AbstractJdbcTest {
  private static final Logger logger = Logger.getLogger(RealtimeReporterFetchSizeTest.class.getName());
  
  @BeforeClass
  public static void setup() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_fetch_size_pkg is");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%suite(JUnit testing)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 1 - 0 seconds) ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_1_0;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 2 - 1 seconds) ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_2_1;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 3 - 2 seconds) ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_3_2;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 4 - 0 seconds) ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_4_0;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 5 - 0 seconds) ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_5_0;");
    _builder.newLine();
    _builder.append("END;");
    _builder.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_fetch_size_pkg is");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_1_0 IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("     ");
    _builder_1.append("NULL;");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_2_1 IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("     ");
    _builder_1.append("dbms_session.sleep(1);");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_3_2 IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("     ");
    _builder_1.append("dbms_session.sleep(2);");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_4_0 IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("     ");
    _builder_1.append("NULL;");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("END;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE test_5_0 IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("BEGIN");
    _builder_1.newLine();
    _builder_1.append("     ");
    _builder_1.append("NULL;");
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
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_fetch_size_pkg");
    } catch (final Throwable _t) {
      if (_t instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  private void delayFreeStreamingConsumtionProducer(final String reporterId) {
    try {
      SingleConnectionDataSource ds = new SingleConnectionDataSource();
      ds.setDriverClassName("oracle.jdbc.OracleDriver");
      ds.setUrl(AbstractJdbcTest.dataSource.getUrl());
      ds.setUsername(AbstractJdbcTest.dataSource.getUsername());
      ds.setPassword(AbstractJdbcTest.dataSource.getPassword());
      Connection _connection = ds.getConnection();
      final RealtimeReporterDao dao = new RealtimeReporterDao(_connection);
      dao.produceReport(reporterId, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("junit_utplsql_fetch_size_pkg")));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void delayFreeStreamingConsumtion() {
    try {
      final long TOLERANCE_MS = 600;
      SingleConnectionDataSource ds = new SingleConnectionDataSource();
      ds.setDriverClassName("oracle.jdbc.OracleDriver");
      ds.setUrl(AbstractJdbcTest.dataSource.getUrl());
      ds.setUsername(AbstractJdbcTest.dataSource.getUsername());
      ds.setPassword(AbstractJdbcTest.dataSource.getPassword());
      final TestRealtimerReporterEventTimedConsumer consumer = new TestRealtimerReporterEventTimedConsumer();
      final String reporterId = UUID.randomUUID().toString().replace("-", "");
      Connection _connection = ds.getConnection();
      final RealtimeReporterDao dao = new RealtimeReporterDao(_connection);
      final Runnable _function = () -> {
        this.delayFreeStreamingConsumtionProducer(reporterId);
      };
      final Runnable runnable = _function;
      final Thread thread = new Thread(runnable);
      thread.setName("utPLSQL run test");
      thread.start();
      dao.consumeReport(reporterId, consumer);
      RealtimeReporterFetchSizeTest.logger.fine(consumer.getPostTestEvents().toString());
      Assert.assertEquals(5, consumer.getPostTestEvents().entrySet().size());
      final Long test_1_0 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_1_0");
      final Long test_2_1 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_2_1");
      final Long test_3_2 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_3_2");
      final Long test_4_0 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_4_0");
      final Long test_5_0 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_5_0");
      final long test_2_1_time = ((test_2_1).longValue() - (test_1_0).longValue());
      RealtimeReporterFetchSizeTest.logger.fine(("test_2_1 time [ms]: " + Long.valueOf(test_2_1_time)));
      Assert.assertTrue("test_2_1 runtime was too long", (test_2_1_time < (1000 + TOLERANCE_MS)));
      Assert.assertTrue("test_2_1 runtime was too short", (test_2_1_time > (1000 - TOLERANCE_MS)));
      final long test_3_2_time = ((test_3_2).longValue() - (test_2_1).longValue());
      RealtimeReporterFetchSizeTest.logger.fine(("test_3_2 time [ms]: " + Long.valueOf(test_3_2_time)));
      Assert.assertTrue("test_3_2 runtime was too long", (test_3_2_time < (2000 + TOLERANCE_MS)));
      Assert.assertTrue("test_3_2 runtime was too short", (test_3_2_time > (2000 - TOLERANCE_MS)));
      final long test_4_0_time = ((test_4_0).longValue() - (test_3_2).longValue());
      RealtimeReporterFetchSizeTest.logger.fine(("test_4_0 time [ms]: " + Long.valueOf(test_4_0_time)));
      Assert.assertTrue("test_4_0 runtime was too long", (test_4_0_time < TOLERANCE_MS));
      final long test_5_0_time = ((test_5_0).longValue() - (test_4_0).longValue());
      RealtimeReporterFetchSizeTest.logger.fine(("test_5_0 time [ms]: " + Long.valueOf(test_5_0_time)));
      Assert.assertTrue("test_5_0 runtime was too long", (test_5_0_time < TOLERANCE_MS));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
