package org.utplsql.sqldev.test.parser

import org.junit.Assert
import org.junit.Test
import org.utplsql.sqldev.parser.SqlDevParser

class SqlDevParserTest {
	val packageSpec = '''
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

	val packageBody = '''
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
	
	@Test
	def void packageSpecMembers() {
		val parser = new SqlDevParser
		val actual = parser.getMembers(packageSpec)
		Assert.assertEquals(6, actual.length)
		val first = actual.get(0)
		Assert.assertEquals("PROCEDURE", first.type)
		Assert.assertEquals("test_1_ok", first.name)
		val last = actual.get(5)
		Assert.assertEquals("FUNCTION", last.type)
		Assert.assertEquals("my_Func", last.name)
	}

	@Test
	def void packageBodyMembers() {
		val parser = new SqlDevParser
		val actual = parser.getMembers(packageBody)
		Assert.assertEquals(6, actual.length)
		val first = actual.get(0)
		Assert.assertEquals("PROCEDURE", first.type)
		Assert.assertEquals("test_1_ok", first.name)
		val last = actual.get(5)
		Assert.assertEquals("FUNCTION", last.type)
		Assert.assertEquals("my_Func", last.name)
	}

	@Test
	def void StartLineSpec() {
		val parser = new SqlDevParser
		val first = parser.getMemberStartLine(packageSpec, 'test_1_ok')
		Assert.assertEquals(8, first)
		val last = parser.getMemberStartLine(packageSpec, 'my_func')
		Assert.assertEquals(24, last)
	}

	@Test
	def void StartLineBody() {
		val parser = new SqlDevParser
		val first = parser.getMemberStartLine(packageBody, 'test_1_ok')
		Assert.assertEquals(2, first)
		val last = parser.getMemberStartLine(packageBody, 'my_func')
		Assert.assertEquals(35, last)
	}



}