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
package org.utplsql.sqldev.ui.coverage

import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.utplsql.sqldev.coverage.CodeCoverageReporter
import org.utplsql.sqldev.resources.UtplsqlResources

class CodeCoverageReporterDialog extends JFrame implements ActionListener, FocusListener {
	
	var CodeCoverageReporter reporter
	var JButton runButton
	var JButton cancelButton
	var JPanel paneParams;
	var int paramPos = -1;
	val pathsTextArea = new JTextArea()
	val schemasTextField = new JTextField()
	val includeObjectsTextArea = new JTextArea()
	val excludeObjectsTextArea = new JTextArea()
	
	def static createAndShow(CodeCoverageReporter reporter) {
		SwingUtilities.invokeLater(new Runnable() {
			override run() {
				CodeCoverageReporterDialog.createAndShowWithinEventThread(reporter);
			}
		});
	}

	private def static createAndShowWithinEventThread(CodeCoverageReporter reporter) {
		// create and layout the dialog
		val frame = new CodeCoverageReporterDialog(reporter)
		reporter.frame = frame
		frame.pack
		// center dialog
		val dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
		frame.alwaysOnTop = true
		frame.visible = true
	}

	new(CodeCoverageReporter reporter) {
		super(UtplsqlResources.getString("WINDOW_CODE_COVERAGE_REPORT_LABEL"))
		this.reporter = reporter
		val pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		val c = new GridBagConstraints();
		
		// parameters pane
		paneParams = new JPanel(new GridBagLayout())
		pathsTextArea.editable = false
		pathsTextArea.enabled = false
		addParam(UtplsqlResources.getString("WINDOW_PATHS_LABEL"), '''«FOR path : reporter.pathList SEPARATOR ", "»«path»«ENDFOR»''', pathsTextArea, 50, 2)
		addParam(UtplsqlResources.getString("WINDOW_SCHEMAS_LABEL"), "", schemasTextField, 0, 0);
		addParam(UtplsqlResources.getString("WINDOW_INCLUDE_OBJECS_LABEL"), '''«FOR i : reporter.includeObjectList SEPARATOR ", "»«i»«ENDFOR»''', includeObjectsTextArea, 66, 4);
		addParam(UtplsqlResources.getString("WINDOW_EXCLUDE_OBJECS_LABEL"), "", excludeObjectsTextArea, 34, 1);
		val scrollPane = new JScrollPane(paneParams)
		scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
		scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
		scrollPane.border = BorderFactory.createEmptyBorder;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(10, 10, 0, 10); // top, left, bottom, right
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		pane.add(scrollPane, c)

		// Buttons pane
		val panelButtons = new JPanel(new GridBagLayout())
		runButton = new JButton(UtplsqlResources.getString("WINDOW_RUN_BUTTON"))
		runButton.addActionListener(this);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 0, 0); // top, left, bottom, right
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		panelButtons.add(runButton, c);
		cancelButton = new JButton(UtplsqlResources.getString("WINDOW_CANCEL_BUTTON"));
		cancelButton.addActionListener(this);
		c.gridx = 1;
		c.insets = new Insets(0, 10, 0, 0); // top, left, bottom, right
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		panelButtons.add(cancelButton, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(30, 10, 10, 10); // top, left, bottom, right
		c.anchor = GridBagConstraints.EAST
		c.fill = GridBagConstraints.NONE
		c.weightx = 0;
		c.weighty = 0;
		pane.add(panelButtons, c);
		pane.setPreferredSize(new Dimension(500, 320));
		SwingUtilities.getRootPane(runButton).defaultButton = runButton
	}
	
	private def addParam(String label, String text, Component component, int height, double weighty) {
		paramPos++
		val c = new GridBagConstraints();
		val paramLabel = new JLabel(label)
		c.gridx = 0
		c.gridy = paramPos
		c.gridwidth = 1
		c.insets = new Insets(10, 10, 0, 0) // top, left, bottom, right
		c.anchor = GridBagConstraints.NORTHWEST
		c.fill = GridBagConstraints.HORIZONTAL
		c.weightx = 0
		c.weighty = 0
		paneParams.add(paramLabel, c);
		c.gridx = 1
		c.gridwidth = GridBagConstraints.REMAINDER
		c.anchor = GridBagConstraints.WEST
		c.fill = GridBagConstraints.BOTH
		c.insets = new Insets(10, 10, 0, 10); // top, left, bottom, right
		c.weightx = 1
		c.weighty = weighty
		if (component instanceof JTextField) {
			component.text = text
			paneParams.add(component, c)			
		} else if (component instanceof JTextArea) {
			component.text = text
			component.lineWrap = true
			component.wrapStyleWord = true
			var scrollPane = new JScrollPane(component);
			scrollPane.viewport.preferredSize = new Dimension(200, height)
			paneParams.add(scrollPane, c)
		}
		component.addFocusListener(this)
	}
	
	def exit() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	override actionPerformed(ActionEvent e) {
		if (e.getSource == runButton) {
			reporter.schemas = schemasTextField.text
			reporter.includeObjects = includeObjectsTextArea.text
			reporter.excludeObjects = excludeObjectsTextArea.text
			schemasTextField.setEnabled(false)
			includeObjectsTextArea.setEnabled(false)
			excludeObjectsTextArea.setEnabled(false)
			runButton.setEnabled(false)
			reporter.runAsync
		} else if (e.getSource == cancelButton) {
			if (runButton.enabled) {
				// report is not yet started, just close the window
				exit
			} else {
				// report is being created...
				// frame will close as soon as the connection is technically aborted
				// database session is not cancelled. This is not a bug.
				// to cancel the session you have to kill it via "ALTER SYSTEM KILL SESSION".
				// However, the abort frees all resources on the client side. 
				reporter.connection.abort(new SimpleAsyncTaskExecutor)
			}
		}
	}
	
	override focusGained(FocusEvent e) {
		if (paneParams.isAncestorOf(e.component)) {
			// make component at cursor position is visible
			val x = e.component.getLocationOnScreen.x - paneParams.getLocationOnScreen.x
			val y = e.component.getLocationOnScreen.y - paneParams.getLocationOnScreen.y
			val width = e.component.getBounds.width
			val height = e.component.getBounds.height
			val rect = new Rectangle(x, y, width, height)
			paneParams.scrollRectToVisible(rect)
		}
	}
	
	override focusLost(FocusEvent e) {
		// ignore
	}

}