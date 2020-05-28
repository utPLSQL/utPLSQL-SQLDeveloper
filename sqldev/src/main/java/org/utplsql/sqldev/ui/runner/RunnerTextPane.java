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

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextPane;
import javax.swing.UIManager;

public class RunnerTextPane extends JTextPane implements FocusListener {
    private static final long serialVersionUID = 1089473481444949272L;

    public RunnerTextPane() {
        super();
        addFocusListener(this);
    }

    @Override
    public void paintComponent(final Graphics g) {
        // default for non-opaque components
        if (isOpaque()) {
            super.paintComponent(g);
            return;
        }

        // use value of JTextField for consistency
        g.setColor(UIManager.getColor("TextField.inactiveBackground"));
        g.fillRect(0, 0, getWidth(), getHeight());
        setOpaque(false);
        
        // do rest, changing opaque to ensure background is not overwritten
        super.paintComponent(g);
        setOpaque(true);
    }

    @Override
    public void focusGained(final FocusEvent e) {
        getCaret().setVisible(true);
    }

    @Override
    public void focusLost(final FocusEvent e) {
        getCaret().setVisible(false);
    }

    @Override
    public void setText(final String t) {
        super.setText(t);
        // ensure left parts of long lines are always visible
        setCaretPosition(0);
    }
}
