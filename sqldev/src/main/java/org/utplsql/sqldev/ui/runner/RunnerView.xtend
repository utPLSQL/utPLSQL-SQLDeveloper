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

import oracle.ide.docking.DockableWindow
import oracle.ide.layout.ViewId
import org.utplsql.sqldev.resources.UtplsqlResources

class RunnerView extends DockableWindow {
	static val VIEW_NAME = "UTPLSQL_RUNNER_VIEW"
	public static val ViewId VIEW_ID = new ViewId(RunnerFactory.FACTORY_NAME, VIEW_NAME)
	var RunnerPanel panel
	
	override getTitleName() {
		return UtplsqlResources.getString("RUNNER_VIEW_TITLE")
	}
	
	override getGUI() {
		if (panel === null) {
			panel = new RunnerPanel
		}
		return panel.getGUI()
	}
	
	def getRunnerPanel() {
		getGUI()
		return panel
	}
}
