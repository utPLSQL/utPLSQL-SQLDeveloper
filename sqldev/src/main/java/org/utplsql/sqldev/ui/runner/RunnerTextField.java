package org.utplsql.sqldev.ui.runner;

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.Caret;

@SuppressWarnings("all")
public class RunnerTextField extends JTextField implements FocusListener {
  public RunnerTextField() {
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
}
