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
package org.utplsql.sqldev.test.preference

import java.awt.Toolkit
import javax.swing.JFrame
import javax.swing.SwingUtilities
import org.junit.Test
import org.utplsql.sqldev.ui.preference.PreferencePanel

class PreferencePanelTest {
	
	@Test
	def void layoutTest() {
		val frame = new JFrame("Preference Panel")
		SwingUtilities.invokeLater(new Runnable() {
			override run() {
				val panel = new PreferencePanel
				frame.add(panel)
				frame.pack
				val dim = Toolkit.getDefaultToolkit().getScreenSize();
				frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
				frame.setVisible(true)
			}
		});
		Thread.sleep(4 * 1000)
		frame.dispose		
	}
	
}