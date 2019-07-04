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

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Insets
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

/** 
 * FlowLayout subclass that fully supports wrapping of components.
 * Converted to Xtend based on http://www.camick.com/java/source/WrapLayout.java
 */
class WrapLayout extends FlowLayout {

	/** 
	 * Constructs a new <code>WrapLayout</code> with a left
	 * alignment and a default 5-unit horizontal and vertical gap.
	 */
	new() {
		super()
	}

	/** 
	 * Constructs a new <code>FlowLayout</code> with the specified
	 * alignment and a default 5-unit horizontal and vertical gap.
	 * The value of the alignment argument must be one of
	 * <code>WrapLayout</code>, <code>WrapLayout</code>,
	 * or <code>WrapLayout</code>.
	 * @param align the alignment value
	 */
	new(int align) {
		super(align)
	}

	/** 
	 * Creates a new flow layout manager with the indicated alignment
	 * and the indicated horizontal and vertical gaps.
	 * <p>
	 * The value of the alignment argument must be one of
	 * <code>WrapLayout</code>, <code>WrapLayout</code>,
	 * or <code>WrapLayout</code>.
	 * @param align the alignment value
	 * @param hgap the horizontal gap between components
	 * @param vgap the vertical gap between components
	 */
	new(int align, int hgap, int vgap) {
		super(align, hgap, vgap)
	}

	/** 
	 * Returns the preferred dimensions for this layout given the
	 * <i>visible</i> components in the specified target container.
	 * @param target the component which needs to be laid out
	 * @return the preferred dimensions to lay out the
	 * subcomponents of the specified container
	 */
	override Dimension preferredLayoutSize(Container target) {
		return layoutSize(target, true)
	}

	/** 
	 * Returns the minimum dimensions needed to layout the <i>visible</i>
	 * components contained in the specified target container.
	 * @param target the component which needs to be laid out
	 * @return the minimum dimensions to lay out the
	 * subcomponents of the specified container
	 */
	override Dimension minimumLayoutSize(Container target) {
		var Dimension minimum = layoutSize(target, false)
		minimum.width -= (getHgap() + 1)
		return minimum
	}

	/** 
	 * Returns the minimum or preferred dimension needed to layout the target
	 * container.
	 * @param target target to get layout size for
	 * @param preferred should preferred size be calculated
	 * @return the dimension to layout the target container
	 */
	def private Dimension layoutSize(Container target, boolean preferred) {
		synchronized (target.getTreeLock()) {
			// Each row must fit with the width allocated to the containter.
			// When the container width = 0, the preferred width of the container
			// has not yet been calculated so lets ask for the maximum.
			var int targetWidth = target.getSize().width
			var Container container = target
			while (container.getSize().width === 0 && container.getParent() !== null) {
				container = container.getParent()
			}
			targetWidth = container.getSize().width
			if(targetWidth === 0) targetWidth = Integer.MAX_VALUE
			var int hgap = getHgap()
			var int vgap = getVgap()
			var Insets insets = target.getInsets()
			var int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2)
			var int maxWidth = targetWidth - horizontalInsetsAndGap
			// Fit components into the allowed width
			var Dimension dim = new Dimension(0, 0)
			var int rowWidth = 0
			var int rowHeight = 0
			var int nmembers = target.getComponentCount()
			for (var int i = 0; i < nmembers; i++) {
				var Component m = target.getComponent(i)
				if (m.isVisible()) {
					var Dimension d = if(preferred) m.getPreferredSize() else m.getMinimumSize()
					// Can't add the component to current row. Start a new row.
					if (rowWidth + d.width > maxWidth) {
						addRow(dim, rowWidth, rowHeight)
						rowWidth = 0
						rowHeight = 0
					}
					// Add a horizontal gap for all components after the first
					if (rowWidth !== 0) {
						rowWidth += hgap
					}
					rowWidth += d.width
					rowHeight = Math.max(rowHeight, d.height)
				}
			}
			addRow(dim, rowWidth, rowHeight)
			dim.width += horizontalInsetsAndGap
			dim.height += insets.top + insets.bottom + vgap * 2
			// When using a scroll pane or the DecoratedLookAndFeel we need to
			// make sure the preferred size is less than the size of the
			// target containter so shrinking the container size works
			// correctly. Removing the horizontal gap is an easy way to do this.
			var Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane, target)
			if (scrollPane !== null && target.isValid()) {
				dim.width -= (hgap + 1)
			}
			return dim
		}
	}

	/*
	 *  A new row has been completed. Use the dimensions of this row
	 *  to update the preferred size for the container.
	 * 
	 *  @param dim update the width and height when appropriate
	 *  @param rowWidth the width of the row to add
	 *  @param rowHeight the height of the row to add
	 */
	def private void addRow(Dimension dim, int rowWidth, int rowHeight) {
		dim.width = Math.max(dim.width, rowWidth)
		if (dim.height > 0) {
			dim.height += getVgap()
		}
		dim.height += rowHeight
	}
}
