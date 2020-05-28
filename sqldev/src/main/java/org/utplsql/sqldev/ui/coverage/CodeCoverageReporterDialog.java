/**
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
package org.utplsql.sqldev.ui.coverage;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.utplsql.sqldev.coverage.CodeCoverageReporter;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.StringTools;
import org.utplsql.sqldev.resources.UtplsqlResources;

public class CodeCoverageReporterDialog extends JFrame implements ActionListener, FocusListener {
    private static final long serialVersionUID = 5503685225300993401L;

    private CodeCoverageReporter reporter;
    private JButton runButton;
    private JButton cancelButton;
    private JPanel paneParams;
    private int paramPos = (-1);
    private final JTextArea pathsTextArea = new JTextArea();
    private final JTextField schemasTextField = new JTextField();
    private final JTextArea includeObjectsTextArea = new JTextArea();
    private final JTextArea excludeObjectsTextArea = new JTextArea();

    public static void createAndShow(final CodeCoverageReporter reporter) {
        SwingUtilities.invokeLater(() -> CodeCoverageReporterDialog.createAndShowWithinEventThread(reporter));
    }

    private static void createAndShowWithinEventThread(final CodeCoverageReporter reporter) {
        // create and layout the dialog
        final CodeCoverageReporterDialog frame = new CodeCoverageReporterDialog(reporter);
        reporter.setFrame(frame);
        frame.pack();
        // center dialog
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    public CodeCoverageReporterDialog(final CodeCoverageReporter reporter) {
        super(UtplsqlResources.getString("WINDOW_CODE_COVERAGE_REPORT_LABEL"));
        this.reporter = reporter;
        final Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        
        // parameters pane
        paneParams = new JPanel(new GridBagLayout());
        pathsTextArea.setEditable(false);
        pathsTextArea.setEnabled(false);
        addParam(UtplsqlResources.getString("WINDOW_PATHS_LABEL"), StringTools.getSimpleCSV(reporter.getPathList()), pathsTextArea, 50, 2);
        addParam(UtplsqlResources.getString("WINDOW_SCHEMAS_LABEL"), "", schemasTextField, 0, 0);
        addParam(UtplsqlResources.getString("WINDOW_INCLUDE_OBJECS_LABEL"), StringTools.getSimpleCSV(reporter.getIncludeObjectList()), includeObjectsTextArea, 66, 4);
        addParam(UtplsqlResources.getString("WINDOW_EXCLUDE_OBJECS_LABEL"), "", excludeObjectsTextArea, 34, 1);
        final JScrollPane scrollPane = new JScrollPane(paneParams);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(10, 10, 0, 10); // top, left, bottom, right
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        pane.add(scrollPane, c);

        // Buttons pane
        final JPanel panelButtons = new JPanel(new GridBagLayout());
        runButton = new JButton(UtplsqlResources.getString("WINDOW_RUN_BUTTON"));
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
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        pane.add(panelButtons, c);
        pane.setPreferredSize(new Dimension(500, 320));
        SwingUtilities.getRootPane(runButton).setDefaultButton(runButton);
    }

    private void addParam(final String label, final String text, final Component component, final int height,
            final double weighty) {
        paramPos++;
        final GridBagConstraints c = new GridBagConstraints();
        final JLabel paramLabel = new JLabel(label);
        c.gridx = 0;
        c.gridy = paramPos;
        c.gridwidth = 1;
        c.insets =  new Insets(10, 10, 0, 0); // top, left, bottom, right
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        paneParams.add(paramLabel, c);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10, 10, 0, 10); // top, left, bottom, right
        c.weightx = 1;
        c.weighty = weighty;
        if (component instanceof JTextField) {
            ((JTextField) component).setText(text);
            paneParams.add(component, c);
        } else if (component instanceof JTextArea) {
            ((JTextArea) component).setText(text);
            ((JTextArea) component).setLineWrap(true);
            ((JTextArea) component).setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(component);
            scrollPane.getViewport().setPreferredSize(new Dimension(200, height));
            paneParams.add(scrollPane, c);
        }
        component.addFocusListener(this);
    }

    public void exit() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == runButton) {
            reporter.setSchemas(schemasTextField.getText());
            reporter.setIncludeObjects(includeObjectsTextArea.getText());
            reporter.setExcludeObjects(excludeObjectsTextArea.getText());
            schemasTextField.setEnabled(false);
            includeObjectsTextArea.setEnabled(false);
            excludeObjectsTextArea.setEnabled(false);
            runButton.setEnabled(false);
            reporter.runAsync();
        } else {
            if (e.getSource() == cancelButton) {
                if (runButton.isEnabled()) {
                    // report is not yet started, just close the window
                    exit();
                } else {
                    // report is being created...
                    // frame will close as soon as the connection is technically aborted
                    // database session is not cancelled. This is not a bug.
                    // to cancel the session you have to kill it via "ALTER SYSTEM KILL SESSION".
                    // However, the abort frees all resources on the client side. 
                    DatabaseTools.abortConnection(reporter.getConnection());
                }
            }
        }
    }

    @Override
    public void focusGained(final FocusEvent e) {
        if (paneParams.isAncestorOf(e.getComponent())) {
            // make component at cursor position is visible
            final int x = e.getComponent().getLocationOnScreen().x - paneParams.getLocationOnScreen().x;
            final int y = e.getComponent().getLocationOnScreen().y - paneParams.getLocationOnScreen().y;
            final int width = e.getComponent().getBounds().width;
            final int height = e.getComponent().getBounds().height;
            final Rectangle rect = new Rectangle(x, y, width, height);
            paneParams.scrollRectToVisible(rect);
        }
    }

    @Override
    public void focusLost(final FocusEvent e) {
        // ignore
    }
}
