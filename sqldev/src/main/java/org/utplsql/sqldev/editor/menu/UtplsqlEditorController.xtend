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

import java.util.logging.Logger
import javax.swing.JEditorPane
import oracle.ide.Context
import oracle.ide.Ide
import oracle.ide.controller.Controller
import oracle.ide.controller.IdeAction
import oracle.ide.editor.Editor
import oracle.ide.^extension.RegisteredByExtension
import org.utplsql.sqldev.parser.UtplsqlParser

@RegisteredByExtension("org.utplsql.sqldev")
class UtplsqlEditorController implements Controller {
	public static int UTLPLSQL_EDITOR_TEST_CMD_ID = Ide.findCmdID("utplsql.editor.test")
	private static final Logger logger = Logger.getLogger(UtplsqlEditorController.name);
	
	override handleEvent(IdeAction action, Context context) {
		if (action.commandId === UTLPLSQL_EDITOR_TEST_CMD_ID) {
			runTest(context)
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
		val view = context.view
		if (view instanceof Editor) {
			val component = view.defaultFocusComponent
			if (component instanceof JEditorPane) {
				val parser = new UtplsqlParser(component.text)
				val position = component.caretPosition
				// TODO: open new worksheet and call utPLSQL
				logger.fine('''Cursor is at «position». Calling «parser.getPathAt(position)»''')
			}
		}
		logger.fine ("utPLSQL test started successfully.")
	}
	
}
