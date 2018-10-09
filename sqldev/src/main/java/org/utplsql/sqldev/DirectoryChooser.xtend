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
package org.utplsql.sqldev

import java.io.File
import java.util.logging.Logger
import javax.swing.JComboBox
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JTextField

class DirectoryChooser {
	val static Logger logger = Logger.getLogger(DirectoryChooser.name)

	def static choose (JFrame parentFrame, String title, String initialDirectory) {
		logger.finest('''parantFrame: «parentFrame»''')
		var String ret = null
		val chooser = new JFileChooser()
		chooser.currentDirectory = new File(initialDirectory)
		chooser.dialogTitle = title
		chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		chooser.acceptAllFileFilterUsed = false
		if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
			ret = chooser.selectedFile.absolutePath
		}
		return ret
	}

	def static void choose (JFrame parentFrame, String title, JTextField textField) {
		val dir = choose(parentFrame, title, textField.text)
		if (dir !== null) {
			textField.text = dir
		}
	}

	def static void choose (JFrame parentFrame, String title, JComboBox<String> comboBox) {
		val dir = choose(parentFrame, title, comboBox.editor.item as String);
		if (dir !== null) {
			comboBox.editor.item = dir
		}
	}	

}
