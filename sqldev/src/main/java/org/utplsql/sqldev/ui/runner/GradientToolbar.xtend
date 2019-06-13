package org.utplsql.sqldev.ui.runner

import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JToolBar

class GradientToolbar extends JToolBar { 
	override paintComponent(Graphics g) {
		// default for non-opaque components
		if (!opaque) {
			super.paintComponent(g)
			return
		}
		
		// paint gradient background from top to bottom with separator line at the bottom
		val g2d = g as Graphics2D
		val w = width
        val h = height - 1
        val h2 = height / 2 as int
        val colorTop = new Color(237, 237, 237)
        val colorMiddle = new Color(244, 244, 244)
        val colorBottom = new Color(254, 254, 254)
        val colorBottomLine = Color.LIGHT_GRAY
        val gp1 = new GradientPaint(0, 0, colorTop, 0, h2, colorMiddle)
        g2d.paint = gp1
        g2d.fillRect(0, 0, w, h2)
        val gp2 = new GradientPaint(0, h2, colorMiddle, 0, h, colorBottom)
        g2d.paint = gp2
        g2d.fillRect(0, h2, w, h)
        g2d.paint = colorBottomLine
        g2d.fillRect(0, h, w, h+1)
		
		// do rest, changing opaque to ensure background is not overwritten
		setOpaque(false)
		super.paintComponent(g)
		setOpaque(true)
	}
	
}