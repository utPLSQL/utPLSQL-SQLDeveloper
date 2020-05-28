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
package org.utplsql.sqldev.test.parser;

import java.sql.Connection;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.utplsql.sqldev.model.parser.PlsqlObject;
import org.utplsql.sqldev.model.parser.Unit;
import org.utplsql.sqldev.parser.UtplsqlParser;
import org.utplsql.sqldev.test.AbstractJdbcTest;

@SuppressWarnings("all")
public class UtplsqlParserTest extends AbstractJdbcTest {
  private static final String sqlScript = new Function0<String>() {
    public String apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("PROMPT");
      _builder.newLine();
      _builder.append("PROMPT Install utPLSQL test package");
      _builder.newLine();
      _builder.append("PROMPT");
      _builder.newLine();
      _builder.newLine();
      _builder.append("/*");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* some comment");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("CREATE OR REPLACE PACKAGE pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %rollback(manual)");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE p (in_p1 INTEGER);");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("FUNCTION f (in_p1 INTEGER) RETURN INTEGER;");
      _builder.newLine();
      _builder.append("END pkg;");
      _builder.newLine();
      _builder.append("/");
      _builder.newLine();
      _builder.append("SHOW ERRORS");
      _builder.newLine();
      _builder.newLine();
      _builder.append("CREATE OR REPLACE PACKAGE BODY \"SCOTT\".\"PKG\" IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE \"P\" (in_p1 INTEGER) IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("BEGIN");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("NULL;");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("END p;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("/* comment 1 */");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- comment 2");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("/* comment 3 */");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- comment 4");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("FUNCTION \"F\" (in_p1 INTEGER) RETURN INTEGER IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("BEGIN");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("RETURN 1;");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("END f;");
      _builder.newLine();
      _builder.append("END pkg;");
      _builder.newLine();
      _builder.append("/");
      _builder.newLine();
      _builder.append("SHOW ERRORS");
      _builder.newLine();
      return _builder.toString();
    }
  }.apply();
  
  @BeforeClass
  @AfterClass
  public static void setupAndTeardown() {
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE pkg");
    } catch (final Throwable _t) {
      if (_t instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void packageWithoutConnection() {
    final UtplsqlParser parser = new UtplsqlParser(UtplsqlParserTest.sqlScript);
    final List<PlsqlObject> objects = parser.getObjects();
    Assert.assertEquals(2, objects.size());
    Assert.assertEquals("pkg", objects.get(0).getName());
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("\"SCOTT\".\"PKG\"");
    Assert.assertEquals(_builder.toString(), objects.get(1).getName());
    Integer _position = objects.get(0).getPosition();
    Integer _position_1 = objects.get(1).getPosition();
    boolean _lessThan = (_position.compareTo(_position_1) < 0);
    Assert.assertTrue(_lessThan);
    final List<Unit> units = parser.getUnits();
    Assert.assertEquals(2, units.size());
    Assert.assertEquals("p", units.get(0).getName());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("\"P\"");
    Assert.assertEquals(_builder_1.toString(), units.get(1).getName());
    Integer _position_2 = units.get(0).getPosition();
    Integer _position_3 = units.get(1).getPosition();
    boolean _lessThan_1 = (_position_2.compareTo(_position_3) < 0);
    Assert.assertTrue(_lessThan_1);
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
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("CREATE OR REPLACE PACKAGE pkg IS");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %suite");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %rollback(manual)");
      _builder.newLine();
      _builder.newLine();
      _builder.append("   ");
      _builder.append("-- %test");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("PROCEDURE p (in_p1 INTEGER);");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("FUNCTION f (in_p1 INTEGER) RETURN INTEGER;");
      _builder.newLine();
      _builder.append("END pkg;");
      _builder.newLine();
      final String plsql = _builder.toString();
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      UtplsqlParser parser = new UtplsqlParser(plsql, _connection, null);
      Assert.assertEquals(0, parser.getObjects().size());
      Assert.assertEquals(0, parser.getUnits().size());
      AbstractJdbcTest.jdbcTemplate.execute(plsql);
      Connection _connection_1 = AbstractJdbcTest.dataSource.getConnection();
      UtplsqlParser _utplsqlParser = new UtplsqlParser(plsql, _connection_1, null);
      parser = _utplsqlParser;
      Assert.assertEquals(1, parser.getObjects().size());
      Assert.assertEquals(1, parser.getUnits().size());
      List<String> _statements = AbstractJdbcTest.getStatements(UtplsqlParserTest.sqlScript);
      for (final String stmt : _statements) {
        AbstractJdbcTest.jdbcTemplate.execute(stmt);
      }
      Connection _connection_2 = AbstractJdbcTest.dataSource.getConnection();
      UtplsqlParser _utplsqlParser_1 = new UtplsqlParser(UtplsqlParserTest.sqlScript, _connection_2, null);
      parser = _utplsqlParser_1;
      Assert.assertEquals(2, parser.getObjects().size());
      Assert.assertEquals(2, parser.getUnits().size());
      Assert.assertEquals("pkg.p", parser.getPathAt(parser.toPosition(13, 1)));
      Assert.assertEquals("SCOTT.PKG.p", parser.getPathAt(parser.toPosition(19, 1)));
      UtplsqlParserTest.setupAndTeardown();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void procedure() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("create or replace procedure z");
    _builder.newLine();
    _builder.append("is");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("null;");
    _builder.newLine();
    _builder.append("end;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    Assert.assertEquals("z", parser.getObjectAt(0).getName());
    Assert.assertEquals("PROCEDURE", parser.getObjectAt(0).getType());
  }
  
  @Test
  public void function() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("create or replace procedure z");
    _builder.newLine();
    _builder.append("is");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("null;");
    _builder.newLine();
    _builder.append("end;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    _builder.newLine();
    _builder.append("create or replace function f return number is");
    _builder.newLine();
    _builder.append("begin");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("null;");
    _builder.newLine();
    _builder.append("end;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    Assert.assertEquals("f", parser.getObjectAt(parser.toPosition(8, 1)).getName());
    Assert.assertEquals("FUNCTION", parser.getObjectAt(parser.toPosition(8, 1)).getType());
  }
  
  @Test
  public void type() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("create or replace type t force is");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("object (");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("a number,");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("b number,");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("c varchar2(10),");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("member procedure p(self in t)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append(")");
    _builder.newLine();
    _builder.append("end;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    Assert.assertEquals("t", parser.getObjectAt(0).getName());
    Assert.assertEquals("TYPE", parser.getObjectAt(0).getType());
  }
  
  @Test
  public void typeBody() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("create or replace type body t force is");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("member procedure p(self in t) is");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("begin");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("null;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("end;");
    _builder.newLine();
    _builder.append("end;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    Assert.assertEquals("t", parser.getObjectAt(0).getName());
    Assert.assertEquals("TYPE", parser.getObjectAt(0).getType());
  }
  
  @Test
  public void unknown() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("create or replace unknown u is");
    _builder.newLine();
    _builder.append("begin");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("null;");
    _builder.newLine();
    _builder.append("end;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    Assert.assertEquals(null, parser.getObjectAt(0));
  }
  
  @Test
  public void StartLineSpec() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg is");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%suite(JUnit testing)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%suitepath(a)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%context(test context)");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 1 - OK) ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PRoCeDURE test_1_ok;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 2 - NOK)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_2_nok;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 3 - disabled)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%disabled");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_3_disabled;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 4 - errored)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_4_errored;");
    _builder.newLine();
    _builder.append("   ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%test(test 5 - warnings)");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_5_warnings;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("--%endcontext");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("function my_Func (p IN number) RETURN BOOLEAN;");
    _builder.newLine();
    _builder.append("END;");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    final int first = parser.getLineOf("test_1_ok");
    Assert.assertEquals(8, first);
    final int last = parser.getLineOf("test_5_warnings");
    Assert.assertEquals(21, last);
  }
  
  @Test
  public void StartLineBody() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test1_pkg IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_1_ok IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("dbms_output.put_line(\'start test 1\');");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("dbms_session.sleep(1);");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("ut.expect(1).to_equal(1);");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("dbms_output.put_line(\'end test 1\');");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_2_nok IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("dbms_output.put_line(\'start test 2\');");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("dbms_session.sleep(2);");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("ut.expect(1, \'first assert.\').to_equal(2);");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("ut.expect(1, \'second assert.\').to_equal(2);");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("dbms_output.put_line(\'end test 2\');");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END;");
    _builder.newLine();
    _builder.append("   ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_3_disabled IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("NULL;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_4_errored IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("EXECUTE IMMEDIATE \'bla bla\';");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE test_5_warnings IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("COMMIT; -- will raise a warning");
    _builder.newLine();
    _builder.append("\t  ");
    _builder.append("ut.expect(1).to_equal(1);");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("   ");
    _builder.append("FUNCTION my_Func (p IN number) RETURN BOOLEAN IS");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("RETURN TRUE;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END;");
    _builder.newLine();
    _builder.append("END;");
    _builder.newLine();
    final String plsql = _builder.toString();
    final UtplsqlParser parser = new UtplsqlParser(plsql);
    final int first = parser.getLineOf("test_1_ok");
    Assert.assertEquals(2, first);
    final int last = parser.getLineOf("test_5_warnings");
    Assert.assertEquals(29, last);
  }
}
