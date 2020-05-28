/**
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
package org.utplsql.sqldev.ui.runner;

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Caret;

@SuppressWarnings("all")
public class RunnerTextPane extends JTextPane implements FocusListener {
  public RunnerTextPane() {
    super();
    this.addFocusListener(this);
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    boolean _isOpaque = this.isOpaque();
    boolean _not = (!_isOpaque);
    if (_not) {
      super.paintComponent(g);
      return;
    }
    g.setColor(UIManager.getColor("TextField.inactiveBackground"));
    g.fillRect(0, 0, this.getWidth(), this.getHeight());
    this.setOpaque(false);
    super.paintComponent(g);
    this.setOpaque(true);
  }
  
  @Override
  public void focusGained(final FocusEvent e) {
    Caret _caret = this.getCaret();
    _caret.setVisible(true);
  }
  
  @Override
  public void focusLost(final FocusEvent e) {
    Caret _caret = this.getCaret();
    _caret.setVisible(false);
  }
  
  @Override
  public void setText(final String t) {
    super.setText(t);
    this.setCaretPosition(0);
  }
}
