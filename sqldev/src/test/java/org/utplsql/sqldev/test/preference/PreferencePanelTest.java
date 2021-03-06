/*
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

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.SystemTools;
import org.utplsql.sqldev.ui.preference.PreferencePanel;

public class PreferencePanelTest {

    @Test
    public void layout() {
        final JFrame frame = new JFrame("Preference Panel");
        SwingUtilities.invokeLater(() -> {
            final PreferencePanel panel = new PreferencePanel();
            frame.add(panel);
            frame.setPreferredSize(new Dimension(600, 400));
            frame.pack();
            final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
            frame.setVisible(true);
        });
        SystemTools.sleep(4 * 1000);
        Assert.assertNotNull(frame);
        frame.dispose();
    }
}
