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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * FlowLayout subclass that fully supports wrapping of components.
 * Converted to Xtend based on http://www.camick.com/java/source/WrapLayout.java
 */
@SuppressWarnings("all")
public class WrapLayout extends FlowLayout {
  /**
   * Constructs a new <code>WrapLayout</code> with a left
   * alignment and a default 5-unit horizontal and vertical gap.
   */
  public WrapLayout() {
    super();
  }
  
  /**
   * Constructs a new <code>FlowLayout</code> with the specified
   * alignment and a default 5-unit horizontal and vertical gap.
   * The value of the alignment argument must be one of
   * <code>WrapLayout</code>, <code>WrapLayout</code>,
   * or <code>WrapLayout</code>.
   * @param align the alignment value
   */
  public WrapLayout(final int align) {
    super(align);
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
  public WrapLayout(final int align, final int hgap, final int vgap) {
    super(align, hgap, vgap);
  }
  
  /**
   * Returns the preferred dimensions for this layout given the
   * <i>visible</i> components in the specified target container.
   * @param target the component which needs to be laid out
   * @return the preferred dimensions to lay out the
   * subcomponents of the specified container
   */
  @Override
  public Dimension preferredLayoutSize(final Container target) {
    return this.layoutSize(target, true);
  }
  
  /**
   * Returns the minimum dimensions needed to layout the <i>visible</i>
   * components contained in the specified target container.
   * @param target the component which needs to be laid out
   * @return the minimum dimensions to lay out the
   * subcomponents of the specified container
   */
  @Override
  public Dimension minimumLayoutSize(final Container target) {
    Dimension minimum = this.layoutSize(target, false);
    int _width = minimum.width;
    int _hgap = this.getHgap();
    int _plus = (_hgap + 1);
    minimum.width = (_width - _plus);
    return minimum;
  }
  
  /**
   * Returns the minimum or preferred dimension needed to layout the target
   * container.
   * @param target target to get layout size for
   * @param preferred should preferred size be calculated
   * @return the dimension to layout the target container
   */
  private Dimension layoutSize(final Container target, final boolean preferred) {
    synchronized (target.getTreeLock()) {
      int targetWidth = target.getSize().width;
      Container container = target;
      while (((container.getSize().width == 0) && (container.getParent() != null))) {
        container = container.getParent();
      }
      targetWidth = container.getSize().width;
      if ((targetWidth == 0)) {
        targetWidth = Integer.MAX_VALUE;
      }
      int hgap = this.getHgap();
      int vgap = this.getVgap();
      Insets insets = target.getInsets();
      int horizontalInsetsAndGap = ((insets.left + insets.right) + (hgap * 2));
      int maxWidth = (targetWidth - horizontalInsetsAndGap);
      Dimension dim = new Dimension(0, 0);
      int rowWidth = 0;
      int rowHeight = 0;
      int nmembers = target.getComponentCount();
      for (int i = 0; (i < nmembers); i++) {
        {
          Component m = target.getComponent(i);
          boolean _isVisible = m.isVisible();
          if (_isVisible) {
            Dimension _xifexpression = null;
            if (preferred) {
              _xifexpression = m.getPreferredSize();
            } else {
              _xifexpression = m.getMinimumSize();
            }
            Dimension d = _xifexpression;
            if (((rowWidth + d.width) > maxWidth)) {
              this.addRow(dim, rowWidth, rowHeight);
              rowWidth = 0;
              rowHeight = 0;
            }
            if ((rowWidth != 0)) {
              int _rowWidth = rowWidth;
              rowWidth = (_rowWidth + hgap);
            }
            int _rowWidth_1 = rowWidth;
            rowWidth = (_rowWidth_1 + d.width);
            rowHeight = Math.max(rowHeight, d.height);
          }
        }
      }
      this.addRow(dim, rowWidth, rowHeight);
      int _width = dim.width;
      dim.width = (_width + horizontalInsetsAndGap);
      int _height = dim.height;
      dim.height = (_height + ((insets.top + insets.bottom) + (vgap * 2)));
      Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
      if (((scrollPane != null) && target.isValid())) {
        int _width_1 = dim.width;
        dim.width = (_width_1 - (hgap + 1));
      }
      return dim;
    }
  }
  
  /**
   * A new row has been completed. Use the dimensions of this row
   *  to update the preferred size for the container.
   * 
   *  @param dim update the width and height when appropriate
   *  @param rowWidth the width of the row to add
   *  @param rowHeight the height of the row to add
   */
  private void addRow(final Dimension dim, final int rowWidth, final int rowHeight) {
    dim.width = Math.max(dim.width, rowWidth);
    if ((dim.height > 0)) {
      int _height = dim.height;
      int _vgap = this.getVgap();
      dim.height = (_height + _vgap);
    }
    int _height_1 = dim.height;
    dim.height = (_height_1 + rowHeight);
  }
}
