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
 package org.utplsql.sqldev.test.runner

import java.awt.Toolkit
import java.util.UUID
import javax.swing.JFrame
import javax.swing.SwingUtilities
import org.junit.Before
import org.junit.Test
import org.utplsql.sqldev.model.runner.Run
import org.utplsql.sqldev.resources.UtplsqlResources
import org.utplsql.sqldev.ui.runner.RunnerPanel

class UtplsqlRunnerPanelTest {
	var Run run
	
	@Before
	def void setup() {
		val reporterId = UUID.randomUUID().toString.replace("-", "")
		run = new Run(null, reporterId)
		run.startTime = "2019-06-09T13:42:42.123456"
		run.counter.disabled = 0
		run.counter.success = 0
		run.counter.failure = 0
		run.counter.error = 0
		run.counter.warning = 0
		run.totalNumberOfTests = 5
	}
	
	@Test 
	def void showGUI() {
		val start = System.currentTimeMillis
		val frame = new JFrame("utPLSQL Runner Panel")
		frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
		val panel = new RunnerPanel
		panel.model = run
		val gui = panel.getGUI

		SwingUtilities.invokeLater(new Runnable() {
			override run() {
				frame.add(gui)
				frame.pack
				val dim = Toolkit.getDefaultToolkit().getScreenSize();
				frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
				frame.setVisible(true)
			}
		});

		run.status="starting"
		panel.update(run.reporterId)
		Thread.sleep(3000);

		run.counter.success = run.counter.success + 1
		run.status="utplsql.test.a"
		panel.update(run.reporterId)
		Thread.sleep(500);

		run.counter.success = run.counter.success + 1
		run.status="utplsql.test.b"
		panel.update(run.reporterId)
		Thread.sleep(500);

		run.counter.success = run.counter.success + 1
		run.status="utplsql.test.c"
		panel.update(run.reporterId)
		Thread.sleep(500);

		run.counter.failure = run.counter.failure + 1
		run.status="utplsql.test.d"
		panel.update(run.reporterId)
		Thread.sleep(500);
		
		run.counter.success = run.counter.success + 1
		run.status="utplsql.test.e"
		val end = System.currentTimeMillis
		run.status = String.format(UtplsqlResources.getString("RUNNER_FINNISHED_TEXT"), new Double(end-start)/1000)
		panel.update(run.reporterId)
		Thread.sleep(2000);
		frame.dispose
	}
	
}