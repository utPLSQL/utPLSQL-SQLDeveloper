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
package org.utplsql.sqldev.test.dal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oddgen.sqldev.generators.model.Node;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.ut.Annotation;
import org.utplsql.sqldev.test.AbstractJdbcTest;

public class DalTest extends AbstractJdbcTest {
    @Before
    @After
    public void setupAndTeardown() {
        sysJdbcTemplate.execute("CREATE OR REPLACE PUBLIC SYNONYM ut FOR ut3.ut");
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_utplsql_test_pkg");
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE BODY junit_utplsql_test_pkg");
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_no_test_pkg");
        executeAndIgnore(jdbcTemplate, "DROP TYPE junit_tab1_ot");
        executeAndIgnore(jdbcTemplate, "DROP TYPE junit_tab2_ot");
        executeAndIgnore(jdbcTemplate, "DROP FUNCTION junit_f");
        executeAndIgnore(jdbcTemplate, "DROP PROCEDURE junit_p");
    }

    @Test
    public void isDbaViewAccessibleAsScott() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        Assert.assertFalse(dao.isDbaViewAccessible());
    }

    @Test
    public void isDbaViewAccessibleAsSys() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(sysDataSource));
        Assert.assertTrue(dao.isDbaViewAccessible());
    }    
    
    @Test
    public void utplsqlSchemaWithoutPublicSynonym() {
        sysJdbcTemplate.execute("DROP PUBLIC SYNONYM ut");
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        Assert.assertEquals(null, dao.getUtplsqlSchema());
    }

    @Test
    public void utplsqlSchemaWithPublicSynonym() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        Assert.assertEquals("UT3", dao.getUtplsqlSchema());
    }

    @Test
    public void isUtAnnotationManagerInstalled() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        Assert.assertTrue(dao.isUtAnnotationManagerInstalled());
    }

    private void containsUtplsqlTestWithSuiteAnnotation(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE t1;\n\n");
        sb.append("   -- %Test\n");
        sb.append("   PROCEDURE t2;\n\n");
        sb.append("   PROCEDURE t3;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        Assert.assertTrue(dao.containsUtplsqlTest("scott"));
        Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"));
        Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t1"));
        Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t2"));
        Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t3"));
    }
    
    @Test
    public void containsUtplsqlTestWithSuiteAnnotation304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            containsUtplsqlTestWithSuiteAnnotation("3.0.4");
        }
    }
    
    @Test
    public void containsUtplsqlTestWithSuiteAnnotation313() {
        containsUtplsqlTestWithSuiteAnnotation("3.1.3");
    }
    
    @Test
    public void containsUtplsqlTestWithSuiteAnnotation318() {
        containsUtplsqlTestWithSuiteAnnotation("3.1.8");
    }

    private void containsUtplsqlTestWithoutSuiteAnnotation(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE t1;\n\n");
        sb.append("   -- %Test\n");
        sb.append("   PROCEDURE t2;\n\n");
        sb.append("   PROCEDURE t3;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        Assert.assertFalse(dao.containsUtplsqlTest("scott"));
        Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"));
        Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t1"));
        Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t2"));
        Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t3"));
    }

    @Test
    public void containsUtplsqlTestWithoutSuiteAnnotation304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            containsUtplsqlTestWithoutSuiteAnnotation("3.0.4");
        }
    }

    @Test
    public void containsUtplsqlTestWithoutSuiteAnnotation313() {
        containsUtplsqlTestWithoutSuiteAnnotation("3.1.3");
    }

    @Test
    public void containsUtplsqlTestWithoutSuiteAnnotation318() {
        containsUtplsqlTestWithoutSuiteAnnotation("3.1.8");
    }

    private void annotations(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE t1;\n\n");
        sb.append("   -- %Test\n");
        sb.append("   PROCEDURE t2;\n\n");
        sb.append("   PROCEDURE t3;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final List<Annotation> actual = dao.annotations("scott", "junit_utplsql_test_pkg");
        final ArrayList<Annotation> expected = new ArrayList<Annotation>();
        final Annotation suite = new Annotation();
        suite.setObjectOwner("SCOTT");
        suite.setObjectName("JUNIT_UTPLSQL_TEST_PKG");
        suite.setName("suite");
        suite.setSubobjectName(suite.getObjectName());
        expected.add(suite);
        final Annotation t1 = new Annotation();
        t1.setObjectOwner("SCOTT");
        t1.setObjectName("JUNIT_UTPLSQL_TEST_PKG");
        t1.setName("test");
        t1.setSubobjectName("T1");
        expected.add(t1);
        final Annotation t2 = new Annotation();
        t2.setObjectOwner("SCOTT");
        t2.setObjectName("JUNIT_UTPLSQL_TEST_PKG");
        t2.setName("test");
        t2.setSubobjectName("T2");
        expected.add(t2);
        Assert.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void annotations304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            annotations("3.0.4");
        }
    }

    @Test
    public void annotations313() {
        annotations("3.1.3");
    }

    @Test
    public void annotations318() {
        annotations("3.1.8");
    }

    private void testablesPackagesWithTests(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE t1;\n\n");
        sb.append("   -- %Test\n");
        sb.append("   PROCEDURE t2;\n\n");
        sb.append("   PROCEDURE t3;\n\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final List<Node> actual = dao.testables("PACKAGE");
        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void testablesPackagesWithTests304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            testablesPackagesWithTests("3.0.4");
        }
    }

    @Test
    public void testablesPackagesWithTests313() {
        testablesPackagesWithTests("3.1.3");
    }

    @Test
    public void testablesPackagesWithTests318() {
        testablesPackagesWithTests("3.1.8");
    }
    
    private void testablesPackagesWithoutTests(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_no_test_pkg IS\n");
        sb.append("   PROCEDURE p1;\n\n");
        sb.append("   PROCEDURE p2;\n");
        sb.append("END junit_no_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final List<Node> actual = dao.testables("PACKAGE");
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals("PACKAGE.JUNIT_NO_TEST_PKG", actual.get(0).getId());
    }

    @Test
    public void testablesPackagesWithoutTests304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            testablesPackagesWithoutTests("3.0.4");
        }
    }

    @Test
    public void testablesPackagesWithoutTests313() {
        testablesPackagesWithoutTests("3.1.3");
    }

    @Test
    public void testablesPackagesWithoutTests318() {
        testablesPackagesWithoutTests("3.1.8");
    }

    private void testablesTypes(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        jdbcTemplate.execute("CREATE OR REPLACE TYPE junit_tab1_ot IS object (a integer, b integer);");
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE TYPE junit_tab2_ot IS object (\n");
        sb.append("   a integer,\n");
        sb.append("   b integer,\n");
        sb.append("   member procedure c(\n");
        sb.append("      self in out nocopy junit_tab2_ot,\n");
        sb.append("      p integer\n");
        sb.append("   )\n");
        sb.append(");");
        jdbcTemplate.execute(sb.toString());
        final List<Node> actual = dao.testables("TYPE");
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals("TYPE.JUNIT_TAB2_OT", actual.get(0).getId());
    }

    @Test
    public void testablesTypes304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            testablesTypes("3.0.4");
        }
    }

    @Test
    public void testablesTypes313() {
        testablesTypes("3.1.3");
    }

    @Test
    public void testablesTypes318() {
        testablesTypes("3.1.8");
    }

    private void testablesFunctions(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE FUNCTION junit_f RETURN INTEGER IS\n");
        sb.append("BEGIN\n");
        sb.append("   RETURN 1;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        final List<Node> actual = dao.testables("FUNCTION");
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals("FUNCTION.JUNIT_F", actual.get(0).getId());
    }

    @Test
    public void testablesFunctions304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            testablesFunctions("3.0.4");
        }
    }

    @Test
    public void testablesFunctions313() {
        testablesFunctions("3.1.3");
    }

    @Test
    public void testablesFunctions318() {
        testablesFunctions("3.1.8");
    }

    public void testablesProcedures(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion(utPlsqlVersion);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PROCEDURE junit_p RETURN INTEGER IS\n");
        sb.append("BEGIN\n");
        sb.append("   NULL;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        final List<Node> actual = dao.testables("PROCEDURE");
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals("PROCEDURE.JUNIT_P", actual.get(0).getId());
    }

    @Test
    public void testablesProcedures304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
                testablesProcedures("3.0.4");
        }
    }

    @Test
    public void testablesProcedures313() {
        testablesProcedures("3.1.3");
    }

    @Test
    public void testablesProcedures318() {
        testablesProcedures("3.1.8");
    }

    public void runnables(final String utPlsqlVersion) {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n");
        sb.append("   -- %suitepath(a.B.c)\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE T0;\n\n");
        sb.append("   -- %context(myContext)\n\n");
        sb.append("   -- %test(t1: test One)\n");
        sb.append("   PROCEDURE t1;\n\n");
        sb.append("   -- %test(t2: test Two)\n");
        sb.append("   PROCEDURE t2;\n\n");
        sb.append("   -- %endcontext\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE t3;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final List<Node> actualNodes = dao.runnables();
        Assert.assertEquals(16, actualNodes.size());
        final HashMap<String, String> actual = new HashMap<String, String>();
        for (final Node node : actualNodes) {
            actual.put(node.getId(), node.getParentId());
        }
        Assert.assertEquals(null, actual.get("SUITE"));
        Assert.assertEquals("SUITE", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG"));
        Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T0"));
        Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T1"));
        Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T2"));
        Assert.assertEquals("SCOTT.JUNIT_UTPLSQL_TEST_PKG", actual.get("SCOTT.JUNIT_UTPLSQL_TEST_PKG.T3"));
        Assert.assertEquals(null, actual.get("SUITEPATH"));
        Assert.assertEquals("SUITEPATH", actual.get("SCOTT:a"));
        Assert.assertEquals("SCOTT:a", actual.get("SCOTT:a.b"));
        Assert.assertEquals("SCOTT:a.b", actual.get("SCOTT:a.b.c"));
        Assert.assertEquals("SCOTT:a.b.c", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg"));
        Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#",
                actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1"));
        Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg",
                actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.t0"));
        Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg",
                actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.t3"));
        Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1",
                actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1.t1"));
        Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1",
                actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1.t2"));
    }

    @Test
    public void runnables304() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        if (dao.normalizedUtPlsqlVersionNumber() < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API) {
            runnables("3.0.4");
        }
    }

    @Test
    public void runnables313() {
        runnables("3.1.3");
    }

    @Test
    public void runnables318() {
        runnables("3.1.8");
    }

    @Test
    public void dbmsOutput() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.enableDbmsOutput();
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN\n");
        sb.append("   sys.dbms_output.put_line(\'line1\');\n");
        sb.append("   sys.dbms_output.put_line(\'line2\');\n");
        sb.append("   sys.dbms_output.put_line(null);\n");
        sb.append("   sys.dbms_output.put_line(\'line4\');\n");
        sb.append("   sys.dbms_output.put_line(\'line5\');\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        final String actual = dao.getDbmsOutput(2);
        final String expected = "line1\nline2\n\nline4\nline5\n";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void htmlCodeCoverage() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        final String actual = dao.htmlCodeCoverage(Arrays.asList("SCOTT"), Arrays.asList("scott"), Arrays.asList(),
                Arrays.asList());
        Assert.assertTrue(actual.startsWith("<!DOCTYPE html>"));
        Assert.assertTrue(actual.trim().endsWith("</html>"));
    }

    @Test
    public void includes() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE FUNCTION junit_f RETURN INTEGER IS\n");
        sb.append("BEGIN\n");
        sb.append("   RETURN 1;\n");
        sb.append("END junit_f;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE f1;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test_pkg IS\n");
        sb.append("   PROCEDURE f1 IS\n");
        sb.append("      l_expected INTEGER := 1;\n");
        sb.append("      l_actual   INTEGER;\n");
        sb.append("   BEGIN\n");
        sb.append("      l_actual := junit_f;\n");
        sb.append("      ut.expect(l_actual).to_equal(l_expected);\n");
        sb.append("   END f1;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        final List<String> actualEmpty = dao.includes("SCOTT", "TEST_F1");
        Assert.assertEquals(Arrays.asList(), actualEmpty);
        final List<String> actual = dao.includes("SCOTT", "junit_utplsql_test_pkg");
        Assert.assertTrue(actual.stream().anyMatch(it -> it.equals("SCOTT.JUNIT_UTPLSQL_TEST_PKG")));
        Assert.assertTrue(actual.stream().anyMatch(it -> it.equals("SCOTT.JUNIT_F")));
        Assert.assertTrue(actual.stream().anyMatch(it -> it.equals("UT3.UT_EXPECTATION")));
    }

    @Test
    public void normalizedPlsqlVersionOkRelease() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion("v3.1.10.1234");
        final String actual = dao.normalizedUtPlsqlVersion();
        Assert.assertEquals("3.1.10", actual);
    }

    @Test
    public void normalizedPlsqlVersionOkDevelop() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion("v3.1.10.1234-develop");
        final String actual = dao.normalizedUtPlsqlVersion();
        Assert.assertEquals("3.1.10", actual);
    }

    @Test
    public void normalizedPlsqlVersionNok() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion("bla bla 1.2");
        final String actual = dao.normalizedUtPlsqlVersion();
        Assert.assertEquals("0.0.0", actual);
    }

    @Test
    public void normaliedPlsqlVersionNumber() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        dao.setUtPlsqlVersion("3.14.37");
        final int actual = dao.normalizedUtPlsqlVersionNumber();
        Assert.assertEquals(3014037, actual);
    }

    @Test
    public void utPlsqlVersion() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        final String actual = dao.getUtPlsqlVersion();
        final String sql = "SELECT ut.version FROM DUAL";
        final String expected = jdbcTemplate.<String>queryForObject(sql, String.class);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getSourceOfPackage() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE p1;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final String actual = dao.getSource("SCOTT", "PACKAGE", "JUNIT_UTPLSQL_TEST_PKG");
        Assert.assertTrue(actual.contains("-- %suite"));
        Assert.assertTrue(actual.contains("PROCEDURE p1;"));
    }

    @Test
    public void getSourceOfPackageBody() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test_pkg IS\n");
        sb.append("   PROCEDURE p1 IS\n");
        sb.append("      l_expected INTEGER := 1;\n");
        sb.append("      l_actual   INTEGER;\n");
        sb.append("   BEGIN\n");
        sb.append("      l_actual := junit_f;\n");
        sb.append("      ut.expect(l_actual).to_equal(l_expected);\n");
        sb.append("   END p1;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final String actual = dao.getSource("SCOTT", "PACKAGE BODY", "JUNIT_UTPLSQL_TEST_PKG");
        Assert.assertTrue(actual.contains("PACKAGE BODY"));
        Assert.assertTrue(actual.contains("PROCEDURE p1 IS"));
    }

    @Test
    public void getObjectType() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE p1;\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final String actual = dao.getObjectType("SCOTT", "JUNIT_UTPLSQL_TEST_PKG");
        Assert.assertEquals("PACKAGE", actual);
    }

    @Test
    public void normalizedUtPlsqlVersion() {
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        final String version = dao.normalizedUtPlsqlVersion();
        Assert.assertTrue((version != null));
    }
}
