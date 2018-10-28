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

import java.util.List
import java.util.logging.Logger
import javax.swing.JSplitPane
import oracle.dbtools.raptor.utils.Connections
import oracle.dbtools.worksheet.editor.OpenWorksheetWizard
import oracle.dbtools.worksheet.editor.Worksheet
import oracle.dbtools.worksheet.utils.WorksheetUtil
import oracle.ide.Ide
import oracle.ide.config.Preferences
import oracle.ide.controller.IdeAction
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.resources.UtplsqlResources

class UtplsqlWorksheet {
	static val Logger logger = Logger.getLogger(UtplsqlWorksheet.name);

	var PreferenceModel preferences
	var String connectionName
	var List<String> pathList

	new(List<String> pathList, String connectionName) {
		this.pathList = pathList
		this.preferences = PreferenceModel.getInstance(Preferences.preferences);
		setConnection(connectionName)
	}

	private def setConnection(String connectionName) {
		if (connectionName !== null && preferences.unsharedWorksheet) {
			// fix for issue #47 - private connections are not closed in SQLDev >= 17.4.0
			try {
				// temporary connection is closed when worksheet is closed, but requires SQLDev >= 17.4.0
				this.connectionName = Connections.instance.createTemporaryConnection(connectionName)
			} catch (Throwable e) {
				// private connection is closed when worksheet is closed in SQLDev < 17.4.0
				this.connectionName = Connections.instance.createPrivateConnection(connectionName)				
			}	
		} else {
			this.connectionName = connectionName;
		}
	}
	
	private def getCode() '''
		«IF preferences.resetPackage»
			EXECUTE dbms_session.reset_package;
		«ENDIF»
		SET SERVEROUTPUT ON SIZE UNLIMITED
		«IF preferences.clearScreen»
			CLEAR SCREEN
		«ENDIF»
		«val paths = pathList»
		«IF paths.size == 1»
			EXECUTE ut.run('«paths.get(0)»');
		«ELSE»
			EXECUTE ut.run(ut_varchar2_list(«FOR path : paths SEPARATOR ', '»'«path»'«ENDFOR»));
		«ENDIF»
	'''

	private def openWorksheet() {
		val worksheet = OpenWorksheetWizard.openNewTempWorksheet(connectionName, code.toString) as Worksheet
		if (connectionName === null) {
			worksheet.comboConnection = null
		}
		WorksheetUtil.setWorksheetTabName(worksheet.context.node.URL, UtplsqlResources.getString("WORKSHEET_TITLE"))
		worksheet.context.node.markDirty(false)
		return worksheet
	}

	private def resizeResultPanel(Worksheet worksheet) {
		Thread.sleep(200) // give script runner time to initiate result panel
		val splitPane = worksheet.selectedResultPanel?.GUI?.parent?.parent?.parent
		if (splitPane instanceof JSplitPane) {
			splitPane.dividerLocation = 0.15 // 15% for Worksheet, 85% for Script Output 
		} else {
			logger.
				severe('''Could not adjust size of worksheet. Expected JSplitPane but got «splitPane?.class?.name».''')
		}
	}

	private def runScript(Worksheet worksheet) {
		if (preferences.autoExecute) {
			Thread.sleep(100) // give worksheet time to initialize
			val action = Ide.getIdeActionMap.get(Ide.findCmdID("Worksheet.RunScript")) as IdeAction
			if (action !== null) {
				action.performAction(worksheet.context)
				worksheet.resizeResultPanel
			}
		}
	}

	private def runTest() {
		val worksheet = openWorksheet
		worksheet.runScript
		logger.fine('''utPLSQL test called for «pathList» in «connectionName».''')
	}

	def runTestAsync() {
		val Runnable runnable = [|runTest]
		val thread = new Thread(runnable)
		thread.name = "utPLSQL run test"
		thread.start
	}

	static def void openWithCode(String code, String connectionName) {
		val worksheet = OpenWorksheetWizard.openNewTempWorksheet(connectionName, code) as Worksheet
		if (connectionName === null) {
			worksheet.comboConnection = null
		}
		WorksheetUtil.setWorksheetTabName(worksheet.context.node.URL, UtplsqlResources.getString("WORKSHEET_TITLE"))
	}

}
