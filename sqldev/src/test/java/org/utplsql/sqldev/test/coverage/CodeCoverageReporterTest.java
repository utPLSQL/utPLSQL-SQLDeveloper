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
package org.utplsql.sqldev.test.coverage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.coverage.CodeCoverageReporter;
import org.utplsql.sqldev.test.AbstractJdbcTest;

@SuppressWarnings("all")
public class CodeCoverageReporterTest extends AbstractJdbcTest {
  @BeforeClass
  public static void setup() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE FUNCTION f RETURN INTEGER IS");
    _builder.newLine();
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("RETURN 1;");
    _builder.newLine();
    _builder.append("END f;");
    _builder.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("CREATE OR REPLACE PACKAGE test_f IS");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("--%suite");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("--%test");
    _builder_1.newLine();
    _builder_1.append("   ");
    _builder_1.append("PROCEDURE f;");
    _builder_1.newLine();
    _builder_1.append("END test_f;");
    _builder_1.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_1.toString());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("CREATE OR REPLACE PACKAGE BODY test_f IS");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("--%test");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("PROCEDURE f IS");
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
    _builder_2.append("l_actual := scott.f();");
    _builder_2.newLine();
    _builder_2.append("      ");
    _builder_2.append("ut.expect(l_actual).to_equal(l_expected);");
    _builder_2.newLine();
    _builder_2.append("   ");
    _builder_2.append("END f;");
    _builder_2.newLine();
    _builder_2.append("END test_f;");
    _builder_2.newLine();
    AbstractJdbcTest.jdbcTemplate.execute(_builder_2.toString());
  }
  
  private Path getNewestOutputFile() {
    try {
      final File file = File.createTempFile("test", ".txt");
      final File dir = file.getParentFile();
      file.delete();
      final Predicate<Path> _function = (Path f) -> {
        boolean _isDirectory = f.toFile().isDirectory();
        return (!_isDirectory);
      };
      final Predicate<Path> _function_1 = (Path f) -> {
        return f.getFileName().toString().startsWith("utplsql_");
      };
      final Predicate<Path> _function_2 = (Path f) -> {
        return f.getFileName().toString().endsWith(".html");
      };
      final ToLongFunction<Path> _function_3 = (Path f) -> {
        return f.toFile().lastModified();
      };
      final Optional<Path> last = Files.list(dir.toPath()).filter(_function).filter(_function_1).filter(_function_2).max(Comparator.<Path>comparingLong(_function_3));
      return last.get();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void produceReportAndCloseConnection() {
    try {
      SingleConnectionDataSource ds = new SingleConnectionDataSource();
      ds.setDriverClassName("oracle.jdbc.OracleDriver");
      ds.setUrl(AbstractJdbcTest.dataSource.getUrl());
      ds.setUsername(AbstractJdbcTest.dataSource.getUsername());
      ds.setPassword(AbstractJdbcTest.dataSource.getPassword());
      final Connection conn = ds.getConnection();
      final List<String> pathList = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(":test_f"));
      final List<String> includeObjectList = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("f"));
      final CodeCoverageReporter reporter = new CodeCoverageReporter(pathList, includeObjectList, conn);
      final Thread run = reporter.runAsync();
      run.join(20000);
      Assert.assertEquals(Boolean.valueOf(true), Boolean.valueOf(conn.isClosed()));
      final Path outputFile = this.getNewestOutputFile();
      Assert.assertTrue((outputFile != null));
      byte[] _readAllBytes = Files.readAllBytes(outputFile);
      final String content = new String(_readAllBytes, StandardCharsets.UTF_8);
      Assert.assertTrue(content.contains("<h3>SCOTT.F</h3><h4><span class=\"green\">100 %</span> lines covered</h4>"));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @AfterClass
  public static void teardown() {
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP PACKAGE test_f");
    } catch (final Throwable _t) {
      if (_t instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    try {
      AbstractJdbcTest.jdbcTemplate.execute("DROP FUNCTION f");
    } catch (final Throwable _t_1) {
      if (_t_1 instanceof BadSqlGrammarException) {
      } else {
        throw Exceptions.sneakyThrow(_t_1);
      }
    }
  }
}
