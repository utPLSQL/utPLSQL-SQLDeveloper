package org.utplsql.sqldev.test.parser;

import java.util.Set;
import oracle.dbtools.raptor.navigator.plsql.Member;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.parser.SqlDevParser;

@SuppressWarnings("all")
public class SqlDevParserTest {
  private final String packageSpec = new Function0<String>() {
    public String apply() {
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
      return _builder.toString();
    }
  }.apply();
  
  private final String packageBody = new Function0<String>() {
    public String apply() {
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
      return _builder.toString();
    }
  }.apply();
  
  @Test
  public void packageSpecMembers() {
    final SqlDevParser parser = new SqlDevParser();
    final Set<Member> actual = parser.getMembers(this.packageSpec);
    Assert.assertEquals(6, ((Object[])Conversions.unwrapArray(actual, Object.class)).length);
    final Member first = ((Member[])Conversions.unwrapArray(actual, Member.class))[0];
    Assert.assertEquals("PROCEDURE", first.type);
    Assert.assertEquals("test_1_ok", first.name);
    final Member last = ((Member[])Conversions.unwrapArray(actual, Member.class))[5];
    Assert.assertEquals("FUNCTION", last.type);
    Assert.assertEquals("my_Func", last.name);
  }
  
  @Test
  public void packageBodyMembers() {
    final SqlDevParser parser = new SqlDevParser();
    final Set<Member> actual = parser.getMembers(this.packageBody);
    Assert.assertEquals(6, ((Object[])Conversions.unwrapArray(actual, Object.class)).length);
    final Member first = ((Member[])Conversions.unwrapArray(actual, Member.class))[0];
    Assert.assertEquals("PROCEDURE", first.type);
    Assert.assertEquals("test_1_ok", first.name);
    final Member last = ((Member[])Conversions.unwrapArray(actual, Member.class))[5];
    Assert.assertEquals("FUNCTION", last.type);
    Assert.assertEquals("my_Func", last.name);
  }
  
  @Test
  public void StartLineSpec() {
    final SqlDevParser parser = new SqlDevParser();
    final int first = parser.getMemberStartLine(this.packageSpec, "test_1_ok");
    Assert.assertEquals(8, first);
    final int last = parser.getMemberStartLine(this.packageSpec, "my_func");
    Assert.assertEquals(24, last);
  }
  
  @Test
  public void StartLineBody() {
    final SqlDevParser parser = new SqlDevParser();
    final int first = parser.getMemberStartLine(this.packageBody, "test_1_ok");
    Assert.assertEquals(2, first);
    final int last = parser.getMemberStartLine(this.packageBody, "my_func");
    Assert.assertEquals(35, last);
  }
}
