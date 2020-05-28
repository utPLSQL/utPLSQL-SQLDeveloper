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
package org.utplsql.sqldev.test.parser

import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.jdbc.BadSqlGrammarException
import org.utplsql.sqldev.parser.UtplsqlParser
import org.utplsql.sqldev.test.AbstractJdbcTest

class UtplsqlParserTest extends AbstractJdbcTest {
	
	static val sqlScript = '''
		PROMPT
		PROMPT Install utPLSQL test package
		PROMPT
		
		/*
		 * some comment
		 */
		CREATE OR REPLACE PACKAGE pkg IS
		   -- %suite
		   -- %rollback(manual)

		   -- %test
		   PROCEDURE p (in_p1 INTEGER);
		   FUNCTION f (in_p1 INTEGER) RETURN INTEGER;
		END pkg;
		/
		SHOW ERRORS
		
		CREATE OR REPLACE PACKAGE BODY "SCOTT"."PKG" IS
		   PROCEDURE "P" (in_p1 INTEGER) IS
		   BEGIN
		      NULL;
		   END p;
		
		   /* comment 1 */
		   -- comment 2
		   /* comment 3 */
		   -- comment 4
		
		   FUNCTION "F" (in_p1 INTEGER) RETURN INTEGER IS
		   BEGIN
		      RETURN 1;
		   END f;
		END pkg;
		/
		SHOW ERRORS
	'''

	@BeforeClass
	@AfterClass
	def static void setupAndTeardown() {
		try {
			jdbcTemplate.execute("DROP PACKAGE pkg")
		} catch (BadSqlGrammarException e) {
			// ignore
		}
	} 

	@Test
	def packageWithoutConnection() {
		val parser = new UtplsqlParser(sqlScript)
		val objects = parser.getObjects
		Assert.assertEquals(2, objects.size)
		Assert.assertEquals("pkg", objects.get(0).name)
		Assert.assertEquals('''"SCOTT"."PKG"'''.toString, objects.get(1).name)
		Assert.assertTrue(objects.get(0).position < objects.get(1).position)
		val units = parser.getUnits
		Assert.assertEquals(2, units.size)
		Assert.assertEquals("p", units.get(0).name)
		Assert.assertEquals('''"P"'''.toString, units.get(1).name)
		Assert.assertTrue(units.get(0).position < units.get(1).position)
		Assert.assertEquals("", parser.getPathAt(0))
		Assert.assertEquals("", parser.getPathAt(parser.toPosition(3,6)))
		Assert.assertEquals("pkg", parser.getPathAt(parser.toPosition(4,1)))
		Assert.assertEquals("pkg.p", parser.getPathAt(parser.toPosition(10,33)))
		Assert.assertEquals("pkg.p", parser.getPathAt(parser.toPosition(13,1)))
		Assert.assertEquals("SCOTT.PKG.p", parser.getPathAt(parser.toPosition(19,1)))
		Assert.assertEquals("SCOTT.PKG.P", parser.getPathAt(parser.toPosition(22,9)))
		Assert.assertEquals("SCOTT.PKG.P", parser.getPathAt(parser.toPosition(22,10)))
		Assert.assertEquals("SCOTT.PKG.P", parser.getPathAt(parser.toPosition(29,1)))
	}

	@Test
	def packageWithConnection() {
		val plsql = '''
			CREATE OR REPLACE PACKAGE pkg IS
			   -- %suite
			   -- %rollback(manual)

			   -- %test
			   PROCEDURE p (in_p1 INTEGER);
			   FUNCTION f (in_p1 INTEGER) RETURN INTEGER;
			END pkg;
		'''
		var parser = new UtplsqlParser(plsql, dataSource.connection, null)
		Assert.assertEquals(0, parser.getObjects.size)
		Assert.assertEquals(0, parser.getUnits.size)
		jdbcTemplate.execute(plsql)
		parser = new UtplsqlParser(plsql, dataSource.connection, null)
		Assert.assertEquals(1, parser.getObjects.size)
		Assert.assertEquals(1, parser.getUnits.size)
		for (stmt : getStatements(sqlScript)) {
			jdbcTemplate.execute(stmt)
		}
		parser = new UtplsqlParser(sqlScript, dataSource.connection, null)
		Assert.assertEquals(2, parser.getObjects.size)
		Assert.assertEquals(2, parser.getUnits.size)
		Assert.assertEquals("pkg.p", parser.getPathAt(parser.toPosition(13,1)))
		Assert.assertEquals("SCOTT.PKG.p", parser.getPathAt(parser.toPosition(19,1)))
		setupAndTeardown
	}

	@Test
	def procedure() {
		val plsql = '''
			create or replace procedure z
			is
			    null;
			end;
			/
		'''
		val parser = new UtplsqlParser(plsql)
		Assert.assertEquals("z", parser.getObjectAt(0).name)
		Assert.assertEquals("PROCEDURE", parser.getObjectAt(0).type)
	}

	@Test
	def function() {
		val plsql = '''
			create or replace procedure z
			is
			    null;
			end;
			/
			
			create or replace function f return number is
			begin
			   null;
			end;
			/
		'''
		val parser = new UtplsqlParser(plsql)
		Assert.assertEquals("f", parser.getObjectAt(parser.toPosition(8,1)).name)
		Assert.assertEquals("FUNCTION", parser.getObjectAt(parser.toPosition(8,1)).type)
	}

	@Test
	def type() {
		val plsql = '''
			create or replace type t force is
			    object (
			      a number,
			      b number,
			      c varchar2(10),
			      member procedure p(self in t)
			    )
			end;
			/
		'''
		val parser = new UtplsqlParser(plsql)
		Assert.assertEquals("t", parser.getObjectAt(0).name)
		Assert.assertEquals("TYPE", parser.getObjectAt(0).type)
	}

	@Test
	def typeBody() {
		val plsql = '''
			create or replace type body t force is
			   member procedure p(self in t) is
			   begin
			      null;
			   end;
			end;
			/
		'''
		val parser = new UtplsqlParser(plsql)
		Assert.assertEquals("t", parser.getObjectAt(0).name)
		Assert.assertEquals("TYPE", parser.getObjectAt(0).type)
	}

	@Test
	def unknown() {
		val plsql = '''
			create or replace unknown u is
			begin
			   null;
			end;
			/
		'''
		val parser = new UtplsqlParser(plsql)
		Assert.assertEquals(null, parser.getObjectAt(0))
	}
	
	@Test
	def void StartLineSpec() {
		val plsql = '''
			CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg is
			   --%suite(JUnit testing)
			   --%suitepath(a)
			
			   --%context(test context)
			
			   --%test(test 1 - OK) 
			   PRoCeDURE test_1_ok;

			   --%test(test 2 - NOK)
			   PROCEDURE test_2_nok;
			
			   --%test(test 3 - disabled)
			   --%disabled
			   PROCEDURE test_3_disabled;

			   --%test(test 4 - errored)
			   PROCEDURE test_4_errored;
			   
			   --%test(test 5 - warnings)
			   PROCEDURE test_5_warnings;
			   --%endcontext

			   function my_Func (p IN number) RETURN BOOLEAN;
			END;
		'''
		val parser = new UtplsqlParser(plsql)
		val first = parser.getLineOf('test_1_ok')
		Assert.assertEquals(8, first)
		val last = parser.getLineOf('test_5_warnings')
		Assert.assertEquals(21, last)
	}

	@Test
	def void StartLineBody() {
		val plsql = '''
			CREATE OR REPLACE PACKAGE BODY junit_utplsql_test1_pkg IS
			   PROCEDURE test_1_ok IS
			   BEGIN
			      dbms_output.put_line('start test 1');
			      dbms_session.sleep(1);
			      ut.expect(1).to_equal(1);
			      dbms_output.put_line('end test 1');
			   END;
			
			   PROCEDURE test_2_nok IS
			   BEGIN
			      dbms_output.put_line('start test 2');
			      dbms_session.sleep(2);
			      ut.expect(1, 'first assert.').to_equal(2);
			      ut.expect(1, 'second assert.').to_equal(2);
			      dbms_output.put_line('end test 2');
			   END;
			   
			   PROCEDURE test_3_disabled IS
			   BEGIN
			      NULL;
			   END;

			   PROCEDURE test_4_errored IS
			   BEGIN
			      EXECUTE IMMEDIATE 'bla bla';
			   END;

			   PROCEDURE test_5_warnings IS
			   BEGIN
			      COMMIT; -- will raise a warning
				  ut.expect(1).to_equal(1);
			   END;

			   FUNCTION my_Func (p IN number) RETURN BOOLEAN IS
			      RETURN TRUE;
			   END;
			END;
		'''
		val parser = new UtplsqlParser(plsql)
		val first = parser.getLineOf('test_1_ok')
		Assert.assertEquals(2, first)
		val last = parser.getLineOf('test_5_warnings')
		Assert.assertEquals(29, last)
	}

}
