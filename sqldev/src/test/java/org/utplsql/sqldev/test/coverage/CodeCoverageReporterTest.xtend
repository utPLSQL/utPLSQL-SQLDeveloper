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
package org.utplsql.sqldev.test.coverage

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.utplsql.sqldev.coverage.CodeCoverageReporter
import org.utplsql.sqldev.test.AbstractJdbcTest

class CodeCoverageReporterTest extends AbstractJdbcTest{

	@BeforeClass
	def static void setup() {
		jdbcTemplate.execute('''
			CREATE OR REPLACE FUNCTION f RETURN INTEGER IS
			BEGIN
			   RETURN 1;
			END f;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE test_f IS
			   --%suite

			   --%test
			   PROCEDURE f;
			END test_f;
		''')
		jdbcTemplate.execute('''
			CREATE OR REPLACE PACKAGE BODY test_f IS
			   --%test
			   PROCEDURE f IS
			      l_expected INTEGER := 1;
			      l_actual   INTEGER;
			   BEGIN
			      l_actual := scott.f();
			      ut.expect(l_actual).to_equal(l_expected);
			   END f;
			END test_f;
		''')
	}
	
	private def Path getNewestOutputFile() {
		val file = File.createTempFile("test", ".txt")
		val dir = file.parentFile
		file.delete
		val last = Files.list(dir.toPath)
			.filter([f | !f.toFile.directory])
			.filter([f | f.fileName.toString.startsWith("utplsql_")])
			.filter([f | f.fileName.toString.endsWith(".html")])
			.max(Comparator.comparingLong([f|f.toFile().lastModified()]))
		return last.get
	}

	@Test
	def void produceReportAndCloseConnection() {
		// create temporary dataSource, closed by reporter
		var ds = new SingleConnectionDataSource()
		ds.driverClassName = "oracle.jdbc.OracleDriver"
		ds.url = dataSource.url
		ds.username = dataSource.username
		ds.password = dataSource.password
		val conn = ds.connection
		val pathList=#[':test_f']
		val includeObjectList = #['f']
		val reporter = new CodeCoverageReporter(pathList, includeObjectList, conn)
		val run = reporter.runAsync
		run.join(20000)
		Assert.assertEquals(true, conn.isClosed)
		val outputFile = getNewestOutputFile
		Assert.assertTrue(outputFile !== null)
		val content = new String(Files.readAllBytes(outputFile), StandardCharsets.UTF_8)
		Assert.assertTrue(content.contains('<h3>SCOTT.F</h3><h4><span class="green">100 %</span> lines covered</h4>'))
	}

	@AfterClass
	def static void teardown() {
		try {
			jdbcTemplate.execute("DROP PACKAGE test_f")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
		try {
			jdbcTemplate.execute("DROP FUNCTION f")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
	}
	

	
}