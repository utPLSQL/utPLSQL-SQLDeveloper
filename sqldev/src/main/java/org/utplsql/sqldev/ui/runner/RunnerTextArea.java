package org.utplsql.sqldev.ui.runner;

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.Caret;

@SuppressWarnings("all")
public class RunnerTextArea extends JTextArea implements FocusListener {
  public RunnerTextArea() {
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
    int _width = this.getWidth();
    int _minus = (_width - 6);
    int _height = this.getHeight();
    int _minus_1 = (_height - 6);
    g.fillRect(3, 3, _minus, _minus_1);
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
