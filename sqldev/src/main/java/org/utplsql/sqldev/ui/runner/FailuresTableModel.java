/**
 * Copyright 2019 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
package org.utplsql.sqldev.ui.runner;

import java.util.Collections;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.utplsql.sqldev.model.runner.Expectation;
import org.utplsql.sqldev.resources.UtplsqlResources;

@SuppressWarnings("all")
public class FailuresTableModel extends DefaultTableModel {
  private List<Expectation> failedExpectations;
  
  public FailuresTableModel() {
    super();
  }
  
  public List<Expectation> setModel(final List<Expectation> failedExpectations) {
    return this.failedExpectations = failedExpectations;
  }
  
  public Expectation getExpectation(final int row) {
    return this.failedExpectations.get(row);
  }
  
  @Override
  public int getRowCount() {
    if ((this.failedExpectations == null)) {
      return 0;
    }
    return this.failedExpectations.size();
  }
  
  @Override
  public int getColumnCount() {
    return 2;
  }
  
  @Override
  public Object getValueAt(final int row, final int col) {
    final Expectation expectation = this.failedExpectations.get(row);
    if ((expectation == null)) {
      return null;
    }
    switch (col) {
      case 0:
        return Integer.valueOf((row + 1));
      case 1:
        return expectation.getShortFailureText();
      default:
        return null;
    }
  }
  
  @Override
  public String getColumnName(final int col) {
    String _string = UtplsqlResources.getString("RUNNER_ASSERT_DESCRIPTION_COLUMN");
    return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("#", _string)).get(col);
  }
  
  @Override
  public boolean isCellEditable(final int row, final int column) {
    return false;
  }
  
  @Override
  public Class<?> getColumnClass(final int col) {
    switch (col) {
      case 0:
        return Integer.class;
      case 1:
        return String.class;
      default:
        return String.class;
    }
  }
}
