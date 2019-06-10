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
 package org.utplsql.sqldev.test.runner

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.utplsql.sqldev.runner.UtplsqlRunner
import org.utplsql.sqldev.test.AbstractJdbcTest

class UtplsqlRunnerTest extends AbstractJdbcTest {
	
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
			      dbms_session.sleep(1);
			      ut.expect(1).to_equal(1);
			   END;
			
			   PROCEDURE test_2_nok IS
			   BEGIN
			      dbms_session.sleep(2);
			      ut.expect(1).to_equal(2);
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
	}
	
	@Test
	def void runTestsWithMaxTime() {
		var ds1 = new SingleConnectionDataSource()
		ds1.driverClassName = "oracle.jdbc.OracleDriver"
		ds1.url = dataSource.url
		ds1.username = dataSource.username
		ds1.password = dataSource.password
		var ds2 = new SingleConnectionDataSource()
		ds2.driverClassName = "oracle.jdbc.OracleDriver"
		ds2.url = dataSource.url
		ds2.username = dataSource.username
		ds2.password = dataSource.password
		var runner = new UtplsqlRunner(#[":a"], ds1.connection, ds2.connection)
		runner.runTestAsync
		runner.producerThread.join(200000)
		runner.consumerThread.join(200000)
		Thread.sleep(4 * 1000)
		runner.dispose
	}
	
}