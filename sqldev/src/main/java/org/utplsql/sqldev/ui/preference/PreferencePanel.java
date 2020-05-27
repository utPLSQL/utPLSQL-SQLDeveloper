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
package org.utplsql.sqldev.ui.preference;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import oracle.dbtools.raptor.templates.CodeTemplateUtil;
import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;
import oracle.javatools.ui.layout.FieldLayoutBuilder;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.resources.UtplsqlResources;
import org.utplsql.sqldev.snippet.SnippetMerger;
import org.utplsql.sqldev.ui.common.DirectoryChooser;

@SuppressWarnings("all")
public class PreferencePanel extends DefaultTraversablePanel {
  private final JPanel runTestPanel = new JPanel();
  
  private final JCheckBox useRealtimeReporterCheckBox = new JCheckBox();
  
  private final JCheckBox unsharedWorksheetCheckBox = new JCheckBox();
  
  private final JCheckBox resetPackageCheckBox = new JCheckBox();
  
  private final JCheckBox clearScreenCheckBox = new JCheckBox();
  
  private final JCheckBox autoExecuteCheckBox = new JCheckBox();
  
  private final JCheckBox checkRunUtplsqlTestCheckBox = new JCheckBox();
  
  private final JCheckBox useSmartTimesCheckBox = new JCheckBox();
  
  private final JButton importSnippetsButton = new JButton(UtplsqlResources.getString("PREF_IMPORT_SNIPPETS_BUTTON_LABEL"));
  
  private final JPanel realtimeReporterPanel = new JPanel();
  
  private final SpinnerNumberModel numberOfRunsInHistoryModel = new SpinnerNumberModel(1, 1, 100, 1);
  
  private final JSpinner numberOfRunsInHistorySpinner = new JSpinner(this.numberOfRunsInHistoryModel);
  
  private final JCheckBox showDisabledCounterCheckBox = new JCheckBox();
  
  private final JCheckBox showWarningsCounterCheckBox = new JCheckBox();
  
  private final JCheckBox showInfoCounterCheckBox = new JCheckBox();
  
  private final JCheckBox showWarningIndicatorCheckBox = new JCheckBox();
  
  private final JCheckBox showInfoIndicatorCheckBox = new JCheckBox();
  
  private final JCheckBox showSuccessfulTestsCheckBox = new JCheckBox();
  
  private final JCheckBox showDisabledTestsCheckBox = new JCheckBox();
  
  private final JCheckBox showTestDescriptionCheckBox = new JCheckBox();
  
  private final JCheckBox syncDetailTabCheckBox = new JCheckBox();
  
  private final JPanel generateTestPanel = new JPanel();
  
  private final JTextField testPackagePrefixTextField = new JTextField();
  
  private final JTextField testPackageSuffixTextField = new JTextField();
  
  private final JTextField testUnitPrefixTextField = new JTextField();
  
  private final JTextField testUnitSuffixTextField = new JTextField();
  
  private final SpinnerNumberModel numberOfTestsPerUnitModel = new SpinnerNumberModel(1, 1, 10, 1);
  
  private final JSpinner numberOfTestsPerUnitSpinner = new JSpinner(this.numberOfTestsPerUnitModel);
  
  private final JCheckBox checkGenerateUtplsqlTestCheckBox = new JCheckBox();
  
  private final DefaultTableModel codeTemplatesModel = new DefaultTableModel(new Object[] { "Id", "Template" }, 0);
  
  private final JButton createCodeTemplatesButton = new JButton();
  
  private final JCheckBox generateCommentsCheckBox = new JCheckBox();
  
  private final JCheckBox disableTestsCheckBox = new JCheckBox();
  
  private final JTextField suitePathTextField = new JTextField();
  
  private final SpinnerNumberModel indentSpacesModel = new SpinnerNumberModel(1, 1, 8, 1);
  
  private final JSpinner indentSpacesSpinner = new JSpinner(this.indentSpacesModel);
  
  private final JPanel oddgenPanel = new JPanel();
  
  private final JTextField rootFolderInOddgenViewTextField = new JTextField();
  
  private final JCheckBox generateFilesCheckBox = new JCheckBox();
  
  private final JTextField outputDirectoryTextField = new JTextField();
  
  private final JButton outputDirectoryBrowse = new JButton();
  
  private final JCheckBox deleteExistingFilesCheckBox = new JCheckBox();
  
  public PreferencePanel() {
    this.layoutControls();
  }
  
  private void layoutControls() {
    final FieldLayoutBuilder runTab = new FieldLayoutBuilder(this.runTestPanel);
    runTab.setAlignLabelsLeft(true);
    runTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_USE_REALTIME_REPORTER_LABEL")).component(
        this.useRealtimeReporterCheckBox).withHint(UtplsqlResources.getString("PREF_USE_REALTIME_REPORTER_HINT")));
    runTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_UNSHARED_WORKSHEET_LABEL")).component(
        this.unsharedWorksheetCheckBox));
    runTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_RESET_PACKAGE_LABEL")).component(
        this.resetPackageCheckBox));
    runTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_CLEAR_SCREEN_LABEL")).component(
        this.clearScreenCheckBox));
    runTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_AUTO_EXECUTE_LABEL")).component(
        this.autoExecuteCheckBox));
    runTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_CHECK_RUN_UTPLSQL_TEST_LABEL")).component(
        this.checkRunUtplsqlTestCheckBox));
    runTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_USE_SMART_TIMES_LABEL")).component(
        this.useSmartTimesCheckBox));
    runTab.addVerticalGap();
    runTab.addRow(this.importSnippetsButton);
    runTab.addVerticalSpring();
    final FieldLayoutBuilder rrTab = new FieldLayoutBuilder(this.realtimeReporterPanel);
    rrTab.setAlignLabelsLeft(true);
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_NUMBER_OF_RUNS_IN_HISTORY_LABEL")).component(
        this.numberOfRunsInHistorySpinner));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_DISABLED_COUNTER_LABEL")).component(
        this.showDisabledCounterCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_WARNINGS_COUNTER_LABEL")).component(
        this.showWarningsCounterCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_INFO_COUNTER_LABEL")).component(
        this.showInfoCounterCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_WARNING_INDICATOR_LABEL")).component(
        this.showWarningIndicatorCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_INFO_INDICATOR_LABEL")).component(
        this.showInfoIndicatorCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_SUCCESSFUL_TESTS_LABEL")).component(
        this.showSuccessfulTestsCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_DISABLED_TESTS_LABEL")).component(
        this.showDisabledTestsCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_TEST_DESCRIPTION_LABEL")).component(
        this.showTestDescriptionCheckBox));
    rrTab.add(
      runTab.field().label().withText(UtplsqlResources.getString("PREF_SYNC_DETAIL_TAB_LABEL")).component(
        this.syncDetailTabCheckBox));
    rrTab.addVerticalSpring();
    final FieldLayoutBuilder generateTab = new FieldLayoutBuilder(this.generateTestPanel);
    generateTab.setAlignLabelsLeft(true);
    generateTab.setStretchComponentsWithNoButton(true);
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_PREFIX_LABEL")).component(
        this.testPackagePrefixTextField));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_SUFFIX_LABEL")).component(
        this.testPackageSuffixTextField));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_UNIT_PREFIX_LABEL")).component(
        this.testUnitPrefixTextField));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_UNIT_SUFFIX_LABEL")).component(
        this.testUnitSuffixTextField));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_NUMBER_OF_TESTS_PER_UNIT_LABEL")).component(
        this.numberOfTestsPerUnitSpinner));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_GENERATE_COMMENTS_LABEL")).component(
        this.generateCommentsCheckBox));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_DISABLE_TESTS_LABEL")).component(
        this.disableTestsCheckBox));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_SUITE_PATH_LABEL")).component(
        this.suitePathTextField));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_INDENT_SPACES_LABEL")).component(
        this.indentSpacesSpinner));
    generateTab.add(
      generateTab.field().label().withText(UtplsqlResources.getString("PREF_CHECK_GENERATE_UTPLSQL_TEST_LABEL")).component(
        this.checkGenerateUtplsqlTestCheckBox).button(this.createCodeTemplatesButton).withText(
        UtplsqlResources.getString("PREF_CREATE_CODE_TEMPLATES_BUTTON_LABEL")));
    generateTab.addVerticalSpring();
    final FieldLayoutBuilder oddgenTab = new FieldLayoutBuilder(this.oddgenPanel);
    oddgenTab.setAlignLabelsLeft(true);
    oddgenTab.setStretchComponentsWithNoButton(true);
    oddgenTab.add(
      oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_ROOT_FOLDER_IN_ODDGEN_VIEW_LABEL")).component(
        this.rootFolderInOddgenViewTextField));
    oddgenTab.add(
      oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_GENERATE_FILES_LABEL")).component(
        this.generateFilesCheckBox));
    oddgenTab.add(
      oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_LABEL")).component(
        this.outputDirectoryTextField).button(this.outputDirectoryBrowse).withText(
        UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_BUTTON_LABEL")));
    oddgenTab.add(
      oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_DELETE_EXISTING_FILES_LABEL")).component(
        this.deleteExistingFilesCheckBox));
    oddgenTab.addVerticalSpring();
    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add(UtplsqlResources.getString("MENU_RUN_TEST_LABEL"), this.runTestPanel);
    tabbedPane.add(UtplsqlResources.getString("MENU_REALTIME_REPORTER_LABEL"), this.realtimeReporterPanel);
    tabbedPane.add(UtplsqlResources.getString("MENU_GENERATE_TEST_LABEL"), this.generateTestPanel);
    tabbedPane.add("oddgen", this.oddgenPanel);
    final FieldLayoutBuilder builder = new FieldLayoutBuilder(this);
    builder.setAlignLabelsLeft(true);
    builder.addVerticalField("", tabbedPane);
    builder.addVerticalSpring();
    this.importSnippetsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        PreferencePanel.this.importSnippets();
      }
    });
    this.createCodeTemplatesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        PreferencePanel.this.saveCodeTemplates();
      }
    });
    this.outputDirectoryBrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        DirectoryChooser.choose(null, UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_LABEL"), 
          PreferencePanel.this.outputDirectoryTextField);
      }
    });
  }
  
  private void importSnippets() {
    final SnippetMerger snippetMerger = new SnippetMerger();
    snippetMerger.merge();
    final String file = snippetMerger.getFile().getAbsolutePath();
    final String message = String.format(UtplsqlResources.getString("PREF_CONFIRM_IMPORT_MESSAGE"), file);
    JOptionPane.showMessageDialog(null, message, UtplsqlResources.getString("PREF_CONFIRM_IMPORT_TITLE"), 
      JOptionPane.INFORMATION_MESSAGE);
  }
  
  private void loadCodeTemplates() {
    final Map<String, String> map = CodeTemplateUtil.loadFiles();
    Set<String> _keySet = map.keySet();
    for (final String key : _keySet) {
      String _get = map.get(key);
      this.codeTemplatesModel.addRow(new Object[] { key, _get });
    }
  }
  
  private void saveCodeTemplates() {
    String _replaceTabsWithSpaces = this.replaceTabsWithSpaces(this.utSpecTemplate());
    this.codeTemplatesModel.addRow(new Object[] { "ut_spec", _replaceTabsWithSpaces });
    String _trimPlusNewLine = this.trimPlusNewLine(this.replaceTabsWithSpaces(this.utSpecProcTemplate()));
    this.codeTemplatesModel.addRow(new Object[] { "ut_spec_proc", _trimPlusNewLine });
    String _replaceTabsWithSpaces_1 = this.replaceTabsWithSpaces(this.utBodyTemplate());
    this.codeTemplatesModel.addRow(new Object[] { "ut_body", _replaceTabsWithSpaces_1 });
    String _trimPlusNewLine_1 = this.trimPlusNewLine(this.replaceTabsWithSpaces(this.utBodyProcTemplate()));
    this.codeTemplatesModel.addRow(new Object[] { "ut_body_proc", _trimPlusNewLine_1 });
    CodeTemplateUtil.save(this.codeTemplatesModel);
  }
  
  private String replaceTabsWithSpaces(final CharSequence input) {
    Object _value = this.indentSpacesSpinner.getValue();
    String _plus = ("%1$" + _value);
    String _plus_1 = (_plus + "s");
    final String spaces = String.format(_plus_1, "");
    return input.toString().replace("\t", spaces);
  }
  
  private String trimPlusNewLine(final String input) {
    String _trim = input.trim();
    String _lineSeparator = System.lineSeparator();
    return (_trim + _lineSeparator);
  }
  
  private CharSequence utSpecTemplate() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE ");
    String _text = this.testPackagePrefixTextField.getText();
    _builder.append(_text);
    _builder.append("[package_name]");
    String _text_1 = this.testPackageSuffixTextField.getText();
    _builder.append(_text_1);
    _builder.append(" IS");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("--%suite");
    _builder.newLine();
    {
      boolean _isEmpty = this.suitePathTextField.getText().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        _builder.append("\t");
        _builder.append("--%suitepath(");
        String _text_2 = this.suitePathTextField.getText();
        _builder.append(_text_2, "\t");
        _builder.append(")");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("\t");
    CharSequence _utSpecProcTemplate = this.utSpecProcTemplate();
    _builder.append(_utSpecProcTemplate, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("END ");
    String _text_3 = this.testPackagePrefixTextField.getText();
    _builder.append(_text_3);
    _builder.append("[package_name]");
    String _text_4 = this.testPackageSuffixTextField.getText();
    _builder.append(_text_4);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.append("/");
    _builder.newLine();
    return _builder;
  }
  
  private CharSequence utSpecProcTemplate() {
    StringConcatenation _builder = new StringConcatenation();
    Object _value = this.numberOfTestsPerUnitModel.getValue();
    final boolean withContext = ((((Integer) _value)).intValue() > 1);
    _builder.newLineIfNotEmpty();
    {
      if (withContext) {
        _builder.append("--%context([procedure_name])");
        _builder.newLine();
        _builder.newLine();
      }
    }
    {
      Object _value_1 = this.numberOfTestsPerUnitModel.getValue();
      IntegerRange _upTo = new IntegerRange(1, (((Integer) _value_1)).intValue());
      for(final Integer i : _upTo) {
        _builder.append("--%test");
        _builder.newLine();
        {
          boolean _isSelected = this.disableTestsCheckBox.isSelected();
          if (_isSelected) {
            _builder.append("--%disabled");
            _builder.newLine();
          }
        }
        _builder.append("PROCEDURE ");
        String _text = this.testUnitPrefixTextField.getText();
        _builder.append(_text);
        _builder.append("[procedure_name]");
        String _text_1 = this.testUnitSuffixTextField.getText();
        _builder.append(_text_1);
        {
          if (withContext) {
            _builder.append(i);
          }
        }
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
      }
    }
    {
      if (withContext) {
        _builder.append("--%endcontext");
        _builder.newLine();
        _builder.newLine();
      }
    }
    return _builder;
  }
  
  private CharSequence utBodyTemplate() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE BODY ");
    String _text = this.testPackagePrefixTextField.getText();
    _builder.append(_text);
    _builder.append("[package_name]");
    String _text_1 = this.testPackageSuffixTextField.getText();
    _builder.append(_text_1);
    _builder.append(" IS");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("\t");
    CharSequence _utBodyProcTemplate = this.utBodyProcTemplate();
    _builder.append(_utBodyProcTemplate, "\t");
    _builder.newLineIfNotEmpty();
    _builder.append("END ");
    String _text_2 = this.testPackagePrefixTextField.getText();
    _builder.append(_text_2);
    _builder.append("[package_name]");
    String _text_3 = this.testPackageSuffixTextField.getText();
    _builder.append(_text_3);
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.append("/");
    _builder.newLine();
    return _builder;
  }
  
  private CharSequence utBodyProcTemplate() {
    StringConcatenation _builder = new StringConcatenation();
    Object _value = this.numberOfTestsPerUnitModel.getValue();
    final boolean withContext = ((((Integer) _value)).intValue() > 1);
    _builder.newLineIfNotEmpty();
    {
      Object _value_1 = this.numberOfTestsPerUnitModel.getValue();
      IntegerRange _upTo = new IntegerRange(1, (((Integer) _value_1)).intValue());
      for(final Integer i : _upTo) {
        {
          boolean _isSelected = this.generateCommentsCheckBox.isSelected();
          if (_isSelected) {
            _builder.append("--");
            _builder.newLine();
            _builder.append("-- test");
            {
              if (withContext) {
                _builder.append(" [procedure_name] case ");
                _builder.append(i);
                _builder.append(": ...");
              }
            }
            _builder.newLineIfNotEmpty();
            _builder.append("--");
            _builder.newLine();
          }
        }
        _builder.append("PROCEDURE ");
        String _text = this.testUnitPrefixTextField.getText();
        _builder.append(_text);
        _builder.append("[procedure_name]");
        String _text_1 = this.testUnitSuffixTextField.getText();
        _builder.append(_text_1);
        {
          if (withContext) {
            _builder.append(i);
          }
        }
        _builder.append(" IS");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("l_actual   INTEGER := 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("l_expected INTEGER := 1;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        {
          boolean _isSelected_1 = this.generateCommentsCheckBox.isSelected();
          if (_isSelected_1) {
            _builder.append("\t");
            _builder.append("-- populate actual");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("-- ...");
            _builder.newLine();
            _builder.newLine();
            _builder.append("\t");
            _builder.append("-- populate expected");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("-- ...");
            _builder.newLine();
            _builder.newLine();
            _builder.append("\t");
            _builder.append("-- assert");
            _builder.newLine();
          }
        }
        _builder.append("\t");
        _builder.append("ut.expect(l_actual).to_equal(l_expected);");
        _builder.newLine();
        _builder.append("END ");
        String _text_2 = this.testUnitPrefixTextField.getText();
        _builder.append(_text_2);
        _builder.append("[procedure_name]");
        String _text_3 = this.testUnitSuffixTextField.getText();
        _builder.append(_text_3);
        {
          if (withContext) {
            _builder.append(i);
          }
        }
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
      }
    }
    return _builder;
  }
  
  @Override
  public void onEntry(final TraversableContext traversableContext) {
    PreferenceModel info = PreferencePanel.getUserInformation(traversableContext);
    this.useRealtimeReporterCheckBox.setSelected(info.isUseRealtimeReporter());
    this.unsharedWorksheetCheckBox.setSelected(info.isUnsharedWorksheet());
    this.resetPackageCheckBox.setSelected(info.isResetPackage());
    this.clearScreenCheckBox.setSelected(info.isClearScreen());
    this.autoExecuteCheckBox.setSelected(info.isAutoExecute());
    this.checkRunUtplsqlTestCheckBox.setSelected(info.isCheckRunUtplsqlTest());
    this.useSmartTimesCheckBox.setSelected(info.isUseSmartTimes());
    this.numberOfRunsInHistorySpinner.setValue(Integer.valueOf(info.getNumberOfRunsInHistory()));
    this.showDisabledCounterCheckBox.setSelected(info.isShowDisabledCounter());
    this.showWarningsCounterCheckBox.setSelected(info.isShowWarningsCounter());
    this.showInfoCounterCheckBox.setSelected(info.isShowInfoCounter());
    this.showWarningIndicatorCheckBox.setSelected(info.isShowWarningIndicator());
    this.showInfoIndicatorCheckBox.setSelected(info.isShowInfoIndicator());
    this.showSuccessfulTestsCheckBox.setSelected(info.isShowSuccessfulTests());
    this.showDisabledTestsCheckBox.setSelected(info.isShowDisabledTests());
    this.showTestDescriptionCheckBox.setSelected(info.isShowTestDescription());
    this.syncDetailTabCheckBox.setSelected(info.isSyncDetailTab());
    this.testPackagePrefixTextField.setText(info.getTestPackagePrefix());
    this.testPackageSuffixTextField.setText(info.getTestPackageSuffix());
    this.testUnitPrefixTextField.setText(info.getTestUnitPrefix());
    this.testUnitSuffixTextField.setText(info.getTestUnitSuffix());
    this.numberOfTestsPerUnitSpinner.setValue(Integer.valueOf(info.getNumberOfTestsPerUnit()));
    this.checkGenerateUtplsqlTestCheckBox.setSelected(info.isCheckGenerateUtplsqlTest());
    this.loadCodeTemplates();
    this.generateCommentsCheckBox.setSelected(info.isGenerateComments());
    this.disableTestsCheckBox.setSelected(info.isDisableTests());
    this.suitePathTextField.setText(info.getSuitePath());
    this.indentSpacesSpinner.setValue(Integer.valueOf(info.getIndentSpaces()));
    this.rootFolderInOddgenViewTextField.setText(info.getRootFolderInOddgenView());
    this.generateFilesCheckBox.setSelected(info.isGenerateFiles());
    this.outputDirectoryTextField.setText(info.getOutputDirectory());
    this.deleteExistingFilesCheckBox.setSelected(info.isDeleteExistingFiles());
    super.onEntry(traversableContext);
  }
  
  @Override
  public void onExit(final TraversableContext traversableContext) throws TraversalException {
    PreferenceModel info = PreferencePanel.getUserInformation(traversableContext);
    info.setUseRealtimeReporter(this.useRealtimeReporterCheckBox.isSelected());
    info.setUnsharedWorksheet(this.unsharedWorksheetCheckBox.isSelected());
    info.setResetPackage(this.resetPackageCheckBox.isSelected());
    info.setClearScreen(this.clearScreenCheckBox.isSelected());
    info.setAutoExecute(this.autoExecuteCheckBox.isSelected());
    Object _value = this.numberOfRunsInHistorySpinner.getValue();
    info.setNumberOfRunsInHistory((((Integer) _value)).intValue());
    info.setCheckRunUtplsqlTest(this.checkRunUtplsqlTestCheckBox.isSelected());
    info.setUseSmartTimes(this.useSmartTimesCheckBox.isSelected());
    info.setShowDisabledCounter(this.showDisabledCounterCheckBox.isSelected());
    info.setShowWarningsCounter(this.showWarningsCounterCheckBox.isSelected());
    info.setShowInfoCounter(this.showInfoCounterCheckBox.isSelected());
    info.setShowWarningIndicator(this.showWarningIndicatorCheckBox.isSelected());
    info.setShowInfoIndicator(this.showInfoIndicatorCheckBox.isSelected());
    info.setShowSuccessfulTests(this.showSuccessfulTestsCheckBox.isSelected());
    info.setShowDisabledTests(this.showDisabledTestsCheckBox.isSelected());
    info.setShowTestDescription(this.showTestDescriptionCheckBox.isSelected());
    info.setSyncDetailTab(this.syncDetailTabCheckBox.isSelected());
    info.setTestPackagePrefix(this.testPackagePrefixTextField.getText());
    info.setTestPackageSuffix(this.testPackageSuffixTextField.getText());
    info.setTestUnitPrefix(this.testUnitPrefixTextField.getText());
    info.setTestUnitSuffix(this.testUnitSuffixTextField.getText());
    Object _value_1 = this.numberOfTestsPerUnitSpinner.getValue();
    info.setNumberOfTestsPerUnit((((Integer) _value_1)).intValue());
    info.setCheckGenerateUtplsqlTest(this.checkGenerateUtplsqlTestCheckBox.isSelected());
    info.setGenerateComments(this.generateCommentsCheckBox.isSelected());
    info.setDisableTests(this.disableTestsCheckBox.isSelected());
    info.setSuitePath(this.suitePathTextField.getText());
    Object _value_2 = this.indentSpacesSpinner.getValue();
    info.setIndentSpaces((((Integer) _value_2)).intValue());
    info.setRootFolderInOddgenView(this.rootFolderInOddgenViewTextField.getText());
    info.setGenerateFiles(this.generateFilesCheckBox.isSelected());
    info.setOutputDirectory(this.outputDirectoryTextField.getText());
    info.setDeleteExistingFiles(this.deleteExistingFilesCheckBox.isSelected());
    super.onExit(traversableContext);
  }
  
  private static PreferenceModel getUserInformation(final TraversableContext tc) {
    return PreferenceModel.getInstance(tc.getPropertyStorage());
  }
}
