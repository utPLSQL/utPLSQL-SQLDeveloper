/*
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
 package org.utplsql.sqldev.ui.runner

import java.util.LinkedHashMap
import javax.swing.Icon
import javax.swing.table.DefaultTableModel
import org.utplsql.sqldev.model.PrefixTools
import org.utplsql.sqldev.model.runner.Test
import org.utplsql.sqldev.resources.UtplsqlResources

class TestOverviewTableModel extends DefaultTableModel {
	LinkedHashMap<String, Test> tests
	String commonPrefix
	boolean commonPrefixCalculated
	boolean showDescription
	boolean useSmartTimes
	
	new() {
		super()
	}
	
	private def calcCommonPrefix() {
		if (!commonPrefixCalculated && tests !== null && tests.size > 0) {
			this.commonPrefix = PrefixTools.commonPrefix(tests.keySet.toList)
			fireTableDataChanged()
			commonPrefixCalculated = true
		}
	}
	
	def setModel(LinkedHashMap<String, Test> tests, boolean showDescription, boolean useSmartTimes) {
		commonPrefixCalculated = false
		this.tests = tests
		this.showDescription = showDescription
		this.useSmartTimes = useSmartTimes
		calcCommonPrefix
		fireTableDataChanged()
	}
	
	def updateModel(boolean showDescription) {
		this.showDescription = showDescription
		fireTableDataChanged()
	}
	
	def getTestIdColumnName() {
		calcCommonPrefix
		if (commonPrefix === null || commonPrefix == "") {
			if (showDescription) {
				UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL")
			} else {
				UtplsqlResources.getString("RUNNER_TEST_ID_COLUMN")			
			}
		} else {
			if (showDescription) {
				'''«UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL")» («commonPrefix»)'''
			} else {
				commonPrefix
			}
		}
	}
	
	def getTimeColumnName() {
		val timeColumnName = '''«UtplsqlResources.getString("RUNNER_TEST_EXECUTION_TIME_COLUMN")»«IF !useSmartTimes» [s]«ENDIF»'''
		return timeColumnName		
	}
	
	def getTest(int row) {
		val entry = tests.entrySet.get(row)
		val test = tests.get(entry.key)
		return test
	}

	override getRowCount() {
		if (tests === null) {
			return 0
		}
		return tests.size()
	}

	override getColumnCount() {
		return 5
	}

	override getValueAt(int row, int col) {
		val test = tests.entrySet.get(row).value
		if (test === null) {
			return null
		}
		switch (col) {
			case 0: {
				return test.statusIcon
			}
			case 1: {
				return test.warningIcon
			}
			case 2: {
				return test.infoIcon
			}
			case 3: {
				return if(showDescription && test.description !== null) {
							test.description
						} else {
							test.id.substring(if(commonPrefix === null) {0} else {commonPrefix.length})
						}
			}
			case 4: {
				return test.executionTime
			}
			default: {
				return null
			}
		}
	}

	override getColumnName(int col) {
		return #["", "", "", UtplsqlResources.getString(if (showDescription) {"RUNNER_DESCRIPTION_LABEL"} else {"RUNNER_TEST_ID_COLUMN"}),
			timeColumnName].get(col)
	}

	override isCellEditable(int row, int column) {
		return false
	}

	override getColumnClass(int col) {
		switch (col) {
			case 0: {
				return Icon
			}
			case 1: {
				return Icon
			}
			case 2: {
				return Icon
			}
			case 3: {
				return String
			}
			case 4: {
				return Double
			}
			default: {
				return String
			}
		}
	}
}
