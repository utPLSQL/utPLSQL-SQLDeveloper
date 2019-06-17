package org.utplsql.sqldev.ui.runner

import java.awt.Graphics
import javax.swing.JTextArea
import javax.swing.UIManager

class RunnerTextArea extends JTextArea {

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
}
