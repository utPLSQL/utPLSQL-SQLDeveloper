/*
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

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import org.utplsql.sqldev.model.StringTools;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.resources.UtplsqlResources;
import org.utplsql.sqldev.snippet.SnippetMerger;
import org.utplsql.sqldev.ui.common.DirectoryChooser;

import oracle.dbtools.raptor.templates.CodeTemplateUtil;
import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;
import oracle.javatools.ui.layout.FieldLayoutBuilder;

public class PreferencePanel extends DefaultTraversablePanel {
    private static final long serialVersionUID = -2583957375062007813L;
    private final JPanel runTestPanel = new JPanel();
    private final JCheckBox useRealtimeReporterCheckBox = new JCheckBox();
    private final JCheckBox unsharedWorksheetCheckBox = new JCheckBox();
    private final JCheckBox resetPackageCheckBox = new JCheckBox();
    private final JCheckBox clearScreenCheckBox = new JCheckBox();
    private final JCheckBox autoExecuteCheckBox = new JCheckBox();
    private final JCheckBox checkRunUtplsqlTestCheckBox = new JCheckBox();
    private final JCheckBox useSmartTimesCheckBox = new JCheckBox();
    private final JButton importSnippetsButton = new JButton(
            UtplsqlResources.getString("PREF_IMPORT_SNIPPETS_BUTTON_LABEL"));
    private final JPanel realtimeReporterPanel = new JPanel();
    private final SpinnerNumberModel numberOfRunsInHistoryModel = new SpinnerNumberModel(1, 1, 100, 1);
    private final JSpinner numberOfRunsInHistorySpinner = new JSpinner(numberOfRunsInHistoryModel);
    private final JCheckBox showDisabledCounterCheckBox = new JCheckBox();
    private final JCheckBox showWarningsCounterCheckBox = new JCheckBox();
    private final JCheckBox showInfoCounterCheckBox = new JCheckBox();
    private final JCheckBox showWarningIndicatorCheckBox = new JCheckBox();
    private final JCheckBox showInfoIndicatorCheckBox = new JCheckBox();
    private final JCheckBox showSuccessfulTestsCheckBox = new JCheckBox();
    private final JCheckBox showDisabledTestsCheckBox = new JCheckBox();
    private final JCheckBox showTestDescriptionCheckBox = new JCheckBox();
    private final JCheckBox syncDetailTabCheckBox = new JCheckBox();
    private final JCheckBox showSuitesCheckBox = new JCheckBox();
    private final JPanel generateTestPanel = new JPanel();
    private final JTextField testPackagePrefixTextField = new JTextField();
    private final JTextField testPackageSuffixTextField = new JTextField();
    private final JTextField testUnitPrefixTextField = new JTextField();
    private final JTextField testUnitSuffixTextField = new JTextField();
    private final SpinnerNumberModel numberOfTestsPerUnitModel = new SpinnerNumberModel(1, 1, 10, 1);
    private final JSpinner numberOfTestsPerUnitSpinner = new JSpinner(numberOfTestsPerUnitModel);
    private final JCheckBox checkGenerateUtplsqlTestCheckBox = new JCheckBox();
    private final DefaultTableModel codeTemplatesModel = new DefaultTableModel(new Object[] { "Id", "Template" }, 0);
    private final JButton createCodeTemplatesButton = new JButton();
    private final JCheckBox generateCommentsCheckBox = new JCheckBox();
    private final JCheckBox disableTestsCheckBox = new JCheckBox();
    private final JTextField suitePathTextField = new JTextField();
    private final SpinnerNumberModel indentSpacesModel = new SpinnerNumberModel(1, 1, 8, 1);
    private final JSpinner indentSpacesSpinner = new JSpinner(indentSpacesModel);
    private final JPanel oddgenPanel = new JPanel();
    private final JTextField rootFolderInOddgenViewTextField = new JTextField();
    private final JCheckBox generateFilesCheckBox = new JCheckBox();
    private final JTextField outputDirectoryTextField = new JTextField();
    private final JButton outputDirectoryBrowse = new JButton();
    private final JCheckBox deleteExistingFilesCheckBox = new JCheckBox();

    public PreferencePanel() {
        layoutControls();
    }

    private void layoutControls() {
        // run test group
        final FieldLayoutBuilder runTab = new FieldLayoutBuilder(runTestPanel);
        runTab.setAlignLabelsLeft(true);
        runTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_USE_REALTIME_REPORTER_LABEL"))
                .component(useRealtimeReporterCheckBox)
                .withHint(UtplsqlResources.getString("PREF_USE_REALTIME_REPORTER_HINT")));
        runTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_UNSHARED_WORKSHEET_LABEL"))
                .component(unsharedWorksheetCheckBox));
        runTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_RESET_PACKAGE_LABEL"))
                .component(resetPackageCheckBox));
        runTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_CLEAR_SCREEN_LABEL"))
                .component(clearScreenCheckBox));
        runTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_AUTO_EXECUTE_LABEL"))
                .component(autoExecuteCheckBox));
        runTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_CHECK_RUN_UTPLSQL_TEST_LABEL"))
                .component(checkRunUtplsqlTestCheckBox));
        runTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_USE_SMART_TIMES_LABEL"))
                .component(useSmartTimesCheckBox));
        runTab.addVerticalGap();
        runTab.addRow(importSnippetsButton);
        runTab.addVerticalSpring();
        
        // realtime reporter group
        final FieldLayoutBuilder rrTab = new FieldLayoutBuilder(realtimeReporterPanel);
        rrTab.setAlignLabelsLeft(true);
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_NUMBER_OF_RUNS_IN_HISTORY_LABEL"))
                .component(numberOfRunsInHistorySpinner));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_DISABLED_COUNTER_LABEL"))
                .component(showDisabledCounterCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_WARNINGS_COUNTER_LABEL"))
                .component(showWarningsCounterCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_INFO_COUNTER_LABEL"))
                .component(showInfoCounterCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_WARNING_INDICATOR_LABEL"))
                .component(showWarningIndicatorCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_INFO_INDICATOR_LABEL"))
                .component(showInfoIndicatorCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_SUCCESSFUL_TESTS_LABEL"))
                .component(showSuccessfulTestsCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_DISABLED_TESTS_LABEL"))
                .component(showDisabledTestsCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_TEST_DESCRIPTION_LABEL"))
                .component(showTestDescriptionCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SYNC_DETAIL_TAB_LABEL"))
                .component(syncDetailTabCheckBox));
        rrTab.add(runTab.field().label().withText(UtplsqlResources.getString("PREF_SHOW_SUITES_LABEL"))
                .component(showSuitesCheckBox));
        rrTab.addVerticalSpring();
        
        // generate test group
        final FieldLayoutBuilder generateTab = new FieldLayoutBuilder(generateTestPanel);
        generateTab.setAlignLabelsLeft(true);
        generateTab.setStretchComponentsWithNoButton(true);
        generateTab
                .add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_PREFIX_LABEL"))
                        .component(testPackagePrefixTextField));
        generateTab
                .add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_SUFFIX_LABEL"))
                        .component(testPackageSuffixTextField));
        generateTab.add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_UNIT_PREFIX_LABEL"))
                .component(testUnitPrefixTextField));
        generateTab.add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_TEST_UNIT_SUFFIX_LABEL"))
                .component(testUnitSuffixTextField));
        generateTab.add(
                generateTab.field().label().withText(UtplsqlResources.getString("PREF_NUMBER_OF_TESTS_PER_UNIT_LABEL"))
                        .component(numberOfTestsPerUnitSpinner));
        generateTab.add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_GENERATE_COMMENTS_LABEL"))
                .component(generateCommentsCheckBox));
        generateTab.add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_DISABLE_TESTS_LABEL"))
                .component(disableTestsCheckBox));
        generateTab.add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_SUITE_PATH_LABEL"))
                .component(suitePathTextField));
        generateTab.add(generateTab.field().label().withText(UtplsqlResources.getString("PREF_INDENT_SPACES_LABEL"))
                .component(indentSpacesSpinner));
        generateTab.add(generateTab.field().label()
                .withText(UtplsqlResources.getString("PREF_CHECK_GENERATE_UTPLSQL_TEST_LABEL"))
                .component(checkGenerateUtplsqlTestCheckBox).button(createCodeTemplatesButton)
                .withText(UtplsqlResources.getString("PREF_CREATE_CODE_TEMPLATES_BUTTON_LABEL")));
        generateTab.addVerticalSpring();
        
        // oddgen group
        final FieldLayoutBuilder oddgenTab = new FieldLayoutBuilder(oddgenPanel);
        oddgenTab.setAlignLabelsLeft(true);
        oddgenTab.setStretchComponentsWithNoButton(true);
        oddgenTab.add(
                oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_ROOT_FOLDER_IN_ODDGEN_VIEW_LABEL"))
                        .component(rootFolderInOddgenViewTextField));
        oddgenTab.add(oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_GENERATE_FILES_LABEL"))
                .component(generateFilesCheckBox));
        oddgenTab.add(oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_LABEL"))
                .component(outputDirectoryTextField).button(outputDirectoryBrowse)
                .withText(UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_BUTTON_LABEL")));
        oddgenTab.add(oddgenTab.field().label().withText(UtplsqlResources.getString("PREF_DELETE_EXISTING_FILES_LABEL"))
                .component(deleteExistingFilesCheckBox));
        oddgenTab.addVerticalSpring();
        
        // putting groups into tabbed panes
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(UtplsqlResources.getString("MENU_RUN_TEST_LABEL"), runTestPanel);
        tabbedPane.add(UtplsqlResources.getString("MENU_REALTIME_REPORTER_LABEL"), realtimeReporterPanel);
        tabbedPane.add(UtplsqlResources.getString("MENU_GENERATE_TEST_LABEL"), generateTestPanel);
        tabbedPane.add("oddgen", oddgenPanel);
        final FieldLayoutBuilder builder = new FieldLayoutBuilder(this);
        builder.setAlignLabelsLeft(true);
        builder.addVerticalField("", tabbedPane);
        builder.addVerticalSpring();
        
        // register action listener for import snippets button 
        importSnippetsButton.addActionListener(event -> importSnippets());
        
        // register action listener for create code template button 
        createCodeTemplatesButton.addActionListener(event -> saveCodeTemplates());
        
        // register action listener for directory chooser
        outputDirectoryBrowse.addActionListener(event -> DirectoryChooser.choose(null,
                UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_LABEL"), outputDirectoryTextField));
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
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            codeTemplatesModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
    }

    private void saveCodeTemplates() {
        Integer indentSpaces = (Integer) indentSpacesSpinner.getValue();
        codeTemplatesModel
                .addRow(new Object[] { "ut_spec", StringTools.replaceTabsWithSpaces(utSpecTemplate(), indentSpaces) });
        codeTemplatesModel.addRow(new Object[] { "ut_spec_proc",
                trimPlusNewLine(StringTools.replaceTabsWithSpaces(utSpecProcTemplate(), indentSpaces)) });
        codeTemplatesModel
                .addRow(new Object[] { "ut_body", StringTools.replaceTabsWithSpaces(utBodyTemplate(), indentSpaces) });
        codeTemplatesModel.addRow(new Object[] { "ut_body_proc",
                trimPlusNewLine(StringTools.replaceTabsWithSpaces(utBodyProcTemplate(), indentSpaces)) });
        CodeTemplateUtil.save(codeTemplatesModel);
    }

    private String trimPlusNewLine(final String input) {
        return input.trim() + System.lineSeparator();
    }

    private CharSequence utSpecTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace package ");
        sb.append(testPackagePrefixTextField.getText());
        sb.append("[package_name]");
        sb.append(testPackageSuffixTextField.getText());
        sb.append(" is\n\n");
        sb.append("\t--%suite\n");
        if (!suitePathTextField.getText().isEmpty()) {
            sb.append("\t--%suitepath(");
            sb.append(suitePathTextField.getText());
            sb.append(")\n");
        }
        sb.append("\n\t");
        sb.append(utSpecProcTemplate().toString().trim().replace("\n", "\n\t"));
        sb.append("\n\n");
        sb.append("end ");
        sb.append(testPackagePrefixTextField.getText());
        sb.append("[package_name]");
        sb.append(testPackageSuffixTextField.getText());
        sb.append(";\n");
        sb.append("/\n");
        return sb;
    }

    private CharSequence utSpecProcTemplate() {
        StringBuilder sb = new StringBuilder();
        final Integer numberOfTestsPerUnit = (Integer) numberOfTestsPerUnitModel.getValue();
        final boolean withContext = numberOfTestsPerUnit > 1;
        if (withContext) {
            sb.append("--%context([procedure_name])\n\n");
        }
        for (int i = 1; i <= numberOfTestsPerUnit; i ++) {
            sb.append("--%test\n");
            if (disableTestsCheckBox.isSelected()) {
                sb.append("--%disabled\n");
            }
            sb.append("procedure ");
            sb.append(testUnitPrefixTextField.getText());
            sb.append("[procedure_name]");
            sb.append(testUnitSuffixTextField.getText());
            if (withContext) {
                sb.append(i);
            }
            sb.append(";\n\n");
        }
        if (withContext) {
            sb.append("--%endcontext\n\n");
        }
        return sb;
    }

    private CharSequence utBodyTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or replace package body ");
        sb.append(testPackagePrefixTextField.getText());
        sb.append("[package_name]");
        sb.append(testPackageSuffixTextField.getText());
        sb.append(" is\n\n\t");
        sb.append(utBodyProcTemplate().toString().trim().replace("\n", "\n\t"));
        sb.append("\n\n");
        sb.append("end ");
        sb.append(testPackagePrefixTextField.getText());
        sb.append("[package_name]");
        sb.append(testPackageSuffixTextField.getText());
        sb.append(";\n");
        sb.append("/\n");
        return sb;
    }

    private CharSequence utBodyProcTemplate() {
        StringBuilder sb = new StringBuilder();
        final Integer numberOfTestsPerUnit = (Integer) numberOfTestsPerUnitModel.getValue();
        final boolean withContext = numberOfTestsPerUnit > 1;
        for (int i = 1; i <= numberOfTestsPerUnit; i++) {
            if (generateCommentsCheckBox.isSelected()) {
                sb.append("--\n");
                sb.append("-- test");
                if (withContext) {
                    sb.append(" [procedure_name] case ");
                    sb.append(i);
                    sb.append(": ...");
                }
                sb.append('\n');
                sb.append("--\n");
            }
            sb.append("procedure ");
            sb.append(testUnitPrefixTextField.getText());
            sb.append("[procedure_name]");
            sb.append(testUnitSuffixTextField.getText());
            if (withContext) {
                sb.append(i);
            }
            sb.append(" is\n");
            sb.append("\tl_actual   integer := 0;\n");
            sb.append("\tl_expected integer := 1;\n");
            sb.append("begin\n");
            if (generateCommentsCheckBox.isSelected()) {
                sb.append("\t-- arrange\n\n");
                sb.append("\t-- act\n\n");
                sb.append("\t-- assert\n");
            }
            sb.append("\tut.expect(l_actual).to_equal(l_expected);\n");
            sb.append("end ");
            sb.append(testUnitPrefixTextField.getText());
            sb.append("[procedure_name]");
            sb.append(testUnitSuffixTextField.getText());
            if (withContext) {
                sb.append(i);
            }
            sb.append(";\n\n");
        }
        return sb;
    }

    @Override
    public void onEntry(final TraversableContext traversableContext) {
        PreferenceModel info = getUserInformation(traversableContext);
        useRealtimeReporterCheckBox.setSelected(info.isUseRealtimeReporter());
        unsharedWorksheetCheckBox.setSelected(info.isUnsharedWorksheet());
        resetPackageCheckBox.setSelected(info.isResetPackage());
        clearScreenCheckBox.setSelected(info.isClearScreen());
        autoExecuteCheckBox.setSelected(info.isAutoExecute());
        checkRunUtplsqlTestCheckBox.setSelected(info.isCheckRunUtplsqlTest());
        useSmartTimesCheckBox.setSelected(info.isUseSmartTimes());
        numberOfRunsInHistorySpinner.setValue(info.getNumberOfRunsInHistory());
        showDisabledCounterCheckBox.setSelected(info.isShowDisabledCounter());
        showWarningsCounterCheckBox.setSelected(info.isShowWarningsCounter());
        showInfoCounterCheckBox.setSelected(info.isShowInfoCounter());
        showWarningIndicatorCheckBox.setSelected(info.isShowWarningIndicator());
        showInfoIndicatorCheckBox.setSelected(info.isShowInfoIndicator());
        showSuccessfulTestsCheckBox.setSelected(info.isShowSuccessfulTests());
        showDisabledTestsCheckBox.setSelected(info.isShowDisabledTests());
        showTestDescriptionCheckBox.setSelected(info.isShowTestDescription());
        syncDetailTabCheckBox.setSelected(info.isSyncDetailTab());
        showSuitesCheckBox.setSelected(info.isShowSuites());
        testPackagePrefixTextField.setText(info.getTestPackagePrefix());
        testPackageSuffixTextField.setText(info.getTestPackageSuffix());
        testUnitPrefixTextField.setText(info.getTestUnitPrefix());
        testUnitSuffixTextField.setText(info.getTestUnitSuffix());
        numberOfTestsPerUnitSpinner.setValue(info.getNumberOfTestsPerUnit());
        checkGenerateUtplsqlTestCheckBox.setSelected(info.isCheckGenerateUtplsqlTest());
        loadCodeTemplates();
        generateCommentsCheckBox.setSelected(info.isGenerateComments());
        disableTestsCheckBox.setSelected(info.isDisableTests());
        suitePathTextField.setText(info.getSuitePath());
        indentSpacesSpinner.setValue(info.getIndentSpaces());
        rootFolderInOddgenViewTextField.setText(info.getRootFolderInOddgenView());
        generateFilesCheckBox.setSelected(info.isGenerateFiles());
        outputDirectoryTextField.setText(info.getOutputDirectory());
        deleteExistingFilesCheckBox.setSelected(info.isDeleteExistingFiles());
        super.onEntry(traversableContext);
    }

    @Override
    public void onExit(final TraversableContext traversableContext) throws TraversalException {
        PreferenceModel info = getUserInformation(traversableContext);
        info.setUseRealtimeReporter(useRealtimeReporterCheckBox.isSelected());
        info.setUnsharedWorksheet(unsharedWorksheetCheckBox.isSelected());
        info.setResetPackage(resetPackageCheckBox.isSelected());
        info.setClearScreen(clearScreenCheckBox.isSelected());
        info.setAutoExecute(autoExecuteCheckBox.isSelected());
        info.setNumberOfRunsInHistory((Integer) numberOfRunsInHistorySpinner.getValue());
        info.setCheckRunUtplsqlTest(checkRunUtplsqlTestCheckBox.isSelected());
        info.setUseSmartTimes(useSmartTimesCheckBox.isSelected());
        info.setShowDisabledCounter(showDisabledCounterCheckBox.isSelected());
        info.setShowWarningsCounter(showWarningsCounterCheckBox.isSelected());
        info.setShowInfoCounter(showInfoCounterCheckBox.isSelected());
        info.setShowWarningIndicator(showWarningIndicatorCheckBox.isSelected());
        info.setShowInfoIndicator(showInfoIndicatorCheckBox.isSelected());
        info.setShowSuccessfulTests(showSuccessfulTestsCheckBox.isSelected());
        info.setShowDisabledTests(showDisabledTestsCheckBox.isSelected());
        info.setShowTestDescription(showTestDescriptionCheckBox.isSelected());
        info.setSyncDetailTab(syncDetailTabCheckBox.isSelected());
        info.setShowSuites(showSuitesCheckBox.isSelected());
        info.setTestPackagePrefix(testPackagePrefixTextField.getText());
        info.setTestPackageSuffix(testPackageSuffixTextField.getText());
        info.setTestUnitPrefix(testUnitPrefixTextField.getText());
        info.setTestUnitSuffix(testUnitSuffixTextField.getText());
        info.setNumberOfTestsPerUnit((Integer) numberOfTestsPerUnitSpinner.getValue());
        info.setCheckGenerateUtplsqlTest(checkGenerateUtplsqlTestCheckBox.isSelected());
        info.setGenerateComments(generateCommentsCheckBox.isSelected());
        info.setDisableTests(disableTestsCheckBox.isSelected());
        info.setSuitePath(suitePathTextField.getText());
        info.setIndentSpaces((Integer) indentSpacesSpinner.getValue());
        info.setRootFolderInOddgenView(rootFolderInOddgenViewTextField.getText());
        info.setGenerateFiles(generateFilesCheckBox.isSelected());
        info.setOutputDirectory(outputDirectoryTextField.getText());
        info.setDeleteExistingFiles(deleteExistingFilesCheckBox.isSelected());
        super.onExit(traversableContext);
    }

    private static PreferenceModel getUserInformation(final TraversableContext tc) {
        return PreferenceModel.getInstance(tc.getPropertyStorage());
    }
}
