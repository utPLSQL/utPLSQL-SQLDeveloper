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

import java.util.ArrayList
import java.util.HashMap
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.model.ut.Annotation
import org.utplsql.sqldev.test.AbstractJdbcTest
import org.junit.Ignore

class DalTest extends AbstractJdbcTest {
	
	@BeforeClass
	@AfterClass
	def static void setupAndTeardown() {
		sysJdbcTemplate.execute("CREATE OR REPLACE PUBLIC SYNONYM ut FOR ut3_latest_release.ut")
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP PACKAGE BODY junit_utplsql_test_pkg")
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
		Assert.assertEquals("UT3_LATEST_RELEASE", dao.utplsqlSchema)
	}
	
	@Test
	def void isUtAnnotationManagerInstalled() {
		val dao = new UtplsqlDao(dataSource.connection)
		Assert.assertTrue(dao.utAnnotationManagerInstalled)
	}
	
	def void containsUtplsqlTest(String utPlsqlVersion) {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = utPlsqlVersion
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
	def void containsUtplsqlTest304() {
		val dao = new UtplsqlDao(dataSource.connection)
		if (dao.normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
			containsUtplsqlTest("3.0.4")
		}
	}

	@Test
	def void containsUtplsqlTest313() {
		containsUtplsqlTest("3.1.3")
	}
	
	@Test
	@Ignore
	def void containsUtplsqlTest999() {
		containsUtplsqlTest("9.9.9")
	}

	def void annotations(String utPlsqlVersion) {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = utPlsqlVersion
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
		val actual = dao.annotations("scott", "junit_utplsql_test_pkg")
		val expected = new ArrayList<Annotation>
		val suite = new Annotation
		suite.objectOwner = "SCOTT"
		suite.objectName = "JUNIT_UTPLSQL_TEST_PKG"
		suite.name = 'suite'
		suite.subobjectName = suite.objectName
		expected.add(suite)
		val t1 = new Annotation
		t1.objectOwner = "SCOTT"
		t1.objectName = "JUNIT_UTPLSQL_TEST_PKG"
		t1.name = 'test'
		t1.subobjectName = 'T1'
		expected.add(t1)
		val t2 = new Annotation
		t2.objectOwner = "SCOTT"
		t2.objectName = "JUNIT_UTPLSQL_TEST_PKG"
		t2.name = 'test'
		t2.subobjectName = 'T2'
		expected.add(t2)
		Assert.assertEquals(expected.toString, actual.toString)
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}

	@Test
	def void annotations304() {
		val dao = new UtplsqlDao(dataSource.connection)
		if (dao.normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
			annotations("3.0.4")
		}
	}

	@Test
	def void annotations313() {
		annotations("3.1.3")
	}
	
	@Test
	def void annotations999() {
		annotations("9.9.9")
	}

	def void testablesPackages(String utPlsqlVersion) {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = utPlsqlVersion		
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
		val actual = dao.testables('PACKAGE')
		Assert.assertEquals(1, actual.size)
		Assert.assertEquals("PACKAGE.JUNIT_NO_TEST_PKG", actual.get(0).id)
	}

	@Test
	def void testablesPackages304() {
		val dao = new UtplsqlDao(dataSource.connection)
		if (dao.normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
			testablesPackages("3.0.4")
		}
	}

	@Test
	def void testablesPackages313() {
		testablesPackages("3.1.3")
	}

	@Test
	def void testablesPackages999() {
		testablesPackages("9.9.9")
	}

	def void testablesTypes(String utPlsqlVersion) {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = utPlsqlVersion		
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
		val actual = dao.testables('TYPE')
		Assert.assertEquals(1, actual.size)
		Assert.assertEquals("TYPE.JUNIT_TAB2_OT", actual.get(0).id)
	}

	@Test
	def void testablesTypes304() {
		val dao = new UtplsqlDao(dataSource.connection)
		if (dao.normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
			testablesTypes("3.0.4")
		}
	}

	@Test
	def void testablesTypes313() {
		testablesTypes("3.1.3")
	}

	@Test
	def void testablesTypes999() {
		testablesTypes("9.9.9")
	}

	def void testablesFunctions(String utPlsqlVersion) {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = utPlsqlVersion
		jdbcTemplate.execute('''
			CREATE OR REPLACE FUNCTION junit_f RETURN INTEGER IS 
			BEGIN
			   RETURN 1;
			END;
		''')
		val actual = dao.testables('FUNCTION')
		Assert.assertEquals(1, actual.size)
		Assert.assertEquals("FUNCTION.JUNIT_F", actual.get(0).id)
	}

	@Test
	def void testablesFunctions304() {
		val dao = new UtplsqlDao(dataSource.connection)
		if (dao.normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
			testablesFunctions("3.0.4")
		}
	}

	@Test
	def void testablesFunctions313() {
		testablesFunctions("3.1.3")
	}

	@Test
	def void testablesFunctions999() {
		testablesFunctions("9.9.9")
	}

	def void testablesProcedures(String utPlsqlVersion) {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = utPlsqlVersion
		jdbcTemplate.execute('''
			CREATE OR REPLACE PROCEDURE junit_p RETURN INTEGER IS 
			BEGIN
			   NULL;
			END;
		''')
		val actual = dao.testables('PROCEDURE')
		Assert.assertEquals(1, actual.size)
		Assert.assertEquals("PROCEDURE.JUNIT_P", actual.get(0).id)
	}

	@Test
	def void testablesProcedures304() {
		val dao = new UtplsqlDao(dataSource.connection)
		if (dao.normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
			testablesProcedures("3.0.4")
		}
	}

	@Test
	def void testablesProcedures313() {
		testablesProcedures("3.1.3")
	}

	@Test
	def void testablesProcedures999() {
		testablesProcedures("9.9.9")
	}

	def void runnables(String utPlsqlVersion) {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = utPlsqlVersion
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite
			   -- %suitepath(a.B.c)

			   -- %test
			   PROCEDURE T0;

			   -- %context(myContext)

			   -- %test(t1: test One)
			   PROCEDURE t1;

			   -- %test(t2: test Two)
			   PROCEDURE t2;

			   -- %endcontext

			   -- %test
			   PROCEDURE t3;
			END junit_utplsql_test_pkg;
		''')
		val actualNodes = dao.runnables()
		Assert.assertEquals(16, actualNodes.size)
		val actual = new HashMap<String, String>
		for (node : actualNodes) {
			actual.put(node.id, node.parentId)
		}
		Assert.assertEquals(null, actual.get("SUITE"))
		Assert.assertEquals("SUITE", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T0"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T1"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T2"))
		Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T3"))
		Assert.assertEquals(null, actual.get("SUITEPATH"))
		Assert.assertEquals("SUITEPATH", actual.get("SCOTT:a"))
		Assert.assertEquals("SCOTT:a", actual.get("SCOTT:a.b"))
		Assert.assertEquals("SCOTT:a.b", actual.get("SCOTT:a.b.c"))
		Assert.assertEquals("SCOTT:a.b.c", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg"))
		Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.myContext"))
		Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.t0"))
		Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.t3"))
		Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.myContext", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.myContext.t1"))
		Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.myContext", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.myContext.t2"))
	}

	@Test
	def void runnables304() {
		val dao = new UtplsqlDao(dataSource.connection)
		if (dao.normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
			runnables("3.0.4")
		}
	}

	@Test
	def void runnables313() {
		runnables("3.1.3")
	}
	
	@Test
	def void runnables999() {
		runnables("9.9.9")
	}

	@Test
	def void dbmsOutput() {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.enableDbmsOutput
		jdbcTemplate.execute('''
			BEGIN
			   sys.dbms_output.put_line('line1');
			   sys.dbms_output.put_line('line2');
			   sys.dbms_output.put_line(null);
			   sys.dbms_output.put_line('line4');
			   sys.dbms_output.put_line('line5');
			END;
		''')
		val actual = dao.getDbmsOutput(2)
		val expected = '''
			line1
			line2

			line4
			line5
		'''
		Assert.assertEquals(expected, actual)
	}
	
	@Test
	def void htmlCodeCoverage() {
		setupAndTeardown
		val dao = new UtplsqlDao(dataSource.connection)
		val actual = dao.htmlCodeCoverage(#["SCOTT"], #['scott'], #[], #[])
		Assert.assertTrue(actual.startsWith("<!DOCTYPE html>"))
		Assert.assertTrue(actual.trim.endsWith("</html>"))
	}
	
	@Test
	def void includes() {
		setupAndTeardown
		jdbcTemplate.execute('''
			CREATE OR REPLACE FUNCTION junit_f RETURN INTEGER IS
			BEGIN
			   RETURN 1;
			END junit_f;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			   -- %test
			   PROCEDURE f1;
			END junit_utplsql_test_pkg;
		''')		
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE BODY junit_utplsql_test_pkg IS
			   PROCEDURE f1 IS
			      l_expected INTEGER := 1;
			      l_actual   INTEGER;
			   BEGIN
			      l_actual := junit_f;
			      ut.expect(l_actual).to_equal(l_expected);
			   END f1;
			END junit_utplsql_test_pkg;
		''')
		val dao = new UtplsqlDao(dataSource.connection)
		val actualEmpty = dao.includes('TEST_F1')
		Assert.assertEquals(#[], actualEmpty)
		val actual = dao.includes('junit_utplsql_test_pkg')
		Assert.assertEquals(#['JUNIT_UTPLSQL_TEST_PKG','JUNIT_F','UT_EXPECTATION'].sort, actual.sort)
	}
	
	@Test
	def void normalizedPlsqlVersionOkRelease() {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = "v3.1.10.1234"
		val actual = dao.normalizedUtPlsqlVersion()
		Assert.assertEquals("3.1.10", actual)
	}

	@Test
	def void normalizedPlsqlVersionOkDevelop() {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = "v3.1.10.1234-develop"
		val actual = dao.normalizedUtPlsqlVersion()
		Assert.assertEquals("3.1.10", actual)
	}

	@Test
	def void normalizedPlsqlVersionNok() {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = "bla bla 1.2"
		val actual = dao.normalizedUtPlsqlVersion()
		Assert.assertEquals("0.0.0", actual)
	}
	
	@Test
	def void normaliedPlsqlVersionNumber() {
		val dao = new UtplsqlDao(dataSource.connection)
		dao.utPlsqlVersion = "3.14.37"
		val actual = dao.normalizedUtPlsqlVersionNumber()
		Assert.assertEquals(3014037, actual)		
	}
	
	@Test
	def void utPlsqlVersion() {
		val dao = new UtplsqlDao(dataSource.connection)
		val actual = dao.utPlsqlVersion
		val sql = "SELECT ut.version FROM DUAL"
		val expected = jdbcTemplate.queryForObject(sql, String)
		Assert.assertEquals(expected, actual)
		
	}
	
	@Test
	def void getSourceOfPackage() {
		val dao = new UtplsqlDao(dataSource.connection)
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			   -- %test
			   PROCEDURE p1;
			END junit_utplsql_test_pkg;
		''')
		val actual = dao.getSource("SCOTT", "PACKAGE", "JUNIT_UTPLSQL_TEST_PKG")
		Assert.assertTrue(actual.contains("-- %suite"))
		Assert.assertTrue(actual.contains("PROCEDURE p1;"))
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}

	@Test
	def void getSourceOfPackageBody() {
		val dao = new UtplsqlDao(dataSource.connection)
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE BODY junit_utplsql_test_pkg IS
			   PROCEDURE p1 IS
			      l_expected INTEGER := 1;
			      l_actual   INTEGER;
			   BEGIN
			      l_actual := junit_f;
			      ut.expect(l_actual).to_equal(l_expected);
			   END p1;
			END junit_utplsql_test_pkg;
		''');
		val actual = dao.getSource("SCOTT", "PACKAGE BODY", "JUNIT_UTPLSQL_TEST_PKG")
		Assert.assertTrue(actual.contains("PACKAGE BODY"))
		Assert.assertTrue(actual.contains("PROCEDURE p1 IS"))
		jdbcTemplate.execute("DROP PACKAGE BODY junit_utplsql_test_pkg")
	}
	
	@Test
	def void getObjectType() {
		val dao = new UtplsqlDao(dataSource.connection)
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			   -- %test
			   PROCEDURE p1;
			END junit_utplsql_test_pkg;
		''')
		val actual = dao.getObjectType("SCOTT", "JUNIT_UTPLSQL_TEST_PKG")
		Assert.assertEquals("PACKAGE", actual)
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}

}