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
 package org.utplsql.sqldev.test.dal

import java.util.UUID
import java.util.logging.Logger
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.utplsql.sqldev.dal.RealtimeReporterDao
import org.utplsql.sqldev.test.AbstractJdbcTest

class RealtimeReporterFetchSizeTest extends AbstractJdbcTest {
	
	static val Logger logger = Logger.getLogger(RealtimeReporterFetchSizeTest.name);
	
	@BeforeClass
	def static void setup() {
		
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_fetch_size_pkg is
			   --%suite(JUnit testing)
			
			   --%test(test 1 - 0 seconds) 
			   PROCEDURE test_1_0;

			   --%test(test 2 - 1 seconds) 
			   PROCEDURE test_2_1;

			   --%test(test 3 - 2 seconds) 
			   PROCEDURE test_3_2;

			   --%test(test 4 - 0 seconds) 
			   PROCEDURE test_4_0;

			   --%test(test 5 - 0 seconds) 
			   PROCEDURE test_5_0;
			END;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE BODY junit_utplsql_fetch_size_pkg is
			   PROCEDURE test_1_0 IS
			   BEGIN
			     NULL;
			   END;

			   PROCEDURE test_2_1 IS
			   BEGIN
			     dbms_session.sleep(1);
			   END;

			   PROCEDURE test_3_2 IS
			   BEGIN
			     dbms_session.sleep(2);
			   END;

			   PROCEDURE test_4_0 IS
			   BEGIN
			     NULL;
			   END;

			   PROCEDURE test_5_0 IS
			   BEGIN
			     NULL;
			   END;
			END;
		''')
	}

	@AfterClass
	def static void teardown() {
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_utplsql_fetch_size_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
	}
	
	private def delayFreeStreamingConsumtionProducer(String reporterId) {
		var ds = new SingleConnectionDataSource()
		ds.driverClassName = "oracle.jdbc.OracleDriver"
		ds.url = dataSource.url
		ds.username = dataSource.username
		ds.password = dataSource.password
		val dao = new RealtimeReporterDao(ds.connection)
		dao.produceReport(reporterId, #["junit_utplsql_fetch_size_pkg"])
	}

	@Test 
	def void delayFreeStreamingConsumtion() {
		val long TOLERANCE_MS = 600
		var ds = new SingleConnectionDataSource()
		ds.driverClassName = "oracle.jdbc.OracleDriver"
		ds.url = dataSource.url
		ds.username = dataSource.username
		ds.password = dataSource.password
		val consumer = new TestRealtimerReporterEventTimedConsumer
		val reporterId = UUID.randomUUID().toString.replace("-", "");
		val dao = new RealtimeReporterDao(ds.connection)
		val Runnable runnable = [|delayFreeStreamingConsumtionProducer(reporterId)]
		val thread = new Thread(runnable)
		thread.name = "utPLSQL run test"
		thread.start
		dao.consumeReport(reporterId, consumer)
		logger.fine(consumer.postTestEvents.toString)
		Assert.assertEquals(5, consumer.postTestEvents.entrySet.size)
		val test_1_0 = consumer.postTestEvents.get("junit_utplsql_fetch_size_pkg.test_1_0")
		val test_2_1 = consumer.postTestEvents.get("junit_utplsql_fetch_size_pkg.test_2_1")
		val test_3_2 = consumer.postTestEvents.get("junit_utplsql_fetch_size_pkg.test_3_2")
		val test_4_0 = consumer.postTestEvents.get("junit_utplsql_fetch_size_pkg.test_4_0")
		val test_5_0 = consumer.postTestEvents.get("junit_utplsql_fetch_size_pkg.test_5_0")
		val test_2_1_time = test_2_1 - test_1_0
		logger.fine("test_2_1 time [ms]: " + test_2_1_time)
		Assert.assertTrue("test_2_1 runtime was too long", test_2_1_time < 1000 + TOLERANCE_MS)
		Assert.assertTrue("test_2_1 runtime was too short", test_2_1_time > 1000 - TOLERANCE_MS)
		val test_3_2_time = test_3_2 - test_2_1
		logger.fine("test_3_2 time [ms]: " + test_3_2_time)
		Assert.assertTrue("test_3_2 runtime was too long", test_3_2_time < 2000 + TOLERANCE_MS)
		Assert.assertTrue("test_3_2 runtime was too short", test_3_2_time > 2000 - TOLERANCE_MS)
		val test_4_0_time = test_4_0 - test_3_2
		logger.fine("test_4_0 time [ms]: " + test_4_0_time)
		Assert.assertTrue("test_4_0 runtime was too long", test_4_0_time < TOLERANCE_MS)
		val test_5_0_time = test_5_0 - test_4_0
		logger.fine("test_5_0 time [ms]: " + test_5_0_time)
		Assert.assertTrue("test_5_0 runtime was too long", test_5_0_time < TOLERANCE_MS)
	}
	
}