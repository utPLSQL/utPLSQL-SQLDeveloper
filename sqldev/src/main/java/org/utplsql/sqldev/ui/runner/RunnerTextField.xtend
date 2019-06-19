package org.utplsql.sqldev.ui.runner

import java.awt.Graphics
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JTextField
import javax.swing.UIManager

class RunnerTextField extends JTextField implements FocusListener {
	
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
}
