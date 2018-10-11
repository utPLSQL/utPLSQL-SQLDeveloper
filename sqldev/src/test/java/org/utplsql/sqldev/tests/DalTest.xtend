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
import java.util.HashMap
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
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_no_test_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP TYPE junit_tab1_ot")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP TYPE junit_tab2_ot")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP FUNCTION junit_f")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP PROCEDURE junit_p")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
	}
	
	@Test 
	def void isDbaViewAccessible() {
		val dao = new UtplsqlDao(dataSource.connection)
		Assert.assertFalse(dao.dbaViewAccessible)
		val sysDao = new UtplsqlDao(sysDataSource.connection)
		Assert.assertTrue(sysDao.dbaViewAccessible)
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
	
	@Test
	def void testablesPackages() {
		val dao = new UtplsqlDao(dataSource.connection)
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
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_no_test_pkg IS
			   PROCEDURE p1;

			   PROCEDURE p2;
			END junit_no_test_pkg;
		''')
		val effective = dao.testables('PACKAGE')
		Assert.assertEquals(1, effective.size)
		Assert.assertEquals("PACKAGE.JUNIT_NO_TEST_PKG", effective.get(0).id)
	}

	@Test
	def void testablesTypes() {
		val dao = new UtplsqlDao(dataSource.connection)
		jdbcTemplate.execute('''
			CREATE OR REPLACE TYPE junit_tab1_ot IS object (a integer, b integer);
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE TYPE junit_tab2_ot IS object (
			   a integer, 
			   b integer, 
			   member procedure c(
			      self in out nocopy junit_tab2_ot, 
			      p integer
			   )
			);
		''')
		val effective = dao.testables('TYPE')
		Assert.assertEquals(1, effective.size)
		Assert.assertEquals("TYPE.JUNIT_TAB2_OT", effective.get(0).id)
	}

	@Test
	def void testablesFunctions() {
		val dao = new UtplsqlDao(dataSource.connection)
		jdbcTemplate.execute('''
			CREATE OR REPLACE FUNCTION junit_f RETURN INTEGER IS 
			BEGIN
			   RETURN 1;
			END;
		''')
		val effective = dao.testables('FUNCTION')
		Assert.assertEquals(1, effective.size)
		Assert.assertEquals("FUNCTION.JUNIT_F", effective.get(0).id)
	}

	@Test
	def void testablesProcedures() {
		val dao = new UtplsqlDao(dataSource.connection)
		jdbcTemplate.execute('''
			CREATE OR REPLACE PROCEDURE junit_p RETURN INTEGER IS 
			BEGIN
			   NULL;
			END;
		''')
		val effective = dao.testables('PROCEDURE')
		Assert.assertEquals(1, effective.size)
		Assert.assertEquals("PROCEDURE.JUNIT_P", effective.get(0).id)
	}

	@Test
	def void runnables() {
		val dao = new UtplsqlDao(dataSource.connection)
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite
			   -- %suitepath(a.b.c)

			   -- %test
			   PROCEDURE t0;

			   -- %context(mycontext)

			   -- %test
			   PROCEDURE t1;

			   -- %test
			   PROCEDURE t2;

			   -- %endcontext

			   -- %test
			   PROCEDURE t3;
			END junit_utplsql_test_pkg;
		''')
		val effectiveNodes = dao.runnables()		
		Assert.assertEquals(16, effectiveNodes.size)
		val effective = new HashMap<String, String>
		for (node : effectiveNodes) {
			effective.put(node.id, node.parentId)
		}
		Assert.assertEquals(null, effective.get("SUITE"))
		Assert.assertEquals("SUITE", effective.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", effective.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.t0"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", effective.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.t1"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", effective.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.t2"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", effective.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.t3"))
		Assert.assertEquals(null, effective.get("SUITEPATH"))
		Assert.assertEquals("SUITEPATH", effective.get("SCOTT:a"))
		Assert.assertEquals("SCOTT:a", effective.get("SCOTT:a.b"))
		Assert.assertEquals("SCOTT:a.b", effective.get("SCOTT:a.b.c"))
		Assert.assertEquals("SCOTT:a.b.c", effective.get("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG"))
		Assert.assertEquals("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG", effective.get("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG.mycontext"))
		Assert.assertEquals("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG", effective.get("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG.t0"))
		Assert.assertEquals("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG", effective.get("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG.t3"))
		Assert.assertEquals("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG.mycontext", effective.get("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG.mycontext.t1"))
		Assert.assertEquals("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG.mycontext", effective.get("SCOTT:a.b.c.JUNIT_UTPLSQL_TEST_PKG.mycontext.t2"))
	}

}