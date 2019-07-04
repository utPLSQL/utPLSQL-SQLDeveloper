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

import org.junit.Assert
import org.junit.Test
import org.utplsql.sqldev.parser.UtplsqlParser

class UtplsqlParserBugFixTest {
	
	@Test
    // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/1
	def issue1MatchingExprInStringLiterals() {
		val plsql = '''
			create or replace package body test_expect_not_to_be_null
			is
			    gc_object_name constant varchar2(30) := 't_not_to_be_null_test';
			    gc_nested_table_name constant varchar2(30) := 'tt_not_to_be_null_test';
			    gc_varray_name constant varchar2(30) := 'tv_not_to_be_null_test';
			
			    procedure cleanup_expectations
			    is
			    begin
			        ut3.ut_expectation_processor.clear_expectations();
			    end;
			
			    procedure create_types
			    is
			        pragma autonomous_transaction;
			    begin
			        execute immediate 'create type '||gc_object_name||' is object (dummy number)';
			        execute immediate ' create type '||gc_nested_table_name||' is table of number';
			        execute immediate '
			        create type '||gc_varray_name||' is varray(1) of number';
			    end;
			
			    procedure drop_types
			    is
			        pragma autonomous_transaction;
			    begin
			        execute immediate 'drop type '||gc_object_name;
			        execute immediate ' drop type '||gc_nested_table_name;
			        execute immediate '
			        drop type '||gc_varray_name;
			    end;
			
			    procedure blob_not_null
			    is
			    begin
			        --Act
			        execute immediate expectations_helpers.unary_expectation_block('not_to_be_null', 'blob', 'to_blob(''abc'')');
			        --Assert
			        ut.expect(anydata.convertCollection(ut3.ut_expectation_processor.get_failed_expectations())).to_be_empty();
			    end;
			
			--and so on...
			
			end;
		'''
		val parser = new UtplsqlParser(plsql)
		Assert.assertEquals("test_expect_not_to_be_null.cleanup_expectations", parser.getPathAt(parser.toPosition(7,1)))
		Assert.assertEquals("test_expect_not_to_be_null.create_types", parser.getPathAt(parser.toPosition(13,1)))
		// was: '||gc_varray_name||'.drop_types
		Assert.assertEquals("test_expect_not_to_be_null.drop_types", parser.getPathAt(parser.toPosition(23,1)))
		// was: '||gc_varray_name||'.blob_not_null
		Assert.assertEquals("test_expect_not_to_be_null.blob_not_null", parser.getPathAt(parser.toPosition(33,1)))
	}

	@Test
	// https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/7
	def issue7WrongPositionWithWindowsLineSeparator() {
		val plsql = '''
			create or replace package test_expect_not_to_be_null
			is
			    --%suite(expectations - not_to_be_null)
			    --%suitepath(utplsql.core.expectations.unary)
			
			    --%aftereach
			    procedure cleanup_expectations;
			
			    --%beforeall
			    procedure create_types;
			
			    --%afterall
			    procedure drop_types;
			
			    --%test(Gives success for not null blob)
			    procedure blob_not_null;
			
			    --%test(Gives success for blob with length 0)
			    procedure blob_0_length;
			    
			    -- ...
			end test_expect_not_to_be_null;
			/
		'''
		val parser = new UtplsqlParser(plsql)
		// was: test_expect_not_to_be_null.create_types
		Assert.assertEquals("test_expect_not_to_be_null.blob_not_null", parser.getPathAt(parser.toPosition(13,26)))
	}

}
