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
package org.utplsql.sqldev.test.parser;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.parser.SqlDevParser;

import oracle.dbtools.raptor.navigator.plsql.Member;

public class SqlDevParserTest {
    
    private String getPackageSpec() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg IS\n");
        sb.append("   --%suite(JUnit testing)\n");
        sb.append("   --%suitepath(a)\n\n");

        sb.append("   --%context(test context)\n\n");

        sb.append("   --%test(test 1 - OK)\n");
        sb.append("   PRoCeDURE test_1_ok;\n\n");
        
        sb.append("   --%test(test 2 - NOK)\n");
        sb.append("   PROCEDURE test_2_nok;\n\n");
        
        sb.append("   --%test(test 3 - disabled)\n");
        sb.append("   --%disabled\n");
        sb.append("   PROCEDURE test_3_disabled;\n\n");

        sb.append("   --%test(test 4 - errored)\n");
        sb.append("   PROCEDURE test_4_errored;\n\n");
        
        sb.append("   --%test(test 5 - warnings)\n");
        sb.append("   PROCEDURE test_5_warnings;\n");
        sb.append("   --%endcontext\n\n");

        sb.append("   function my_Func (p IN number) RETURN BOOLEAN;\n");
        sb.append("END;");
        return sb.toString();
    }
    
    private String getPackageBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test1_pkg IS\n");
        sb.append("   PROCEDURE test_1_ok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      dbms_output.put_line('start test 1');\n");
        sb.append("      dbms_session.sleep(1);\n");
        sb.append("      ut.expect(1).to_equal(1);\n");
        sb.append("      dbms_output.put_line('end test 1');\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_2_nok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      dbms_output.put_line('start test 2');\n");
        sb.append("      dbms_session.sleep(2);\n");
        sb.append("      ut.expect(1, 'first assert.').to_equal(2);\n");
        sb.append("      ut.expect(1, 'second assert.').to_equal(2);\n");
        sb.append("      dbms_output.put_line('end test 2');\n");
        sb.append("   END;\n\n");
        
        sb.append("   PROCEDURE test_3_disabled IS\n");
        sb.append("   BEGIN\n");
        sb.append("      NULL;\n");
        sb.append("   END;\n\n");
        
        sb.append("   PROCEDURE test_4_errored IS\n");
        sb.append("   BEGIN\n");
        sb.append("      EXECUTE IMMEDIATE 'bla bla';\n");
        sb.append("   END;\n\n");
        
        sb.append("   PROCEDURE test_5_warnings IS\n");
        sb.append("   BEGIN\n");
        sb.append("      COMMIT; -- will raise a warning\n");
        sb.append("   ut.expect(1).to_equal(1);\n");
        sb.append("   END;\n\n");
        
        sb.append("   FUNCTION my_Func (p IN number) RETURN BOOLEAN IS\n");
        sb.append("      RETURN TRUE;\n");
        sb.append("   END;\n");
        sb.append("END;");
        return sb.toString();
    }

    @Test
    public void packageSpecMembers() {
        final SqlDevParser parser = new SqlDevParser();
        final Set<Member> actual = parser.getMembers(getPackageSpec());
        Assert.assertEquals(6, actual.size());
        final Member first = actual.stream().findFirst().get();
        Assert.assertEquals("PROCEDURE", first.type);
        Assert.assertEquals("test_1_ok", first.name);
        final Member last =  actual.stream().reduce((m1, m2) -> m2).get();
        Assert.assertEquals("FUNCTION", last.type);
        Assert.assertEquals("my_Func", last.name);
    }

    @Test
    public void packageBodyMembers() {
        final SqlDevParser parser = new SqlDevParser();
        final Set<Member> actual = parser.getMembers(getPackageBody());
        Assert.assertEquals(6, actual.size());
        final Member first = actual.stream().findFirst().get();
        Assert.assertEquals("PROCEDURE", first.type);
        Assert.assertEquals("test_1_ok", first.name);
        final Member last =  actual.stream().reduce((m1, m2) -> m2).get();
        Assert.assertEquals("FUNCTION", last.type);
        Assert.assertEquals("my_Func", last.name);
    }

    @Test
    public void StartLineSpec() {
        final SqlDevParser parser = new SqlDevParser();
        final int first = parser.getMemberStartLine(getPackageSpec(), "test_1_ok");
        Assert.assertEquals(8, first);
        final int last = parser.getMemberStartLine(getPackageSpec(), "my_func");
        Assert.assertEquals(24, last);
    }

    @Test
    public void StartLineBody() {
        final SqlDevParser parser = new SqlDevParser();
        final int first = parser.getMemberStartLine(getPackageBody(), "test_1_ok");
        Assert.assertEquals(2, first);
        final int last = parser.getMemberStartLine(getPackageBody(), "my_func");
        Assert.assertEquals(35, last);
    }
}
