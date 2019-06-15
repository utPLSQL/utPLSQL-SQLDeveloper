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
package org.utplsql.sqldev.ui.preference

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.Map
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTabbedPane
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel
import javax.swing.table.DefaultTableModel
import oracle.dbtools.raptor.templates.CodeTemplateUtil
import oracle.ide.panels.DefaultTraversablePanel
import oracle.ide.panels.TraversableContext
import oracle.ide.panels.TraversalException
import oracle.javatools.ui.layout.FieldLayoutBuilder
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.resources.UtplsqlResources
import org.utplsql.sqldev.ui.common.DirectoryChooser

class PreferencePanel extends DefaultTraversablePanel {
	val JPanel runTestPanel = new JPanel();
	val JCheckBox useRealtimeReporterCheckBox = new JCheckBox
	val JCheckBox unsharedWorksheetCheckBox = new JCheckBox
	val JCheckBox resetPackageCheckBox = new JCheckBox
	val JCheckBox clearScreenCheckBox = new JCheckBox
	val JCheckBox autoExecuteCheckBox = new JCheckBox
	val JCheckBox checkRunUtplsqlTestCheckBox = new JCheckBox
	val JPanel realtimeReporterPanel = new JPanel
	val JCheckBox showDisabledCounterCheckBox = new JCheckBox
	val JCheckBox showWarningsCounterCheckBox = new JCheckBox
	val JCheckBox showInfoCounterCheckBox = new JCheckBox
	val JCheckBox showWarningIndicatorCheckBox = new JCheckBox
	val JCheckBox showInfoIndicatorCheckBox = new JCheckBox
	val JCheckBox syncDetailTabCheckBox = new JCheckBox
	val JPanel generateTestPanel = new JPanel();
	val JTextField testPackagePrefixTextField = new JTextField
	val JTextField testPackageSuffixTextField = new JTextField
	val JTextField testUnitPrefixTextField = new JTextField
	val JTextField testUnitSuffixTextField = new JTextField
	val SpinnerNumberModel numberOfTestsPerUnitModel = new SpinnerNumberModel(1, 1, 10, 1);
	val JSpinner numberOfTestsPerUnitSpinner = new JSpinner(numberOfTestsPerUnitModel);
	val JCheckBox checkGenerateUtplsqlTestCheckBox = new JCheckBox
	val DefaultTableModel codeTemplatesModel = new DefaultTableModel(#["Id", "Template"], 0);
	val JButton createCodeTemplatesButton = new JButton()
	val JCheckBox generateCommentsCheckBox = new JCheckBox
	val JCheckBox disableTestsCheckBox = new JCheckBox
	val JTextField suitePathTextField = new JTextField
	val SpinnerNumberModel indentSpacesModel = new SpinnerNumberModel(1, 1, 8, 1);
	val JSpinner indentSpacesSpinner = new JSpinner(indentSpacesModel);
	val JPanel oddgenPanel = new JPanel();
	val JTextField rootFolderInOddgenViewTextField = new JTextField
	val JCheckBox generateFilesCheckBox = new JCheckBox
	val JTextField outputDirectoryTextField = new JTextField
	val JButton outputDirectoryBrowse = new JButton()
	val JCheckBox deleteExistingFilesCheckBox = new JCheckBox

	new() {
		layoutControls()
	}

	private def layoutControls() {
		// run test group
		val FieldLayoutBuilder runTab = new FieldLayoutBuilder(runTestPanel)
		runTab.alignLabelsLeft = true
		runTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_USE_REALTIME_REPORTER_LABEL")).component(
				useRealtimeReporterCheckBox))
		runTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_UNSHARED_WORKSHEET_LABEL")).component(
				unsharedWorksheetCheckBox))
		runTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_RESET_PACKAGE_LABEL")).component(
				resetPackageCheckBox))
		runTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_CLEAR_SCREEN_LABEL")).component(
				clearScreenCheckBox))
		runTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_AUTO_EXECUTE_LABEL")).component(
				autoExecuteCheckBox))
		runTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_CHECK_RUN_UTPLSQL_TEST_LABEL")).component(
				checkRunUtplsqlTestCheckBox))
		runTab.addVerticalSpring
		
		// realtime reporter group
		val FieldLayoutBuilder rrTab = new FieldLayoutBuilder(realtimeReporterPanel)
		rrTab.alignLabelsLeft = true
		rrTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_SHOW_DISABLED_COUNTER_LABEL")).component(
				showDisabledCounterCheckBox))
		rrTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_SHOW_WARNINGS_COUNTER_LABEL")).component(
				showWarningsCounterCheckBox))
		rrTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_SHOW_INFO_COUNTER_LABEL")).component(
				showInfoCounterCheckBox))
		rrTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_SHOW_WARNING_INDICATOR_LABEL")).component(
				showWarningIndicatorCheckBox))
		rrTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_SHOW_INFO_INDICATOR_LABEL")).component(
				showInfoIndicatorCheckBox))
		rrTab.add(
			runTab.field.label.withText(UtplsqlResources.getString("PREF_SYNC_DETAIL_TAB_LABEL")).component(
				syncDetailTabCheckBox))
		rrTab.addVerticalSpring

		// generate test group
		val FieldLayoutBuilder generateTab = new FieldLayoutBuilder(generateTestPanel)
		generateTab.alignLabelsLeft = true
		generateTab.stretchComponentsWithNoButton = true
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_PREFIX_LABEL")).component(
				testPackagePrefixTextField))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_SUFFIX_LABEL")).component(
				testPackageSuffixTextField))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_TEST_UNIT_PREFIX_LABEL")).component(
				testUnitPrefixTextField))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_TEST_UNIT_SUFFIX_LABEL")).component(
				testUnitSuffixTextField))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_NUMBER_OF_TESTS_PER_UNIT_LABEL")).component(
				numberOfTestsPerUnitSpinner))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_GENERATE_COMMENTS_LABEL")).component(
				generateCommentsCheckBox))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_DISABLE_TESTS_LABEL")).component(
				disableTestsCheckBox))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_SUITE_PATH_LABEL")).component(
				suitePathTextField))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_INDENT_SPACES_LABEL")).component(
				indentSpacesSpinner))
		generateTab.add(
			generateTab.field.label.withText(UtplsqlResources.getString("PREF_CHECK_GENERATE_UTPLSQL_TEST_LABEL")).component(
				checkGenerateUtplsqlTestCheckBox).button(createCodeTemplatesButton).withText(
				UtplsqlResources.getString("PREF_CREATE_CODE_TEMPLATES_BUTTON_LABEL")))
		generateTab.addVerticalSpring

		// oddgen group
		val FieldLayoutBuilder oddgenTab = new FieldLayoutBuilder(oddgenPanel)
		oddgenTab.alignLabelsLeft = true
		oddgenTab.stretchComponentsWithNoButton = true
		oddgenTab.add(
			oddgenTab.field.label.withText(UtplsqlResources.getString("PREF_ROOT_FOLDER_IN_ODDGEN_VIEW_LABEL")).component(
				rootFolderInOddgenViewTextField))
		oddgenTab.add(
			oddgenTab.field.label.withText(UtplsqlResources.getString("PREF_GENERATE_FILES_LABEL")).component(
				generateFilesCheckBox))
		oddgenTab.add(
			oddgenTab.field.label.withText(UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_LABEL")).component(
				outputDirectoryTextField).button(outputDirectoryBrowse).withText(
				UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_BUTTON_LABEL")))
		oddgenTab.add(
			oddgenTab.field.label.withText(UtplsqlResources.getString("PREF_DELETE_EXISTING_FILES_LABEL")).component(
				deleteExistingFilesCheckBox))
		oddgenTab.addVerticalSpring
				
		// putting groups into tabbed panes
		val tabbedPane = new JTabbedPane()
		tabbedPane.add(UtplsqlResources.getString("MENU_RUN_TEST_LABEL"), runTestPanel)
		tabbedPane.add(UtplsqlResources.getString("MENU_REALTIME_REPORTER_LABEL"), realtimeReporterPanel)
		tabbedPane.add(UtplsqlResources.getString("MENU_GENERATE_TEST_LABEL"), generateTestPanel)
		tabbedPane.add("oddgen", oddgenPanel)	
		val FieldLayoutBuilder builder = new FieldLayoutBuilder(this)
		builder.alignLabelsLeft = true
		builder.addVerticalField("", tabbedPane)
		builder.addVerticalSpring
		
		// register action listener for create code template button 
		createCodeTemplatesButton.addActionListener(new ActionListener() {
			override actionPerformed(ActionEvent event) {
				saveCodeTemplates
			}			
		})
		
		// register action listener for directory chooser
		outputDirectoryBrowse.addActionListener(new ActionListener() {
			override actionPerformed(ActionEvent event) {
				DirectoryChooser.choose(null, UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_LABEL"),
					outputDirectoryTextField)
			}
		})		
	}
	
	private def loadCodeTemplates() {
		val Map<String, String> map = CodeTemplateUtil.loadFiles()
		for (key : map.keySet) {
			codeTemplatesModel.addRow(#[key, map.get(key)])
		}
	}
	
	private def saveCodeTemplates() {
		codeTemplatesModel.addRow(#["ut_spec", utSpecTemplate.replaceTabsWithSpaces])
		codeTemplatesModel.addRow(#["ut_spec_proc", utSpecProcTemplate.replaceTabsWithSpaces.trimPlusNewLine])
		codeTemplatesModel.addRow(#["ut_body", utBodyTemplate.replaceTabsWithSpaces])
		codeTemplatesModel.addRow(#["ut_body_proc", utBodyProcTemplate.replaceTabsWithSpaces.trimPlusNewLine])
		CodeTemplateUtil.save(codeTemplatesModel)
	}

	private def replaceTabsWithSpaces(CharSequence input) {
		val spaces = String.format("%1$"+indentSpacesSpinner.value+"s", "")
		return input.toString.replace("\t", spaces)
	}
	
	private def trimPlusNewLine(String input) {
		input.trim + System.lineSeparator
	}
	
	private def utSpecTemplate() '''
		CREATE OR REPLACE PACKAGE «testPackagePrefixTextField.text»[package_name]«testPackageSuffixTextField.text» IS
		
			--%suite
			«IF !suitePathTextField.text.empty»
				--%suitepath(«suitePathTextField.text»)
			«ENDIF»
		
			«utSpecProcTemplate»
		END «testPackagePrefixTextField.text»[package_name]«testPackageSuffixTextField.text»;
		/
	'''

	private def utSpecProcTemplate() '''
		«val withContext = numberOfTestsPerUnitModel.value as Integer > 1»
		«IF withContext»
			--%context([procedure_name])

		«ENDIF»
		«FOR i : 1 .. numberOfTestsPerUnitModel.value as Integer»
			--%test
			«IF disableTestsCheckBox.selected»
				--%disabled
			«ENDIF»
			PROCEDURE «testUnitPrefixTextField.text»[procedure_name]«testUnitSuffixTextField.text»«IF withContext»«i»«ENDIF»;

		«ENDFOR»
		«IF withContext»
			--%endcontext

		«ENDIF»
	'''

	private def utBodyTemplate() '''
		CREATE OR REPLACE PACKAGE BODY «testPackagePrefixTextField.text»[package_name]«testPackageSuffixTextField.text» IS
		
			«utBodyProcTemplate»
		END «testPackagePrefixTextField.text»[package_name]«testPackageSuffixTextField.text»;
		/
	'''

	private def utBodyProcTemplate() '''
		«val withContext = numberOfTestsPerUnitModel.value as Integer > 1»
		«FOR i : 1 .. numberOfTestsPerUnitModel.value as Integer»
			«IF generateCommentsCheckBox.selected»
				--
				-- test«IF withContext» [procedure_name] case «i»: ...«ENDIF»
				--
			«ENDIF»
			PROCEDURE «testUnitPrefixTextField.text»[procedure_name]«testUnitSuffixTextField.text»«IF withContext»«i»«ENDIF» IS
				l_actual   INTEGER := 0;
				l_expected INTEGER := 1;
			BEGIN
				«IF generateCommentsCheckBox.selected»
					-- populate actual
					-- ...

					-- populate expected
					-- ...

					-- assert
				«ENDIF»
				ut.expect(l_actual).to_equal(l_expected);
			END «testUnitPrefixTextField.text»[procedure_name]«testUnitSuffixTextField.text»«IF withContext»«i»«ENDIF»;

		«ENDFOR»
	'''

	override onEntry(TraversableContext traversableContext) {
		var PreferenceModel info = traversableContext.userInformation
		useRealtimeReporterCheckBox.selected = info.useRealtimeReporter
		unsharedWorksheetCheckBox.selected = info.unsharedWorksheet
		resetPackageCheckBox.selected = info.resetPackage
		clearScreenCheckBox.selected = info.clearScreen
		autoExecuteCheckBox.selected = info.autoExecute
		checkRunUtplsqlTestCheckBox.selected = info.checkRunUtplsqlTest
		showDisabledCounterCheckBox.selected = info.showDisabledCounter
		showWarningsCounterCheckBox.selected = info.showWarningsCounter
		showInfoCounterCheckBox.selected = info.showInfoCounter
		showWarningIndicatorCheckBox.selected = info.showWarningIndicator
		showInfoIndicatorCheckBox.selected = info.showInfoIndicator
		syncDetailTabCheckBox.selected = info.syncDetailTab
		testPackagePrefixTextField.text = info.testPackagePrefix
		testPackageSuffixTextField.text = info.testPackageSuffix
		testUnitPrefixTextField.text = info.testUnitPrefix
		testUnitSuffixTextField.text = info.testUnitSuffix
		numberOfTestsPerUnitSpinner.value = info.numberOfTestsPerUnit
		checkGenerateUtplsqlTestCheckBox.selected = info.checkGenerateUtplsqlTest
		loadCodeTemplates
		generateCommentsCheckBox.selected = info.generateComments
		disableTestsCheckBox.selected = info.disableTests
		suitePathTextField.text = info.suitePath
		indentSpacesSpinner.value = info.indentSpaces
		rootFolderInOddgenViewTextField.text = info.rootFolderInOddgenView
		generateFilesCheckBox.selected = info.generateFiles
		outputDirectoryTextField.text = info.outputDirectory
		deleteExistingFilesCheckBox.selected = info.deleteExistingFiles
		super.onEntry(traversableContext)
	}

	override onExit(TraversableContext traversableContext) throws TraversalException {
		var PreferenceModel info = traversableContext.userInformation
		info.useRealtimeReporter = useRealtimeReporterCheckBox.selected
		info.unsharedWorksheet = unsharedWorksheetCheckBox.selected
		info.resetPackage = resetPackageCheckBox.selected
		info.clearScreen = clearScreenCheckBox.selected
		info.autoExecute = autoExecuteCheckBox.selected
		info.checkRunUtplsqlTest = checkRunUtplsqlTestCheckBox.selected
		info.showDisabledCounter = showDisabledCounterCheckBox.selected
		info.showWarningsCounter = showWarningsCounterCheckBox.selected
		info.showInfoCounter = showInfoCounterCheckBox.selected
		info.showWarningIndicator = showWarningIndicatorCheckBox.selected
		info.showInfoIndicator = showInfoIndicatorCheckBox.selected
		info.syncDetailTab = syncDetailTabCheckBox.selected
		info.testPackagePrefix = testPackagePrefixTextField.text
		info.testPackageSuffix = testPackageSuffixTextField.text
		info.testUnitPrefix = testUnitPrefixTextField.text
		info.testUnitSuffix = testUnitSuffixTextField.text
		info.numberOfTestsPerUnit = numberOfTestsPerUnitSpinner.value as Integer
		info.checkGenerateUtplsqlTest = checkGenerateUtplsqlTestCheckBox.selected
		info.generateComments = generateCommentsCheckBox.selected
		info.disableTests = disableTestsCheckBox.selected
		info.suitePath = suitePathTextField.text
		info.indentSpaces = indentSpacesSpinner.value as Integer
		info.rootFolderInOddgenView = rootFolderInOddgenViewTextField.text
		info.generateFiles = generateFilesCheckBox.selected
		info.outputDirectory = outputDirectoryTextField.text
		info.deleteExistingFiles = deleteExistingFilesCheckBox.selected
		super.onExit(traversableContext)
	}

	private def static PreferenceModel getUserInformation(TraversableContext tc) {
		return PreferenceModel.getInstance(tc.propertyStorage)
	}
}
