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
package org.utplsql.sqldev.test.parser;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.parser.PlsqlObject;
import org.utplsql.sqldev.model.parser.Unit;
import org.utplsql.sqldev.parser.UtplsqlParser;
import org.utplsql.sqldev.test.AbstractJdbcTest;

public class UtplsqlParserTest extends AbstractJdbcTest {

    private String getSqlScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("PROMPT\n");
        sb.append("PROMPT Install utPLSQL test package\n");
        sb.append("PROMPT\n\n");

        sb.append("/*\n");
        sb.append(" * some comment\n");
        sb.append(" */\n");
        sb.append("CREATE OR REPLACE PACKAGE pkg IS\n");
        sb.append("   -- %suite\n");
        sb.append("   -- %rollback(manual)\n\n");

        sb.append("   -- %test\n");
        sb.append("   PROCEDURE p (in_p1 INTEGER);\n");
        sb.append("   FUNCTION f (in_p1 INTEGER) RETURN INTEGER;\n");
        sb.append("END pkg;\n");
        sb.append("/\n");
        sb.append("SHOW ERRORS\n\n");

        sb.append("CREATE OR REPLACE PACKAGE BODY \"SCOTT\".\"PKG\" IS\n");
        sb.append("   PROCEDURE \"P\" (in_p1 INTEGER) IS\n");
        sb.append("   BEGIN\n");
        sb.append("      NULL;\n");
        sb.append("   END p;\n\n");

        sb.append("   /* comment 1 */\n");
        sb.append("   -- comment 2\n");
        sb.append("   /* comment 3 */\n");
        sb.append("   -- comment 4\n\n");
 
        sb.append("   FUNCTION \"F\" (in_p1 INTEGER) RETURN INTEGER IS\n");
        sb.append("   BEGIN\n");
        sb.append("      RETURN 1;\n");
        sb.append("   END f;\n");
        sb.append("END pkg;\n");
        sb.append("/\n");
        sb.append("SHOW ERRORS\n");
        return sb.toString();
    }

    @Before
    @After
    public void setupAndTeardown() {
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE pkg");
    }

    @Test
    public void packageWithoutConnection() {
        final UtplsqlParser parser = new UtplsqlParser(getSqlScript());
        final List<PlsqlObject> objects = parser.getObjects();
        Assert.assertEquals(2, objects.size());
        Assert.assertEquals("pkg", objects.get(0).getName());
        Assert.assertEquals("\"SCOTT\".\"PKG\"", objects.get(1).getName());
        Assert.assertTrue(objects.get(0).getPosition() < objects.get(1).getPosition());
        final List<Unit> units = parser.getUnits();
        Assert.assertEquals(2, units.size());
        Assert.assertEquals("p", units.get(0).getName());
        Assert.assertEquals("\"P\"", units.get(1).getName());
        Assert.assertTrue(units.get(0).getPosition() < units.get(1).getPosition());
        Assert.assertEquals("", parser.getPathAt(0));
        Assert.assertEquals("", parser.getPathAt(parser.toPosition(3, 6)));
        Assert.assertEquals("pkg", parser.getPathAt(parser.toPosition(4, 1)));
        Assert.assertEquals("pkg.p", parser.getPathAt(parser.toPosition(10, 33)));
        Assert.assertEquals("pkg.p", parser.getPathAt(parser.toPosition(13, 1)));
        Assert.assertEquals("SCOTT.PKG.p", parser.getPathAt(parser.toPosition(19, 1)));
        Assert.assertEquals("SCOTT.PKG.P", parser.getPathAt(parser.toPosition(22, 9)));
        Assert.assertEquals("SCOTT.PKG.P", parser.getPathAt(parser.toPosition(22, 10)));
        Assert.assertEquals("SCOTT.PKG.P", parser.getPathAt(parser.toPosition(29, 1)));
    }

    @Test
    public void packageWithConnection() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE pkg IS\n");
        sb.append("   -- %suite\n");
        sb.append("   -- %rollback(manual)\n\n");

        sb.append("   -- %test\n");
        sb.append("   PROCEDURE p (in_p1 INTEGER);\n");
        sb.append("   FUNCTION f (in_p1 INTEGER) RETURN INTEGER;\n");
        sb.append("END pkg;");
        final String plsql = sb.toString();
        UtplsqlParser parser = new UtplsqlParser(plsql, DatabaseTools.getConnection(dataSource), null);
        Assert.assertEquals(0, parser.getObjects().size());
        Assert.assertEquals(0, parser.getUnits().size());

        jdbcTemplate.execute(plsql);
        parser = new UtplsqlParser(plsql, DatabaseTools.getConnection(dataSource), null);
        Assert.assertEquals(1, parser.getObjects().size());
        Assert.assertEquals(1, parser.getUnits().size());

        for (final String stmt : getStatements(getSqlScript())) {
            jdbcTemplate.execute(stmt);
        }
        parser = new UtplsqlParser(getSqlScript(), DatabaseTools.getConnection(dataSource), null);
        Assert.assertEquals(2, parser.getObjects().size());
        Assert.assertEquals(2, parser.getUnits().size());
        Assert.assertEquals("pkg.p", parser.getPathAt(parser.toPosition(13, 1)));
        Assert.assertEquals("SCOTT.PKG.p", parser.getPathAt(parser.toPosition(19, 1)));
    }

    @Test
    public void procedure() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace procedure z\n");
        sb.append("is\n");
        sb.append("    null;\n");
        sb.append("end;\n");
        sb.append("/\n");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        Assert.assertEquals("z", parser.getObjectAt(0).getName());
        Assert.assertEquals("PROCEDURE", parser.getObjectAt(0).getType());
    }

    @Test
    public void function() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace procedure z\n");
        sb.append("is\n");
        sb.append("    null;\n");
        sb.append("end;\n");
        sb.append("/\n\n");

        sb.append("create or replace function f return number is\n");
        sb.append("begin\n");
        sb.append("   null;\n");
        sb.append("end;\n");
        sb.append("/\n");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        Assert.assertEquals("f", parser.getObjectAt(parser.toPosition(8, 1)).getName());
        Assert.assertEquals("FUNCTION", parser.getObjectAt(parser.toPosition(8, 1)).getType());
    }

    @Test
    public void type() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace type t force is\n");
        sb.append("    object (\n");
        sb.append("      a number,\n");
        sb.append("      b number,\n");
        sb.append("      c varchar2(10),\n");
        sb.append("      member procedure p(self in t)\n");
        sb.append("    )\n");
        sb.append("end;\n");
        sb.append("/\n");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        Assert.assertEquals("t", parser.getObjectAt(0).getName());
        Assert.assertEquals("TYPE", parser.getObjectAt(0).getType());
    }

    @Test
    public void typeBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace type body t force is\n");
        sb.append("   member procedure p(self in t) is\n");
        sb.append("   begin\n");
        sb.append("      null;\n");
        sb.append("   end;\n");
        sb.append("end;\n");
        sb.append("/\n");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        Assert.assertEquals("t", parser.getObjectAt(0).getName());
        Assert.assertEquals("TYPE", parser.getObjectAt(0).getType());
    }

    @Test
    public void unknown() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace unknown u is\n");
        sb.append("begin\n");
        sb.append("   null;\n");
        sb.append("end;\n");
        sb.append("/\n");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        Assert.assertEquals(null, parser.getObjectAt(0));
    }

    @Test
    public void StartLineSpec() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg is\n");
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
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        final int first = parser.getLineOf("test_1_ok");
        Assert.assertEquals(8, first);
        final int last = parser.getLineOf("test_5_warnings");
        Assert.assertEquals(21, last);
    }

    @Test
    public void StartLineBody() {
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
        sb.append("      ut.expect(1).to_equal(1);\n");
        sb.append("   END;\n\n");

        sb.append("   FUNCTION my_Func (p IN number) RETURN BOOLEAN IS\n");
        sb.append("      RETURN TRUE;\n");
        sb.append("   END;\n");
        sb.append("END;");
        final String plsql = sb.toString();
        final UtplsqlParser parser = new UtplsqlParser(plsql);
        final int first = parser.getLineOf("test_1_ok");
        Assert.assertEquals(2, first);
        final int last = parser.getLineOf("test_5_warnings");
        Assert.assertEquals(29, last);
    }
}
