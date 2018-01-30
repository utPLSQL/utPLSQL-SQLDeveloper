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
 package org.utplsql.sqldev.tests

import java.util.ArrayList
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.model.ut.Annotation

class DalTest extends AbstractJdbcTest {
	
	@BeforeClass
	@AfterClass
	def static void setupAndTeardown() {
		sysJdbcTemplate.execute("CREATE OR REPLACE PUBLIC SYNONYM ut FOR ut3.ut")
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
	} 
	
	@Test
	def void utplsqlSchema() {
		sysJdbcTemplate.execute("DROP PUBLIC SYNONYM ut")
		val dao = new UtplsqlDao(dataSource.connection)
		Assert.assertEquals(null, dao.utplsqlSchema)
		setupAndTeardown
		Assert.assertEquals("UT3", dao.utplsqlSchema)
	}
	
	@Test
	def void isUtAnnotationManagerInstalled() {
		val dao = new UtplsqlDao(dataSource.connection)
		Assert.assertTrue(dao.utAnnotationManagerInstalled)
	}
	
	@Test
	def void containsUtplsqlTest() {
		val dao = new UtplsqlDao(dataSource.connection)
		Assert.assertFalse(dao.containsUtplsqlTest("scott"))
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			   -- %test
			   PROCEDURE t1;

			   -- %Test
			   PROCEDURE t2;

			   PROCEDURE t3;
			END junit_utplsql_test_pkg;
		''')
		Assert.assertTrue(dao.containsUtplsqlTest("scott"))
		Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"))
		Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t1"))
		Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t2"))
		Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t3"))
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %test
			   PROCEDURE t1;

			   -- %Test
			   PROCEDURE t2;

			   PROCEDURE t3;
			END junit_utplsql_test_pkg;
		''')
		Assert.assertFalse(dao.containsUtplsqlTest("scott"))
		Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"))
		Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t1"))
		Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t2"))
		Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t3"))
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}
	
	@Test
	def void annotations() {
		val dao = new UtplsqlDao(dataSource.connection)
		Assert.assertEquals(new ArrayList<Annotation>, dao.annotations("scott", "junit_utplsql_test_pkg"))
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			   -- %test
			   PROCEDURE t1;

			   -- %Test
			   PROCEDURE t2;

			   PROCEDURE t3;
			END junit_utplsql_test_pkg;
		''')
		val effective = dao.annotations("scott", "junit_utplsql_test_pkg")
		val expected = new ArrayList<Annotation>
		val suite = new Annotation
		suite.objectOwner = "SCOTT"
		suite.objectType = "PACKAGE"
		suite.objectName = "JUNIT_UTPLSQL_TEST_PKG"
		suite.name = 'suite'
		expected.add(suite)
		val t1 = new Annotation
		t1.objectOwner = "SCOTT"
		t1.objectType = "PACKAGE"
		t1.objectName = "JUNIT_UTPLSQL_TEST_PKG"
		t1.name = 'test'
		t1.subobjectName = 't1'
		expected.add(t1)
		val t2 = new Annotation
		t2.objectOwner = "SCOTT"
		t2.objectType = "PACKAGE"
		t2.objectName = "JUNIT_UTPLSQL_TEST_PKG"
		t2.name = 'test'
		t2.subobjectName = 't2'
		expected.add(t2)
		Assert.assertEquals(expected.toString, effective.toString)
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}
}