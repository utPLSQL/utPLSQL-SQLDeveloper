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

import javax.swing.JCheckBox
import oracle.ide.panels.DefaultTraversablePanel
import oracle.ide.panels.TraversableContext
import oracle.ide.panels.TraversalException
import oracle.javatools.ui.layout.FieldLayoutBuilder
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.resources.UtplsqlResources

class PreferencePanel extends DefaultTraversablePanel {
	val JCheckBox unsharedWorksheetCheckBox = new JCheckBox
	val JCheckBox resetPackageCheckBox = new JCheckBox
	val JCheckBox autoExecuteCheckBox = new JCheckBox

	new() {
		layoutControls()
	}

	def private layoutControls() {
		val FieldLayoutBuilder builder = new FieldLayoutBuilder(this)
		builder.alignLabelsLeft = true
		builder.add(
			builder.field.label.withText(UtplsqlResources.getString("PREF_UNSHARED_WORKSHEET_LABEL")).component(
				unsharedWorksheetCheckBox))
		builder.add(
			builder.field.label.withText(UtplsqlResources.getString("PREF_RESET_PACKAGE_LABEL")).component(
				resetPackageCheckBox))
		builder.add(
			builder.field.label.withText(UtplsqlResources.getString("PREF_AUTO_EXECUTE_LABEL")).component(
				autoExecuteCheckBox))
		builder.addVerticalSpring
	}

	override onEntry(TraversableContext traversableContext) {
		var PreferenceModel info = traversableContext.userInformation
		unsharedWorksheetCheckBox.selected = info.unsharedWorksheet
		resetPackageCheckBox.selected = info.resetPackage
		autoExecuteCheckBox.selected = info.autoExecute
		super.onEntry(traversableContext)
	}

	override onExit(TraversableContext traversableContext) throws TraversalException {
		var PreferenceModel info = traversableContext.userInformation
		info.unsharedWorksheet = unsharedWorksheetCheckBox.selected
		info.resetPackage = resetPackageCheckBox.selected
		info.autoExecute = autoExecuteCheckBox.selected
		super.onExit(traversableContext)
	}

	def private static PreferenceModel getUserInformation(TraversableContext tc) {
		return PreferenceModel.getInstance(tc.propertyStorage)
	}
}
