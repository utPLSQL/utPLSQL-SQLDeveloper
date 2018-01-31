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

import java.io.StringReader
import java.util.ArrayList
import java.util.Properties
import oracle.dbtools.raptor.newscriptrunner.SQLCommand.StmtType
import oracle.dbtools.worksheet.scriptparser.sqlplus.SQLPlusScriptParser
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

abstract class AbstractJdbcTest {
	protected static var SingleConnectionDataSource dataSource
	protected static var JdbcTemplate jdbcTemplate
	protected static var SingleConnectionDataSource sysDataSource
	protected static var JdbcTemplate sysJdbcTemplate
	// static initializer not supported in Xtend, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=429141
	protected static val _staticInitializerForDataSourceAndJdbcTemplate = {
		val p = new Properties()
		p.load(AbstractJdbcTest.getClass().getResourceAsStream("/test.properties"))
		// create dataSource and jdbcTemplate
		dataSource = new SingleConnectionDataSource()
		dataSource.driverClassName = "oracle.jdbc.OracleDriver"
		dataSource.url = '''jdbc:oracle:thin:@«p.getProperty("host")»:«p.getProperty("port")»/«p.getProperty("service")»'''
		dataSource.username = p.getProperty("scott_username")
		dataSource.password = p.getProperty("scott_password")
		jdbcTemplate = new JdbcTemplate(dataSource)
		// create dbaDataSource and dbaJdbcTemplate
		sysDataSource = new SingleConnectionDataSource()
		sysDataSource.driverClassName = "oracle.jdbc.OracleDriver"
		sysDataSource.url = '''jdbc:oracle:thin:@«p.getProperty("host")»:«p.getProperty("port")»/«p.getProperty("service")»'''
		sysDataSource.username = p.getProperty("sys_username")
		sysDataSource.password = p.getProperty("sys_password")
		sysJdbcTemplate = new JdbcTemplate(AbstractJdbcTest.sysDataSource)
	}

	def static getStatements(String sqlplusScript) {
		var SQLPlusScriptParser p = new SQLPlusScriptParser(new StringReader(sqlplusScript))
		val stmts = new ArrayList<String>
		while (p.hasNext) {
			val stmt = p.next
			if ((stmt.executable || stmt.runnable) && stmt.stmtType != StmtType.G_C_COMMENT &&
				stmt.stmtType != StmtType.G_C_MULTILINECOMMENT && stmt.stmtType != StmtType.G_C_SQLPLUS) {
				stmts.add(stmt.sql)
			}
		}
		return stmts;
	}
}
