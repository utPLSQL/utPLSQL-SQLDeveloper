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
package org.utplsql.sqldev

import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.SpinnerNumberModel
import oracle.ide.panels.DefaultTraversablePanel
import oracle.ide.panels.TraversableContext
import oracle.ide.panels.TraversalException
import oracle.javatools.ui.layout.FieldLayoutBuilder
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.resources.UtplsqlResources

class PreferencePanel extends DefaultTraversablePanel {
	val JPanel runTestPanel = new JPanel();
	val JCheckBox unsharedWorksheetCheckBox = new JCheckBox
	val JCheckBox resetPackageCheckBox = new JCheckBox
	val JCheckBox clearScreenCheckBox = new JCheckBox
	val JCheckBox autoExecuteCheckBox = new JCheckBox
	val JCheckBox checkRunUtplsqlTestCheckBox = new JCheckBox
	val JPanel generateTestPanel = new JPanel();
	val JTextField testPackagePrefixTextField = new JTextField
	val JTextField testPackageSuffixTextField = new JTextField
	val JTextField testUnitPrefixTextField = new JTextField
	val JTextField testUnitSuffixTextField = new JTextField
	val SpinnerNumberModel numberOfTestsPerUnitModel = new SpinnerNumberModel(1, 1, 10, 1);
	val JSpinner numberOfTestsPerUnitSpinner = new JSpinner(numberOfTestsPerUnitModel);
	val JCheckBox checkGenerateUtplsqlTestCheckBox = new JCheckBox

	new() {
		layoutControls()
	}

	def private layoutControls() {
		runTestPanel.border = BorderFactory.createTitledBorder(UtplsqlResources.getString("MENU_RUN_TEST_LABEL"))
		val FieldLayoutBuilder b1 = new FieldLayoutBuilder(runTestPanel)
		b1.alignLabelsLeft = true
		b1.add(
			b1.field.label.withText(UtplsqlResources.getString("PREF_UNSHARED_WORKSHEET_LABEL")).component(
				unsharedWorksheetCheckBox))
		b1.add(
			b1.field.label.withText(UtplsqlResources.getString("PREF_RESET_PACKAGE_LABEL")).component(
				resetPackageCheckBox))
		b1.add(
			b1.field.label.withText(UtplsqlResources.getString("PREF_CLEAR_SCREEN_LABEL")).component(
				clearScreenCheckBox))
		b1.add(
			b1.field.label.withText(UtplsqlResources.getString("PREF_AUTO_EXECUTE_LABEL")).component(
				autoExecuteCheckBox))
		b1.add(
			b1.field.label.withText(UtplsqlResources.getString("PREF_CHECK_RUN_UTPLSQL_TEST_LABEL")).component(
				checkRunUtplsqlTestCheckBox))
		generateTestPanel.border = BorderFactory.createTitledBorder(UtplsqlResources.getString("MENU_GENERATE_TEST_LABEL"))
		val FieldLayoutBuilder b2 = new FieldLayoutBuilder(generateTestPanel)
		b2.alignLabelsLeft = true
		b2.add(
			b2.field.label.withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_PREFIX_LABEL")).component(
				testPackagePrefixTextField))
		b2.add(
			b2.field.label.withText(UtplsqlResources.getString("PREF_TEST_PACKAGE_SUFFIX_LABEL")).component(
				testPackageSuffixTextField))
		b2.add(
			b2.field.label.withText(UtplsqlResources.getString("PREF_TEST_UNIT_PREFIX_LABEL")).component(
				testUnitPrefixTextField))
		b2.add(
			b2.field.label.withText(UtplsqlResources.getString("PREF_TEST_UNIT_SUFFIX_LABEL")).component(
				testUnitSuffixTextField))
		b2.add(
			b2.field.label.withText(UtplsqlResources.getString("PREF_NUMBER_OF_TESTS_PER_UNIT")).component(
				numberOfTestsPerUnitSpinner))
		b2.add(
			b2.field.label.withText(UtplsqlResources.getString("PREF_CHECK_GENERATE_UTPLSQL_TEST_LABEL")).component(
				checkGenerateUtplsqlTestCheckBox))
		val FieldLayoutBuilder builder = new FieldLayoutBuilder(this)
		builder.alignLabelsLeft = true
		builder.addVerticalField("", runTestPanel)
		builder.addVerticalField("", generateTestPanel)
		builder.addVerticalSpring
	}

	override onEntry(TraversableContext traversableContext) {
		var PreferenceModel info = traversableContext.userInformation
		unsharedWorksheetCheckBox.selected = info.unsharedWorksheet
		resetPackageCheckBox.selected = info.resetPackage
		clearScreenCheckBox.selected = info.clearScreen
		autoExecuteCheckBox.selected = info.autoExecute
		checkRunUtplsqlTestCheckBox.selected = info.checkRunUtplsqlTest
		testPackagePrefixTextField.text = info.testPackagePrefix
		testPackageSuffixTextField.text = info.testPackageSuffix
		testUnitPrefixTextField.text = info.testUnitPrefix
		testUnitSuffixTextField.text = info.testUnitSuffix
		numberOfTestsPerUnitSpinner.value = info.numberOfTestsPerUnit
		checkGenerateUtplsqlTestCheckBox.selected = info.checkGenerateUtplsqlTest
		super.onEntry(traversableContext)
	}

	override onExit(TraversableContext traversableContext) throws TraversalException {
		var PreferenceModel info = traversableContext.userInformation
		info.unsharedWorksheet = unsharedWorksheetCheckBox.selected
		info.resetPackage = resetPackageCheckBox.selected
		info.clearScreen = clearScreenCheckBox.selected
		info.autoExecute = autoExecuteCheckBox.selected
		info.checkRunUtplsqlTest = checkRunUtplsqlTestCheckBox.selected
		info.testPackagePrefix = testPackagePrefixTextField.text
		info.testPackageSuffix = testPackageSuffixTextField.text
		info.testUnitPrefix = testUnitPrefixTextField.text
		info.testUnitSuffix = testUnitSuffixTextField.text
		info.numberOfTestsPerUnit = numberOfTestsPerUnitSpinner.value as Integer
		info.checkGenerateUtplsqlTest = checkGenerateUtplsqlTestCheckBox.selected
		super.onExit(traversableContext)
	}

	def private static PreferenceModel getUserInformation(TraversableContext tc) {
		return PreferenceModel.getInstance(tc.propertyStorage)
	}
}
