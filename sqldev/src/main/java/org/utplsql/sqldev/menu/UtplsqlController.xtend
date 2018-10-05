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
package org.utplsql.sqldev.menu

import java.net.URL
import java.util.logging.Logger
import javax.swing.JEditorPane
import oracle.dbtools.raptor.navigator.db.DBNavigatorWindow
import oracle.dbtools.raptor.navigator.db.DatabaseConnection
import oracle.dbtools.raptor.navigator.impl.ChildObjectElement
import oracle.dbtools.raptor.navigator.impl.DatabaseSourceNode
import oracle.dbtools.raptor.navigator.impl.ObjectFolder
import oracle.dbtools.raptor.navigator.plsql.PlSqlNode
import oracle.dbtools.raptor.utils.Connections
import oracle.dbtools.worksheet.editor.Worksheet
import oracle.ide.Context
import oracle.ide.Ide
import oracle.ide.config.Preferences
import oracle.ide.controller.Controller
import oracle.ide.controller.IdeAction
import oracle.ide.editor.Editor
import org.utplsql.sqldev.UtplsqlWorksheet
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.model.URLTools
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.parser.UtplsqlParser

class UtplsqlController implements Controller {
	private static final Logger logger = Logger.getLogger(UtplsqlController.name);
	private val extension URLTools urlTools = new URLTools

	public static int UTLPLSQL_TEST_CMD_ID = Ide.findCmdID("utplsql.test")
	public static final IdeAction UTLPLSQL_TEST_ACTION = IdeAction.get(UtplsqlController.UTLPLSQL_TEST_CMD_ID)

	override handleEvent(IdeAction action, Context context) {
		if (action.commandId === UtplsqlController.UTLPLSQL_TEST_CMD_ID) {
			runTest(context)
			return true
		}
		return false
	}

	override update(IdeAction action, Context context) {
		if (action.commandId === UTLPLSQL_TEST_CMD_ID) {
			val preferences = PreferenceModel.getInstance(Preferences.preferences)
			action.enabled = false
			val view = context.view
			if (view instanceof Editor) {
				val component = view.defaultFocusComponent
				if (component instanceof JEditorPane) {
					if (preferences.checkRunUtplsqlTest) {
						val node = context.node
						var String connectionName = null;
						var String owner = null;
						if (node instanceof DatabaseSourceNode) {
							connectionName = node.connectionName
							owner = node.owner
						} else if (view instanceof Worksheet) {
							connectionName = view.connectionName
						}
						logger.fine('''connectionName: «connectionName»''')
						val parser = new UtplsqlParser(component.text, Connections.instance.getConnection(connectionName), owner)
						if (!parser.getPathAt(component.caretPosition).empty) {
							action.enabled = true
						}
					} else {
						action.enabled = true
					}
				}
			} else if (view instanceof DBNavigatorWindow) {
				if (context.selection.length == 1) {
					val element = context.selection.get(0)
					if (Connections.instance.isConnectionOpen(context.URL.connectionName)) {
						val dao = new UtplsqlDao(Connections.instance.getConnection(context.URL.connectionName))
						if (preferences.checkRunUtplsqlTest && dao.utAnnotationManagerInstalled) {
							if (element instanceof DatabaseConnection) {
								action.enabled = dao.containsUtplsqlTest(element.connection.schema)
							} else if (element instanceof ObjectFolder) {
								action.enabled = dao.containsUtplsqlTest(element.URL.schema)
							} else if (element instanceof PlSqlNode) {
								action.enabled = dao.containsUtplsqlTest(element.owner, element.objectName)
							} else if (element instanceof ChildObjectElement) {
								action.enabled = dao.containsUtplsqlTest(element.URL.schema, element.URL.memberObject, element.shortLabel)
							}
						} else {
							action.enabled = true
						}
					}
				}
			}
			return true
		}
		return false
	}
	
	private def getPath(Context context) {
		var String path
		val element = context.selection.get(0)
		if (element instanceof DatabaseConnection) {
			path = element.connection.schema
		} else if (element instanceof ObjectFolder) {
			path = element.URL.schema
		} else if (element instanceof PlSqlNode) {
			path = '''«element.owner».«element.objectName»'''
		} else if (element instanceof ChildObjectElement) {
			path = '''«element.URL.schema».«element.URL.memberObject».«element.shortLabel»'''
		} else {
			path = ""
		}
		logger.fine('''path: «path»''')
		return path
	}

	private def getURL(Context context) {
		var URL url
		val element = context.selection.get(0)
		if (element instanceof DatabaseConnection) {
			url = element.URL
		} else if (element instanceof ObjectFolder) {
			url = element.URL
		} else if (element instanceof PlSqlNode) {
			url = element.URL
		} else if (element instanceof ChildObjectElement) {
			url = element.URL
		}
		logger.fine('''url: «url»''')
		return url
	}

	def runTest(Context context) {
		val view = context.view
		val node = context.node
		logger.finer('''Run utPLSQL from view «view?.class?.name» and node «node?.class?.name».''')
		if (view instanceof Editor) {
			val component = view.defaultFocusComponent
			if (component instanceof JEditorPane) {
				var String connectionName = null;
				var String owner = null;
				if (node instanceof DatabaseSourceNode) {
					connectionName = node.connectionName
					owner = node.owner
				} else if (view instanceof Worksheet) {
					connectionName = view.connectionName
				}
				logger.fine('''connectionName: «connectionName»''')
				val preferences = PreferenceModel.getInstance(Preferences.preferences)
				val parser = new UtplsqlParser(component.text, if (preferences.checkRunUtplsqlTest) {Connections.instance.getConnection(connectionName)} else {null}, owner)
				val position = component.caretPosition
				val path = parser.getPathAt(position)
				val utPlsqlWorksheet = new UtplsqlWorksheet(path, connectionName)
				utPlsqlWorksheet.runTestAsync
			}
		} else if (view instanceof DBNavigatorWindow) {
			val url=context.URL
			if (url !== null) {
				val connectionName = url.connectionName
				logger.fine('''connectionName: «connectionName»''')
				val path=context.path
				val utPlsqlWorksheet = new UtplsqlWorksheet(path, connectionName)
				utPlsqlWorksheet.runTestAsync
			}
		}
	}
}
