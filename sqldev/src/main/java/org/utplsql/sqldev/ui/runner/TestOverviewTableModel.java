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

import com.google.common.base.Objects;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.utplsql.sqldev.model.PrefixTools;
import org.utplsql.sqldev.model.runner.Test;
import org.utplsql.sqldev.resources.UtplsqlResources;

@SuppressWarnings("all")
public class TestOverviewTableModel extends DefaultTableModel {
  private LinkedHashMap<String, Test> tests;
  
  private String commonPrefix;
  
  private boolean commonPrefixCalculated;
  
  private boolean showDescription;
  
  private boolean useSmartTimes;
  
  public TestOverviewTableModel() {
    super();
  }
  
  private boolean calcCommonPrefix() {
    boolean _xifexpression = false;
    if ((((!this.commonPrefixCalculated) && (this.tests != null)) && (this.tests.size() > 0))) {
      boolean _xblockexpression = false;
      {
        this.commonPrefix = PrefixTools.commonPrefix(IterableExtensions.<String>toList(this.tests.keySet()));
        this.fireTableDataChanged();
        _xblockexpression = this.commonPrefixCalculated = true;
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  public void setModel(final LinkedHashMap<String, Test> tests, final boolean showDescription, final boolean useSmartTimes) {
    this.commonPrefixCalculated = false;
    this.tests = tests;
    this.showDescription = showDescription;
    this.useSmartTimes = useSmartTimes;
    this.calcCommonPrefix();
    this.fireTableDataChanged();
  }
  
  public void updateModel(final boolean showDescription) {
    this.showDescription = showDescription;
    this.fireTableDataChanged();
  }
  
  public CharSequence getTestIdColumnName() {
    CharSequence _xblockexpression = null;
    {
      this.calcCommonPrefix();
      CharSequence _xifexpression = null;
      if (((this.commonPrefix == null) || Objects.equal(this.commonPrefix, ""))) {
        String _xifexpression_1 = null;
        if (this.showDescription) {
          _xifexpression_1 = UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL");
        } else {
          _xifexpression_1 = UtplsqlResources.getString("RUNNER_TEST_ID_COLUMN");
        }
        _xifexpression = _xifexpression_1;
      } else {
        CharSequence _xifexpression_2 = null;
        if (this.showDescription) {
          StringConcatenation _builder = new StringConcatenation();
          String _string = UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL");
          _builder.append(_string);
          _builder.append(" (");
          _builder.append(this.commonPrefix);
          _builder.append(")");
          _xifexpression_2 = _builder;
        } else {
          _xifexpression_2 = this.commonPrefix;
        }
        _xifexpression = _xifexpression_2;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  public String getTimeColumnName() {
    StringConcatenation _builder = new StringConcatenation();
    String _string = UtplsqlResources.getString("RUNNER_TEST_EXECUTION_TIME_COLUMN");
    _builder.append(_string);
    {
      if ((!this.useSmartTimes)) {
        _builder.append(" [s]");
      }
    }
    final String timeColumnName = _builder.toString();
    return timeColumnName;
  }
  
  public Test getTest(final int row) {
    final Map.Entry<String, Test> entry = ((Map.Entry<String, Test>[])Conversions.unwrapArray(this.tests.entrySet(), Map.Entry.class))[row];
    final Test test = this.tests.get(entry.getKey());
    return test;
  }
  
  @Override
  public int getRowCount() {
    if ((this.tests == null)) {
      return 0;
    }
    return this.tests.size();
  }
  
  @Override
  public int getColumnCount() {
    return 5;
  }
  
  @Override
  public Object getValueAt(final int row, final int col) {
    final Test test = ((Map.Entry<String, Test>[])Conversions.unwrapArray(this.tests.entrySet(), Map.Entry.class))[row].getValue();
    if ((test == null)) {
      return null;
    }
    switch (col) {
      case 0:
        return test.getStatusIcon();
      case 1:
        return test.getWarningIcon();
      case 2:
        return test.getInfoIcon();
      case 3:
        String _xifexpression = null;
        if ((this.showDescription && (test.getDescription() != null))) {
          _xifexpression = test.getDescription();
        } else {
          String _id = test.getId();
          int _xifexpression_1 = (int) 0;
          if ((this.commonPrefix == null)) {
            _xifexpression_1 = 0;
          } else {
            _xifexpression_1 = this.commonPrefix.length();
          }
          _xifexpression = _id.substring(_xifexpression_1);
        }
        return _xifexpression;
      case 4:
        return test.getExecutionTime();
      default:
        return null;
    }
  }
  
  @Override
  public String getColumnName(final int col) {
    String _xifexpression = null;
    if (this.showDescription) {
      _xifexpression = "RUNNER_DESCRIPTION_LABEL";
    } else {
      _xifexpression = "RUNNER_TEST_ID_COLUMN";
    }
    String _string = UtplsqlResources.getString(_xifexpression);
    String _timeColumnName = this.getTimeColumnName();
    return Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("", "", "", _string, _timeColumnName)).get(col);
  }
  
  @Override
  public boolean isCellEditable(final int row, final int column) {
    return false;
  }
  
  @Override
  public Class<?> getColumnClass(final int col) {
    switch (col) {
      case 0:
        return Icon.class;
      case 1:
        return Icon.class;
      case 2:
        return Icon.class;
      case 3:
        return String.class;
      case 4:
        return Double.class;
      default:
        return String.class;
    }
  }
}
