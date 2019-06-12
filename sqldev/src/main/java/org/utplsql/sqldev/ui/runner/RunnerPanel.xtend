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
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.text.DecimalFormat
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicProgressBarUI
import javax.swing.table.DefaultTableCellRenderer
import org.utplsql.sqldev.model.LimitedLinkedHashMap
import org.utplsql.sqldev.model.runner.Run
import org.utplsql.sqldev.resources.UtplsqlResources

class RunnerPanel implements FocusListener {
	static val GREEN = new Color(0, 153, 0)
	static val RED = new Color(153, 0, 0)
	LimitedLinkedHashMap<String, Run> runs = new LimitedLinkedHashMap<String, Run>(10)
	JPanel basePanel
	JLabel statusLabel
	JLabel testCounterValueLabel
	JLabel errorCounterValueLabel
	JLabel failureCounterValueLabel
	JLabel disabledCounterValueLabel
	JLabel warningsCounterValueLabel
	JProgressBar progressBar;
	TestOverviewTableModel testOverviewTableModel
	JTable testOverviewTable
	JTextArea testIdTextArea
	JTextField testOwnerTextField
	JTextField testPackageTextField
	JTextField testProcedureTextField
	JTextArea testDescriptionTextArea
	JTextField testStartTextField
	JTextField testEndTextField
	JTextArea testFailureDescriptionTextArea
	JTextArea testFailureMessageTextArea
	JTextArea testFailureCallerTextArea
	JTextArea testServerOutputTextArea
	JTextArea testErrorStackTextArea
	
	def Component getGUI() {
		if (basePanel === null) {
			initializeGUI()
		}
		return basePanel
	}
	
	def setModel(Run run) {
		runs.put(run.reporterId, run)
		testOverviewTableModel.model = run.tests
		testOverviewTable.rowSorter.sortKeys = null
		testIdTextArea.text = null
		testOwnerTextField.text = null
		testPackageTextField.text = null
		testProcedureTextField.text = null
		testDescriptionTextArea.text = null
		testStartTextField.text = null
		testEndTextField.text = null
		testFailureDescriptionTextArea.text = null
		testFailureMessageTextArea.text = null
		testFailureCallerTextArea.text = null
		testServerOutputTextArea.text = null
		testErrorStackTextArea.text = null
	}
	
	def update(String reporterId) {
		val run = runs.get(reporterId)
		val row = run.totalNumberOfCompletedTests - 1
		val header = testOverviewTableModel.testIdColumnName
		val idColumn = testOverviewTable.columnModel.getColumn(1)
		if (idColumn.headerValue != header) {
			idColumn.headerValue = header
			testOverviewTable.tableHeader.repaint
		}
		if (row < 0) {
			testOverviewTableModel.fireTableDataChanged
		} else {
			if (testOverviewTableModel.rowCount > row) {
				testOverviewTableModel.fireTableRowsUpdated(row, row)
				val positionOfCurrentTest = testOverviewTable.getCellRect(row, 0, true);
				testOverviewTable.scrollRectToVisible = positionOfCurrentTest
			}
		}
		statusLabel.text = run.status
		testCounterValueLabel.text = '''«run.totalNumberOfCompletedTests»/«run.totalNumberOfTests»'''
		errorCounterValueLabel.text = '''«run.counter.error»'''
		failureCounterValueLabel.text = '''«run.counter.failure»'''
		disabledCounterValueLabel.text = '''«run.counter.disabled»'''
		warningsCounterValueLabel.text = '''«run.counter.warning»'''
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

	override void focusGained(FocusEvent e) {
		if (e.source == testIdTextArea) {
			testIdTextArea.caret.visible = true
		} else if (e.source == testDescriptionTextArea) {
			testDescriptionTextArea.caret.visible = true
		} else if (e.source == testFailureDescriptionTextArea) {
			testFailureDescriptionTextArea.caret.visible = true
		} else if (e.source == testFailureMessageTextArea) {
			testFailureMessageTextArea.caret.visible = true
		} else if (e.source == testFailureCallerTextArea) {
			testFailureCallerTextArea.caret.visible = true
		} else if (e.source == testServerOutputTextArea) {
			testServerOutputTextArea.caret.visible = true
		} else if (e.source == testErrorStackTextArea) {
			testErrorStackTextArea.caret.visible = true
		}
	}

	override focusLost(FocusEvent e) {
		if (e.source == testIdTextArea) {
			testIdTextArea.caret.visible = false
		} else if (e.source == testDescriptionTextArea) {
			testDescriptionTextArea.caret.visible = false
		} else if (e.source == testFailureDescriptionTextArea) {
			testFailureDescriptionTextArea.caret.visible = false
		} else if (e.source == testFailureMessageTextArea) {
			testFailureMessageTextArea.caret.visible = false
		} else if (e.source == testFailureCallerTextArea) {
			testFailureCallerTextArea.caret.visible = false
		} else if (e.source == testServerOutputTextArea) {
			testServerOutputTextArea.caret.visible = false
		} else if (e.source == testErrorStackTextArea) {
			testErrorStackTextArea.caret.visible = false
		}
	}
	
	private static def formatDateTime(String dateTime) {
		if (dateTime === null) {
			return null
		} else {
			if (dateTime.length == 26) {
				return dateTime.replace("T", " ").substring(0, 23)
			} else {
				return dateTime
			}
		}
	}

	static class TestOverviewRowListener implements ListSelectionListener {
		RunnerPanel p
		
		new (RunnerPanel p) {
			this.p = p
		}
	
		override void valueChanged(ListSelectionEvent event) {
			val rowIndex = p.testOverviewTable.selectedRow
			if (rowIndex != -1) {
				val row =  p.testOverviewTable.convertRowIndexToModel(rowIndex)
				val test = p.testOverviewTableModel.getTest(row)
				p.testIdTextArea.text = test.id
				p.testOwnerTextField.text = test.ownerName
				p.testPackageTextField.text = test.objectName
				p.testProcedureTextField.text = test.procedureName
				p.testDescriptionTextArea.text = test.description
				p.testStartTextField.text = formatDateTime(test.startTime)
				p.testEndTextField.text = formatDateTime(test.endTime)
				if (test.failedExpectations !== null && test.failedExpectations.size > 0) {
					val expectation = test.failedExpectations.get(0)
					p.testFailureDescriptionTextArea.text = expectation.description
					p.testFailureMessageTextArea.text = expectation.message
					p.testFailureCallerTextArea.text = expectation.caller
				} else {
					p.testFailureDescriptionTextArea.text = null
					p.testFailureMessageTextArea.text = null
					p.testFailureCallerTextArea.text = null
				}
				p.testServerOutputTextArea.text = test.serverOutput
				p.testErrorStackTextArea.text = test.errorStack
			}
		}		
	}

   static class TimeFormatRenderer extends DefaultTableCellRenderer {
		static val DecimalFormat formatter = new DecimalFormat("#,##0.000")

		override getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {
			val renderedValue = if (value === null) {null} else {formatter.format(value as Number)}
			return super.getTableCellRendererComponent(table, renderedValue, isSelected, hasFocus, row, col)
		}
	}
	
	static class TestTableHeaderRenderer extends DefaultTableCellRenderer {
		override getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {
			val renderer = table.getTableHeader().getDefaultRenderer()
			val label = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
			label.horizontalAlignment = if (col === 2) {JLabel.RIGHT} else {JLabel.LEFT}
			return label
		}
	}
	
	private def makeLabelledComponent (JLabel label, JComponent comp) {
		val groupPanel = new JPanel
		groupPanel.layout = new GridBagLayout
		var GridBagConstraints c = new GridBagConstraints
		// label
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 5, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		groupPanel.add(label, c)
		// component
		c.gridx = 1
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 5, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		groupPanel.add(comp, c)
		return groupPanel
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
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		basePanel.add(statusLabel, c)
		
		// Counters
		// - Test counter
		val counterPanel = new JPanel
		counterPanel.layout = new WrapLayout(FlowLayout.LEFT, 0, 0)
		val testCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_TESTS_LABEL") + ":",
			UtplsqlResources.getIcon("UTPLSQL_ICON"), JLabel::LEADING)
		testCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledComponent(testCounterLabel, testCounterValueLabel))
		// - Failure counter
		val failureCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_FAILURES_LABEL") + ":",
			UtplsqlResources.getIcon("FAILURE_ICON"), JLabel::LEADING)
		failureCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledComponent(failureCounterLabel,failureCounterValueLabel))
		// - Error counter
		val errorCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_ERRORS_LABEL") + ":",
			UtplsqlResources.getIcon("ERROR_ICON"), JLabel::LEADING)
		errorCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledComponent(errorCounterLabel, errorCounterValueLabel))
		// - Disabled counter
		val disabledCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_DISABLED_LABEL") + ":",
			UtplsqlResources.getIcon("DISABLED_ICON"), JLabel::LEADING)
		disabledCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledComponent(disabledCounterLabel, disabledCounterValueLabel))
		// - Warnings counter
		val warningsCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_WARNINGS_LABEL") + ":",
			UtplsqlResources.getIcon("WARNING_ICON"), JLabel::LEADING)
		warningsCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledComponent(warningsCounterLabel, warningsCounterValueLabel))
		// - add everything to basePanel		
		c.gridx = 0
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 0, 5, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		basePanel.add(counterPanel,c)
		
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
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		basePanel.add(progressBar, c)

		// Test overview
		testOverviewTableModel = new TestOverviewTableModel
		testOverviewTable = new JTable(testOverviewTableModel)
		testOverviewTable.tableHeader.reorderingAllowed = false
		testOverviewTable.autoCreateRowSorter = true
		testOverviewTable.selectionModel.addListSelectionListener(new TestOverviewRowListener(this)) 		
		val testTableHeaderRenderer = new TestTableHeaderRenderer
		val overviewTableIcon = testOverviewTable.columnModel.getColumn(0)
		overviewTableIcon.minWidth = 20
		overviewTableIcon.preferredWidth = 20
		overviewTableIcon.maxWidth = 20
		val overviewTableId = testOverviewTable.columnModel.getColumn(1)
		overviewTableId.headerRenderer = testTableHeaderRenderer
		val overviewTableTime = testOverviewTable.columnModel.getColumn(2)
		overviewTableTime.preferredWidth = 60
		overviewTableTime.maxWidth = 100
		overviewTableTime.headerRenderer = testTableHeaderRenderer		
		val timeFormatRenderer = new TimeFormatRenderer
		timeFormatRenderer.horizontalAlignment = JLabel.RIGHT
		overviewTableTime.cellRenderer = timeFormatRenderer
		val testOverviewScrollPane = new JScrollPane(testOverviewTable)		
		
		// Test info tabbed pane
		// - Id
		val testInfoPanel = new ScrollablePanel
		testInfoPanel.setLayout(new GridBagLayout())
		val testIdLabel = new JLabel("Id")
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::NORTHWEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		testInfoPanel.add(testIdLabel, c)
		testIdTextArea = new JTextArea
		testIdTextArea.editable = false
		testIdTextArea.enabled = true
		testIdTextArea.lineWrap = true
		testIdTextArea.wrapStyleWord = false
		testIdTextArea.addFocusListener(this)
		c.gridx = 1
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testIdTextArea, c)
		// - Owner
		val testOwnerLabel = new JLabel("Owner")
		c.gridx = 0
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testOwnerLabel, c)
		testOwnerTextField = new JTextField
		testOwnerTextField.editable = false
		c.gridx = 1
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testOwnerTextField, c)
		// - Package
		val testPackageLabel = new JLabel("Package")
		c.gridx = 0
		c.gridy = 2
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testPackageLabel, c)
		testPackageTextField = new JTextField
		testPackageTextField.editable = false
		c.gridx = 1
		c.gridy = 2
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testPackageTextField, c)
		// - Procedure
		val testProcedureLabel = new JLabel("Procedure")
		c.gridx = 0
		c.gridy = 3
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testProcedureLabel, c)
		testProcedureTextField = new JTextField
		testProcedureTextField.editable = false
		c.gridx = 1
		c.gridy = 3
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testProcedureTextField, c)
		// - Description
		val testDescriptionLabel = new JLabel("Description")
		c.gridx = 0
		c.gridy = 4
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::NORTHWEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		testInfoPanel.add(testDescriptionLabel, c)
		testDescriptionTextArea = new JTextArea
		testDescriptionTextArea.editable = false
		testDescriptionTextArea.enabled = true
		testDescriptionTextArea.lineWrap = true
		testDescriptionTextArea.wrapStyleWord = true
		testDescriptionTextArea.addFocusListener(this)
		c.gridx = 1
		c.gridy = 4
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testDescriptionTextArea, c)
		// - Start
		val testStartLabel = new JLabel("Start")
		c.gridx = 0
		c.gridy = 5
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testStartLabel, c)
		testStartTextField = new JTextField
		testStartTextField.editable = false
		c.gridx = 1
		c.gridy = 5
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testStartTextField, c)
		// - End
		val testEndLabel = new JLabel("End")
		c.gridx = 0
		c.gridy = 6
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 10, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testEndLabel, c)
		testEndTextField = new JTextField
		testEndTextField.editable = false
		c.gridx = 1
		c.gridy = 6
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testEndTextField, c)
		// - Vertical spring and scrollbar for info panel
		val testInfoVerticalSpringLabel = new JLabel
		c.gridx = 0
		c.gridy = 7
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(0, 0, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 0
		c.weighty = 1
		testInfoPanel.add(testInfoVerticalSpringLabel, c)
		val testInfoScrollPane = new JScrollPane(testInfoPanel)

		// failures tabbed pane (failed expectations)
		// TODO support unbound number of failed expectations
		// - description
		val testFailurePanel = new JPanel
		testFailurePanel.setLayout(new GridBagLayout())
		val testFailureDescriptionLabel = new JLabel("Description")
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		testFailurePanel.add(testFailureDescriptionLabel, c)
		testFailureDescriptionTextArea = new JTextArea
		testFailureDescriptionTextArea.editable = false
		testFailureDescriptionTextArea.enabled = true
		testFailureDescriptionTextArea.lineWrap = true
		testFailureDescriptionTextArea.wrapStyleWord = true
		testFailureDescriptionTextArea.addFocusListener(this)
		val testFailureDescriptionScrollPane = new JScrollPane(testFailureDescriptionTextArea)
		c.gridx = 1
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testFailurePanel.add(testFailureDescriptionScrollPane, c)
		// - message
		val testFailureMessageLabel = new JLabel("Message")
		c.gridx = 0
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::NORTHWEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		testFailurePanel.add(testFailureMessageLabel, c)
		testFailureMessageTextArea = new JTextArea
		testFailureMessageTextArea.editable = false
		testFailureMessageTextArea.enabled = true
		testFailureMessageTextArea.lineWrap = true
		testFailureMessageTextArea.wrapStyleWord = true
		testFailureMessageTextArea.addFocusListener(this)
		val testFailureMessageScrollPane = new JScrollPane(testFailureMessageTextArea)
		c.gridx = 1
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 6
		testFailurePanel.add(testFailureMessageScrollPane, c)
		// - caller
		val testFailureCallerLabel = new JLabel("Caller")
		c.gridx = 0
		c.gridy = 2
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::NORTHWEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		testFailurePanel.add(testFailureCallerLabel, c)
		testFailureCallerTextArea = new JTextArea
		testFailureCallerTextArea.editable = false
		testFailureCallerTextArea.enabled = true
		testFailureCallerTextArea.lineWrap = true
		testFailureCallerTextArea.wrapStyleWord = true
		testFailureCallerTextArea.addFocusListener(this)
		val testFailureCallerScrollPane = new JScrollPane(testFailureCallerTextArea)
		c.gridx = 1
		c.gridy = 2
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 2
		testFailurePanel.add(testFailureCallerScrollPane, c)

		// server output tabbed pane
		val testServerOutputPanel = new JPanel
		testServerOutputPanel.setLayout(new GridBagLayout())
		testServerOutputTextArea = new JTextArea
		testServerOutputTextArea.editable = false
		testServerOutputTextArea.enabled = true
		testServerOutputTextArea.lineWrap = true
		testServerOutputTextArea.wrapStyleWord = true
		testServerOutputTextArea.addFocusListener(this)
		val testServerOutputScrollPane = new JScrollPane(testServerOutputTextArea)
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		testServerOutputPanel.add(testServerOutputScrollPane, c)
		
		// error stack tabbed pane
		val testErrorStackPanel = new JPanel
		testErrorStackPanel.setLayout(new GridBagLayout())
		testErrorStackTextArea = new JTextArea
		testErrorStackTextArea.editable = false
		testErrorStackTextArea.enabled = true
		testErrorStackTextArea.lineWrap = true
		testErrorStackTextArea.wrapStyleWord = true
		testErrorStackTextArea.addFocusListener(this)
		val testErrorStackScrollPane = new JScrollPane(testErrorStackTextArea)
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		testErrorStackPanel.add(testErrorStackScrollPane, c)

		// split pane with all tabs
		val testDetailTabbedPane = new JTabbedPane()
		testDetailTabbedPane.add("Info", testInfoScrollPane)
		testDetailTabbedPane.add("Failures", testFailurePanel)
		testDetailTabbedPane.add("Server Output", testServerOutputPanel)
		testDetailTabbedPane.add("Error Stack", testErrorStackPanel)
		val horizontalSplitPane = new JSplitPane(SwingConstants.HORIZONTAL, testOverviewScrollPane, testDetailTabbedPane)
		horizontalSplitPane.resizeWeight = 0.5
		c.gridx = 0
		c.gridy = 3
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		basePanel.add(horizontalSplitPane, c)
	}
	
}