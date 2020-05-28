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
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
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
import org.utplsql.sqldev.test.AbstractJdbcTest;

@SuppressWarnings("all")
public class DalBugFixTest extends AbstractJdbcTest {
  @BeforeClass
  @AfterClass
  public static void setupAndTeardown() {
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (final Throwable _t) {
      if (_t instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void issue54FolderIconForSuitesWithoutTests() {
    try {
      DalBugFixTest.setupAndTeardown();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final List<Node> actualNodes = dao.runnables();
      Assert.assertEquals(4, actualNodes.size());
      final Function1<Node, Boolean> _function = (Node it) -> {
        String _id = it.getId();
        return Boolean.valueOf(Objects.equal(_id, "SCOTT:junit_utplsql_test_pkg"));
      };
      final Node pkg = IterableExtensions.<Node>findFirst(actualNodes, _function);
      Assert.assertEquals("FOLDER_ICON", pkg.getIconName());
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void issue54PackageIconForSuitesWithTests() {
    try {
      DalBugFixTest.setupAndTeardown();
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
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final List<Node> actualNodes = dao.runnables();
      Assert.assertEquals(6, actualNodes.size());
      final Function1<Node, Boolean> _function = (Node it) -> {
        String _id = it.getId();
        return Boolean.valueOf(Objects.equal(_id, "SCOTT:junit_utplsql_test_pkg"));
      };
      final Node pkg = IterableExtensions.<Node>findFirst(actualNodes, _function);
      Assert.assertEquals("PACKAGE_ICON", pkg.getIconName());
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void issue55SuiteWithoutTests() {
    try {
      DalBugFixTest.setupAndTeardown();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final List<Node> actualNodes = dao.runnables();
      Assert.assertEquals(4, actualNodes.size());
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void issue56SuiteWithoutTests() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.newLine();
      _builder.append("END junit_utplsql_test_pkg;");
      _builder.newLine();
      AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"));
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE junit_utplsql_test_pkg");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
