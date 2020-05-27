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

import com.google.common.base.Objects;
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
import java.sql.Connection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.utplsql.sqldev.coverage.CodeCoverageReporter;
import org.utplsql.sqldev.resources.UtplsqlResources;

@SuppressWarnings("all")
public class CodeCoverageReporterDialog extends JFrame implements ActionListener, FocusListener {
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
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        CodeCoverageReporterDialog.createAndShowWithinEventThread(reporter);
      }
    });
  }
  
  private static void createAndShowWithinEventThread(final CodeCoverageReporter reporter) {
    final CodeCoverageReporterDialog frame = new CodeCoverageReporterDialog(reporter);
    reporter.setFrame(frame);
    frame.pack();
    final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(((dim.width / 2) - (frame.getSize().width / 2)), ((dim.height / 2) - (frame.getSize().height / 2)));
    frame.setAlwaysOnTop(true);
    frame.setVisible(true);
  }
  
  public CodeCoverageReporterDialog(final CodeCoverageReporter reporter) {
    super(UtplsqlResources.getString("WINDOW_CODE_COVERAGE_REPORT_LABEL"));
    this.reporter = reporter;
    final Container pane = this.getContentPane();
    GridBagLayout _gridBagLayout = new GridBagLayout();
    pane.setLayout(_gridBagLayout);
    final GridBagConstraints c = new GridBagConstraints();
    GridBagLayout _gridBagLayout_1 = new GridBagLayout();
    JPanel _jPanel = new JPanel(_gridBagLayout_1);
    this.paneParams = _jPanel;
    this.pathsTextArea.setEditable(false);
    this.pathsTextArea.setEnabled(false);
    String _string = UtplsqlResources.getString("WINDOW_PATHS_LABEL");
    StringConcatenation _builder = new StringConcatenation();
    {
      List<String> _pathList = reporter.getPathList();
      boolean _hasElements = false;
      for(final String path : _pathList) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate(", ", "");
        }
        _builder.append(path);
      }
    }
    this.addParam(_string, _builder.toString(), this.pathsTextArea, 50, 2);
    this.addParam(UtplsqlResources.getString("WINDOW_SCHEMAS_LABEL"), "", this.schemasTextField, 0, 0);
    String _string_1 = UtplsqlResources.getString("WINDOW_INCLUDE_OBJECS_LABEL");
    StringConcatenation _builder_1 = new StringConcatenation();
    {
      List<String> _includeObjectList = reporter.getIncludeObjectList();
      boolean _hasElements_1 = false;
      for(final String i : _includeObjectList) {
        if (!_hasElements_1) {
          _hasElements_1 = true;
        } else {
          _builder_1.appendImmediate(", ", "");
        }
        _builder_1.append(i);
      }
    }
    this.addParam(_string_1, _builder_1.toString(), this.includeObjectsTextArea, 66, 4);
    this.addParam(UtplsqlResources.getString("WINDOW_EXCLUDE_OBJECS_LABEL"), "", this.excludeObjectsTextArea, 34, 1);
    final JScrollPane scrollPane = new JScrollPane(this.paneParams);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    Insets _insets = new Insets(10, 10, 0, 10);
    c.insets = _insets;
    c.anchor = GridBagConstraints.NORTH;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    pane.add(scrollPane, c);
    GridBagLayout _gridBagLayout_2 = new GridBagLayout();
    final JPanel panelButtons = new JPanel(_gridBagLayout_2);
    String _string_2 = UtplsqlResources.getString("WINDOW_RUN_BUTTON");
    JButton _jButton = new JButton(_string_2);
    this.runButton = _jButton;
    this.runButton.addActionListener(this);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    Insets _insets_1 = new Insets(0, 0, 0, 0);
    c.insets = _insets_1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    panelButtons.add(this.runButton, c);
    String _string_3 = UtplsqlResources.getString("WINDOW_CANCEL_BUTTON");
    JButton _jButton_1 = new JButton(_string_3);
    this.cancelButton = _jButton_1;
    this.cancelButton.addActionListener(this);
    c.gridx = 1;
    Insets _insets_2 = new Insets(0, 10, 0, 0);
    c.insets = _insets_2;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    panelButtons.add(this.cancelButton, c);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 1;
    Insets _insets_3 = new Insets(30, 10, 10, 10);
    c.insets = _insets_3;
    c.anchor = GridBagConstraints.EAST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.weighty = 0;
    pane.add(panelButtons, c);
    Dimension _dimension = new Dimension(500, 320);
    pane.setPreferredSize(_dimension);
    JRootPane _rootPane = SwingUtilities.getRootPane(this.runButton);
    _rootPane.setDefaultButton(this.runButton);
  }
  
  private void addParam(final String label, final String text, final Component component, final int height, final double weighty) {
    this.paramPos++;
    final GridBagConstraints c = new GridBagConstraints();
    final JLabel paramLabel = new JLabel(label);
    c.gridx = 0;
    c.gridy = this.paramPos;
    c.gridwidth = 1;
    Insets _insets = new Insets(10, 10, 0, 0);
    c.insets = _insets;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;
    c.weighty = 0;
    this.paneParams.add(paramLabel, c);
    c.gridx = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.BOTH;
    Insets _insets_1 = new Insets(10, 10, 0, 10);
    c.insets = _insets_1;
    c.weightx = 1;
    c.weighty = weighty;
    if ((component instanceof JTextField)) {
      ((JTextField)component).setText(text);
      this.paneParams.add(component, c);
    } else {
      if ((component instanceof JTextArea)) {
        ((JTextArea)component).setText(text);
        ((JTextArea)component).setLineWrap(true);
        ((JTextArea)component).setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(component);
        JViewport _viewport = scrollPane.getViewport();
        Dimension _dimension = new Dimension(200, height);
        _viewport.setPreferredSize(_dimension);
        this.paneParams.add(scrollPane, c);
      }
    }
    component.addFocusListener(this);
  }
  
  public void exit() {
    WindowEvent _windowEvent = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
    this.dispatchEvent(_windowEvent);
  }
  
  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      Object _source = e.getSource();
      boolean _equals = Objects.equal(_source, this.runButton);
      if (_equals) {
        this.reporter.setSchemas(this.schemasTextField.getText());
        this.reporter.setIncludeObjects(this.includeObjectsTextArea.getText());
        this.reporter.setExcludeObjects(this.excludeObjectsTextArea.getText());
        this.schemasTextField.setEnabled(false);
        this.includeObjectsTextArea.setEnabled(false);
        this.excludeObjectsTextArea.setEnabled(false);
        this.runButton.setEnabled(false);
        this.reporter.runAsync();
      } else {
        Object _source_1 = e.getSource();
        boolean _equals_1 = Objects.equal(_source_1, this.cancelButton);
        if (_equals_1) {
          boolean _isEnabled = this.runButton.isEnabled();
          if (_isEnabled) {
            this.exit();
          } else {
            Connection _connection = this.reporter.getConnection();
            SimpleAsyncTaskExecutor _simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
            _connection.abort(_simpleAsyncTaskExecutor);
          }
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public void focusGained(final FocusEvent e) {
    boolean _isAncestorOf = this.paneParams.isAncestorOf(e.getComponent());
    if (_isAncestorOf) {
      final int x = (e.getComponent().getLocationOnScreen().x - this.paneParams.getLocationOnScreen().x);
      final int y = (e.getComponent().getLocationOnScreen().y - this.paneParams.getLocationOnScreen().y);
      final int width = e.getComponent().getBounds().width;
      final int height = e.getComponent().getBounds().height;
      final Rectangle rect = new Rectangle(x, y, width, height);
      this.paneParams.scrollRectToVisible(rect);
    }
  }
  
  @Override
  public void focusLost(final FocusEvent e) {
  }
}
