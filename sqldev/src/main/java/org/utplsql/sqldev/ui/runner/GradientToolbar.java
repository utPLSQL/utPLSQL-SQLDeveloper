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
package org.utplsql.sqldev.ui.runner;

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

public class GradientToolbar extends JToolBar {
    private static final long serialVersionUID = 6128707792081725058L;

    private boolean isOracleLookAndFeel() {
        LookAndFeel laf = UIManager.getLookAndFeel();
        final String lafName = laf != null ? laf.getName() : null;
        return "Oracle Look and Feel version 2".equals(lafName);
    }

    public GradientToolbar() {
        super();
        if (isOracleLookAndFeel()) {
            setBorder(new EmptyBorder(new Insets(2, 2, 2, 2))); // insets: top, left, bottom, right
        } else {
            setBorder(new BevelBorder(BevelBorder.RAISED));
        }
    }

    @Override
    public void paintComponent(final Graphics g) {
        if (isOracleLookAndFeel()) {
            // emulate Oracle toolbar
            // 1. default for non-opaque components
            if (!isOpaque()) {
                super.paintComponent(g);
                return;
            }

            // 2. paint gradient background from top to bottom with separator line at the bottom
            final Graphics2D g2d = ((Graphics2D) g);
            final int w = getWidth();
            final int h = (getHeight() - 1);
            final int h2 = (getHeight() / 2);
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

            // 3. do rest, changing opaque to ensure background is not overwritten
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(true);
        } else {
            // default logic
            super.paintComponent(g);
        }
    }
}
