package org.utplsql.sqldev.ui.runner

import java.util.LinkedHashMap
import javax.swing.Icon
import javax.swing.table.DefaultTableModel
import org.utplsql.sqldev.model.runner.Test
import org.utplsql.sqldev.resources.UtplsqlResources

class TestOverviewTableModel extends DefaultTableModel {
	LinkedHashMap<String, Test> tests
	
	new() {
		super()
	}
	
	def setModel(LinkedHashMap<String, Test> tests) {
		this.tests = tests
		fireTableDataChanged()
	}

	override getRowCount() {
		if (tests === null) {
			return 0
		}
		return tests.size()
	}

	override getColumnCount() {
		return 3
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
				return test.id
			}
			case 2: {
				return test.executionTime
			}
			default: {
				return null
			}
		}
	}

	override getColumnName(int col) {
		return #[UtplsqlResources.getString("RUNNER_TEST_STATUS"), UtplsqlResources.getString("RUNNER_TEST_ID"),
			UtplsqlResources.getString("RUNNER_TEST_EXECUTION_TIME")].get(col)
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
				return String
			}
			case 2: {
				return Double
			}
			default: {
				return String
			}
		}
	}
}
