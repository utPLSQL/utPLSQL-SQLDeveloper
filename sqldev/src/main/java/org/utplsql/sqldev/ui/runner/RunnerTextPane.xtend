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

import java.awt.Graphics
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JTextPane
import javax.swing.UIManager

class RunnerTextPane extends JTextPane implements FocusListener{

	new() {
		super()
		this.addFocusListener = this
	}

	override paintComponent(Graphics g) {
		// default for non-opaque components
		if (!opaque) {
			super.paintComponent(g)
			return
		}

		// use value of JTextField for consistency
		g.color = UIManager.getColor("TextField.inactiveBackground")
		g.fillRect(0, 0, width, height)

		// do rest, changing opaque to ensure background is not overwritten
		setOpaque(false)
		super.paintComponent(g)
		setOpaque(true)
	}

	override void focusGained(FocusEvent e) {
		this.caret.visible = true
	}

	override focusLost(FocusEvent e) {
		this.caret.visible = false
	}
	
	override setText(String t) {
		super.setText(t)
		// ensure left parts of long lines are always visible
		caretPosition = 0
	}
}
