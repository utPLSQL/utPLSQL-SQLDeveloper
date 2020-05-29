/**
 * Copyright 2018 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
package org.utplsql.sqldev.test.preference;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Test;
import org.utplsql.sqldev.ui.preference.PreferencePanel;

@SuppressWarnings("all")
public class PreferencePanelTest {
  @Test
  public void layout() {
    try {
      final JFrame frame = new JFrame("Preference Panel");
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final PreferencePanel panel = new PreferencePanel();
          frame.add(panel);
          Dimension _dimension = new Dimension(600, 400);
          frame.setPreferredSize(_dimension);
          frame.pack();
          final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
          frame.setLocation(((dim.width / 2) - (frame.getSize().width / 2)), ((dim.height / 2) - (frame.getSize().height / 2)));
          frame.setVisible(true);
        }
      });
      Thread.sleep((4 * 1000));
      frame.dispose();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
