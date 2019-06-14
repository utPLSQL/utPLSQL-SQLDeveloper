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
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.text.DecimalFormat
import java.util.ArrayList
import javax.swing.Box
import javax.swing.DefaultComboBoxModel
import javax.swing.JCheckBoxMenuItem
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicProgressBarUI
import javax.swing.table.DefaultTableCellRenderer
import oracle.ide.config.Preferences
import oracle.javatools.ui.table.ToolbarButton
import org.utplsql.sqldev.model.LimitedLinkedHashMap
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.model.runner.Run
import org.utplsql.sqldev.resources.UtplsqlResources
import org.utplsql.sqldev.runner.UtplsqlRunner
import org.utplsql.sqldev.runner.UtplsqlWorksheetRunner

class RunnerPanel implements FocusListener, ActionListener {
	static val GREEN = new Color(0, 153, 0)
	static val RED = new Color(153, 0, 0)
	static val INDICATOR_WIDTH = 20
	LimitedLinkedHashMap<String, Run> runs = new LimitedLinkedHashMap<String, Run>(10)
	Run currentRun
	JPanel basePanel
	ToolbarButton refreshButton
	ToolbarButton rerunButton
	ToolbarButton rerunWorksheetButton
	DefaultComboBoxModel<ComboBoxItem<String, String>> runComboBoxModel
	ToolbarButton clearButton
	JComboBox<ComboBoxItem<String, String>> runComboBox
	JLabel statusLabel
	JLabel testCounterValueLabel
	JLabel errorCounterValueLabel
	JLabel failureCounterValueLabel
	JLabel disabledCounterValueLabel
	JLabel warningsCounterValueLabel
	JLabel infoCounterValueLabel
	JCheckBoxMenuItem showDisabledCounterCheckBoxMenuItem
	JCheckBoxMenuItem showWarningsCounterCheckBoxMenuItem
	JCheckBoxMenuItem showInfoCounterCheckBoxMenuItem
	JProgressBar progressBar;
	TestOverviewTableModel testOverviewTableModel
	JTable testOverviewTable
	JMenuItem testOverviewRunMenuItem
	JMenuItem testOverviewRunWorksheetMenuItem
	JCheckBoxMenuItem showWarningIndicatorCheckBoxMenuItem
	JCheckBoxMenuItem showInfoIndicatorCheckBoxMenuItem
	JTextArea testIdTextArea
	JTextField testOwnerTextField
	JTextField testPackageTextField
	JTextField testProcedureTextField
	JTextArea testDescriptionTextArea
	JTextField testStartTextField
	JTextField testEndTextField
	FailuresTableModel failuresTableModel
	JTable failuresTable
	JTextArea testFailureMessageTextArea
	JTextArea testErrorStackTextArea
	JTextArea testWarningsTextArea
	JTextArea testServerOutputTextArea
	JTabbedPane testDetailTabbedPane
	
	def Component getGUI() {
		if (basePanel === null) {
			initializeGUI()
		}
		return basePanel
	}

	private def resetDerived() {
		testOverviewTable.rowSorter.sortKeys = null
		testOverviewRunMenuItem.enabled = false
		testOverviewRunWorksheetMenuItem.enabled = false
		testIdTextArea.text = null
		testOwnerTextField.text = null
		testPackageTextField.text = null
		testProcedureTextField.text = null
		testDescriptionTextArea.text = null
		testStartTextField.text = null
		testEndTextField.text = null
		failuresTableModel.model = null
		failuresTableModel.fireTableDataChanged
		testFailureMessageTextArea.text = null
		testErrorStackTextArea.text = null
		testWarningsTextArea.text = null
		testServerOutputTextArea.text = null
	}
	
	private def refreshRunsComboBox() {
		if (runs.size > 0) {
			runComboBox.removeActionListener(this)
			runComboBoxModel.removeAllElements
			for (var i = runs.size - 1 ; i >= 0; i--) {
				val entry = runs.entrySet.get(i)
				val item = new ComboBoxItem<String, String>(entry.key, entry.value.name)
				runComboBoxModel.addElement(item)
			}
			runComboBox.selectedIndex = 0
			runComboBox.addActionListener(this)
		}
	}
	
	private def applyShowDisabledCounter(boolean show) {
		disabledCounterValueLabel.parent.visible = showDisabledCounterCheckBoxMenuItem.selected
	}
	
	private def applyShowWarningsCounter(boolean show) {
		warningsCounterValueLabel.parent.visible = showWarningsCounterCheckBoxMenuItem.selected
	}
	
	private def applyShowInfoCounter(boolean show) {
		infoCounterValueLabel.parent.visible = showInfoCounterCheckBoxMenuItem.selected
	}

	private def applyShowWarningIndicator(boolean show) {
		val col = testOverviewTable.columnModel.getColumn(1)
		if (show) {
			col.width = INDICATOR_WIDTH
			col.minWidth = INDICATOR_WIDTH
			col.maxWidth = INDICATOR_WIDTH
			col.preferredWidth = INDICATOR_WIDTH
		} else {
			col.width = 0
			col.minWidth = 0
			col.maxWidth = 0
			col.preferredWidth = 0
		}
	}
	
	private def applyShowInfoIndicator(boolean show) {
		val col = testOverviewTable.columnModel.getColumn(2)
		if (show) {
			col.width = INDICATOR_WIDTH
			col.minWidth = INDICATOR_WIDTH
			col.maxWidth = INDICATOR_WIDTH
			col.preferredWidth = INDICATOR_WIDTH
		} else {
			col.width = 0
			col.minWidth = 0
			col.maxWidth = 0
			col.preferredWidth = 0
		} 
	}
	
	private def getPreferenceModel() {
		var PreferenceModel preferences
		try {
			preferences = PreferenceModel.getInstance(Preferences.preferences)
		} catch (NoClassDefFoundError e) {
			preferences = PreferenceModel.getInstance(null)
		}
		return preferences
	}
	
	private def applyPreferences() {
		val PreferenceModel preferences = preferenceModel
		showDisabledCounterCheckBoxMenuItem.selected = preferences.showDisabledCounter
		applyShowDisabledCounter(showDisabledCounterCheckBoxMenuItem.selected)
		showWarningsCounterCheckBoxMenuItem.selected = preferences.showWarningsCounter
		applyShowWarningsCounter(showWarningsCounterCheckBoxMenuItem.selected)
		showInfoCounterCheckBoxMenuItem.selected = preferences.showInfoCounter
		applyShowInfoCounter(showInfoCounterCheckBoxMenuItem.selected)
		showWarningIndicatorCheckBoxMenuItem.selected = preferences.showWarningIndicator
		applyShowWarningIndicator(showWarningIndicatorCheckBoxMenuItem.selected)
		showInfoIndicatorCheckBoxMenuItem.selected = preferences.showInfoIndicator
		applyShowInfoIndicator(showInfoIndicatorCheckBoxMenuItem.selected)
	}
		
	def setModel(Run run) {
		runs.put(run.reporterId, run)
		refreshRunsComboBox
		setCurrentRun(run)
	}
	
	private def setCurrentRun(Run run) {
		if (run !== currentRun) {
			currentRun = run
			testOverviewTableModel.model = run.tests
			resetDerived
			val item = new ComboBoxItem<String, String>(currentRun.reporterId, currentRun.name)
			runComboBox.selectedItem = item
		}		
	}

	def synchronized update(String reporterId) {
		setCurrentRun(runs.get(reporterId))
		val row = currentRun.totalNumberOfCompletedTests - 1
		val header = testOverviewTableModel.testIdColumnName
		val idColumn = testOverviewTable.columnModel.getColumn(3)
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
		statusLabel.text = currentRun.status
		testCounterValueLabel.text = '''«currentRun.totalNumberOfCompletedTests»/«currentRun.totalNumberOfTests»'''
		errorCounterValueLabel.text = '''«currentRun.counter.error»'''
		failureCounterValueLabel.text = '''«currentRun.counter.failure»'''
		disabledCounterValueLabel.text = '''«currentRun.counter.disabled»'''
		warningsCounterValueLabel.text = '''«currentRun.counter.warning»'''
		infoCounterValueLabel.text = '''«currentRun.infoCount»'''
		if (currentRun.totalNumberOfTests == 0) {
			progressBar.value = 100
		} else {
			progressBar.value = Math.round(100 * currentRun.totalNumberOfCompletedTests / currentRun.totalNumberOfTests)
		}
		if (currentRun.counter.error > 0 || currentRun.counter.failure > 0) {
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
		} else if (e.source == testFailureMessageTextArea) {
			testFailureMessageTextArea.caret.visible = true
		} else if (e.source == testServerOutputTextArea) {
			testServerOutputTextArea.caret.visible = true
		} else if (e.source == testErrorStackTextArea) {
			testErrorStackTextArea.caret.visible = true
		} else if (e.source == testWarningsTextArea) {
			testWarningsTextArea.caret.visible = true
		}
	}

	override focusLost(FocusEvent e) {
		if (e.source == testIdTextArea) {
			testIdTextArea.caret.visible = false
		} else if (e.source == testDescriptionTextArea) {
			testDescriptionTextArea.caret.visible = false
		} else if (e.source == testFailureMessageTextArea) {
			testFailureMessageTextArea.caret.visible = false
		} else if (e.source == testServerOutputTextArea) {
			testServerOutputTextArea.caret.visible = false
		} else if (e.source == testErrorStackTextArea) {
			testErrorStackTextArea.caret.visible = false
		} else if (e.source == testWarningsTextArea) {
			testWarningsTextArea.caret.visible = false
		}
	}
	
	private def getPathListFromSelectedTests() {
		val pathList = new ArrayList<String>
		for (row : testOverviewTable.selectedRows) {
			val test = testOverviewTableModel.getTest(row)
			val path = '''«test.ownerName».«test.objectName».«test.procedureName»'''
			pathList.add(path)
		}
		return pathList
	}

	override actionPerformed(ActionEvent e) {
		if (e.source == refreshButton) {
			resetDerived
			testDetailTabbedPane.selectedIndex = 0
			testOverviewTableModel.fireTableDataChanged
		} else if (e.source == rerunButton) {
			val runner = new UtplsqlRunner(currentRun.pathList, currentRun.connectionName)
			runner.runTestAsync
		} else if (e.source == rerunWorksheetButton) {
			val worksheet = new UtplsqlWorksheetRunner(currentRun.pathList, currentRun.connectionName)
			worksheet.runTestAsync
		} else if (e.source == runComboBox) {
			if (currentRun !== null) {
				val comboBoxItem = runComboBox.selectedItem as ComboBoxItem<String, String>
				if (currentRun.reporterId != comboBoxItem.key) {
					update(comboBoxItem.key)
					testDetailTabbedPane.selectedIndex = 0
				}
			}
		} else if (e.source == clearButton) {
			val run = currentRun
			runs.clear
			currentRun = null
			setModel(run)
			update(run.reporterId)
		} else if (e.source == testOverviewRunMenuItem) {
			val runner = new UtplsqlRunner(pathListFromSelectedTests, currentRun.connectionName)
			runner.runTestAsync
		} else if (e.source == testOverviewRunWorksheetMenuItem) {
			val worksheet = new UtplsqlWorksheetRunner(pathListFromSelectedTests, currentRun.connectionName)
			worksheet.runTestAsync
		} else if (e.source == showDisabledCounterCheckBoxMenuItem) {
			applyShowDisabledCounter(showDisabledCounterCheckBoxMenuItem.selected)
		} else if (e.source == showWarningsCounterCheckBoxMenuItem) {
			applyShowWarningsCounter( showWarningsCounterCheckBoxMenuItem.selected)
		} else if (e.source == showInfoCounterCheckBoxMenuItem) {
			applyShowInfoCounter(showInfoCounterCheckBoxMenuItem.selected)
		} else if (e.source == showWarningIndicatorCheckBoxMenuItem) {
			applyShowWarningIndicator(showWarningIndicatorCheckBoxMenuItem.selected)
		} else if (e.source == showInfoIndicatorCheckBoxMenuItem) {
			applyShowInfoIndicator(showInfoIndicatorCheckBoxMenuItem.selected)
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
				p.testDescriptionTextArea.text = test.description?.trim
				p.testStartTextField.text = formatDateTime(test.startTime)
				p.testEndTextField.text = formatDateTime(test.endTime)
				p.failuresTableModel.model = test.failedExpectations
				p.testFailureMessageTextArea.text = null
				if (test.failedExpectations !== null && test.failedExpectations.size > 0) {
					p.failuresTableModel.fireTableDataChanged
					p.failuresTable.setRowSelectionInterval(0, 0)
				}
				p.testErrorStackTextArea.text = test.errorStack?.trim
				p.testWarningsTextArea.text = test.warnings?.trim
				p.testServerOutputTextArea.text = test.serverOutput?.trim
				var int tabIndex
				if (test.counter?.failure !== null && test.counter.failure > 0) {
					tabIndex = 1
				} else if (test.counter?.error !== null && test.counter.error > 0) {
					tabIndex = 2
				} else if (test.counter?.warning !== null && test.counter.warning > 0) {
					tabIndex = 3
				} else if (test.serverOutput !== null && test.serverOutput.length > 0) {
					tabIndex = 4
				} else {
					tabIndex = 0
				}
				p.testDetailTabbedPane.selectedIndex = tabIndex
				p.testOverviewRunMenuItem.enabled = true
				p.testOverviewRunWorksheetMenuItem.enabled = true
			}
		}
	}

	static class FailuresRowListener implements ListSelectionListener {
		RunnerPanel p
		
		new (RunnerPanel p) {
			this.p = p
		}
	
		override void valueChanged(ListSelectionEvent event) {
			val rowIndex = p.failuresTable.selectedRow
			if (rowIndex != -1) {
				val row =  p.failuresTable.convertRowIndexToModel(rowIndex)
				val expectation = p.failuresTableModel.getExpectation(row)
				p.testFailureMessageTextArea.text = expectation.failureText
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
			val renderer = table.tableHeader.defaultRenderer
			val label = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
			if (col === 0) {
				label.icon = UtplsqlResources.getIcon("UTPLSQL_ICON")
				label.horizontalAlignment = JLabel.CENTER
			} else if (col === 1) {
				label.icon = UtplsqlResources.getIcon("WARNING_ICON")
				label.horizontalAlignment = JLabel.CENTER
			} else if (col === 2) {
				label.icon = UtplsqlResources.getIcon("INFO_ICON")
				label.horizontalAlignment = JLabel.CENTER
			} else if (col === 3) {
				label.icon = null
				label.horizontalAlignment = JLabel.LEFT
			} else if (col === 4) {
				label.icon = null
				label.horizontalAlignment = JLabel.RIGHT
			}
			return label
		}
	}

	static class FailuresTableHeaderRenderer extends DefaultTableCellRenderer {
		override getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {
			val renderer = table.tableHeader.defaultRenderer
			val label = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) as JLabel
			if (col === 0) {
				label.horizontalAlignment = JLabel.RIGHT
			} else {
				label.horizontalAlignment = JLabel.LEFT
			}
			return label
		}
	}
	
	private def makeLabelledCounterComponent (JLabel label, JComponent comp) {
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
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		groupPanel.add(comp, c)
		val dim = new Dimension(154, 24)
		groupPanel.minimumSize = dim
		groupPanel.preferredSize = dim
		return groupPanel
	}
			
	private def initializeGUI() {
		// Base panel containing all components 
		basePanel = new JPanel()
		basePanel.setLayout(new GridBagLayout())
		var GridBagConstraints c = new GridBagConstraints()
		
		// Toolbar
		var toolbar = new GradientToolbar
		toolbar.floatable = false
		toolbar.border = new EmptyBorder(new Insets(2, 2, 2, 2)) // top, left, bottom, right
		refreshButton = new ToolbarButton(UtplsqlResources.getIcon("REFRESH_ICON"))
		refreshButton.toolTipText = "Reset ordering and refresh"
		refreshButton.addActionListener(this)
		toolbar.add(refreshButton)
		rerunButton = new ToolbarButton(UtplsqlResources.getIcon("RUN_ICON"))
		rerunButton.toolTipText = "Rerun all tests"
		rerunButton.addActionListener(this)
		toolbar.add(rerunButton)
		rerunWorksheetButton = new ToolbarButton(UtplsqlResources.getIcon("RUN_WORKSHEET_ICON"))
		rerunWorksheetButton.toolTipText = "Rerun all tests in a new worksheet"
		rerunWorksheetButton.addActionListener(this)
		toolbar.add(rerunWorksheetButton)
		toolbar.add(Box.createHorizontalGlue())
		runComboBoxModel = new DefaultComboBoxModel<ComboBoxItem<String, String>>;
		runComboBox = new JComboBox<ComboBoxItem<String, String>>(runComboBoxModel);
		runComboBox.editable = false
		val comboBoxDim = new Dimension(500, 50)
		runComboBox.maximumSize = comboBoxDim
		runComboBox.addActionListener(this)
		toolbar.add(runComboBox)
		clearButton = new ToolbarButton(UtplsqlResources.getIcon("CLEAR_ICON"))
		clearButton.toolTipText = "Clear history"
		clearButton.addActionListener(this)
		toolbar.add(clearButton)
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(0, 0, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::NORTH
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		basePanel.add(toolbar, c)
		
		// Status line
		statusLabel = new JLabel
		c.gridx = 0
		c.gridy = 1
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
		counterPanel.add(makeLabelledCounterComponent(testCounterLabel, testCounterValueLabel))
		// - Failure counter
		val failureCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_FAILURES_LABEL") + ":",
			UtplsqlResources.getIcon("FAILURE_ICON"), JLabel::LEADING)
		failureCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledCounterComponent(failureCounterLabel,failureCounterValueLabel))
		// - Error counter
		val errorCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_ERRORS_LABEL") + ":",
			UtplsqlResources.getIcon("ERROR_ICON"), JLabel::LEADING)
		errorCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledCounterComponent(errorCounterLabel, errorCounterValueLabel))
		// - Disabled counter
		val disabledCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_DISABLED_LABEL") + ":",
			UtplsqlResources.getIcon("DISABLED_ICON"), JLabel::LEADING)
		disabledCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledCounterComponent(disabledCounterLabel, disabledCounterValueLabel))
		// - Warnings counter
		val warningsCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_WARNINGS_LABEL") + ":",
			UtplsqlResources.getIcon("WARNING_ICON"), JLabel::LEADING)
		warningsCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledCounterComponent(warningsCounterLabel, warningsCounterValueLabel))
		// - Info counter
		val infoCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_INFO_LABEL") + ":",
			UtplsqlResources.getIcon("INFO_ICON"), JLabel::LEADING)
		infoCounterValueLabel = new JLabel
		counterPanel.add(makeLabelledCounterComponent(infoCounterLabel, infoCounterValueLabel))
		// - add everything to basePanel		
		c.gridx = 0
		c.gridy = 2
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 0, 5, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		basePanel.add(counterPanel,c)

		// Context menu for counters panel
		val countersPopupMenu = new JPopupMenu
		showDisabledCounterCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_DISABLED_COUNTER_LABEL").replace("?",""))
		showDisabledCounterCheckBoxMenuItem.selected = true
		showDisabledCounterCheckBoxMenuItem.addActionListener(this)
		countersPopupMenu.add(showDisabledCounterCheckBoxMenuItem)
		showWarningsCounterCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_WARNINGS_COUNTER_LABEL").replace("?",""))
		showWarningsCounterCheckBoxMenuItem.selected = true
		showWarningsCounterCheckBoxMenuItem.addActionListener(this)
		countersPopupMenu.add(showWarningsCounterCheckBoxMenuItem)
		showInfoCounterCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_INFO_COUNTER_LABEL").replace("?",""))
		showInfoCounterCheckBoxMenuItem.selected = true
		showInfoCounterCheckBoxMenuItem.addActionListener(this)
		countersPopupMenu.add(showInfoCounterCheckBoxMenuItem)
		counterPanel.componentPopupMenu = countersPopupMenu
		
		// Progress bar
		progressBar = new JProgressBar
		val progressBarDim = new Dimension(10, 20)
		progressBar.preferredSize = progressBarDim
		progressBar.minimumSize = progressBarDim
		progressBar.stringPainted = false
		progressBar.foreground = GREEN
		progressBar.UI = new BasicProgressBarUI
		c.gridx = 0
		c.gridy = 3
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
		val overviewTableStatus = testOverviewTable.columnModel.getColumn(0)
		overviewTableStatus.minWidth = INDICATOR_WIDTH
		overviewTableStatus.preferredWidth = INDICATOR_WIDTH
		overviewTableStatus.maxWidth = INDICATOR_WIDTH
		overviewTableStatus.headerRenderer = testTableHeaderRenderer
		val overviewTableWarning = testOverviewTable.columnModel.getColumn(1)
		overviewTableWarning.minWidth = INDICATOR_WIDTH
		overviewTableWarning.preferredWidth = INDICATOR_WIDTH
		overviewTableWarning.maxWidth = INDICATOR_WIDTH
		overviewTableWarning.headerRenderer = testTableHeaderRenderer
		val overviewTableInfo = testOverviewTable.columnModel.getColumn(2)
		overviewTableInfo.minWidth = INDICATOR_WIDTH
		overviewTableInfo.preferredWidth = INDICATOR_WIDTH
		overviewTableInfo.maxWidth = INDICATOR_WIDTH
		overviewTableInfo.headerRenderer = testTableHeaderRenderer
		val overviewTableId = testOverviewTable.columnModel.getColumn(3)
		overviewTableId.headerRenderer = testTableHeaderRenderer
		val overviewTableTime = testOverviewTable.columnModel.getColumn(4)
		overviewTableTime.preferredWidth = 60
		overviewTableTime.maxWidth = 100
		overviewTableTime.headerRenderer = testTableHeaderRenderer		
		val timeFormatRenderer = new TimeFormatRenderer
		timeFormatRenderer.horizontalAlignment = JLabel.RIGHT
		overviewTableTime.cellRenderer = timeFormatRenderer
		val testOverviewScrollPane = new JScrollPane(testOverviewTable)
		
		// Context menu for test overview
		val testOverviewPopupMenu = new JPopupMenu
		testOverviewRunMenuItem = new JMenuItem("Run test", UtplsqlResources.getIcon("RUN_ICON"));
		testOverviewRunMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(testOverviewRunMenuItem)
		testOverviewRunWorksheetMenuItem = new JMenuItem("Run test in new worksheet", UtplsqlResources.getIcon("RUN_WORKSHEET_ICON"));
		testOverviewRunWorksheetMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(testOverviewRunWorksheetMenuItem)
		testOverviewPopupMenu.add(new JSeparator)
		showWarningIndicatorCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_WARNING_INDICATOR_LABEL").replace("?",""))
		showWarningIndicatorCheckBoxMenuItem.selected = true
		showWarningIndicatorCheckBoxMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(showWarningIndicatorCheckBoxMenuItem)
		showInfoIndicatorCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_INFO_INDICATOR_LABEL").replace("?",""))
		showInfoIndicatorCheckBoxMenuItem.selected = true
		showInfoIndicatorCheckBoxMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(showInfoIndicatorCheckBoxMenuItem)
		testOverviewTable.componentPopupMenu = testOverviewPopupMenu

		// Test tabbed pane (Test Properties)
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
		val testDescriptionLabel = new JLabel(UtplsqlResources.getString("RUNNER_DESCRIPTION"))
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
		val testPropertiesScrollPane = new JScrollPane(testInfoPanel)

		// Failures tabbed pane (failed expectations)
		// - failures table (number and description)
		failuresTableModel = new FailuresTableModel
		failuresTable = new JTable(failuresTableModel)
		failuresTable.tableHeader.reorderingAllowed = false
		failuresTable.selectionModel.addListSelectionListener(new FailuresRowListener(this))
		val failuresTableHeaderRenderer = new FailuresTableHeaderRenderer		
		val failuresTableNumber = failuresTable.columnModel.getColumn(0)
		failuresTableNumber.headerRenderer = failuresTableHeaderRenderer
		failuresTableNumber.preferredWidth = 30
		failuresTableNumber.maxWidth = 30
		val failuresDescription = failuresTable.columnModel.getColumn(1)
		failuresDescription.headerRenderer = failuresTableHeaderRenderer
		val failuresTableScrollPane = new JScrollPane(failuresTable)		
		// - failures details
		testFailureMessageTextArea = new JTextArea
		testFailureMessageTextArea.editable = false
		testFailureMessageTextArea.enabled = true
		testFailureMessageTextArea.lineWrap = true
		testFailureMessageTextArea.wrapStyleWord = true
		testFailureMessageTextArea.addFocusListener(this)
		val testFailureMessageScrollPane = new JScrollPane(testFailureMessageTextArea)
		c.gridx = 1
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 6

		// - split pane
		val failuresSplitPane = new JSplitPane(SwingConstants.HORIZONTAL, failuresTableScrollPane, testFailureMessageScrollPane)
		failuresSplitPane.resizeWeight = 0.2

		// Errors tabbed pane (Error Stack)
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
		
		// Warnings tabbed pane
		val testWarningsPanel = new JPanel
		testWarningsPanel.setLayout(new GridBagLayout())
		testWarningsTextArea = new JTextArea
		testWarningsTextArea.editable = false
		testWarningsTextArea.enabled = true
		testWarningsTextArea.lineWrap = true
		testWarningsTextArea.wrapStyleWord = true
		testWarningsTextArea.addFocusListener(this)
		val testWarningsScrollPane = new JScrollPane(testWarningsTextArea)
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		testWarningsPanel.add(testWarningsScrollPane, c)

		// Info tabbed pane (Server Output)
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

		// split pane with all tabs
		testDetailTabbedPane = new JTabbedPane()
		testDetailTabbedPane.add("Test", testPropertiesScrollPane)
		testDetailTabbedPane.add("Failures", failuresSplitPane)
		testDetailTabbedPane.add("Errors", testErrorStackPanel)
		testDetailTabbedPane.add("Warnings", testWarningsPanel)
		testDetailTabbedPane.add("Info", testServerOutputPanel)
		val horizontalSplitPane = new JSplitPane(SwingConstants.HORIZONTAL, testOverviewScrollPane, testDetailTabbedPane)
		horizontalSplitPane.resizeWeight = 0.5
		c.gridx = 0
		c.gridy = 4
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		basePanel.add(horizontalSplitPane, c)
		applyPreferences
	}	
}