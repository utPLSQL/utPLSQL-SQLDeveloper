package org.utplsql.sqldev.test;

import java.util.Collections;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.PrefixTools;

@SuppressWarnings("all")
public class PrefixToolsTest {
  @Test
  public void two() {
    final String actual = PrefixTools.commonPrefix(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("junit.test.a", "junit.test.b")));
    final String expected = "junit.test.";
    Assert.assertEquals(expected, actual);
  }
  
  @Test
  public void oneWithDot() {
    final String actual = PrefixTools.commonPrefix(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("junit.test.a")));
    final String expected = "junit.test.";
    Assert.assertEquals(expected, actual);
  }
  
  @Test
  public void oneWithoutDot() {
    final String actual = PrefixTools.commonPrefix(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("junit-test-a")));
    final String expected = "";
    Assert.assertEquals(expected, actual);
  }
  
  @Test
  public void twoOverlapLeft() {
    final String actual = PrefixTools.commonPrefix(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("a.b.c", "a.b.c.d")));
    final String expected = "";
    Assert.assertEquals(expected, actual);
  }
  
  @Test
  public void twoOverlapRight() {
    final String actual = PrefixTools.commonPrefix(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("a.b.c.d", "a.b.c")));
    final String expected = "";
    Assert.assertEquals(expected, actual);
  }
}
