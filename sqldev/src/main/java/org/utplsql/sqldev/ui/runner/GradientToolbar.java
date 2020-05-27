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

import com.google.common.base.Objects;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("all")
public class GradientToolbar extends JToolBar {
  private boolean isOracleLookAndFeel() {
    LookAndFeel _lookAndFeel = UIManager.getLookAndFeel();
    String _name = null;
    if (_lookAndFeel!=null) {
      _name=_lookAndFeel.getName();
    }
    final String laf = _name;
    boolean _equals = Objects.equal(laf, "Oracle Look and Feel version 2");
    if (_equals) {
      return true;
    } else {
      return false;
    }
  }
  
  public GradientToolbar() {
    super();
    boolean _isOracleLookAndFeel = this.isOracleLookAndFeel();
    if (_isOracleLookAndFeel) {
      Insets _insets = new Insets(2, 2, 2, 2);
      EmptyBorder _emptyBorder = new EmptyBorder(_insets);
      this.setBorder(_emptyBorder);
    } else {
      BevelBorder _bevelBorder = new BevelBorder(BevelBorder.RAISED);
      this.setBorder(_bevelBorder);
    }
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    boolean _isOracleLookAndFeel = this.isOracleLookAndFeel();
    if (_isOracleLookAndFeel) {
      boolean _isOpaque = this.isOpaque();
      boolean _not = (!_isOpaque);
      if (_not) {
        super.paintComponent(g);
        return;
      }
      final Graphics2D g2d = ((Graphics2D) g);
      final int w = this.getWidth();
      int _height = this.getHeight();
      final int h = (_height - 1);
      int _height_1 = this.getHeight();
      final int h2 = (_height_1 / 2);
      final Color colorTop = new Color(237, 237, 237);
      final Color colorMiddle = new Color(244, 244, 244);
      final Color colorBottom = new Color(254, 254, 254);
      final Color colorBottomLine = Color.LIGHT_GRAY;
      final GradientPaint gp1 = new GradientPaint(0, 0, colorTop, 0, h2, colorMiddle);
      g2d.setPaint(gp1);
      g2d.fillRect(0, 0, w, h2);
      final GradientPaint gp2 = new GradientPaint(0, h2, colorMiddle, 0, h, colorBottom);
      g2d.setPaint(gp2);
      g2d.fillRect(0, h2, w, h);
      g2d.setPaint(colorBottomLine);
      g2d.fillRect(0, h, w, (h + 1));
      this.setOpaque(false);
      super.paintComponent(g);
      this.setOpaque(true);
    } else {
      super.paintComponent(g);
    }
  }
}
