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

import java.sql.Connection;
import java.util.Collections;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Test;
import org.utplsql.sqldev.coverage.CodeCoverageReporter;
import org.utplsql.sqldev.test.AbstractJdbcTest;
import org.utplsql.sqldev.ui.coverage.CodeCoverageReporterDialog;

@SuppressWarnings("all")
public class CodeCoverageReporterDialogTest extends AbstractJdbcTest {
  @Test
  public void layout() {
    try {
      Connection _connection = AbstractJdbcTest.dataSource.getConnection();
      final CodeCoverageReporter reporter = new CodeCoverageReporter(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("SCOTT")), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("a", "b", "c")), _connection);
      reporter.showParameterWindow();
      Thread.sleep((4 * 1000));
      CodeCoverageReporterDialog _frame = reporter.getFrame();
      if (_frame!=null) {
        _frame.exit();
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
