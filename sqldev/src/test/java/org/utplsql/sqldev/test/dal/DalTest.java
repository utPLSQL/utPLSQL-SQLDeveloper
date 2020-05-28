/**
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

import com.google.common.base.Objects;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oddgen.sqldev.generators.model.Node;
import org.springframework.jdbc.BadSqlGrammarException;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.model.ut.Annotation;
import org.utplsql.sqldev.test.AbstractJdbcTest;

@SuppressWarnings("all")
public class DalTest extends AbstractJdbcTest {
  @BeforeClass
  @AfterClass
  public static void setupAndTeardown() {
    AbstractJdbcTest.sysJdbcTemplate.execute("CREATE OR REPLACE PUBLIC SYNONYM ut FOR ut3.ut");
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (final Throwable _t) {
      if (_t instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE BODY junit_utplsql_test_pkg");
    } catch (final Throwable _t_1) {
      if (_t_1 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_1);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_no_test_pkg");
    } catch (final Throwable _t_2) {
      if (_t_2 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_2);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP TYPE junit_tab1_ot");
    } catch (final Throwable _t_3) {
      if (_t_3 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_3);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP TYPE junit_tab2_ot");
    } catch (final Throwable _t_4) {
      if (_t_4 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_4);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP FUNCTION junit_f");
    } catch (final Throwable _t_5) {
      if (_t_5 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_5);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PROCEDURE junit_p");
    } catch (final Throwable _t_6) {
      if (_t_6 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_6);
      }
    }
  }
  
  @Test
  public void isDbaViewAccessible() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      Assert.assertFalse(dao.isDbaViewAccessible());
      Connection _connection_1 = AbstractJdbcTest.sysDataSource.getConnection();
      final UtplsqlDao sysDao = new UtplsqlDao(_connection_1);
      Assert.assertTrue(sysDao.isDbaViewAccessible());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void utplsqlSchema() {
    try {
      AbstractJdbcTest.sysJdbcTemplate.execute("DROP PUBLIC SYNONYM ut");
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      Assert.assertEquals(null, dao.getUtplsqlSchema());
      DalTest.setupAndTeardown();
      Assert.assertEquals("UT3", dao.getUtplsqlSchema());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void isUtAnnotationManagerInstalled() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      Assert.assertTrue(dao.isUtAnnotationManagerInstalled());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void containsUtplsqlTest(final String utPlsqlVersion) {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion(utPlsqlVersion);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t1;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %Test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t2;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t3;");
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      Assert.assertTrue(dao.containsUtplsqlTest("scott"));
      Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"));
      Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t1"));
      Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t2"));
      Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t3"));
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("-- %test");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("PROCEDURE t1;");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("-- %Test");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("PROCEDURE t2;");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("PROCEDURE t3;");
      _builder_1.newLine();
      _builder_1.append("END junit_utplsql_test_pkg;");
      _builder_1.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder_1.toString());
      Assert.assertFalse(dao.containsUtplsqlTest("scott"));
      Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"));
      Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t1"));
      Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t2"));
      Assert.assertFalse(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg", "t3"));
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void containsUtplsqlTest304() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      int _normalizedUtPlsqlVersionNumber = dao.normalizedUtPlsqlVersionNumber();
      boolean _lessThan = (_normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API);
      if (_lessThan) {
        this.containsUtplsqlTest("3.0.4");
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void containsUtplsqlTest313() {
    this.containsUtplsqlTest("3.1.3");
  }
  
  @Test
  public void containsUtplsqlTest318() {
    this.containsUtplsqlTest("3.1.8");
  }
  
  public void annotations(final String utPlsqlVersion) {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion(utPlsqlVersion);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t1;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %Test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t2;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t3;");
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
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
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void annotations304() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      int _normalizedUtPlsqlVersionNumber = dao.normalizedUtPlsqlVersionNumber();
      boolean _lessThan = (_normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API);
      if (_lessThan) {
        this.annotations("3.0.4");
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void annotations313() {
    this.annotations("3.1.3");
  }
  
  @Test
  public void annotations318() {
    this.annotations("3.1.8");
  }
  
  public void testablesPackages(final String utPlsqlVersion) {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion(utPlsqlVersion);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t1;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %Test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t2;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t3;");
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("CREATE OR REPLACE PACKAGE junit_no_test_pkg IS");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("PROCEDURE p1;");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("PROCEDURE p2;");
      _builder_1.newLine();
      _builder_1.append("END junit_no_test_pkg;");
      _builder_1.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder_1.toString());
      final List<Node> actual = dao.testables("PACKAGE");
      Assert.assertEquals(1, actual.size());
      Assert.assertEquals("PACKAGE.JUNIT_NO_TEST_PKG", actual.get(0).getId());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesPackages304() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      int _normalizedUtPlsqlVersionNumber = dao.normalizedUtPlsqlVersionNumber();
      boolean _lessThan = (_normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API);
      if (_lessThan) {
        this.testablesPackages("3.0.4");
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesPackages313() {
    this.testablesPackages("3.1.3");
  }
  
  @Test
  public void testablesPackages318() {
    this.testablesPackages("3.1.8");
  }
  
  public void testablesTypes(final String utPlsqlVersion) {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion(utPlsqlVersion);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE TYPE junit_tab1_ot IS object (a integer, b integer);");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("CREATE OR REPLACE TYPE junit_tab2_ot IS object (");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("a integer, ");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("b integer, ");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("member procedure c(");
      _builder_1.newLine();
      _builder_1.append("      ");
      _builder_1.append("self in out nocopy junit_tab2_ot, ");
      _builder_1.newLine();
      _builder_1.append("      ");
      _builder_1.append("p integer");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append(")");
      _builder_1.newLine();
      _builder_1.append(");");
      _builder_1.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder_1.toString());
      final List<Node> actual = dao.testables("TYPE");
      Assert.assertEquals(1, actual.size());
      Assert.assertEquals("TYPE.JUNIT_TAB2_OT", actual.get(0).getId());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesTypes304() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      int _normalizedUtPlsqlVersionNumber = dao.normalizedUtPlsqlVersionNumber();
      boolean _lessThan = (_normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API);
      if (_lessThan) {
        this.testablesTypes("3.0.4");
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesTypes313() {
    this.testablesTypes("3.1.3");
  }
  
  @Test
  public void testablesTypes318() {
    this.testablesTypes("3.1.8");
  }
  
  public void testablesFunctions(final String utPlsqlVersion) {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion(utPlsqlVersion);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE FUNCTION junit_f RETURN INTEGER IS ");
      _builder.newLine();
      _builder.append("BEGIN");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("RETURN 1;");
      _builder.newLine();
      _builder.append("END;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      final List<Node> actual = dao.testables("FUNCTION");
      Assert.assertEquals(1, actual.size());
      Assert.assertEquals("FUNCTION.JUNIT_F", actual.get(0).getId());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesFunctions304() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      int _normalizedUtPlsqlVersionNumber = dao.normalizedUtPlsqlVersionNumber();
      boolean _lessThan = (_normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API);
      if (_lessThan) {
        this.testablesFunctions("3.0.4");
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesFunctions313() {
    this.testablesFunctions("3.1.3");
  }
  
  @Test
  public void testablesFunctions318() {
    this.testablesFunctions("3.1.8");
  }
  
  public void testablesProcedures(final String utPlsqlVersion) {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion(utPlsqlVersion);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PROCEDURE junit_p RETURN INTEGER IS ");
      _builder.newLine();
      _builder.append("BEGIN");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("NULL;");
      _builder.newLine();
      _builder.append("END;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      final List<Node> actual = dao.testables("PROCEDURE");
      Assert.assertEquals(1, actual.size());
      Assert.assertEquals("PROCEDURE.JUNIT_P", actual.get(0).getId());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesProcedures304() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      int _normalizedUtPlsqlVersionNumber = dao.normalizedUtPlsqlVersionNumber();
      boolean _lessThan = (_normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API);
      if (_lessThan) {
        this.testablesProcedures("3.0.4");
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testablesProcedures313() {
    this.testablesProcedures("3.1.3");
  }
  
  @Test
  public void testablesProcedures318() {
    this.testablesProcedures("3.1.8");
  }
  
  public void runnables(final String utPlsqlVersion) {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion(utPlsqlVersion);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suitepath(a.B.c)");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE T0;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %context(myContext)");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test(t1: test One)");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t1;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test(t2: test Two)");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t2;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %endcontext");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE t3;");
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
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
      Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1"));
      Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.t0"));
      Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.t3"));
      Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1.t1"));
      Assert.assertEquals("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1", actual.get("SCOTT:a.b.c.junit_utplsql_test_pkg.nested_context_#1.t2"));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void runnables304() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      int _normalizedUtPlsqlVersionNumber = dao.normalizedUtPlsqlVersionNumber();
      boolean _lessThan = (_normalizedUtPlsqlVersionNumber < UtplsqlDao.FIRST_VERSION_WITHOUT_INTERNAL_API);
      if (_lessThan) {
        this.runnables("3.0.4");
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void runnables313() {
    this.runnables("3.1.3");
  }
  
  @Test
  public void runnables318() {
    this.runnables("3.1.8");
  }
  
  @Test
  public void dbmsOutput() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.enableDbmsOutput();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("BEGIN");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("sys.dbms_output.put_line(\'line1\');");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("sys.dbms_output.put_line(\'line2\');");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("sys.dbms_output.put_line(null);");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("sys.dbms_output.put_line(\'line4\');");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("sys.dbms_output.put_line(\'line5\');");
      _builder.newLine();
      _builder.append("END;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      final String actual = dao.getDbmsOutput(2);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("line1");
      _builder_1.newLine();
      _builder_1.append("line2");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("line4");
      _builder_1.newLine();
      _builder_1.append("line5");
      _builder_1.newLine();
      final String expected = _builder_1.toString();
      Assert.assertEquals(expected, actual);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void htmlCodeCoverage() {
    try {
      DalTest.setupAndTeardown();
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final String actual = dao.htmlCodeCoverage(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("SCOTT")), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("scott")), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
      Assert.assertTrue(actual.startsWith("<!DOCTYPE html>"));
      Assert.assertTrue(actual.trim().endsWith("</html>"));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void includes() {
    try {
      DalTest.setupAndTeardown();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE FUNCTION junit_f RETURN INTEGER IS");
      _builder.newLine();
      _builder.append("BEGIN");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("RETURN 1;");
      _builder.newLine();
      _builder.append("END junit_f;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("-- %suite");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("-- %test");
      _builder_1.newLine();
      _builder_1.append("   ");
      _builder_1.append("PROCEDURE f1;");
      _builder_1.newLine();
      _builder_1.append("END junit_utplsql_test_pkg;");
      _builder_1.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder_1.toString());
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test_pkg IS");
      _builder_2.newLine();
      _builder_2.append("   ");
      _builder_2.append("PROCEDURE f1 IS");
      _builder_2.newLine();
      _builder_2.append("      ");
      _builder_2.append("l_expected INTEGER := 1;");
      _builder_2.newLine();
      _builder_2.append("      ");
      _builder_2.append("l_actual   INTEGER;");
      _builder_2.newLine();
      _builder_2.append("   ");
      _builder_2.append("BEGIN");
      _builder_2.newLine();
      _builder_2.append("      ");
      _builder_2.append("l_actual := junit_f;");
      _builder_2.newLine();
      _builder_2.append("      ");
      _builder_2.append("ut.expect(l_actual).to_equal(l_expected);");
      _builder_2.newLine();
      _builder_2.append("   ");
      _builder_2.append("END f1;");
      _builder_2.newLine();
      _builder_2.append("END junit_utplsql_test_pkg;");
      _builder_2.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder_2.toString());
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final List<String> actualEmpty = dao.includes("SCOTT", "TEST_F1");
      Assert.assertEquals(Collections.<Object>unmodifiableList(CollectionLiterals.<Object>newArrayList()), actualEmpty);
      final List<String> actual = dao.includes("SCOTT", "junit_utplsql_test_pkg");
      final Function1<String, Boolean> _function = (String it) -> {
        return Boolean.valueOf(Objects.equal(it, "SCOTT.JUNIT_UTPLSQL_TEST_PKG"));
      };
      String _findFirst = IterableExtensions.<String>findFirst(actual, _function);
      boolean _tripleNotEquals = (_findFirst != null);
      Assert.assertTrue(_tripleNotEquals);
      final Function1<String, Boolean> _function_1 = (String it) -> {
        return Boolean.valueOf(Objects.equal(it, "SCOTT.JUNIT_F"));
      };
      String _findFirst_1 = IterableExtensions.<String>findFirst(actual, _function_1);
      boolean _tripleNotEquals_1 = (_findFirst_1 != null);
      Assert.assertTrue(_tripleNotEquals_1);
      final Function1<String, Boolean> _function_2 = (String it) -> {
        return Boolean.valueOf(Objects.equal(it, "UT3.UT_EXPECTATION"));
      };
      String _findFirst_2 = IterableExtensions.<String>findFirst(actual, _function_2);
      boolean _tripleNotEquals_2 = (_findFirst_2 != null);
      Assert.assertTrue(_tripleNotEquals_2);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void normalizedPlsqlVersionOkRelease() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion("v3.1.10.1234");
      final String actual = dao.normalizedUtPlsqlVersion();
      Assert.assertEquals("3.1.10", actual);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void normalizedPlsqlVersionOkDevelop() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion("v3.1.10.1234-develop");
      final String actual = dao.normalizedUtPlsqlVersion();
      Assert.assertEquals("3.1.10", actual);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void normalizedPlsqlVersionNok() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion("bla bla 1.2");
      final String actual = dao.normalizedUtPlsqlVersion();
      Assert.assertEquals("0.0.0", actual);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void normaliedPlsqlVersionNumber() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      dao.setUtPlsqlVersion("3.14.37");
      final int actual = dao.normalizedUtPlsqlVersionNumber();
      Assert.assertEquals(3014037, actual);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void utPlsqlVersion() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final String actual = dao.getUtPlsqlVersion();
      final String sql = "SELECT ut.version FROM DUAL";
      final String expected = AbstractJdbcTest.jdbcTemplate.<String>queryForObject(sql, String.class);
      Assert.assertEquals(expected, actual);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void getSourceOfPackage() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE p1;");
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      final String actual = dao.getSource("SCOTT", "PACKAGE", "JUNIT_UTPLSQL_TEST_PKG");
      Assert.assertTrue(actual.contains("-- %suite"));
      Assert.assertTrue(actual.contains("PROCEDURE p1;"));
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void getSourceOfPackageBody() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE p1 IS");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("l_expected INTEGER := 1;");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("l_actual   INTEGER;");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("BEGIN");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("l_actual := junit_f;");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("ut.expect(l_actual).to_equal(l_expected);");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("END p1;");
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      final String actual = dao.getSource("SCOTT", "PACKAGE BODY", "JUNIT_UTPLSQL_TEST_PKG");
      Assert.assertTrue(actual.contains("PACKAGE BODY"));
      Assert.assertTrue(actual.contains("PROCEDURE p1 IS"));
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE BODY junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void getObjectType() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE p1;");
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      final String actual = dao.getObjectType("SCOTT", "JUNIT_UTPLSQL_TEST_PKG");
      Assert.assertEquals("PACKAGE", actual);
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void normalizedUtPlsqlVersion() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final String version = dao.normalizedUtPlsqlVersion();
      Assert.assertTrue((version != null));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
