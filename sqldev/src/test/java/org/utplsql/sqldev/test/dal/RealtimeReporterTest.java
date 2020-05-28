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
 package org.utplsql.sqldev.test.dal

import java.util.UUID
import java.util.logging.Logger
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.utplsql.sqldev.dal.RealtimeReporterDao
import org.utplsql.sqldev.model.runner.PostRunEvent
import org.utplsql.sqldev.model.runner.PostSuiteEvent
import org.utplsql.sqldev.model.runner.PostTestEvent
import org.utplsql.sqldev.model.runner.PreRunEvent
import org.utplsql.sqldev.model.runner.PreSuiteEvent
import org.utplsql.sqldev.model.runner.PreTestEvent
import org.utplsql.sqldev.test.AbstractJdbcTest

class RealtimeReporterTest extends AbstractJdbcTest {
	
	static val Logger logger = Logger.getLogger(RealtimeReporterTest.name);
	
	@BeforeClass
	def static void setup() {
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg is
			   --%suite(JUnit testing)
			   --%suitepath(a)
			
			   --%context(test context)
			
			   --%test(test 1 - OK) 
			   PROCEDURE test_1_ok;

			   --%test(test 2 - NOK)
			   PROCEDURE test_2_nok;
			
			   --%endcontext
			END;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE BODY junit_utplsql_test1_pkg IS
			   PROCEDURE test_1_ok IS
			   BEGIN
			      ut.expect(1).to_equal(1);
			   END;
			
			   PROCEDURE test_2_nok IS
			   BEGIN
			      ut.expect(1).to_equal(2);
			   END;
			END;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test2_pkg IS
			   --%suite
			   --%suitepath(b)
			
			   --%test 
			   PROCEDURE test_3_ok;

			   --%test
			   PROCEDURE test_4_nok;
			
			   --%test
			   --%disabled
			   PROCEDURE test_5;
			end;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE BODY junit_utplsql_test2_pkg IS
			   PROCEDURE test_3_ok IS
			   BEGIN
			      ut3.ut.expect(2).to_equal(2);
			   END;
			
			   PROCEDURE test_4_nok IS
			   BEGIN
			      ut3.ut.expect(2).to_equal(3);
			      ut3.ut.expect(2).to_equal(4);
			   END;

			  PROCEDURE test_5 IS
			  BEGIN
			     null;
			  END;
			END;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test3_pkg IS
			   --%suite
			   --%suitepath(b)
			
			   --%test 
			   PROCEDURE test_6_with_runtime_error;
			
			   --%test
			   PROCEDURE test_7_with_serveroutput;
			
			   --%afterall
			   PROCEDURE print_and_raise;
			END;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE BODY junit_utplsql_test3_pkg IS
			   PROCEDURE test_6_with_runtime_error is
			      l_actual INTEGER;
			   BEGIN
			      EXECUTE IMMEDIATE 'select 6 from non_existing_table' INTO l_actual;
			      ut3.ut.expect(6).to_equal(l_actual);
			   END;
			
			   PROCEDURE test_7_with_serveroutput IS
			   BEGIN
			      dbms_output.put_line('before test 7');
			      ut3.ut.expect(7).to_equal(7);
			      dbms_output.put_line('after test 7');
			   END;
			
			   PROCEDURE print_and_raise IS
			   BEGIN
			      dbms_output.put_line('Now, a no_data_found exception is raised');
			      dbms_output.put_line('dbms_output and error stack is reported for this suite.');
			      dbms_output.put_line('A runtime error in afterall is counted as a warning.');
			      raise no_data_found;
			    END;
			END;
		''')
	}

	@AfterClass
	def static void teardown() {
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test1_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test2_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test3_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
	}
	
	@Test 
	def void produceAndConsume() {
		val dao = new RealtimeReporterDao(dataSource.connection)
		val reporterId = UUID.randomUUID().toString.replace("-", "");
		val consumer = new TestRealtimerReporterEventConsumer
		dao.produceReport(reporterId, #[":a", ":b"])
		dao.consumeReport(reporterId, consumer)
		logger.fine(consumer.consumedList.toString)
		Assert.assertEquals(1, consumer.consumedList.filter[it instanceof PreRunEvent].size)
		Assert.assertEquals(1, consumer.consumedList.filter[it instanceof PostRunEvent].size)
		// 2 suitepaths (a, b), 1 context, 3 packages -> 6 suites
		Assert.assertEquals(6, consumer.consumedList.filter[it instanceof PreSuiteEvent].size)
		Assert.assertEquals(6, consumer.consumedList.filter[it instanceof PostSuiteEvent].size)
		Assert.assertEquals(7, consumer.consumedList.filter[it instanceof PreTestEvent].size)
		Assert.assertEquals(7, consumer.consumedList.filter[it instanceof PostTestEvent].size)
		Assert.assertEquals(28, consumer.consumedList.size)
	}
	
}