package org.utplsql.sqldev.test.template;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.utplsql.sqldev.model.oddgen.GenContext;
import org.utplsql.sqldev.oddgen.TestTemplate;
import org.utplsql.sqldev.test.AbstractJdbcTest;
import org.utplsql.sqldev.test.coverage.CodeCoverageReporterTest;

import java.sql.SQLException;

public class TemplateTest extends AbstractJdbcTest {

    @Before
    public void setup() {
        executeAndIgnore(jdbcTemplate, """
                create or replace package junit_pkg is
                   procedure proc;
                end;
                """);
    }


    @After
    public void teardown() {
        executeAndIgnore(jdbcTemplate, "drop package junit_pkg");
    }

    @Test
    public void spec_no_comment_no_disable_no_path() throws SQLException {
        // arrange
        var context = new GenContext();
        context.setConn(dataSource.getConnection());
        context.setObjectType("PACKAGE");
        context.setObjectName("JUNIT_PKG");
        context.setTestPackagePrefix("");
        context.setTestPackageSuffix("_test");
        context.setTestUnitPrefix("");
        context.setTestUnitSuffix("");
        context.setNumberOfTestsPerUnit(1);
        context.setGenerateComments(false);
        context.setDisableTests(false);
        context.setSuitePath("");
        context.setIndentSpaces(3);

        // act
        var template = new TestTemplate(context);
        var actual = template.generateSpec();

        // assert
        var expected = """
                create or replace package junit_pkg_test is
                   --%suite(junit_pkg_test)

                   --%test
                   procedure proc;
                                
                end junit_pkg_test;
                /
                """.trim();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void spec_no_comment_no_disable_with_path() throws SQLException {
        // arrange
        var context = new GenContext();
        context.setConn(dataSource.getConnection());
        context.setObjectType("PACKAGE");
        context.setObjectName("JUNIT_PKG");
        context.setTestPackagePrefix("");
        context.setTestPackageSuffix("_test");
        context.setTestUnitPrefix("");
        context.setTestUnitSuffix("");
        context.setNumberOfTestsPerUnit(1);
        context.setGenerateComments(false);
        context.setDisableTests(false);
        context.setSuitePath("org.utplsql");
        context.setIndentSpaces(3);

        // act
        var template = new TestTemplate(context);
        var actual = template.generateSpec();

        // assert
        var expected = """
                create or replace package junit_pkg_test is
                   --%suite(junit_pkg_test)
                   --%suitepath(org.utplsql)

                   --%test
                   procedure proc;
                                
                end junit_pkg_test;
                /
                """.trim();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void spec_no_comment_disable_with_path() throws SQLException {
        // arrange
        var context = new GenContext();
        context.setConn(dataSource.getConnection());
        context.setObjectType("PACKAGE");
        context.setObjectName("JUNIT_PKG");
        context.setTestPackagePrefix("");
        context.setTestPackageSuffix("_test");
        context.setTestUnitPrefix("");
        context.setTestUnitSuffix("");
        context.setNumberOfTestsPerUnit(1);
        context.setGenerateComments(false);
        context.setDisableTests(true);
        context.setSuitePath("org.utplsql");
        context.setIndentSpaces(3);

        // act
        var template = new TestTemplate(context);
        var actual = template.generateSpec();

        // assert
        var expected = """
                create or replace package junit_pkg_test is
                   --%suite(junit_pkg_test)
                   --%suitepath(org.utplsql)

                   --%test
                   --%disabled
                   procedure proc;
                                
                end junit_pkg_test;
                /
                """.trim();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void spec_comment_disable_with_path() throws SQLException {
        // arrange
        var context = new GenContext();
        context.setConn(dataSource.getConnection());
        context.setObjectType("PACKAGE");
        context.setObjectName("JUNIT_PKG");
        context.setTestPackagePrefix("");
        context.setTestPackageSuffix("_test");
        context.setTestUnitPrefix("");
        context.setTestUnitSuffix("");
        context.setNumberOfTestsPerUnit(1);
        context.setGenerateComments(true);
        context.setDisableTests(true);
        context.setSuitePath("org.utplsql");
        context.setIndentSpaces(3);

        // act
        var template = new TestTemplate(context);
        var actual = template.generateSpec()
                .replaceAll("[0-9]{4}-[0-9]{2}-[0-9]{2}[ ]{1}[0-9]{2}:[0-9]{2}:[0-9]{2}", "datetime");

        // assert
        var expected = """
                create or replace package junit_pkg_test is

                   -- generated by utPLSQL for SQL Developer on datetime

                   --%suite(junit_pkg_test)
                   --%suitepath(org.utplsql)

                   --%test
                   --%disabled
                   procedure proc;
                                
                end junit_pkg_test;
                /
                """.trim();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void spec_comment_disable_with_path_two_units() throws SQLException {
        // arrange
        var context = new GenContext();
        context.setConn(dataSource.getConnection());
        context.setObjectType("PACKAGE");
        context.setObjectName("JUNIT_PKG");
        context.setTestPackagePrefix("");
        context.setTestPackageSuffix("_test");
        context.setTestUnitPrefix("");
        context.setTestUnitSuffix("");
        context.setNumberOfTestsPerUnit(2);
        context.setGenerateComments(true);
        context.setDisableTests(true);
        context.setSuitePath("org.utplsql");
        context.setIndentSpaces(3);

        // act
        var template = new TestTemplate(context);
        var actual = template.generateSpec()
                .replaceAll("[0-9]{4}-[0-9]{2}-[0-9]{2}[ ]{1}[0-9]{2}:[0-9]{2}:[0-9]{2}", "datetime");

        // assert
        var expected = """
                create or replace package junit_pkg_test is
                                
                   -- generated by utPLSQL for SQL Developer on datetime
                                
                   --%suite(junit_pkg_test)
                   --%suitepath(org.utplsql)
                                
                   --%context(proc)
                                
                   --%test
                   --%disabled
                   procedure proc1;
                                
                   --%test
                   --%disabled
                   procedure proc2;
                                
                   --%endcontext
                                
                end junit_pkg_test;
                /
                """.trim();
        Assert.assertEquals(expected, actual);
    }
}
