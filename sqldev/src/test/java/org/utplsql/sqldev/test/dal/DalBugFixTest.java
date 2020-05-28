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

import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.test.AbstractJdbcTest

class DalBugFixTest extends AbstractJdbcTest {
	
	@BeforeClass
	@AfterClass
	def static void setupAndTeardown() {
		try {
			jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
	}

	@Test
	// https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/54
	def void issue54FolderIconForSuitesWithoutTests() {
		setupAndTeardown
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			END junit_utplsql_test_pkg;
		''')
		val dao = new UtplsqlDao(dataSource.connection)
		val actualNodes = dao.runnables()		
		Assert.assertEquals(4, actualNodes.size)
		val pkg = actualNodes.findFirst[it.id == "SCOTT:junit_utplsql_test_pkg"]
		Assert.assertEquals("FOLDER_ICON", pkg.iconName)
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}

	@Test
	// https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/54
	def void issue54PackageIconForSuitesWithTests() {
		setupAndTeardown
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			   -- %test
			   PROCEDURE t1;

			END junit_utplsql_test_pkg;
		''')
		val dao = new UtplsqlDao(dataSource.connection)
		val actualNodes = dao.runnables()		
		Assert.assertEquals(6, actualNodes.size)
		val pkg = actualNodes.findFirst[it.id == "SCOTT:junit_utplsql_test_pkg"]
		Assert.assertEquals("PACKAGE_ICON", pkg.iconName)
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}

	@Test
	// https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/55
	def void issue55SuiteWithoutTests() {
		setupAndTeardown
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			END junit_utplsql_test_pkg;
		''')
		val dao = new UtplsqlDao(dataSource.connection)
		val actualNodes = dao.runnables()		
		Assert.assertEquals(4, actualNodes.size)
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}
	
	@Test
	// https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/56
	def void issue56SuiteWithoutTests() {
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS
			   -- %suite

			END junit_utplsql_test_pkg;
		''')
		val dao = new UtplsqlDao(dataSource.connection)
		Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"))			
		jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg")
	}

}