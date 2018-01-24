/* Copyright 2018 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
package org.utplsql.sqldev.editor.menu

import java.awt.Dimension
import java.util.logging.Logger
import javax.swing.JEditorPane
import javax.swing.JSplitPane
import oracle.dbtools.raptor.navigator.impl.DatabaseSourceNode
import oracle.dbtools.raptor.utils.Connections
import oracle.dbtools.worksheet.editor.OpenWorksheetWizard
import oracle.dbtools.worksheet.editor.Worksheet
import oracle.dbtools.worksheet.editor.WorksheetGUI
import oracle.dbtools.worksheet.utils.WorksheetUtil
import oracle.ide.Context
import oracle.ide.Ide
import oracle.ide.config.Preferences
import oracle.ide.controller.Controller
import oracle.ide.controller.IdeAction
import oracle.ide.editor.Editor
import oracle.ide.^extension.RegisteredByExtension
import oracle.ide.model.Node
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.parser.UtplsqlParser
import org.utplsql.sqldev.resources.UtplsqlResources

@RegisteredByExtension("org.utplsql.sqldev")
class UtplsqlEditorController implements Controller {
	public static int UTLPLSQL_EDITOR_TEST_CMD_ID = Ide.findCmdID("utplsql.editor.test")
	private static final Logger logger = Logger.getLogger(UtplsqlEditorController.name);

	override handleEvent(IdeAction action, Context context) {
		if (action.commandId === UTLPLSQL_EDITOR_TEST_CMD_ID) {
			val Runnable runnable = [|runTest(context)]
			val thread = new Thread(runnable)
			thread.name = "utPLSQL run test"
			thread.start
			return true
		}
		return false
	}

	override update(IdeAction action, Context context) {
		if (action.commandId === UTLPLSQL_EDITOR_TEST_CMD_ID) {
			action.enabled = false
			val view = context.view
			if (view instanceof Editor) {
				val component = view.defaultFocusComponent
				if (component instanceof JEditorPane) {
					val parser = new UtplsqlParser(component.text)
					if (!parser.getPathAt(component.caretPosition).empty) {
						action.enabled = true
					}
				}
			}
			return true
		}
		return false
	}

	def runTest(Context context) {
		logger.fine("Start utPLSQL test from editor.")
		logger.fine('''context.view is of type «context.view.class.name».''')
		logger.fine('''context.node is of type «context.node.class.name».''')
		val view = context.view
		if (view instanceof Editor) {
			val component = view.defaultFocusComponent
			if (component instanceof JEditorPane) {
				val parser = new UtplsqlParser(component.text)
				val position = component.caretPosition
				var String connectionName = null;				
				val Node node = context.node
				if (node instanceof DatabaseSourceNode) {
					connectionName = node.connectionName
				} else if (view instanceof Worksheet) {
					connectionName = view.connectionName
				}
				val preferences = PreferenceModel.getInstance(Preferences.getPreferences());
				if (connectionName !== null && preferences.unsharedWorksheet) {
					connectionName = Connections.instance.createPrivateConnection(connectionName)
				}
				val code = '''
					«IF preferences.resetPackage»
						EXECUTE dbms_session.reset_package;
					«ENDIF»
					SET SERVEROUTPUT ON SIZE 1000000
					CLEAR SCREEN
					EXECUTE ut.run('«parser.getPathAt(position)»');
				'''
				val worksheet = OpenWorksheetWizard.openNewTempWorksheet(connectionName, code) as Worksheet
				if (connectionName === null) {
					worksheet.comboConnection = null
				}
				WorksheetUtil.setWorksheetTabName(worksheet.context.node.URL, UtplsqlResources.getString("WORKSHEET_TITLE"));
				if (preferences.autoExecute) {
					Thread.sleep(100) // give worksheet time to initialize
					val action = Ide.getIdeActionMap.get(Ide.findCmdID("Worksheet.RunScript")) as IdeAction
					action.performAction(worksheet.context)
					Thread.sleep(200) // give script runner time to initiate result panel
					val splitPane = worksheet.selectedResultPanel?.GUI?.parent?.parent?.parent as JSplitPane
					if (splitPane === null) {
						logger.severe("Could not adjust size of worksheet.")
					} else {
						splitPane.dividerLocation = 0.15 // 15% for Worksheet, 85% for Script Output 
					}
				}
				logger.fine('''utPLSQL test called for code at cursor position «position».»''')
			}
		}
	}
}
