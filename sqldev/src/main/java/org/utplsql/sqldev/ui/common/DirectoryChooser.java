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
package org.utplsql.sqldev.ui.common;

import java.io.File;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class DirectoryChooser {
    private static final Logger logger = Logger.getLogger(DirectoryChooser.class.getName());

    // do not instantiate this class
    private DirectoryChooser() {
        super();
    }

    public static String choose(final JFrame parentFrame, final String title, final String initialDirectory) {
        logger.finest(() -> "parantFrame: " + parentFrame);
        String ret = null;
        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(initialDirectory));
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            ret = chooser.getSelectedFile().getAbsolutePath();
        }
        return ret;
    }

    public static void choose(final JFrame parentFrame, final String title, final JTextField textField) {
        final String dir = choose(parentFrame, title, textField.getText());
        if (dir != null) {
            textField.setText(dir);
        }
    }
}
