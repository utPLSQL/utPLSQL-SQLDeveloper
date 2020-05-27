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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.RepaintManager;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import oracle.dbtools.raptor.controls.grid.DefaultDrillLink;
import oracle.dbtools.raptor.utils.Connections;
import oracle.ide.config.Preferences;
import oracle.javatools.ui.table.ToolbarButton;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.springframework.web.util.HtmlUtils;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.model.LimitedLinkedHashMap;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.model.runner.Counter;
import org.utplsql.sqldev.model.runner.Expectation;
import org.utplsql.sqldev.model.runner.Run;
import org.utplsql.sqldev.model.runner.Test;
import org.utplsql.sqldev.parser.UtplsqlParser;
import org.utplsql.sqldev.resources.UtplsqlResources;
import org.utplsql.sqldev.runner.UtplsqlRunner;
import org.utplsql.sqldev.runner.UtplsqlWorksheetRunner;
import org.utplsql.sqldev.ui.runner.ComboBoxItem;
import org.utplsql.sqldev.ui.runner.FailuresTableModel;
import org.utplsql.sqldev.ui.runner.GradientToolbar;
import org.utplsql.sqldev.ui.runner.RunnerTextArea;
import org.utplsql.sqldev.ui.runner.RunnerTextField;
import org.utplsql.sqldev.ui.runner.RunnerTextPane;
import org.utplsql.sqldev.ui.runner.ScrollablePanel;
import org.utplsql.sqldev.ui.runner.SmartTime;
import org.utplsql.sqldev.ui.runner.TestOverviewTableModel;
import org.utplsql.sqldev.ui.runner.WrapLayout;

@SuppressWarnings("all")
public class RunnerPanel implements ActionListener, MouseListener, HyperlinkListener {
  public static class TestOverviewRowListener implements ListSelectionListener {
    private RunnerPanel p;
    
    public TestOverviewRowListener(final RunnerPanel p) {
      this.p = p;
    }
    
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      final int rowIndex = this.p.testOverviewTable.getSelectedRow();
      if ((rowIndex != (-1))) {
        final int row = this.p.testOverviewTable.convertRowIndexToModel(rowIndex);
        final Test test = this.p.testOverviewTableModel.getTest(row);
        this.p.testOwnerTextField.setText(test.getOwnerName());
        this.p.testPackageTextField.setText(test.getObjectName());
        this.p.testProcedureTextField.setText(test.getProcedureName());
        String _description = test.getDescription();
        String _trim = null;
        if (_description!=null) {
          _trim=_description.trim();
        }
        this.p.testDescriptionTextArea.setText(_trim);
        this.p.testIdTextArea.setText(test.getId());
        this.p.testStartTextField.setText(RunnerPanel.formatDateTime(test.getStartTime()));
        this.p.failuresTableModel.setModel(test.getFailedExpectations());
        this.p.failuresTableModel.fireTableDataChanged();
        this.p.testFailureMessageTextPane.setText(null);
        if (((test.getFailedExpectations() != null) && (test.getFailedExpectations().size() > 0))) {
          this.p.failuresTable.setRowSelectionInterval(0, 0);
        }
        String _errorStack = test.getErrorStack();
        String _trim_1 = null;
        if (_errorStack!=null) {
          _trim_1=_errorStack.trim();
        }
        this.p.testErrorStackTextPane.setText(this.p.getHtml(_trim_1));
        String _warnings = test.getWarnings();
        String _trim_2 = null;
        if (_warnings!=null) {
          _trim_2=_warnings.trim();
        }
        this.p.testWarningsTextPane.setText(this.p.getHtml(_trim_2));
        String _serverOutput = test.getServerOutput();
        String _trim_3 = null;
        if (_serverOutput!=null) {
          _trim_3=_serverOutput.trim();
        }
        this.p.testServerOutputTextPane.setText(this.p.getHtml(_trim_3));
        this.p.syncDetailTab();
        this.p.testOverviewRunMenuItem.setEnabled(true);
        this.p.testOverviewRunWorksheetMenuItem.setEnabled(true);
      }
    }
  }
  
  public static class FailuresRowListener implements ListSelectionListener {
    private RunnerPanel p;
    
    public FailuresRowListener(final RunnerPanel p) {
      this.p = p;
    }
    
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      final int rowIndex = this.p.failuresTable.getSelectedRow();
      if ((rowIndex != (-1))) {
        final int row = this.p.failuresTable.convertRowIndexToModel(rowIndex);
        final Expectation expectation = this.p.failuresTableModel.getExpectation(row);
        final String html = this.p.getHtml(expectation.getFailureText());
        this.p.testFailureMessageTextPane.setText(html);
      }
    }
  }
  
  public static class TimeFormatRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int col) {
      final SmartTime smartTime = new SmartTime(((Double) value), RunnerPanel.useSmartTimes);
      return super.getTableCellRendererComponent(table, smartTime.toString(), isSelected, hasFocus, row, col);
    }
  }
  
  public static class TestTableHeaderRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int col) {
      final TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
      Component _tableCellRendererComponent = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
      final JLabel label = ((JLabel) _tableCellRendererComponent);
      if ((col == 0)) {
        label.setIcon(UtplsqlResources.getIcon("STATUS_ICON"));
        label.setHorizontalAlignment(JLabel.CENTER);
      } else {
        if ((col == 1)) {
          label.setIcon(UtplsqlResources.getIcon("WARNING_ICON"));
          label.setHorizontalAlignment(JLabel.CENTER);
        } else {
          if ((col == 2)) {
            label.setIcon(UtplsqlResources.getIcon("INFO_ICON"));
            label.setHorizontalAlignment(JLabel.CENTER);
          } else {
            if ((col == 3)) {
              label.setIcon(null);
              label.setHorizontalAlignment(JLabel.LEFT);
            } else {
              if ((col == 4)) {
                label.setIcon(null);
                label.setHorizontalAlignment(JLabel.RIGHT);
              }
            }
          }
        }
      }
      return label;
    }
  }
  
  public static class FailuresTableHeaderRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int col) {
      final TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
      Component _tableCellRendererComponent = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
      final JLabel label = ((JLabel) _tableCellRendererComponent);
      if ((col == 0)) {
        label.setHorizontalAlignment(JLabel.RIGHT);
      } else {
        label.setHorizontalAlignment(JLabel.LEFT);
      }
      return label;
    }
  }
  
  private static final Color GREEN = new Color(0, 153, 0);
  
  private static final Color RED = new Color(153, 0, 0);
  
  private static final int INDICATOR_WIDTH = 20;
  
  private static final int OVERVIEW_TABLE_ROW_HEIGHT = 20;
  
  private static final Dimension TEXTPANE_DIM = new Dimension(100, 100);
  
  private static boolean useSmartTimes;
  
  private LimitedLinkedHashMap<String, Run> runs = new LimitedLinkedHashMap<String, Run>(10);
  
  private Run currentRun;
  
  private JPanel basePanel;
  
  private ToolbarButton refreshButton;
  
  private ToolbarButton rerunButton;
  
  private ToolbarButton rerunWorksheetButton;
  
  private DefaultComboBoxModel<ComboBoxItem<String, String>> runComboBoxModel;
  
  private ToolbarButton clearButton;
  
  private JComboBox<ComboBoxItem<String, String>> runComboBox;
  
  private JLabel statusLabel;
  
  private JLabel elapsedTimeLabel;
  
  private Timer elapsedTimeTimer;
  
  private JLabel testCounterValueLabel;
  
  private JLabel errorCounterValueLabel;
  
  private JLabel failureCounterValueLabel;
  
  private JLabel disabledCounterValueLabel;
  
  private JLabel warningsCounterValueLabel;
  
  private JLabel infoCounterValueLabel;
  
  private JCheckBoxMenuItem showDisabledCounterCheckBoxMenuItem;
  
  private JCheckBoxMenuItem showWarningsCounterCheckBoxMenuItem;
  
  private JCheckBoxMenuItem showInfoCounterCheckBoxMenuItem;
  
  private JProgressBar progressBar;
  
  private TestOverviewTableModel testOverviewTableModel;
  
  private JTable testOverviewTable;
  
  private JMenuItem testOverviewRunMenuItem;
  
  private JMenuItem testOverviewRunWorksheetMenuItem;
  
  private JCheckBoxMenuItem showTestDescriptionCheckBoxMenuItem;
  
  private JCheckBoxMenuItem showWarningIndicatorCheckBoxMenuItem;
  
  private JCheckBoxMenuItem showInfoIndicatorCheckBoxMenuItem;
  
  private JCheckBoxMenuItem showSuccessfulTestsCheckBoxMenuItem;
  
  private JCheckBoxMenuItem showDisabledTestsCheckBoxMenuItem;
  
  private JCheckBoxMenuItem syncDetailTabCheckBoxMenuItem;
  
  private RunnerTextField testOwnerTextField;
  
  private RunnerTextField testPackageTextField;
  
  private RunnerTextField testProcedureTextField;
  
  private RunnerTextArea testDescriptionTextArea;
  
  private RunnerTextArea testIdTextArea;
  
  private RunnerTextField testStartTextField;
  
  private FailuresTableModel failuresTableModel;
  
  private JTable failuresTable;
  
  private RunnerTextPane testFailureMessageTextPane;
  
  private RunnerTextPane testErrorStackTextPane;
  
  private RunnerTextPane testWarningsTextPane;
  
  private RunnerTextPane testServerOutputTextPane;
  
  private JTabbedPane testDetailTabbedPane;
  
  public Component getGUI() {
    if ((this.basePanel == null)) {
      this.initializeGUI();
    }
    boolean _isShowing = this.basePanel.isShowing();
    boolean _not = (!_isShowing);
    if (_not) {
      this.applyPreferences();
    }
    return this.basePanel;
  }
  
  private void resetDerived() {
    RowSorter<? extends TableModel> _rowSorter = this.testOverviewTable.getRowSorter();
    _rowSorter.setSortKeys(null);
    this.testOverviewRunMenuItem.setEnabled(false);
    this.testOverviewRunWorksheetMenuItem.setEnabled(false);
    this.testIdTextArea.setText(null);
    this.testOwnerTextField.setText(null);
    this.testPackageTextField.setText(null);
    this.testProcedureTextField.setText(null);
    this.testDescriptionTextArea.setText(null);
    this.testStartTextField.setText(null);
    this.failuresTableModel.setModel(null);
    this.failuresTableModel.fireTableDataChanged();
    this.testFailureMessageTextPane.setText(null);
    this.testErrorStackTextPane.setText(null);
    this.testWarningsTextPane.setText(null);
    this.testServerOutputTextPane.setText(null);
  }
  
  private void refreshRunsComboBox() {
    int _size = this.runs.size();
    boolean _greaterThan = (_size > 0);
    if (_greaterThan) {
      this.runComboBox.removeActionListener(this);
      this.runComboBoxModel.removeAllElements();
      for (int i = (this.runs.size() - 1); (i >= 0); i--) {
        {
          final Map.Entry<String, Run> entry = ((Map.Entry<String, Run>[])Conversions.unwrapArray(this.runs.entrySet(), Map.Entry.class))[i];
          String _key = entry.getKey();
          String _name = entry.getValue().getName();
          final ComboBoxItem<String, String> item = new ComboBoxItem<String, String>(_key, _name);
          this.runComboBoxModel.addElement(item);
        }
      }
      this.runComboBox.setSelectedIndex(0);
      this.runComboBox.addActionListener(this);
    }
  }
  
  private LimitedLinkedHashMap<String, Run> applyShowNumberOfRunsInHistory(final int maxRuns) {
    LimitedLinkedHashMap<String, Run> _xifexpression = null;
    int _maxEntries = this.runs.getMaxEntries();
    boolean _notEquals = (maxRuns != _maxEntries);
    if (_notEquals) {
      LimitedLinkedHashMap<String, Run> _xblockexpression = null;
      {
        final LimitedLinkedHashMap<String, Run> newRuns = new LimitedLinkedHashMap<String, Run>(maxRuns);
        Set<Map.Entry<String, Run>> _entrySet = this.runs.entrySet();
        for (final Map.Entry<String, Run> entry : _entrySet) {
          newRuns.put(entry.getKey(), entry.getValue());
        }
        _xblockexpression = this.runs = newRuns;
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  private void applyShowDisabledCounter(final boolean show) {
    Container _parent = this.disabledCounterValueLabel.getParent();
    _parent.setVisible(this.showDisabledCounterCheckBoxMenuItem.isSelected());
  }
  
  private void applyShowWarningsCounter(final boolean show) {
    Container _parent = this.warningsCounterValueLabel.getParent();
    _parent.setVisible(this.showWarningsCounterCheckBoxMenuItem.isSelected());
  }
  
  private void applyShowInfoCounter(final boolean show) {
    Container _parent = this.infoCounterValueLabel.getParent();
    _parent.setVisible(this.showInfoCounterCheckBoxMenuItem.isSelected());
  }
  
  private void applyShowTestDescription(final boolean show) {
    this.testOverviewTableModel.updateModel(this.showTestDescriptionCheckBoxMenuItem.isSelected());
    final TableColumn idColumn = this.testOverviewTable.getColumnModel().getColumn(3);
    idColumn.setHeaderValue(this.testOverviewTableModel.getTestIdColumnName());
    this.testOverviewTable.getTableHeader().repaint();
  }
  
  private void applyShowWarningIndicator(final boolean show) {
    final TableColumn col = this.testOverviewTable.getColumnModel().getColumn(1);
    if (show) {
      col.setWidth(RunnerPanel.INDICATOR_WIDTH);
      col.setMinWidth(RunnerPanel.INDICATOR_WIDTH);
      col.setMaxWidth(RunnerPanel.INDICATOR_WIDTH);
      col.setPreferredWidth(RunnerPanel.INDICATOR_WIDTH);
    } else {
      col.setWidth(0);
      col.setMinWidth(0);
      col.setMaxWidth(0);
      col.setPreferredWidth(0);
    }
  }
  
  private void applyShowInfoIndicator(final boolean show) {
    final TableColumn col = this.testOverviewTable.getColumnModel().getColumn(2);
    if (show) {
      col.setWidth(RunnerPanel.INDICATOR_WIDTH);
      col.setMinWidth(RunnerPanel.INDICATOR_WIDTH);
      col.setMaxWidth(RunnerPanel.INDICATOR_WIDTH);
      col.setPreferredWidth(RunnerPanel.INDICATOR_WIDTH);
    } else {
      col.setWidth(0);
      col.setMinWidth(0);
      col.setMaxWidth(0);
      col.setPreferredWidth(0);
    }
  }
  
  private void applyFilter(final boolean showSuccessfulTests, final boolean showDisabledTests) {
    RowSorter<? extends TableModel> _rowSorter = this.testOverviewTable.getRowSorter();
    final TableRowSorter<TestOverviewTableModel> sorter = ((TableRowSorter<TestOverviewTableModel>) _rowSorter);
    final RowFilter<TestOverviewTableModel, Integer> filter = new RowFilter<TestOverviewTableModel, Integer>() {
      @Override
      public boolean include(final RowFilter.Entry<? extends TestOverviewTableModel, ? extends Integer> entry) {
        final Test test = entry.getModel().getTest((entry.getIdentifier()).intValue());
        final Counter counter = test.getCounter();
        if ((counter != null)) {
          Integer _success = counter.getSuccess();
          boolean _greaterThan = ((_success).intValue() > 0);
          if (_greaterThan) {
            if ((!showSuccessfulTests)) {
              return false;
            }
          }
          Integer _disabled = counter.getDisabled();
          boolean _greaterThan_1 = ((_disabled).intValue() > 0);
          if (_greaterThan_1) {
            if ((!showDisabledTests)) {
              return false;
            }
          }
        }
        return true;
      }
    };
    sorter.setRowFilter(filter);
  }
  
  private void openTest(final Test test) {
    try {
      Connection _connection = Connections.getInstance().getConnection(this.currentRun.getConnectionName());
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      final String source = dao.getSource(test.getOwnerName(), "PACKAGE", test.getObjectName().toUpperCase()).trim();
      final UtplsqlParser parser = new UtplsqlParser(source);
      final int line = parser.getLineOf(test.getProcedureName());
      this.openEditor(test.getOwnerName(), "PACKAGE", test.getObjectName().toUpperCase(), line, 1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private void openSelectedTest() {
    final int rowIndex = this.testOverviewTable.getSelectedRow();
    if ((rowIndex != (-1))) {
      final int row = this.testOverviewTable.convertRowIndexToModel(rowIndex);
      final Test test = this.testOverviewTableModel.getTest(row);
      this.openTest(test);
    }
  }
  
  private void openSelectedFailure() {
    final int rowIndex = this.failuresTable.getSelectedRow();
    if ((rowIndex != (-1))) {
      final int row = this.failuresTable.convertRowIndexToModel(rowIndex);
      final Expectation expectation = this.failuresTableModel.getExpectation(row);
      final Test test = this.testOverviewTableModel.getTest(this.testOverviewTable.convertRowIndexToModel(this.testOverviewTable.getSelectedRow()));
      final Integer callerLine = expectation.getCallerLine();
      if ((callerLine != null)) {
        this.openEditor(test.getOwnerName(), "PACKAGE BODY", test.getObjectName().toUpperCase(), (expectation.getCallerLine()).intValue(), 1);
      } else {
        this.openTest(test);
      }
    }
  }
  
  private String getHtml(final String text) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<html>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<head>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<style type=\"text/css\">");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("body, p {font-family: ");
    String _family = this.testOwnerTextField.getFont().getFamily();
    _builder.append(_family, "\t\t\t");
    _builder.append("; font-size: 1.0em; line-height: 1.1em; margin-top: 0px; margin-bottom: 0px;}");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("</style>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</head>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<body>");
    _builder.newLine();
    _builder.append("\t\t");
    String _linkedAndFormattedText = this.getLinkedAndFormattedText(text);
    _builder.append(_linkedAndFormattedText, "\t\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("</body>");
    _builder.newLine();
    _builder.append("</html>");
    _builder.newLine();
    final String html = _builder.toString();
    return html;
  }
  
  private void openLink(final String link) {
    try {
      final String[] parts = link.split("/");
      final String type = parts[0];
      final String ownerName = parts[1];
      final String objectName = parts[2];
      int line = Integer.parseInt(parts[3]);
      Connection _connection = Connections.getInstance().getConnection(this.currentRun.getConnectionName());
      final UtplsqlDao dao = new UtplsqlDao(_connection);
      String _xifexpression = null;
      boolean _equals = Objects.equal(type, "UNKNOWN");
      if (_equals) {
        _xifexpression = dao.getObjectType(ownerName, objectName);
      } else {
        _xifexpression = type;
      }
      final String objectType = _xifexpression;
      int _size = ((List<String>)Conversions.doWrapArray(parts)).size();
      boolean _equals_1 = (_size == 5);
      if (_equals_1) {
        final String procedureName = parts[4];
        final String source = dao.getSource(ownerName, objectType, objectName).trim();
        final UtplsqlParser parser = new UtplsqlParser(source);
        line = parser.getLineOf(procedureName);
      }
      this.openEditor(ownerName, objectType, objectName.toUpperCase(), line, 1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private void openEditor(final String owner, final String type, final String name, final int line, final int col) {
    DefaultDrillLink drillLink = new DefaultDrillLink();
    drillLink.setConnName(this.currentRun.getConnectionName());
    String _valueOf = String.valueOf(line);
    String _valueOf_1 = String.valueOf(col);
    drillLink.setArgs(new String[] { owner, type, name, _valueOf, _valueOf_1, "OpenEditor", "oracle.dbtools.raptor.controls.grid.DefaultDrillLink" });
    drillLink.performDrill();
  }
  
  private void syncDetailTab() {
    boolean _isSelected = this.syncDetailTabCheckBoxMenuItem.isSelected();
    if (_isSelected) {
      final int rowIndex = this.testOverviewTable.getSelectedRow();
      if ((rowIndex != (-1))) {
        final int row = this.testOverviewTable.convertRowIndexToModel(rowIndex);
        final Test test = this.testOverviewTableModel.getTest(row);
        int tabIndex = 0;
        boolean _and = false;
        Counter _counter = test.getCounter();
        Integer _failure = null;
        if (_counter!=null) {
          _failure=_counter.getFailure();
        }
        boolean _tripleNotEquals = (_failure != null);
        if (!_tripleNotEquals) {
          _and = false;
        } else {
          Integer _failure_1 = test.getCounter().getFailure();
          boolean _greaterThan = ((_failure_1).intValue() > 0);
          _and = _greaterThan;
        }
        if (_and) {
          tabIndex = 1;
        } else {
          boolean _and_1 = false;
          Counter _counter_1 = test.getCounter();
          Integer _error = null;
          if (_counter_1!=null) {
            _error=_counter_1.getError();
          }
          boolean _tripleNotEquals_1 = (_error != null);
          if (!_tripleNotEquals_1) {
            _and_1 = false;
          } else {
            Integer _error_1 = test.getCounter().getError();
            boolean _greaterThan_1 = ((_error_1).intValue() > 0);
            _and_1 = _greaterThan_1;
          }
          if (_and_1) {
            tabIndex = 2;
          } else {
            boolean _and_2 = false;
            Counter _counter_2 = test.getCounter();
            Integer _warning = null;
            if (_counter_2!=null) {
              _warning=_counter_2.getWarning();
            }
            boolean _tripleNotEquals_2 = (_warning != null);
            if (!_tripleNotEquals_2) {
              _and_2 = false;
            } else {
              Integer _warning_1 = test.getCounter().getWarning();
              boolean _greaterThan_2 = ((_warning_1).intValue() > 0);
              _and_2 = _greaterThan_2;
            }
            if (_and_2) {
              tabIndex = 3;
            } else {
              if (((test.getServerOutput() != null) && (test.getServerOutput().length() > 0))) {
                tabIndex = 4;
              } else {
                tabIndex = 0;
              }
            }
          }
        }
        this.testDetailTabbedPane.setSelectedIndex(tabIndex);
      }
    }
  }
  
  private PreferenceModel getPreferenceModel() {
    PreferenceModel preferences = null;
    try {
      preferences = PreferenceModel.getInstance(Preferences.getPreferences());
    } catch (final Throwable _t) {
      if (_t instanceof NoClassDefFoundError) {
        preferences = PreferenceModel.getInstance(null);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return preferences;
  }
  
  private boolean applyPreferences() {
    boolean _xblockexpression = false;
    {
      final PreferenceModel preferences = this.getPreferenceModel();
      this.applyShowNumberOfRunsInHistory(preferences.getNumberOfRunsInHistory());
      this.showDisabledCounterCheckBoxMenuItem.setSelected(preferences.isShowDisabledCounter());
      this.applyShowDisabledCounter(this.showDisabledCounterCheckBoxMenuItem.isSelected());
      this.fixCheckBoxMenuItem(this.showDisabledCounterCheckBoxMenuItem);
      this.showWarningsCounterCheckBoxMenuItem.setSelected(preferences.isShowWarningsCounter());
      this.applyShowWarningsCounter(this.showWarningsCounterCheckBoxMenuItem.isSelected());
      this.fixCheckBoxMenuItem(this.showWarningsCounterCheckBoxMenuItem);
      this.showInfoCounterCheckBoxMenuItem.setSelected(preferences.isShowInfoCounter());
      this.applyShowInfoCounter(this.showInfoCounterCheckBoxMenuItem.isSelected());
      this.fixCheckBoxMenuItem(this.showInfoCounterCheckBoxMenuItem);
      this.showTestDescriptionCheckBoxMenuItem.setSelected(preferences.isShowTestDescription());
      this.applyShowTestDescription(this.showTestDescriptionCheckBoxMenuItem.isSelected());
      this.fixCheckBoxMenuItem(this.showTestDescriptionCheckBoxMenuItem);
      this.showWarningIndicatorCheckBoxMenuItem.setSelected(preferences.isShowWarningIndicator());
      this.applyShowWarningIndicator(this.showWarningIndicatorCheckBoxMenuItem.isSelected());
      this.fixCheckBoxMenuItem(this.showWarningIndicatorCheckBoxMenuItem);
      this.showInfoIndicatorCheckBoxMenuItem.setSelected(preferences.isShowInfoIndicator());
      this.applyShowInfoIndicator(this.showInfoIndicatorCheckBoxMenuItem.isSelected());
      this.showSuccessfulTestsCheckBoxMenuItem.setSelected(preferences.isShowSuccessfulTests());
      this.fixCheckBoxMenuItem(this.showSuccessfulTestsCheckBoxMenuItem);
      this.showDisabledTestsCheckBoxMenuItem.setSelected(preferences.isShowDisabledTests());
      this.fixCheckBoxMenuItem(this.showDisabledTestsCheckBoxMenuItem);
      this.applyFilter(this.showSuccessfulTestsCheckBoxMenuItem.isSelected(), this.showDisabledTestsCheckBoxMenuItem.isSelected());
      this.fixCheckBoxMenuItem(this.showInfoIndicatorCheckBoxMenuItem);
      this.syncDetailTabCheckBoxMenuItem.setSelected(preferences.isSyncDetailTab());
      this.fixCheckBoxMenuItem(this.syncDetailTabCheckBoxMenuItem);
      _xblockexpression = RunnerPanel.useSmartTimes = preferences.isUseSmartTimes();
    }
    return _xblockexpression;
  }
  
  public void setModel(final Run run) {
    this.runs.put(run.getReporterId(), run);
    this.refreshRunsComboBox();
    this.setCurrentRun(run);
  }
  
  private void setCurrentRun(final Run run) {
    if ((run != this.currentRun)) {
      this.currentRun = run;
      this.testOverviewTableModel.setModel(run.getTests(), this.showTestDescriptionCheckBoxMenuItem.isSelected(), RunnerPanel.useSmartTimes);
      final String header = this.testOverviewTableModel.getTimeColumnName();
      final TableColumn timeColumn = this.testOverviewTable.getColumnModel().getColumn(4);
      Object _headerValue = timeColumn.getHeaderValue();
      boolean _notEquals = (!Objects.equal(_headerValue, header));
      if (_notEquals) {
        timeColumn.setHeaderValue(header);
        this.testOverviewTable.getTableHeader().repaint();
      }
      this.resetDerived();
      String _reporterId = this.currentRun.getReporterId();
      String _name = this.currentRun.getName();
      final ComboBoxItem<String, String> item = new ComboBoxItem<String, String>(_reporterId, _name);
      this.runComboBox.setSelectedItem(item);
      this.elapsedTimeTimer.start();
    }
  }
  
  public synchronized void update(final String reporterId) {
    try {
      this.setCurrentRun(this.runs.get(reporterId));
      Integer _currentTestNumber = this.currentRun.getCurrentTestNumber();
      final int row = ((_currentTestNumber).intValue() - 1);
      final CharSequence header = this.testOverviewTableModel.getTestIdColumnName();
      final TableColumn idColumn = this.testOverviewTable.getColumnModel().getColumn(3);
      Object _headerValue = idColumn.getHeaderValue();
      boolean _notEquals = (!Objects.equal(_headerValue, header));
      if (_notEquals) {
        idColumn.setHeaderValue(header);
        this.testOverviewTable.getTableHeader().repaint();
      }
      if ((row < 0)) {
        this.testOverviewTableModel.fireTableDataChanged();
      } else {
        int _rowCount = this.testOverviewTableModel.getRowCount();
        boolean _greaterThan = (_rowCount > row);
        if (_greaterThan) {
          final Rectangle positionOfCurrentTest = this.testOverviewTable.getCellRect(this.testOverviewTable.convertRowIndexToView(row), 0, true);
          this.testOverviewTable.scrollRectToVisible(positionOfCurrentTest);
          this.testOverviewTableModel.fireTableRowsUpdated(row, row);
          Thread.sleep(5);
          if (((!this.showSuccessfulTestsCheckBoxMenuItem.isSelected()) || (!this.showDisabledTestsCheckBoxMenuItem.isSelected()))) {
            this.applyFilter(this.showSuccessfulTestsCheckBoxMenuItem.isSelected(), this.showDisabledTestsCheckBoxMenuItem.isSelected());
          }
          this.testOverviewTable.scrollRectToVisible(positionOfCurrentTest);
        }
      }
      this.statusLabel.setText(this.currentRun.getStatus());
      StringConcatenation _builder = new StringConcatenation();
      int _totalNumberOfCompletedTests = this.currentRun.getTotalNumberOfCompletedTests();
      _builder.append(_totalNumberOfCompletedTests);
      {
        Integer _totalNumberOfTests = this.currentRun.getTotalNumberOfTests();
        boolean _greaterEqualsThan = ((_totalNumberOfTests).intValue() >= 0);
        if (_greaterEqualsThan) {
          _builder.append("/");
          Integer _totalNumberOfTests_1 = this.currentRun.getTotalNumberOfTests();
          _builder.append(_totalNumberOfTests_1);
        }
      }
      this.testCounterValueLabel.setText(_builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      Integer _error = this.currentRun.getCounter().getError();
      _builder_1.append(_error);
      this.errorCounterValueLabel.setText(_builder_1.toString());
      StringConcatenation _builder_2 = new StringConcatenation();
      Integer _failure = this.currentRun.getCounter().getFailure();
      _builder_2.append(_failure);
      this.failureCounterValueLabel.setText(_builder_2.toString());
      StringConcatenation _builder_3 = new StringConcatenation();
      Integer _disabled = this.currentRun.getCounter().getDisabled();
      _builder_3.append(_disabled);
      this.disabledCounterValueLabel.setText(_builder_3.toString());
      StringConcatenation _builder_4 = new StringConcatenation();
      Integer _warning = this.currentRun.getCounter().getWarning();
      _builder_4.append(_warning);
      this.warningsCounterValueLabel.setText(_builder_4.toString());
      StringConcatenation _builder_5 = new StringConcatenation();
      Integer _infoCount = this.currentRun.getInfoCount();
      _builder_5.append(_infoCount);
      this.infoCounterValueLabel.setText(_builder_5.toString());
      Integer _totalNumberOfTests_2 = this.currentRun.getTotalNumberOfTests();
      boolean _equals = ((_totalNumberOfTests_2).intValue() == 0);
      if (_equals) {
        this.progressBar.setValue(100);
      } else {
        int _totalNumberOfCompletedTests_1 = this.currentRun.getTotalNumberOfCompletedTests();
        int _multiply = (100 * _totalNumberOfCompletedTests_1);
        Integer _totalNumberOfTests_3 = this.currentRun.getTotalNumberOfTests();
        int _divide = (_multiply / (_totalNumberOfTests_3).intValue());
        this.progressBar.setValue(Math.round(_divide));
      }
      if ((((this.currentRun.getCounter().getError()).intValue() > 0) || ((this.currentRun.getCounter().getFailure()).intValue() > 0))) {
        this.progressBar.setForeground(RunnerPanel.RED);
      } else {
        this.progressBar.setForeground(RunnerPanel.GREEN);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private ArrayList<String> getPathListFromSelectedTests() {
    final ArrayList<String> pathList = new ArrayList<String>();
    int[] _selectedRows = this.testOverviewTable.getSelectedRows();
    for (final int rowIndex : _selectedRows) {
      {
        final int row = this.testOverviewTable.convertRowIndexToModel(rowIndex);
        final Test test = this.testOverviewTableModel.getTest(row);
        StringConcatenation _builder = new StringConcatenation();
        String _ownerName = test.getOwnerName();
        _builder.append(_ownerName);
        _builder.append(".");
        String _objectName = test.getObjectName();
        _builder.append(_objectName);
        _builder.append(".");
        String _procedureName = test.getProcedureName();
        _builder.append(_procedureName);
        final String path = _builder.toString();
        pathList.add(path);
      }
    }
    return pathList;
  }
  
  private boolean isWindowsLookAndFeel() {
    LookAndFeel _lookAndFeel = UIManager.getLookAndFeel();
    String _name = null;
    if (_lookAndFeel!=null) {
      _name=_lookAndFeel.getName();
    }
    final String laf = _name;
    boolean _equals = Objects.equal(laf, "Windows");
    if (_equals) {
      return true;
    } else {
      return false;
    }
  }
  
  private boolean isMacLookAndFeel() {
    LookAndFeel _lookAndFeel = UIManager.getLookAndFeel();
    String _name = null;
    if (_lookAndFeel!=null) {
      _name=_lookAndFeel.getName();
    }
    final String laf = _name;
    boolean _equals = Objects.equal(laf, "Mac OS X");
    if (_equals) {
      return true;
    } else {
      return false;
    }
  }
  
  private void fixCheckBoxMenuItem(final JCheckBoxMenuItem item) {
    boolean _isWindowsLookAndFeel = this.isWindowsLookAndFeel();
    if (_isWindowsLookAndFeel) {
      boolean _isSelected = item.isSelected();
      if (_isSelected) {
        item.setIcon(UtplsqlResources.getIcon("CHECKMARK_ICON"));
      } else {
        item.setIcon(null);
      }
    }
  }
  
  @Override
  public void actionPerformed(final ActionEvent e) {
    Object _source = e.getSource();
    boolean _equals = Objects.equal(_source, this.refreshButton);
    if (_equals) {
      this.resetDerived();
      this.testDetailTabbedPane.setSelectedIndex(0);
      this.testOverviewTableModel.fireTableDataChanged();
    } else {
      Object _source_1 = e.getSource();
      boolean _equals_1 = Objects.equal(_source_1, this.rerunButton);
      if (_equals_1) {
        List<String> _pathList = this.currentRun.getPathList();
        String _connectionName = this.currentRun.getConnectionName();
        final UtplsqlRunner runner = new UtplsqlRunner(_pathList, _connectionName);
        runner.runTestAsync();
      } else {
        Object _source_2 = e.getSource();
        boolean _equals_2 = Objects.equal(_source_2, this.rerunWorksheetButton);
        if (_equals_2) {
          List<String> _pathList_1 = this.currentRun.getPathList();
          String _connectionName_1 = this.currentRun.getConnectionName();
          final UtplsqlWorksheetRunner worksheet = new UtplsqlWorksheetRunner(_pathList_1, _connectionName_1);
          worksheet.runTestAsync();
        } else {
          Object _source_3 = e.getSource();
          boolean _equals_3 = Objects.equal(_source_3, this.runComboBox);
          if (_equals_3) {
            if ((this.currentRun != null)) {
              Object _selectedItem = this.runComboBox.getSelectedItem();
              final ComboBoxItem<String, String> comboBoxItem = ((ComboBoxItem<String, String>) _selectedItem);
              String _reporterId = this.currentRun.getReporterId();
              String _key = comboBoxItem.getKey();
              boolean _notEquals = (!Objects.equal(_reporterId, _key));
              if (_notEquals) {
                this.update(comboBoxItem.getKey());
                this.testDetailTabbedPane.setSelectedIndex(0);
              }
            }
          } else {
            Object _source_4 = e.getSource();
            boolean _equals_4 = Objects.equal(_source_4, this.clearButton);
            if (_equals_4) {
              final Run run = this.currentRun;
              this.runs.clear();
              this.currentRun = null;
              this.setModel(run);
              this.update(run.getReporterId());
            } else {
              Object _source_5 = e.getSource();
              boolean _equals_5 = Objects.equal(_source_5, this.testOverviewRunMenuItem);
              if (_equals_5) {
                ArrayList<String> _pathListFromSelectedTests = this.getPathListFromSelectedTests();
                String _connectionName_2 = this.currentRun.getConnectionName();
                final UtplsqlRunner runner_1 = new UtplsqlRunner(_pathListFromSelectedTests, _connectionName_2);
                runner_1.runTestAsync();
              } else {
                Object _source_6 = e.getSource();
                boolean _equals_6 = Objects.equal(_source_6, this.testOverviewRunWorksheetMenuItem);
                if (_equals_6) {
                  ArrayList<String> _pathListFromSelectedTests_1 = this.getPathListFromSelectedTests();
                  String _connectionName_3 = this.currentRun.getConnectionName();
                  final UtplsqlWorksheetRunner worksheet_1 = new UtplsqlWorksheetRunner(_pathListFromSelectedTests_1, _connectionName_3);
                  worksheet_1.runTestAsync();
                } else {
                  Object _source_7 = e.getSource();
                  boolean _equals_7 = Objects.equal(_source_7, this.showDisabledCounterCheckBoxMenuItem);
                  if (_equals_7) {
                    this.applyShowDisabledCounter(this.showDisabledCounterCheckBoxMenuItem.isSelected());
                    this.fixCheckBoxMenuItem(this.showDisabledCounterCheckBoxMenuItem);
                  } else {
                    Object _source_8 = e.getSource();
                    boolean _equals_8 = Objects.equal(_source_8, this.showWarningsCounterCheckBoxMenuItem);
                    if (_equals_8) {
                      this.applyShowWarningsCounter(this.showWarningsCounterCheckBoxMenuItem.isSelected());
                      this.fixCheckBoxMenuItem(this.showWarningsCounterCheckBoxMenuItem);
                    } else {
                      Object _source_9 = e.getSource();
                      boolean _equals_9 = Objects.equal(_source_9, this.showInfoCounterCheckBoxMenuItem);
                      if (_equals_9) {
                        this.applyShowInfoCounter(this.showInfoCounterCheckBoxMenuItem.isSelected());
                        this.fixCheckBoxMenuItem(this.showInfoCounterCheckBoxMenuItem);
                      } else {
                        Object _source_10 = e.getSource();
                        boolean _equals_10 = Objects.equal(_source_10, this.showSuccessfulTestsCheckBoxMenuItem);
                        if (_equals_10) {
                          this.applyFilter(this.showSuccessfulTestsCheckBoxMenuItem.isSelected(), this.showDisabledTestsCheckBoxMenuItem.isSelected());
                          this.fixCheckBoxMenuItem(this.showSuccessfulTestsCheckBoxMenuItem);
                        } else {
                          Object _source_11 = e.getSource();
                          boolean _equals_11 = Objects.equal(_source_11, this.showDisabledTestsCheckBoxMenuItem);
                          if (_equals_11) {
                            this.applyFilter(this.showSuccessfulTestsCheckBoxMenuItem.isSelected(), this.showDisabledTestsCheckBoxMenuItem.isSelected());
                            this.fixCheckBoxMenuItem(this.showDisabledTestsCheckBoxMenuItem);
                          } else {
                            Object _source_12 = e.getSource();
                            boolean _equals_12 = Objects.equal(_source_12, this.showTestDescriptionCheckBoxMenuItem);
                            if (_equals_12) {
                              this.applyShowTestDescription(this.showTestDescriptionCheckBoxMenuItem.isSelected());
                              this.fixCheckBoxMenuItem(this.showTestDescriptionCheckBoxMenuItem);
                            } else {
                              Object _source_13 = e.getSource();
                              boolean _equals_13 = Objects.equal(_source_13, this.showWarningIndicatorCheckBoxMenuItem);
                              if (_equals_13) {
                                this.applyShowWarningIndicator(this.showWarningIndicatorCheckBoxMenuItem.isSelected());
                                this.fixCheckBoxMenuItem(this.showWarningIndicatorCheckBoxMenuItem);
                              } else {
                                Object _source_14 = e.getSource();
                                boolean _equals_14 = Objects.equal(_source_14, this.showInfoIndicatorCheckBoxMenuItem);
                                if (_equals_14) {
                                  this.applyShowInfoIndicator(this.showInfoIndicatorCheckBoxMenuItem.isSelected());
                                  this.fixCheckBoxMenuItem(this.showInfoIndicatorCheckBoxMenuItem);
                                } else {
                                  Object _source_15 = e.getSource();
                                  boolean _equals_15 = Objects.equal(_source_15, this.syncDetailTabCheckBoxMenuItem);
                                  if (_equals_15) {
                                    this.syncDetailTab();
                                    this.fixCheckBoxMenuItem(this.syncDetailTabCheckBoxMenuItem);
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  @Override
  public void mouseClicked(final MouseEvent e) {
    int _clickCount = e.getClickCount();
    boolean _equals = (_clickCount == 2);
    if (_equals) {
      Object _source = e.getSource();
      boolean _equals_1 = Objects.equal(_source, this.testOverviewTable);
      if (_equals_1) {
        int _selectedRowCount = this.failuresTable.getSelectedRowCount();
        boolean _equals_2 = (_selectedRowCount == 1);
        if (_equals_2) {
          this.openSelectedFailure();
        } else {
          this.openSelectedTest();
        }
      } else {
        Object _source_1 = e.getSource();
        boolean _equals_3 = Objects.equal(_source_1, this.failuresTable);
        if (_equals_3) {
          int _selectedRowCount_1 = this.failuresTable.getSelectedRowCount();
          boolean _equals_4 = (_selectedRowCount_1 == 1);
          if (_equals_4) {
            this.openSelectedFailure();
          }
        }
      }
    }
  }
  
  @Override
  public void mouseEntered(final MouseEvent e) {
  }
  
  @Override
  public void mouseExited(final MouseEvent e) {
  }
  
  @Override
  public void mousePressed(final MouseEvent e) {
  }
  
  @Override
  public void mouseReleased(final MouseEvent e) {
  }
  
  @Override
  public void hyperlinkUpdate(final HyperlinkEvent e) {
    HyperlinkEvent.EventType _eventType = e.getEventType();
    boolean _equals = Objects.equal(_eventType, HyperlinkEvent.EventType.ACTIVATED);
    if (_equals) {
      final String link = e.getDescription();
      this.openLink(link);
    }
  }
  
  private static String formatDateTime(final String dateTime) {
    if ((dateTime == null)) {
      return null;
    } else {
      int _length = dateTime.length();
      boolean _equals = (_length == 26);
      if (_equals) {
        return dateTime.replace("T", " ").substring(0, 23);
      } else {
        return dateTime;
      }
    }
  }
  
  private String getLinkedAndFormattedText(final String text) {
    if ((text == null)) {
      return "";
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("\\s+(package\\s+)?(&quot;(\\S+?)\\.(\\S+?)(?:\\.(\\S+?))?&quot;,\\s+line\\s+([0-9]+))");
    final Pattern p1 = Pattern.compile(_builder.toString());
    String localText = HtmlUtils.htmlEscape(text);
    Matcher m = p1.matcher(localText);
    while (m.find()) {
      {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("<a href=\"");
        {
          String _group = m.group(1);
          boolean _tripleNotEquals = (_group != null);
          if (_tripleNotEquals) {
            _builder_1.append("PACKAGE");
          } else {
            _builder_1.append("UNKNOWN");
          }
        }
        _builder_1.append("/");
        String _group_1 = m.group(3);
        _builder_1.append(_group_1);
        _builder_1.append("/");
        String _group_2 = m.group(4);
        _builder_1.append(_group_2);
        _builder_1.append("/");
        String _group_3 = m.group(6);
        _builder_1.append(_group_3);
        _builder_1.append("\">");
        String _group_4 = m.group(2);
        _builder_1.append(_group_4);
        _builder_1.append("</a>");
        final String link = _builder_1.toString();
        final int start = m.start(2);
        final int end = m.end(2);
        StringConcatenation _builder_2 = new StringConcatenation();
        String _substring = localText.substring(0, start);
        _builder_2.append(_substring);
        _builder_2.append(link);
        String _substring_1 = localText.substring(end);
        _builder_2.append(_substring_1);
        localText = _builder_2.toString();
        m = p1.matcher(localText);
      }
    }
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("^\\s{2}((\\S+?)\\.(\\S+?)\\.(\\S+?))$");
    final Pattern p2 = Pattern.compile(_builder_1.toString(), Pattern.MULTILINE);
    m = p2.matcher(localText);
    while (m.find()) {
      {
        StringConcatenation _builder_2 = new StringConcatenation();
        _builder_2.append("&nbsp;&nbsp;<a href=\"UNKNOWN/");
        String _upperCase = m.group(2).toUpperCase();
        _builder_2.append(_upperCase);
        _builder_2.append("/");
        String _upperCase_1 = m.group(3).toUpperCase();
        _builder_2.append(_upperCase_1);
        _builder_2.append("/1/");
        String _upperCase_2 = m.group(4).toUpperCase();
        _builder_2.append(_upperCase_2);
        _builder_2.append("\">");
        String _group = m.group(1);
        _builder_2.append(_group);
        _builder_2.append("</a>");
        final String link = _builder_2.toString();
        final int start = m.start(0);
        final int end = m.end(0);
        StringConcatenation _builder_3 = new StringConcatenation();
        String _substring = localText.substring(0, start);
        _builder_3.append(_substring);
        _builder_3.append(link);
        String _substring_1 = localText.substring(end);
        _builder_3.append(_substring_1);
        localText = _builder_3.toString();
        m = p2.matcher(localText);
      }
    }
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("^For suite ([^:]+):$");
    final Pattern p3 = Pattern.compile(_builder_2.toString(), Pattern.MULTILINE);
    m = p3.matcher(localText);
    while (m.find()) {
      {
        StringConcatenation _builder_3 = new StringConcatenation();
        _builder_3.append("<font size=\"4\"><b>For suite \"");
        String _group = m.group(1);
        _builder_3.append(_group);
        _builder_3.append("\"</b></font>");
        final String title = _builder_3.toString();
        final int start = m.start(0);
        final int end = m.end(0);
        StringConcatenation _builder_4 = new StringConcatenation();
        String _substring = localText.substring(0, start);
        _builder_4.append(_substring);
        _builder_4.append(title);
        String _substring_1 = localText.substring(end);
        _builder_4.append(_substring_1);
        localText = _builder_4.toString();
        m = p3.matcher(localText);
      }
    }
    StringConcatenation _builder_3 = new StringConcatenation();
    {
      String[] _split = localText.split("\n");
      for(final String p : _split) {
        _builder_3.append("<p>");
        _builder_3.append(p);
        _builder_3.append("</p>");
        _builder_3.newLineIfNotEmpty();
      }
    }
    final String result = _builder_3.toString();
    return result;
  }
  
  private JPanel makeLabelledCounterComponent(final JLabel label, final JComponent comp) {
    final JPanel groupPanel = new JPanel();
    GridBagLayout _gridBagLayout = new GridBagLayout();
    groupPanel.setLayout(_gridBagLayout);
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets = new Insets(5, 10, 5, 0);
    c.insets = _insets;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    groupPanel.add(label, c);
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_1 = new Insets(5, 5, 5, 10);
    c.insets = _insets_1;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    groupPanel.add(comp, c);
    final Dimension dim = new Dimension(134, 24);
    groupPanel.setMinimumSize(dim);
    groupPanel.setPreferredSize(dim);
    return groupPanel;
  }
  
  private void initializeGUI() {
    JPanel _jPanel = new JPanel();
    this.basePanel = _jPanel;
    GridBagLayout _gridBagLayout = new GridBagLayout();
    this.basePanel.setLayout(_gridBagLayout);
    GridBagConstraints c = new GridBagConstraints();
    GradientToolbar toolbar = new GradientToolbar();
    toolbar.setFloatable(false);
    Insets _insets = new Insets(2, 4, 2, 4);
    final EmptyBorder buttonBorder = new EmptyBorder(_insets);
    Icon _icon = UtplsqlResources.getIcon("REFRESH_ICON");
    ToolbarButton _toolbarButton = new ToolbarButton(_icon);
    this.refreshButton = _toolbarButton;
    this.refreshButton.setToolTipText(UtplsqlResources.getString("RUNNER_REFRESH_TOOLTIP"));
    this.refreshButton.setBorder(buttonBorder);
    this.refreshButton.addActionListener(this);
    toolbar.add(this.refreshButton);
    Icon _icon_1 = UtplsqlResources.getIcon("RUN_ICON");
    ToolbarButton _toolbarButton_1 = new ToolbarButton(_icon_1);
    this.rerunButton = _toolbarButton_1;
    this.rerunButton.setToolTipText(UtplsqlResources.getString("RUNNER_RERUN_TOOLTIP"));
    this.rerunButton.setBorder(buttonBorder);
    this.rerunButton.addActionListener(this);
    toolbar.add(this.rerunButton);
    Icon _icon_2 = UtplsqlResources.getIcon("RUN_WORKSHEET_ICON");
    ToolbarButton _toolbarButton_2 = new ToolbarButton(_icon_2);
    this.rerunWorksheetButton = _toolbarButton_2;
    this.rerunWorksheetButton.setToolTipText(UtplsqlResources.getString("RUNNER_RERUN_WORKSHEET_TOOLTIP"));
    this.rerunWorksheetButton.setBorder(buttonBorder);
    this.rerunWorksheetButton.addActionListener(this);
    toolbar.add(this.rerunWorksheetButton);
    toolbar.add(Box.createHorizontalGlue());
    DefaultComboBoxModel<ComboBoxItem<String, String>> _defaultComboBoxModel = new DefaultComboBoxModel<ComboBoxItem<String, String>>();
    this.runComboBoxModel = _defaultComboBoxModel;
    JComboBox<ComboBoxItem<String, String>> _jComboBox = new JComboBox<ComboBoxItem<String, String>>(this.runComboBoxModel);
    this.runComboBox = _jComboBox;
    this.runComboBox.setEditable(false);
    final Dimension comboBoxDim = new Dimension(500, 50);
    this.runComboBox.setMaximumSize(comboBoxDim);
    this.runComboBox.addActionListener(this);
    toolbar.add(this.runComboBox);
    Icon _icon_3 = UtplsqlResources.getIcon("CLEAR_ICON");
    ToolbarButton _toolbarButton_3 = new ToolbarButton(_icon_3);
    this.clearButton = _toolbarButton_3;
    this.clearButton.setToolTipText(UtplsqlResources.getString("RUNNER_CLEAR_BUTTON"));
    this.clearButton.setBorder(buttonBorder);
    this.clearButton.addActionListener(this);
    toolbar.add(this.clearButton);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.gridheight = 1;
    Insets _insets_1 = new Insets(0, 0, 0, 0);
    c.insets = _insets_1;
    c.anchor = GridBagConstraints.NORTH;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    this.basePanel.add(toolbar, c);
    JLabel _jLabel = new JLabel();
    this.statusLabel = _jLabel;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_2 = new Insets(10, 10, 10, 0);
    c.insets = _insets_2;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    this.basePanel.add(this.statusLabel, c);
    JLabel _jLabel_1 = new JLabel();
    this.elapsedTimeLabel = _jLabel_1;
    Dimension _dimension = new Dimension(60, 0);
    this.elapsedTimeLabel.setPreferredSize(_dimension);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_3 = new Insets(10, 10, 10, 10);
    c.insets = _insets_3;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    this.basePanel.add(this.elapsedTimeLabel, c);
    Timer _timer = new Timer(100, new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (((RunnerPanel.this.currentRun != null) && (RunnerPanel.this.currentRun.getStart() != null))) {
          final SmartTime time = new SmartTime();
          time.setSmart(RunnerPanel.useSmartTimes);
          Double _executionTime = RunnerPanel.this.currentRun.getExecutionTime();
          boolean _tripleNotEquals = (_executionTime != null);
          if (_tripleNotEquals) {
            time.setSeconds(RunnerPanel.this.currentRun.getExecutionTime());
            RunnerPanel.this.elapsedTimeTimer.stop();
          } else {
            final long now = System.currentTimeMillis();
            Long _start = RunnerPanel.this.currentRun.getStart();
            long _minus = (now - (_start).longValue());
            Double _double = new Double(_minus);
            double _divide = ((_double).doubleValue() / 1000);
            time.setSeconds(Double.valueOf(_divide));
          }
          StringConcatenation _builder = new StringConcatenation();
          String _string = time.toString();
          _builder.append(_string);
          {
            if ((!RunnerPanel.useSmartTimes)) {
              _builder.append(" s");
            }
          }
          RunnerPanel.this.elapsedTimeLabel.setText(_builder.toString());
        } else {
          RunnerPanel.this.elapsedTimeLabel.setText(null);
        }
      }
    });
    this.elapsedTimeTimer = _timer;
    final JPanel counterPanel = new JPanel();
    WrapLayout _wrapLayout = new WrapLayout(FlowLayout.LEFT, 0, 0);
    counterPanel.setLayout(_wrapLayout);
    String _string = UtplsqlResources.getString("RUNNER_TESTS_LABEL");
    String _plus = (_string + ":");
    final JLabel testCounterLabel = new JLabel(_plus, JLabel.LEADING);
    JLabel _jLabel_2 = new JLabel();
    this.testCounterValueLabel = _jLabel_2;
    counterPanel.add(this.makeLabelledCounterComponent(testCounterLabel, this.testCounterValueLabel));
    String _string_1 = UtplsqlResources.getString("RUNNER_FAILURES_LABEL");
    String _plus_1 = (_string_1 + ":");
    Icon _icon_4 = UtplsqlResources.getIcon("FAILURE_ICON");
    final JLabel failureCounterLabel = new JLabel(_plus_1, _icon_4, JLabel.LEADING);
    JLabel _jLabel_3 = new JLabel();
    this.failureCounterValueLabel = _jLabel_3;
    counterPanel.add(this.makeLabelledCounterComponent(failureCounterLabel, this.failureCounterValueLabel));
    String _string_2 = UtplsqlResources.getString("RUNNER_ERRORS_LABEL");
    String _plus_2 = (_string_2 + ":");
    Icon _icon_5 = UtplsqlResources.getIcon("ERROR_ICON");
    final JLabel errorCounterLabel = new JLabel(_plus_2, _icon_5, JLabel.LEADING);
    JLabel _jLabel_4 = new JLabel();
    this.errorCounterValueLabel = _jLabel_4;
    counterPanel.add(this.makeLabelledCounterComponent(errorCounterLabel, this.errorCounterValueLabel));
    String _string_3 = UtplsqlResources.getString("RUNNER_DISABLED_LABEL");
    String _plus_3 = (_string_3 + ":");
    Icon _icon_6 = UtplsqlResources.getIcon("DISABLED_ICON");
    final JLabel disabledCounterLabel = new JLabel(_plus_3, _icon_6, JLabel.LEADING);
    JLabel _jLabel_5 = new JLabel();
    this.disabledCounterValueLabel = _jLabel_5;
    counterPanel.add(this.makeLabelledCounterComponent(disabledCounterLabel, this.disabledCounterValueLabel));
    String _string_4 = UtplsqlResources.getString("RUNNER_WARNINGS_LABEL");
    String _plus_4 = (_string_4 + ":");
    Icon _icon_7 = UtplsqlResources.getIcon("WARNING_ICON");
    final JLabel warningsCounterLabel = new JLabel(_plus_4, _icon_7, JLabel.LEADING);
    JLabel _jLabel_6 = new JLabel();
    this.warningsCounterValueLabel = _jLabel_6;
    counterPanel.add(this.makeLabelledCounterComponent(warningsCounterLabel, this.warningsCounterValueLabel));
    String _string_5 = UtplsqlResources.getString("RUNNER_INFO_LABEL");
    String _plus_5 = (_string_5 + ":");
    Icon _icon_8 = UtplsqlResources.getIcon("INFO_ICON");
    final JLabel infoCounterLabel = new JLabel(_plus_5, _icon_8, JLabel.LEADING);
    JLabel _jLabel_7 = new JLabel();
    this.infoCounterValueLabel = _jLabel_7;
    counterPanel.add(this.makeLabelledCounterComponent(infoCounterLabel, this.infoCounterValueLabel));
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 2;
    c.gridheight = 1;
    Insets _insets_4 = new Insets(5, 0, 5, 0);
    c.insets = _insets_4;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    this.basePanel.add(counterPanel, c);
    final JPopupMenu countersPopupMenu = new JPopupMenu();
    String _replace = UtplsqlResources.getString("PREF_SHOW_DISABLED_COUNTER_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem = new JCheckBoxMenuItem(_replace, true);
    this.showDisabledCounterCheckBoxMenuItem = _jCheckBoxMenuItem;
    this.showDisabledCounterCheckBoxMenuItem.addActionListener(this);
    countersPopupMenu.add(this.showDisabledCounterCheckBoxMenuItem);
    String _replace_1 = UtplsqlResources.getString("PREF_SHOW_WARNINGS_COUNTER_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_1 = new JCheckBoxMenuItem(_replace_1, true);
    this.showWarningsCounterCheckBoxMenuItem = _jCheckBoxMenuItem_1;
    this.showWarningsCounterCheckBoxMenuItem.addActionListener(this);
    countersPopupMenu.add(this.showWarningsCounterCheckBoxMenuItem);
    String _replace_2 = UtplsqlResources.getString("PREF_SHOW_INFO_COUNTER_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_2 = new JCheckBoxMenuItem(_replace_2, true);
    this.showInfoCounterCheckBoxMenuItem = _jCheckBoxMenuItem_2;
    this.showInfoCounterCheckBoxMenuItem.addActionListener(this);
    countersPopupMenu.add(this.showInfoCounterCheckBoxMenuItem);
    counterPanel.setComponentPopupMenu(countersPopupMenu);
    JProgressBar _jProgressBar = new JProgressBar();
    this.progressBar = _jProgressBar;
    final Dimension progressBarDim = new Dimension(10, 20);
    this.progressBar.setPreferredSize(progressBarDim);
    this.progressBar.setMinimumSize(progressBarDim);
    this.progressBar.setStringPainted(false);
    this.progressBar.setForeground(RunnerPanel.GREEN);
    BasicProgressBarUI _basicProgressBarUI = new BasicProgressBarUI();
    this.progressBar.setUI(_basicProgressBarUI);
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 2;
    c.gridheight = 1;
    Insets _insets_5 = new Insets(10, 10, 10, 10);
    c.insets = _insets_5;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    this.basePanel.add(this.progressBar, c);
    TestOverviewTableModel _testOverviewTableModel = new TestOverviewTableModel();
    this.testOverviewTableModel = _testOverviewTableModel;
    JTable _jTable = new JTable(this.testOverviewTableModel);
    this.testOverviewTable = _jTable;
    JTableHeader _tableHeader = this.testOverviewTable.getTableHeader();
    _tableHeader.setReorderingAllowed(false);
    this.testOverviewTable.setAutoCreateRowSorter(true);
    this.testOverviewTable.setRowHeight(RunnerPanel.OVERVIEW_TABLE_ROW_HEIGHT);
    JTableHeader _tableHeader_1 = this.testOverviewTable.getTableHeader();
    Dimension _dimension_1 = new Dimension(this.testOverviewTable.getTableHeader().getPreferredSize().width, RunnerPanel.OVERVIEW_TABLE_ROW_HEIGHT);
    _tableHeader_1.setPreferredSize(_dimension_1);
    ListSelectionModel _selectionModel = this.testOverviewTable.getSelectionModel();
    RunnerPanel.TestOverviewRowListener _testOverviewRowListener = new RunnerPanel.TestOverviewRowListener(this);
    _selectionModel.addListSelectionListener(_testOverviewRowListener);
    this.testOverviewTable.addMouseListener(this);
    RepaintManager _currentManager = RepaintManager.currentManager(this.testOverviewTable);
    _currentManager.setDoubleBufferingEnabled(true);
    final RunnerPanel.TestTableHeaderRenderer testTableHeaderRenderer = new RunnerPanel.TestTableHeaderRenderer();
    final TableColumn overviewTableStatus = this.testOverviewTable.getColumnModel().getColumn(0);
    overviewTableStatus.setMinWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableStatus.setPreferredWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableStatus.setMaxWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableStatus.setHeaderRenderer(testTableHeaderRenderer);
    final TableColumn overviewTableWarning = this.testOverviewTable.getColumnModel().getColumn(1);
    overviewTableWarning.setMinWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableWarning.setPreferredWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableWarning.setMaxWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableWarning.setHeaderRenderer(testTableHeaderRenderer);
    final TableColumn overviewTableInfo = this.testOverviewTable.getColumnModel().getColumn(2);
    overviewTableInfo.setMinWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableInfo.setPreferredWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableInfo.setMaxWidth(RunnerPanel.INDICATOR_WIDTH);
    overviewTableInfo.setHeaderRenderer(testTableHeaderRenderer);
    final TableColumn overviewTableId = this.testOverviewTable.getColumnModel().getColumn(3);
    overviewTableId.setHeaderRenderer(testTableHeaderRenderer);
    final TableColumn overviewTableTime = this.testOverviewTable.getColumnModel().getColumn(4);
    overviewTableTime.setPreferredWidth(60);
    overviewTableTime.setMaxWidth(100);
    overviewTableTime.setHeaderRenderer(testTableHeaderRenderer);
    final RunnerPanel.TimeFormatRenderer timeFormatRenderer = new RunnerPanel.TimeFormatRenderer();
    timeFormatRenderer.setHorizontalAlignment(JLabel.RIGHT);
    overviewTableTime.setCellRenderer(timeFormatRenderer);
    final JScrollPane testOverviewScrollPane = new JScrollPane(this.testOverviewTable);
    final JPopupMenu testOverviewPopupMenu = new JPopupMenu();
    String _string_6 = UtplsqlResources.getString("RUNNER_RUN_MENUITEM");
    Icon _icon_9 = UtplsqlResources.getIcon("RUN_ICON");
    JMenuItem _jMenuItem = new JMenuItem(_string_6, _icon_9);
    this.testOverviewRunMenuItem = _jMenuItem;
    this.testOverviewRunMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.testOverviewRunMenuItem);
    String _string_7 = UtplsqlResources.getString("RUNNER_RUN_WORKSHEET_MENUITEM");
    Icon _icon_10 = UtplsqlResources.getIcon("RUN_WORKSHEET_ICON");
    JMenuItem _jMenuItem_1 = new JMenuItem(_string_7, _icon_10);
    this.testOverviewRunWorksheetMenuItem = _jMenuItem_1;
    this.testOverviewRunWorksheetMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.testOverviewRunWorksheetMenuItem);
    JSeparator _jSeparator = new JSeparator();
    testOverviewPopupMenu.add(_jSeparator);
    String _replace_3 = UtplsqlResources.getString("PREF_SHOW_SUCCESSFUL_TESTS_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_3 = new JCheckBoxMenuItem(_replace_3, true);
    this.showSuccessfulTestsCheckBoxMenuItem = _jCheckBoxMenuItem_3;
    this.showSuccessfulTestsCheckBoxMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.showSuccessfulTestsCheckBoxMenuItem);
    String _replace_4 = UtplsqlResources.getString("PREF_SHOW_DISABLED_TESTS_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_4 = new JCheckBoxMenuItem(_replace_4, true);
    this.showDisabledTestsCheckBoxMenuItem = _jCheckBoxMenuItem_4;
    this.showDisabledTestsCheckBoxMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.showDisabledTestsCheckBoxMenuItem);
    JSeparator _jSeparator_1 = new JSeparator();
    testOverviewPopupMenu.add(_jSeparator_1);
    String _replace_5 = UtplsqlResources.getString("PREF_SHOW_TEST_DESCRIPTION_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_5 = new JCheckBoxMenuItem(_replace_5, true);
    this.showTestDescriptionCheckBoxMenuItem = _jCheckBoxMenuItem_5;
    this.showTestDescriptionCheckBoxMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.showTestDescriptionCheckBoxMenuItem);
    String _replace_6 = UtplsqlResources.getString("PREF_SHOW_WARNING_INDICATOR_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_6 = new JCheckBoxMenuItem(_replace_6, true);
    this.showWarningIndicatorCheckBoxMenuItem = _jCheckBoxMenuItem_6;
    this.showWarningIndicatorCheckBoxMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.showWarningIndicatorCheckBoxMenuItem);
    String _replace_7 = UtplsqlResources.getString("PREF_SHOW_INFO_INDICATOR_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_7 = new JCheckBoxMenuItem(_replace_7, true);
    this.showInfoIndicatorCheckBoxMenuItem = _jCheckBoxMenuItem_7;
    this.showInfoIndicatorCheckBoxMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.showInfoIndicatorCheckBoxMenuItem);
    String _replace_8 = UtplsqlResources.getString("PREF_SYNC_DETAIL_TAB_LABEL").replace("?", "");
    JCheckBoxMenuItem _jCheckBoxMenuItem_8 = new JCheckBoxMenuItem(_replace_8, true);
    this.syncDetailTabCheckBoxMenuItem = _jCheckBoxMenuItem_8;
    this.syncDetailTabCheckBoxMenuItem.addActionListener(this);
    testOverviewPopupMenu.add(this.syncDetailTabCheckBoxMenuItem);
    this.testOverviewTable.setComponentPopupMenu(testOverviewPopupMenu);
    JTableHeader _tableHeader_2 = this.testOverviewTable.getTableHeader();
    _tableHeader_2.setComponentPopupMenu(testOverviewPopupMenu);
    final ScrollablePanel testInfoPanel = new ScrollablePanel();
    GridBagLayout _gridBagLayout_1 = new GridBagLayout();
    testInfoPanel.setLayout(_gridBagLayout_1);
    String _string_8 = UtplsqlResources.getString("RUNNER_OWNER_LABEL");
    final JLabel testOwnerLabel = new JLabel(_string_8);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_6 = new Insets(10, 10, 0, 0);
    c.insets = _insets_6;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    testInfoPanel.add(testOwnerLabel, c);
    RunnerTextField _runnerTextField = new RunnerTextField();
    this.testOwnerTextField = _runnerTextField;
    this.testOwnerTextField.setEditable(false);
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_7 = new Insets(10, 5, 0, 10);
    c.insets = _insets_7;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    testInfoPanel.add(this.testOwnerTextField, c);
    String _string_9 = UtplsqlResources.getString("RUNNER_PACKAGE_LABEL");
    final JLabel testPackageLabel = new JLabel(_string_9);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_8 = new Insets(5, 10, 0, 0);
    c.insets = _insets_8;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    testInfoPanel.add(testPackageLabel, c);
    RunnerTextField _runnerTextField_1 = new RunnerTextField();
    this.testPackageTextField = _runnerTextField_1;
    this.testPackageTextField.setEditable(false);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_9 = new Insets(5, 5, 0, 10);
    c.insets = _insets_9;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    testInfoPanel.add(this.testPackageTextField, c);
    String _string_10 = UtplsqlResources.getString("RUNNER_PROCEDURE_LABEL");
    final JLabel testProcedureLabel = new JLabel(_string_10);
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_10 = new Insets(5, 10, 0, 0);
    c.insets = _insets_10;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    testInfoPanel.add(testProcedureLabel, c);
    RunnerTextField _runnerTextField_2 = new RunnerTextField();
    this.testProcedureTextField = _runnerTextField_2;
    this.testProcedureTextField.setEditable(false);
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_11 = new Insets(5, 5, 0, 10);
    c.insets = _insets_11;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    testInfoPanel.add(this.testProcedureTextField, c);
    String _string_11 = UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL");
    final JLabel testDescriptionLabel = new JLabel(_string_11);
    int _xifexpression = (int) 0;
    boolean _isMacLookAndFeel = this.isMacLookAndFeel();
    if (_isMacLookAndFeel) {
      _xifexpression = 5;
    } else {
      _xifexpression = 3;
    }
    testDescriptionLabel.setBorder(BorderFactory.createEmptyBorder(_xifexpression, 0, 0, 0));
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_12 = new Insets(5, 10, 0, 0);
    c.insets = _insets_12;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    testInfoPanel.add(testDescriptionLabel, c);
    RunnerTextArea _runnerTextArea = new RunnerTextArea();
    this.testDescriptionTextArea = _runnerTextArea;
    this.testDescriptionTextArea.setEditable(false);
    this.testDescriptionTextArea.setEnabled(true);
    this.testDescriptionTextArea.setLineWrap(true);
    this.testDescriptionTextArea.setWrapStyleWord(true);
    c.gridx = 1;
    c.gridy = 3;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_13 = new Insets(5, 5, 0, 10);
    c.insets = _insets_13;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    testInfoPanel.add(this.testDescriptionTextArea, c);
    String _string_12 = UtplsqlResources.getString("RUNNER_TEST_ID_COLUMN");
    final JLabel testIdLabel = new JLabel(_string_12);
    int _xifexpression_1 = (int) 0;
    boolean _isMacLookAndFeel_1 = this.isMacLookAndFeel();
    if (_isMacLookAndFeel_1) {
      _xifexpression_1 = 5;
    } else {
      _xifexpression_1 = 3;
    }
    testIdLabel.setBorder(BorderFactory.createEmptyBorder(_xifexpression_1, 0, 0, 0));
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_14 = new Insets(5, 10, 0, 0);
    c.insets = _insets_14;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    testInfoPanel.add(testIdLabel, c);
    RunnerTextArea _runnerTextArea_1 = new RunnerTextArea();
    this.testIdTextArea = _runnerTextArea_1;
    this.testIdTextArea.setEditable(false);
    this.testIdTextArea.setEnabled(true);
    this.testIdTextArea.setLineWrap(true);
    this.testIdTextArea.setWrapStyleWord(false);
    c.gridx = 1;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_15 = new Insets(5, 5, 0, 10);
    c.insets = _insets_15;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    testInfoPanel.add(this.testIdTextArea, c);
    String _string_13 = UtplsqlResources.getString("RUNNER_START_LABEL");
    final JLabel testStartLabel = new JLabel(_string_13);
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_16 = new Insets(5, 10, 10, 0);
    c.insets = _insets_16;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    testInfoPanel.add(testStartLabel, c);
    RunnerTextField _runnerTextField_3 = new RunnerTextField();
    this.testStartTextField = _runnerTextField_3;
    this.testStartTextField.setEditable(false);
    c.gridx = 1;
    c.gridy = 5;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_17 = new Insets(5, 5, 10, 10);
    c.insets = _insets_17;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    testInfoPanel.add(this.testStartTextField, c);
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_18 = new Insets(0, 0, 0, 0);
    c.insets = _insets_18;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.weighty = 1;
    testInfoPanel.add(Box.createVerticalGlue(), c);
    final JScrollPane testPropertiesScrollPane = new JScrollPane(testInfoPanel);
    FailuresTableModel _failuresTableModel = new FailuresTableModel();
    this.failuresTableModel = _failuresTableModel;
    JTable _jTable_1 = new JTable(this.failuresTableModel);
    this.failuresTable = _jTable_1;
    JTableHeader _tableHeader_3 = this.failuresTable.getTableHeader();
    _tableHeader_3.setReorderingAllowed(false);
    ListSelectionModel _selectionModel_1 = this.failuresTable.getSelectionModel();
    RunnerPanel.FailuresRowListener _failuresRowListener = new RunnerPanel.FailuresRowListener(this);
    _selectionModel_1.addListSelectionListener(_failuresRowListener);
    this.failuresTable.addMouseListener(this);
    final RunnerPanel.FailuresTableHeaderRenderer failuresTableHeaderRenderer = new RunnerPanel.FailuresTableHeaderRenderer();
    final TableColumn failuresTableNumber = this.failuresTable.getColumnModel().getColumn(0);
    failuresTableNumber.setHeaderRenderer(failuresTableHeaderRenderer);
    failuresTableNumber.setPreferredWidth(30);
    failuresTableNumber.setMaxWidth(30);
    final TableColumn failuresDescription = this.failuresTable.getColumnModel().getColumn(1);
    failuresDescription.setHeaderRenderer(failuresTableHeaderRenderer);
    final JScrollPane failuresTableScrollPane = new JScrollPane(this.failuresTable);
    RunnerTextPane _runnerTextPane = new RunnerTextPane();
    this.testFailureMessageTextPane = _runnerTextPane;
    this.testFailureMessageTextPane.setEditable(false);
    this.testFailureMessageTextPane.setEnabled(true);
    this.testFailureMessageTextPane.setContentType("text/html");
    this.testFailureMessageTextPane.setMinimumSize(RunnerPanel.TEXTPANE_DIM);
    this.testFailureMessageTextPane.setPreferredSize(RunnerPanel.TEXTPANE_DIM);
    this.testFailureMessageTextPane.addHyperlinkListener(this);
    final JScrollPane testFailureMessageScrollPane = new JScrollPane(this.testFailureMessageTextPane);
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_19 = new Insets(10, 5, 0, 10);
    c.insets = _insets_19;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 6;
    final JSplitPane failuresSplitPane = new JSplitPane(SwingConstants.HORIZONTAL, failuresTableScrollPane, testFailureMessageScrollPane);
    failuresSplitPane.setResizeWeight(0.2);
    final JPanel testErrorStackPanel = new JPanel();
    GridBagLayout _gridBagLayout_2 = new GridBagLayout();
    testErrorStackPanel.setLayout(_gridBagLayout_2);
    RunnerTextPane _runnerTextPane_1 = new RunnerTextPane();
    this.testErrorStackTextPane = _runnerTextPane_1;
    this.testErrorStackTextPane.setEditable(false);
    this.testErrorStackTextPane.setEnabled(true);
    this.testErrorStackTextPane.setContentType("text/html");
    this.testErrorStackTextPane.setMinimumSize(RunnerPanel.TEXTPANE_DIM);
    this.testErrorStackTextPane.setPreferredSize(RunnerPanel.TEXTPANE_DIM);
    this.testErrorStackTextPane.addHyperlinkListener(this);
    final JScrollPane testErrorStackScrollPane = new JScrollPane(this.testErrorStackTextPane);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_20 = new Insets(0, 0, 0, 0);
    c.insets = _insets_20;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    testErrorStackPanel.add(testErrorStackScrollPane, c);
    final JPanel testWarningsPanel = new JPanel();
    GridBagLayout _gridBagLayout_3 = new GridBagLayout();
    testWarningsPanel.setLayout(_gridBagLayout_3);
    RunnerTextPane _runnerTextPane_2 = new RunnerTextPane();
    this.testWarningsTextPane = _runnerTextPane_2;
    this.testWarningsTextPane.setEditable(false);
    this.testWarningsTextPane.setEnabled(true);
    this.testWarningsTextPane.setContentType("text/html");
    this.testWarningsTextPane.setMinimumSize(RunnerPanel.TEXTPANE_DIM);
    this.testWarningsTextPane.setPreferredSize(RunnerPanel.TEXTPANE_DIM);
    this.testWarningsTextPane.addHyperlinkListener(this);
    final JScrollPane testWarningsScrollPane = new JScrollPane(this.testWarningsTextPane);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_21 = new Insets(0, 0, 0, 0);
    c.insets = _insets_21;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    testWarningsPanel.add(testWarningsScrollPane, c);
    final JPanel testServerOutputPanel = new JPanel();
    GridBagLayout _gridBagLayout_4 = new GridBagLayout();
    testServerOutputPanel.setLayout(_gridBagLayout_4);
    RunnerTextPane _runnerTextPane_3 = new RunnerTextPane();
    this.testServerOutputTextPane = _runnerTextPane_3;
    this.testServerOutputTextPane.setEditable(false);
    this.testServerOutputTextPane.setEnabled(true);
    this.testServerOutputTextPane.setContentType("text/html");
    this.testServerOutputTextPane.setMinimumSize(RunnerPanel.TEXTPANE_DIM);
    this.testServerOutputTextPane.setPreferredSize(RunnerPanel.TEXTPANE_DIM);
    this.testServerOutputTextPane.addHyperlinkListener(this);
    final JScrollPane testServerOutputScrollPane = new JScrollPane(this.testServerOutputTextPane);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    Insets _insets_22 = new Insets(0, 0, 0, 0);
    c.insets = _insets_22;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    testServerOutputPanel.add(testServerOutputScrollPane, c);
    JTabbedPane _jTabbedPane = new JTabbedPane();
    this.testDetailTabbedPane = _jTabbedPane;
    this.testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_TEST_TAB_LABEL"), testPropertiesScrollPane);
    this.testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_FAILURES_TAB_LABEL"), failuresSplitPane);
    this.testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_ERRORS_TAB_LABEL"), testErrorStackPanel);
    this.testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_WARNINGS_TAB_LABEL"), testWarningsPanel);
    this.testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_INFO_TAB_LABEL"), testServerOutputPanel);
    final JSplitPane horizontalSplitPane = new JSplitPane(SwingConstants.HORIZONTAL, testOverviewScrollPane, this.testDetailTabbedPane);
    horizontalSplitPane.setResizeWeight(0.5);
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 2;
    c.gridheight = 1;
    Insets _insets_23 = new Insets(10, 10, 10, 10);
    c.insets = _insets_23;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    this.basePanel.add(horizontalSplitPane, c);
    boolean _isMacLookAndFeel_2 = this.isMacLookAndFeel();
    if (_isMacLookAndFeel_2) {
      Color _color = new Color(219, 219, 219);
      final CompoundBorder border = BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3), 
        BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(_color), 
          BorderFactory.createEmptyBorder(1, 1, 1, 1)));
      this.testDescriptionTextArea.setBorder(border);
      this.testIdTextArea.setBorder(border);
    } else {
      final Border referenceBorder = this.testOwnerTextField.getBorder();
      this.testDescriptionTextArea.setBorder(referenceBorder);
      this.testIdTextArea.setBorder(referenceBorder);
    }
  }
}
