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
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.regex.Pattern
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
import javax.swing.SwingConstants
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicProgressBarUI
import javax.swing.table.DefaultTableCellRenderer
import oracle.dbtools.raptor.controls.grid.DefaultDrillLink
import oracle.dbtools.raptor.utils.Connections
import oracle.ide.config.Preferences
import oracle.javatools.ui.table.ToolbarButton
import org.springframework.web.util.HtmlUtils
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.model.LimitedLinkedHashMap
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.model.runner.Run
import org.utplsql.sqldev.parser.UtplsqlParser
import org.utplsql.sqldev.resources.UtplsqlResources
import org.utplsql.sqldev.runner.UtplsqlRunner
import org.utplsql.sqldev.runner.UtplsqlWorksheetRunner

class RunnerPanel implements ActionListener, MouseListener, HyperlinkListener {
	static val GREEN = new Color(0, 153, 0)
	static val RED = new Color(153, 0, 0)
	static val INDICATOR_WIDTH = 20
	static val OVERVIEW_TABLE_ROW_HEIGHT = 20
	static val TEXTPANE_DIM = new Dimension(100, 100)
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
	JCheckBoxMenuItem syncDetailTabCheckBoxMenuItem
	RunnerTextField testOwnerTextField
	RunnerTextField testPackageTextField
	RunnerTextField testProcedureTextField
	RunnerTextArea testDescriptionTextArea
	RunnerTextArea testIdTextArea
	RunnerTextField testStartTextField
	FailuresTableModel failuresTableModel
	JTable failuresTable
	RunnerTextPane testFailureMessageTextPane
	RunnerTextPane testErrorStackTextPane
	RunnerTextPane testWarningsTextPane
	RunnerTextPane testServerOutputTextPane
	JTabbedPane testDetailTabbedPane
	
	def Component getGUI() {
		if (basePanel === null) {
			initializeGUI()
		}
		if (!basePanel.showing) {
			applyPreferences
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
		failuresTableModel.model = null
		failuresTableModel.fireTableDataChanged
		testFailureMessageTextPane.text = null
		testErrorStackTextPane.text = null
		testWarningsTextPane.text = null
		testServerOutputTextPane.text = null
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

	private def openSelectedTest() {
		val rowIndex = testOverviewTable.selectedRow
		if (rowIndex != -1) {
			val row = testOverviewTable.convertRowIndexToModel(rowIndex)
			val test = testOverviewTableModel.getTest(row)
			val dao = new UtplsqlDao(Connections.instance.getConnection(currentRun.connectionName))
			val source = dao.getSource(test.ownerName, "PACKAGE", test.objectName.toUpperCase).trim
			val parser = new UtplsqlParser(source)
			val line = parser.getLineOf(test.procedureName)
			openEditor(test.ownerName, "PACKAGE", test.objectName.toUpperCase, line, 1)
		}
	}
	
	private def openSelectedFailure() {
		val rowIndex = failuresTable.selectedRow
		if (rowIndex != -1) {
			val row = failuresTable.convertRowIndexToModel(rowIndex)
			val expectation = failuresTableModel.getExpectation(row)
			val test = testOverviewTableModel.getTest(testOverviewTable.convertRowIndexToModel(testOverviewTable.selectedRow))
			openEditor(test.ownerName, "PACKAGE BODY", test.objectName.toUpperCase, expectation.callerLine, 1)
		}
	}
	
	private def getHtml(String text) {
		val html = '''
			<html>
				<head>
					<style type="text/css">
						body, p {font-family: «testOwnerTextField.font.family»; font-size: 1.0em; line-height: 1.1em; margin-top: 0px; margin-bottom: 0px;}
					</style>
				</head>
				<body>
					«getLinkedText(text)»
				</body>
			</html>
		'''
		return html
	}
	
	private def openLink(String link) {
		val parts = link.split("/")
		val ownerName = parts.get(0)
		val objectName = parts.get(1)
		var line = Integer.parseInt(parts.get(2))
		val dao = new UtplsqlDao(Connections.instance.getConnection(currentRun.connectionName))
		val objectType = dao.getObjectType(ownerName, objectName)
		val fixedObjectType = '''«objectType»«IF objectType == "PACKAGE" || objectType == "TYPE"» BODY«ENDIF»'''
		if (parts.size == 4) {
			val procedureName = parts.get(3)
			val source = dao.getSource(ownerName, fixedObjectType, objectName).trim
			val parser = new UtplsqlParser(source)
			line = parser.getLineOf(procedureName)
		}
		openEditor(ownerName, '''«objectType»«IF objectType == "PACKAGE" || objectType == "TYPE"» BODY«ENDIF»''', objectName.toUpperCase, line, 1)
	}
	
	private def openEditor(String owner, String type, String name, int line, int col) {
		var drillLink = new DefaultDrillLink
		drillLink.connName = currentRun.connectionName
		// argument order is based on SQLDEV:LINK that can be used in SQL query result tables (editors, reports)
		drillLink.args = #[owner, type, name, String.valueOf(line), String.valueOf(col), "OpenEditor", "oracle.dbtools.raptor.controls.grid.DefaultDrillLink"]
		drillLink.performDrill
	}
 	
	private def syncDetailTab() {
		if (syncDetailTabCheckBoxMenuItem.selected) {
			val rowIndex = testOverviewTable.selectedRow
			if (rowIndex != -1) {
				val row =  testOverviewTable.convertRowIndexToModel(rowIndex)
				val test = testOverviewTableModel.getTest(row)
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
				testDetailTabbedPane.selectedIndex = tabIndex
			}
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
		fixCheckBoxMenuItem(showDisabledCounterCheckBoxMenuItem) 
		showWarningsCounterCheckBoxMenuItem.selected = preferences.showWarningsCounter
		applyShowWarningsCounter(showWarningsCounterCheckBoxMenuItem.selected)
		fixCheckBoxMenuItem(showWarningsCounterCheckBoxMenuItem)
		showInfoCounterCheckBoxMenuItem.selected = preferences.showInfoCounter
		applyShowInfoCounter(showInfoCounterCheckBoxMenuItem.selected)
		fixCheckBoxMenuItem(showInfoCounterCheckBoxMenuItem)
		showWarningIndicatorCheckBoxMenuItem.selected = preferences.showWarningIndicator
		applyShowWarningIndicator(showWarningIndicatorCheckBoxMenuItem.selected)
		fixCheckBoxMenuItem(showWarningIndicatorCheckBoxMenuItem)
		showInfoIndicatorCheckBoxMenuItem.selected = preferences.showInfoIndicator
		applyShowInfoIndicator(showInfoIndicatorCheckBoxMenuItem.selected)
		fixCheckBoxMenuItem(showInfoIndicatorCheckBoxMenuItem)
		syncDetailTabCheckBoxMenuItem.selected = preferences.syncDetailTab
		fixCheckBoxMenuItem(syncDetailTabCheckBoxMenuItem)
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
		val row = currentRun.currentTestNumber - 1
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
		testCounterValueLabel.text = '''«currentRun.totalNumberOfCompletedTests»«IF currentRun.totalNumberOfTests >= 0»/«currentRun.totalNumberOfTests»«ENDIF»'''
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
	
	private def getPathListFromSelectedTests() {
		val pathList = new ArrayList<String>
		for (rowIndex : testOverviewTable.selectedRows) {
			val row = testOverviewTable.convertRowIndexToModel(rowIndex) 
			val test = testOverviewTableModel.getTest(row)
			val path = '''«test.ownerName».«test.objectName».«test.procedureName»'''
			pathList.add(path)
		}
		return pathList
	}
	
	private def isWindowsLookAndFeel() {
		val laf = UIManager.systemLookAndFeelClassName
		if (laf.toLowerCase.contains("windows")) {
			return true
		} else {
			return false
		}
	}
	
	private def void fixCheckBoxMenuItem(JCheckBoxMenuItem item) {
		if (windowsLookAndFeel) {
			if (item.selected) {
				item.icon = UtplsqlResources.getIcon("CHECKMARK_ICON")
			} else {
				item.icon = null
			}
		}
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
			fixCheckBoxMenuItem(showDisabledCounterCheckBoxMenuItem) 
		} else if (e.source == showWarningsCounterCheckBoxMenuItem) {
			applyShowWarningsCounter( showWarningsCounterCheckBoxMenuItem.selected)
			fixCheckBoxMenuItem(showWarningsCounterCheckBoxMenuItem)
		} else if (e.source == showInfoCounterCheckBoxMenuItem) {
			applyShowInfoCounter(showInfoCounterCheckBoxMenuItem.selected)
			fixCheckBoxMenuItem(showInfoCounterCheckBoxMenuItem)
		} else if (e.source == showWarningIndicatorCheckBoxMenuItem) {
			applyShowWarningIndicator(showWarningIndicatorCheckBoxMenuItem.selected)
			fixCheckBoxMenuItem(showWarningIndicatorCheckBoxMenuItem)
		} else if (e.source == showInfoIndicatorCheckBoxMenuItem) {
			applyShowInfoIndicator(showInfoIndicatorCheckBoxMenuItem.selected)
			fixCheckBoxMenuItem(showInfoIndicatorCheckBoxMenuItem)
		} else if (e.source == syncDetailTabCheckBoxMenuItem) {
			syncDetailTab
			fixCheckBoxMenuItem(syncDetailTabCheckBoxMenuItem)
		}
	}

	override mouseClicked(MouseEvent e) {
		if (e.clickCount == 2) {
			if (e.source == testOverviewTable) {
				if (failuresTable.selectedRowCount == 1) {
					openSelectedFailure
				} else {
					openSelectedTest
				}
				
			} else if (e.source == failuresTable) {
				if (failuresTable.selectedRowCount == 1) {
					openSelectedFailure
		        }
			}
		}
	}
	
	override mouseEntered(MouseEvent e) {
	}
	
	override mouseExited(MouseEvent e) {
	}
	
	override mousePressed(MouseEvent e) {
	}
	
	override mouseReleased(MouseEvent e) {
	}

	override hyperlinkUpdate(HyperlinkEvent e) {
		if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
			val link = e.description
			openLink(link)
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
				p.testOwnerTextField.text = test.ownerName
				p.testPackageTextField.text = test.objectName
				p.testProcedureTextField.text = test.procedureName
				p.testDescriptionTextArea.text = test.description?.trim
				p.testIdTextArea.text = test.id
				p.testStartTextField.text = formatDateTime(test.startTime)
				p.failuresTableModel.model = test.failedExpectations
				p.failuresTableModel.fireTableDataChanged
				p.testFailureMessageTextPane.text = null
				if (test.failedExpectations !== null && test.failedExpectations.size > 0) {
					p.failuresTable.setRowSelectionInterval(0, 0)
				}
				p.testErrorStackTextPane.text = p.getHtml(test.errorStack?.trim)
				p.testWarningsTextPane.text =  p.getHtml(test.warnings?.trim)
				p.testServerOutputTextPane.text = p.getHtml(test.serverOutput?.trim)
				p.syncDetailTab
				p.testOverviewRunMenuItem.enabled = true
				p.testOverviewRunWorksheetMenuItem.enabled = true
			}
		}
	}
	
	private def getLinkedText(String text) {
		if (text === null) {
			return ""
		}
		// Patterns (primarily Asserts, Errors, ServerOutput): 
		// at "OWNER.PACKAGE.PROCEDURE", line 42 
		// at "OWNER.PROCEDURE", line 42 
		val p1 = Pattern.compile('''\s+(&quot;(\S+?)\.(\S+?)(?:\.(\S+?))?&quot;,\s+line\s+([0-9]+))''')
		var localText = HtmlUtils.htmlEscape(text)
		var m = p1.matcher(localText)
		while(m.find) {
			val link = ''' <a href="«m.group(2)»/«m.group(3)»/«m.group(5)»">«m.group(1)»</a>'''
			localText = localText.replaceFirst(p1.pattern, link)
			m = p1.matcher(localText)
		}
		// Patterns (primarily Warnings, without line reference, calculate when opening link):
		//   owner.package.procedure
		val p2 = Pattern.compile('''^\s{2}((\S+?)\.(\S+?)\.(\S+?))$''', Pattern.MULTILINE)
		m = p2.matcher(localText)
		while(m.find) {
			val link = '''&nbsp;&nbsp;<a href="«m.group(2).toUpperCase»/«m.group(3).toUpperCase»/1/«m.group(4).toUpperCase»">«m.group(1)»</a>'''
			val start = m.start(0)
			val end = m.end(0)
			localText = '''«localText.substring(0, start)»«link»«localText.substring(end)»'''
			m = p2.matcher(localText)
		}
		val result = '''
			«FOR p : localText.split("\n")»
				<p>«p»</p>
			«ENDFOR»
		'''
		return result
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
				val html = p.getHtml(expectation.failureText)
				p.testFailureMessageTextPane.text = html
				
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
				label.icon = UtplsqlResources.getIcon("STATUS_ICON")
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
		val dim = new Dimension(134, 24)
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
		refreshButton.toolTipText = UtplsqlResources.getString("RUNNER_REFRESH_BUTTON")
		refreshButton.addActionListener(this)
		toolbar.add(refreshButton)
		rerunButton = new ToolbarButton(UtplsqlResources.getIcon("RUN_ICON"))
		rerunButton.toolTipText = UtplsqlResources.getString("RUNNER_RERUN_TOOLTIP")
		rerunButton.addActionListener(this)
		toolbar.add(rerunButton)
		rerunWorksheetButton = new ToolbarButton(UtplsqlResources.getIcon("RUN_WORKSHEET_ICON"))
		rerunWorksheetButton.toolTipText = UtplsqlResources.getString("RUNNER_RERUN_WORKSHEET_TOOLTIP")
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
		clearButton.toolTipText = UtplsqlResources.getString("RUNNER_CLEAR_HISTORY_BUTTON")
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
		val testCounterLabel = new JLabel(UtplsqlResources.getString("RUNNER_TESTS_LABEL") + ":", JLabel::LEADING)
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
		showDisabledCounterCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_DISABLED_COUNTER_LABEL").replace("?",""), true)
		showDisabledCounterCheckBoxMenuItem.addActionListener(this)
		countersPopupMenu.add(showDisabledCounterCheckBoxMenuItem)
		showWarningsCounterCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_WARNINGS_COUNTER_LABEL").replace("?",""), true)
		showWarningsCounterCheckBoxMenuItem.addActionListener(this)
		countersPopupMenu.add(showWarningsCounterCheckBoxMenuItem)
		showInfoCounterCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_INFO_COUNTER_LABEL").replace("?",""), true)
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
		testOverviewTable.rowHeight = OVERVIEW_TABLE_ROW_HEIGHT
		testOverviewTable.tableHeader.preferredSize = new Dimension(testOverviewTable.tableHeader.getPreferredSize.width, OVERVIEW_TABLE_ROW_HEIGHT)
		testOverviewTable.selectionModel.addListSelectionListener(new TestOverviewRowListener(this))
		testOverviewTable.addMouseListener(this)		
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
		testOverviewRunMenuItem = new JMenuItem(UtplsqlResources.getString("RUNNER_RUN_MENUITEM"), UtplsqlResources.getIcon("RUN_ICON"));
		testOverviewRunMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(testOverviewRunMenuItem)
		testOverviewRunWorksheetMenuItem = new JMenuItem(UtplsqlResources.getString("RUNNER_RUN_WORKSHEET_MENUITEM"), UtplsqlResources.getIcon("RUN_WORKSHEET_ICON"));
		testOverviewRunWorksheetMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(testOverviewRunWorksheetMenuItem)
		testOverviewPopupMenu.add(new JSeparator)
		showWarningIndicatorCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_WARNING_INDICATOR_LABEL").replace("?",""), true)
		showWarningIndicatorCheckBoxMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(showWarningIndicatorCheckBoxMenuItem)
		showInfoIndicatorCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SHOW_INFO_INDICATOR_LABEL").replace("?",""), true)
		showInfoIndicatorCheckBoxMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(showInfoIndicatorCheckBoxMenuItem)
		syncDetailTabCheckBoxMenuItem = new JCheckBoxMenuItem(UtplsqlResources.getString("PREF_SYNC_DETAIL_TAB_LABEL").replace("?",""), true)
		syncDetailTabCheckBoxMenuItem.addActionListener(this)
		testOverviewPopupMenu.add(syncDetailTabCheckBoxMenuItem)
		testOverviewTable.componentPopupMenu = testOverviewPopupMenu

		// Test tabbed pane (Test Properties)
		val testInfoPanel = new ScrollablePanel
		testInfoPanel.setLayout(new GridBagLayout())
		// - Owner
		val testOwnerLabel = new JLabel(UtplsqlResources.getString("RUNNER_OWNER_LABEL"))
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testOwnerLabel, c)
		testOwnerTextField = new RunnerTextField
		testOwnerTextField.editable = false
		c.gridx = 1
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(10, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testOwnerTextField, c)
		// - Package
		val testPackageLabel = new JLabel(UtplsqlResources.getString("RUNNER_PACKAGE_LABEL"))
		c.gridx = 0
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testPackageLabel, c)
		testPackageTextField = new RunnerTextField
		testPackageTextField.editable = false
		c.gridx = 1
		c.gridy = 1
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testPackageTextField, c)
		// - Procedure
		val testProcedureLabel = new JLabel(UtplsqlResources.getString("RUNNER_PROCEDURE_LABEL"))
		c.gridx = 0
		c.gridy = 2
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testProcedureLabel, c)
		testProcedureTextField = new RunnerTextField
		testProcedureTextField.editable = false
		c.gridx = 1
		c.gridy = 2
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testProcedureTextField, c)
		// - Description
		val testDescriptionLabel = new JLabel(UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL"))
		c.gridx = 0
		c.gridy = 3
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::NORTHWEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		testInfoPanel.add(testDescriptionLabel, c)
		testDescriptionTextArea = new RunnerTextArea
		testDescriptionTextArea.editable = false
		testDescriptionTextArea.enabled = true
		testDescriptionTextArea.lineWrap = true
		testDescriptionTextArea.wrapStyleWord = true
		c.gridx = 1
		c.gridy = 3
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testDescriptionTextArea, c)
		// - Suitepath (id)
		val testIdLabel = new JLabel(UtplsqlResources.getString("RUNNER_TEST_ID_COLUMN"))
		c.gridx = 0
		c.gridy = 4
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::NORTHWEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0
		testInfoPanel.add(testIdLabel, c)
		testIdTextArea = new RunnerTextArea
		testIdTextArea.editable = false
		testIdTextArea.enabled = true
		testIdTextArea.lineWrap = true
		testIdTextArea.wrapStyleWord = false
		c.gridx = 1
		c.gridy = 4
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 0, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testIdTextArea, c)
		// - Start
		val testStartLabel = new JLabel(UtplsqlResources.getString("RUNNER_START_LABEL"))
		c.gridx = 0
		c.gridy = 5
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 10, 10, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::NONE
		c.weightx = 0
		c.weighty = 0		
		testInfoPanel.add(testStartLabel, c)
		testStartTextField = new RunnerTextField
		testStartTextField.editable = false
		c.gridx = 1
		c.gridy = 5
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(5, 5, 10, 10) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::HORIZONTAL
		c.weightx = 1
		c.weighty = 0
		testInfoPanel.add(testStartTextField, c)
		// - Vertical spring and scrollbar for info panel
		c.gridx = 0
		c.gridy = 6
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(0, 0, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 0
		c.weighty = 1
		testInfoPanel.add(Box.createVerticalGlue(), c)
		val testPropertiesScrollPane = new JScrollPane(testInfoPanel)

		// Failures tabbed pane (failed expectations)
		// - failures table (number and description)
		failuresTableModel = new FailuresTableModel
		failuresTable = new JTable(failuresTableModel)
		failuresTable.tableHeader.reorderingAllowed = false
		failuresTable.selectionModel.addListSelectionListener(new FailuresRowListener(this))
		failuresTable.addMouseListener(this)
		val failuresTableHeaderRenderer = new FailuresTableHeaderRenderer		
		val failuresTableNumber = failuresTable.columnModel.getColumn(0)
		failuresTableNumber.headerRenderer = failuresTableHeaderRenderer
		failuresTableNumber.preferredWidth = 30
		failuresTableNumber.maxWidth = 30
		val failuresDescription = failuresTable.columnModel.getColumn(1)
		failuresDescription.headerRenderer = failuresTableHeaderRenderer
		val failuresTableScrollPane = new JScrollPane(failuresTable)		
		// - failures details
		testFailureMessageTextPane = new RunnerTextPane
		testFailureMessageTextPane.editable = false
		testFailureMessageTextPane.enabled = true
		testFailureMessageTextPane.contentType = "text/html"
		testFailureMessageTextPane.minimumSize = TEXTPANE_DIM	
		testFailureMessageTextPane.preferredSize = TEXTPANE_DIM	
		testFailureMessageTextPane.addHyperlinkListener(this)
		val testFailureMessageScrollPane = new JScrollPane(testFailureMessageTextPane)
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
		testErrorStackTextPane = new RunnerTextPane
		testErrorStackTextPane.editable = false
		testErrorStackTextPane.enabled = true
		testErrorStackTextPane.contentType = "text/html"
		testErrorStackTextPane.minimumSize = TEXTPANE_DIM	
		testErrorStackTextPane.preferredSize = TEXTPANE_DIM	
		testErrorStackTextPane.addHyperlinkListener(this)
		val testErrorStackScrollPane = new JScrollPane(testErrorStackTextPane)
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(0, 0, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		testErrorStackPanel.add(testErrorStackScrollPane, c)
		
		// Warnings tabbed pane
		val testWarningsPanel = new JPanel
		testWarningsPanel.setLayout(new GridBagLayout())
		testWarningsTextPane = new RunnerTextPane
		testWarningsTextPane.editable = false
		testWarningsTextPane.enabled = true
		testWarningsTextPane.contentType = "text/html"
		testWarningsTextPane.minimumSize = TEXTPANE_DIM	
		testWarningsTextPane.preferredSize = TEXTPANE_DIM	
		testWarningsTextPane.addHyperlinkListener(this)
		val testWarningsScrollPane = new JScrollPane(testWarningsTextPane)
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(0, 0, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		testWarningsPanel.add(testWarningsScrollPane, c)

		// Info tabbed pane (Server Output)
		val testServerOutputPanel = new JPanel
		testServerOutputPanel.setLayout(new GridBagLayout())
		testServerOutputTextPane = new RunnerTextPane
		testServerOutputTextPane.editable = false
		testServerOutputTextPane.enabled = true
		testServerOutputTextPane.contentType = "text/html"
		testServerOutputTextPane.minimumSize = TEXTPANE_DIM	
		testServerOutputTextPane.preferredSize = TEXTPANE_DIM	
		testServerOutputTextPane.addHyperlinkListener(this)
		val testServerOutputScrollPane = new JScrollPane(testServerOutputTextPane)
		c.gridx = 0
		c.gridy = 0
		c.gridwidth = 1
		c.gridheight = 1
		c.insets = new Insets(0, 0, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints::WEST
		c.fill = GridBagConstraints::BOTH
		c.weightx = 1
		c.weighty = 1
		testServerOutputPanel.add(testServerOutputScrollPane, c)

		// split pane with all tabs
		testDetailTabbedPane = new JTabbedPane()
		testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_TEST_TAB_LABEL"), testPropertiesScrollPane)
		testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_FAILURES_TAB_LABEL"), failuresSplitPane)
		testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_ERRORS_TAB_LABEL"), testErrorStackPanel)
		testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_WARNINGS_TAB_LABEL"), testWarningsPanel)
		testDetailTabbedPane.add(UtplsqlResources.getString("RUNNER_INFO_TAB_LABEL"), testServerOutputPanel)
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
		
		// fix missing borders (e.g. on windows look and feel)
		val referenceBorder = testOwnerTextField.border
		testDescriptionTextArea.border = referenceBorder
		testIdTextArea.border = referenceBorder	
	}
	
}