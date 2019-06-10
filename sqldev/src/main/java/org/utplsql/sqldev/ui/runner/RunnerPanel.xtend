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

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.plaf.basic.BasicProgressBarUI
import org.utplsql.sqldev.model.LimitedLinkedHashMap
import org.utplsql.sqldev.model.runner.Run
import org.utplsql.sqldev.resources.UtplsqlResources

class RunnerPanel {
	static val GREEN = new Color(0, 153, 0)
	static val RED = new Color(153, 0, 0)
	LimitedLinkedHashMap<String, Run> runs = new LimitedLinkedHashMap<String, Run>(10)
	JPanel basePanel
	JLabel statusLabel
	JLabel testCounterValueLabel
	JLabel errorCounterValueLabel
	JLabel failureCounterValueLabel
	JProgressBar progressBar;
	TestOverviewTableModel testOverviewTableModel
	JTable testOverviewTable
	
	def Component getGUI() {
		if (basePanel === null) {
			initializeGUI()
		}
		return basePanel
	}
	
	def setModel(Run run) {
		runs.put(run.reporterId, run)
		testOverviewTableModel.model = run.tests
	}
	
	def update(String reporterId) {
		val run = runs.get(reporterId)
		val row = run.totalNumberOfCompletedTests - 1
		if (row < 0) {
			testOverviewTableModel.fireTableDataChanged
		} else {
			testOverviewTableModel.fireTableRowsUpdated(row, row)
		}
		statusLabel.text = run.status
		testCounterValueLabel.text = '''«run.totalNumberOfCompletedTests»/«run.totalNumberOfTests»'''
		errorCounterValueLabel.text = '''«run.counter.error»'''
		failureCounterValueLabel.text = '''«run.counter.failure»'''
		if (run.totalNumberOfTests == 0) {
			progressBar.value = 100
		} else {
			progressBar.value = Math.round(100 * run.totalNumberOfCompletedTests / run.totalNumberOfTests)
		}
		if (run.counter.error > 0 || run.counter.failure > 0) {
			progressBar.foreground = RED
		} else {
			progressBar.foreground = GREEN
		}
	}
		
	private def initializeGUI() {
		// Base panel containing all components 
		basePanel = new JPanel()
		basePanel.setLayout(new GridBagLayout())
		var GridBagConstraints c = new GridBagConstraints()
		
		// Status line
		statusLabel = new JLabel
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 6
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		basePanel.add(statusLabel, c)
		
		// Test counter
		val testCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_TESTS_LABEL") + ":",
			UtplsqlResources.getIcon("UTPLSQL_ICON"), JLabel::LEADING)
		c.gridx = 0
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 5) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		basePanel.add(testCounterLabel, c)
		testCounterValueLabel = new JLabel
		c.gridx = 1
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 0, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		basePanel.add(testCounterValueLabel, c)
		
		// Error counter
		val errorCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_ERRORS_LABEL") + ":",
			UtplsqlResources.getIcon("ERROR_ICON"), JLabel::LEADING)
		c.gridx = 2
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 5) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		basePanel.add(errorCounterLabel, c)
		errorCounterValueLabel = new JLabel
		c.gridx = 3
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 0, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		basePanel.add(errorCounterValueLabel, c)		

		// Failure counter
		val failureCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_FAILURES_LABEL") + ":",
			UtplsqlResources.getIcon("FAILURE_ICON"), JLabel::LEADING)
		c.gridx = 4
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 5) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		basePanel.add(failureCounterLabel, c)
		failureCounterValueLabel = new JLabel
		c.gridx = 5
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 0, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		basePanel.add(failureCounterValueLabel, c)
		
		// Progress bar
		progressBar = new JProgressBar
		val progressBarDim = new Dimension(10, 20)
		progressBar.preferredSize = progressBarDim
		progressBar.minimumSize = progressBarDim
		progressBar.stringPainted = false
		progressBar.foreground = GREEN
		progressBar.UI = new BasicProgressBarUI
		c.gridx = 0
		c.gridy = 2
		c.gridwidth = 6
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		basePanel.add(progressBar, c)

		// Test overview - first part of the horizontal split pane
		testOverviewTableModel = new TestOverviewTableModel
		testOverviewTable = new JTable(testOverviewTableModel)
		val overviewTableIcon = testOverviewTable.columnModel.getColumn(0)
		overviewTableIcon.minWidth = 20
		overviewTableIcon.preferredWidth = 20
		overviewTableIcon.maxWidth = 20
		val overviewTableTime = testOverviewTable.columnModel.getColumn(2)
		overviewTableTime.preferredWidth = 60
		overviewTableTime.maxWidth = 100
		testOverviewTable.tableHeader.reorderingAllowed = false
		val testOverviewScrollPane = new JScrollPane(testOverviewTable)
		c.gridx = 0
		c.gridy = 3
		c.gridwidth = 6
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		basePanel.add(testOverviewScrollPane, c)		
		
		// Test details tabbed pane - second part of the horizontal split pane

		
	}
}