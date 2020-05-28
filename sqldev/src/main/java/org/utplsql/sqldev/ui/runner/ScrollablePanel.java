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

import java.awt.Rectangle
import javax.swing.JPanel
import javax.swing.Scrollable

/*
 * Fixes resizing issues of JTextArea when put into JPanel and JPanel into JScrollPane 
 * Solution is based on https://stackoverflow.com/questions/15783014/jtextarea-on-jpanel-inside-jscrollpane-does-not-resize-properly/15786939
 */ 
class ScrollablePanel extends JPanel implements Scrollable {

	override getPreferredScrollableViewportSize() {
		return super.getPreferredSize()
	}

	override getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 0
	}

	override getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 0
	}

	override getScrollableTracksViewportWidth() {
		return true
	}

	override getScrollableTracksViewportHeight() {
		return false
	}
}
