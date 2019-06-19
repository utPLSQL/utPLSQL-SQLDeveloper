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

import java.util.List
import javax.swing.table.DefaultTableModel
import org.utplsql.sqldev.model.runner.Expectation
import org.utplsql.sqldev.resources.UtplsqlResources

class FailuresTableModel extends DefaultTableModel {
	List<Expectation> failedExpectations
	
	new() {
		super()
	}
	
	def setModel(List<Expectation> failedExpectations) {
		this.failedExpectations = failedExpectations
	}

	def getExpectation(int row) {
		return failedExpectations.get(row)
	}
	
	override getRowCount() {
		if (failedExpectations === null) {
			return 0
		}
		return failedExpectations.size()
	}

	override getColumnCount() {
		return 2
	}

	override getValueAt(int row, int col) {
		val expectation = failedExpectations.get(row)
		if (expectation === null) {
			return null
		}
		switch (col) {
			case 0: {
				return row + 1
			}
			case 1: {
				return expectation.shortFailureText
			}
			default: {
				return null
			}
		}
	}

	override getColumnName(int col) {
		return #["#", UtplsqlResources.getString("RUNNER_ASSERT_DESCRIPTION_COLUMN")].get(col)
	}

	override isCellEditable(int row, int column) {
		return false
	}

	override getColumnClass(int col) {
		switch (col) {
			case 0: {
				return Integer
			}
			case 1: {
				return String
			}
			default: {
				return String
			}
		}
	}
}
