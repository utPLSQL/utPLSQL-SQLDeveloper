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

import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import javax.swing.JToolBar
import javax.swing.UIManager
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder

class GradientToolbar extends JToolBar { 

	private def isOracleLookAndFeel() {
		val laf = UIManager.lookAndFeel?.name
		if (laf == "Oracle Look and Feel version 2") {
			return true
		} else {
			return false
		}		
	}
	
	new() {
		super()
		if (oracleLookAndFeel) {
			this.border = new EmptyBorder(new Insets(2, 2, 2, 2)) // top, left, bottom, right
		} else {
			this.border =  new BevelBorder(BevelBorder.RAISED)
		}
	}

	override paintComponent(Graphics g) {
		if (oracleLookAndFeel) {
			// emulate Oracle toolbar
			// 1. default for non-opaque components
			if (!opaque) {
				super.paintComponent(g)
				return
			}
			
			// 2. paint gradient background from top to bottom with separator line at the bottom
			val g2d = g as Graphics2D
			val w = width
	        val h = height - 1
	        val int h2 = height / 2
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
			
			// 3. do rest, changing opaque to ensure background is not overwritten
			setOpaque(false)
			super.paintComponent(g)
			setOpaque(true)
		} else {
			// default logic
			super.paintComponent(g)
		}
	}
	
}