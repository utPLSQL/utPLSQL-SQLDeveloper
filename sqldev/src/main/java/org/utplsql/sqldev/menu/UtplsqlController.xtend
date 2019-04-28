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
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.swing.JEditorPane
import oracle.dbtools.raptor.navigator.db.DBNavigatorWindow
import oracle.dbtools.raptor.navigator.db.DatabaseConnection
import oracle.dbtools.raptor.navigator.impl.ChildObjectElement
import oracle.dbtools.raptor.navigator.impl.DatabaseSourceNode
import oracle.dbtools.raptor.navigator.impl.ObjectFolder
import oracle.dbtools.raptor.navigator.impl.SchemaFolder
import oracle.dbtools.raptor.navigator.plsql.PlSqlNode
import oracle.dbtools.raptor.utils.Connections
import oracle.dbtools.worksheet.editor.Worksheet
import oracle.ide.Context
import oracle.ide.Ide
import oracle.ide.config.Preferences
import oracle.ide.controller.Controller
import oracle.ide.controller.IdeAction
import oracle.ide.editor.Editor
import org.utplsql.sqldev.coverage.CodeCoverageReporter
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.model.URLTools
import org.utplsql.sqldev.model.oddgen.GenContext
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.oddgen.TestTemplate
import org.utplsql.sqldev.parser.UtplsqlParser
import org.utplsql.sqldev.runner.UtplsqlWorksheetRunner

class UtplsqlController implements Controller {
	static final Logger logger = Logger.getLogger(UtplsqlController.name);
	val extension URLTools urlTools = new URLTools

	public static int UTPLSQL_TEST_CMD_ID = Ide.findCmdID("utplsql.test")
	public static int UTPLSQL_COVERAGE_CMD_ID = Ide.findCmdID("utplsql.coverage")
	public static int UTPLSQL_GENERATE_CMD_ID = Ide.findCmdID("utplsql.generate")
	public static final IdeAction UTPLSQL_TEST_ACTION = IdeAction.get(UtplsqlController.UTPLSQL_TEST_CMD_ID)
	public static final IdeAction UTPLSQL_COVERAGE_ACTION = IdeAction.get(UtplsqlController.UTPLSQL_COVERAGE_CMD_ID)
	public static final IdeAction UTPLSQL_GENERATE_ACTION = IdeAction.get(UtplsqlController.UTPLSQL_GENERATE_CMD_ID)

	override handleEvent(IdeAction action, Context context) {
		if (action.commandId === UTPLSQL_TEST_CMD_ID) {
			runTest(context)
			return true
		} else if (action.commandId === UTPLSQL_COVERAGE_CMD_ID) {
			codeCoverage(context)
			return true
		} else if (action.commandId === UTPLSQL_GENERATE_CMD_ID) {
			generateTest(context)
			return true
		}
		return false
	}

	override update(IdeAction action, Context context) {
		if (action.commandId === UTPLSQL_TEST_CMD_ID || action.commandId === UTPLSQL_COVERAGE_CMD_ID) {
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
				action.enabled = true
				// disable action if a node in the selection is not runnable
				for (i : 0 ..< context.selection.length) {
					logger.fine('''section «i» is «context.selection.get(i).toString» of class «context.selection.get(i).class.name»''')
					if (action.enabled) {
						val element = context.selection.get(i)
						if (Connections.instance.isConnectionOpen(context.URL.connectionName)) {
							val dao = new UtplsqlDao(Connections.instance.getConnection(context.URL.connectionName))
							if (preferences.checkRunUtplsqlTest && dao.utAnnotationManagerInstalled) {
								if (element instanceof DatabaseConnection) {
									action.enabled = dao.containsUtplsqlTest(element.connection.schema)
								} else if (element instanceof SchemaFolder) {
									action.enabled = dao.containsUtplsqlTest(element.schemaName)
								} else if (element instanceof ObjectFolder) {
									action.enabled = dao.containsUtplsqlTest(element.URL.schema)
								} else if (element instanceof PlSqlNode) {
									action.enabled = dao.containsUtplsqlTest(element.owner, element.objectName)
								} else if (element instanceof ChildObjectElement) {
									action.enabled = dao.containsUtplsqlTest(element.URL.schema, element.URL.memberObject, element.shortLabel)
								}
							}
						} else {
							action.enabled = false
						}
					}
				}
			}
			return true
		} else if (action.commandId === UTPLSQL_GENERATE_CMD_ID) {
			action.enabled = false
			// enable if generation is possible
			val view = context.view
			if (view instanceof Editor) {
				val component = view.defaultFocusComponent
				if (component instanceof JEditorPane) {
					val preferences = PreferenceModel.getInstance(Preferences.preferences)
					if (preferences.checkGenerateUtplsqlTest) {
						val parser = new UtplsqlParser(component.text)
						action.enabled = parser.getObjectAt(component.caretPosition) !== null
					} else {
						action.enabled = true
					}
				}
			} else if  (view instanceof DBNavigatorWindow) {
				// multiselection is not supported, use oddgen to generte tests for multiple objects
				if (context.selection.length == 1) {
					val element = context.selection.get(0)
					if (element instanceof PlSqlNode) {
						val ot = element.objectType 
						if (ot.startsWith("PACKAGE") || ot.startsWith("TYPE")  || ot == "FUNCTION" || ot == "PROCEDURE") {
							action.enabled = true
						}
					}
				}
			}
		}
		return false
	}

	private def getPath(Object element) {
		var String path
		if (element instanceof DatabaseConnection) {
			path = element.connection.schema
		} else if (element instanceof SchemaFolder) {
			path = element.schemaName
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
	
	private def getPathList(Context context) {
		val pathList = new ArrayList<String>()
		for (i : 0 ..< context.selection.length) {
			val element = context.selection.get(i)
			pathList.add(element.path)
		}
		return pathList
	}
	
	private def getPathList(String path) {
		val pathList = new ArrayList<String>
		pathList.add(path)
		return pathList
	}
	
	private def dedupPathList(List<String> pathList) {
		val set = new HashSet<String>
		for (path : pathList) {
			set.add(path)
		}
		val ret = new ArrayList<String>
		val p = Pattern.compile("((((\\w+)\\.)?\\w+)\\.)?\\w+")
		for (path : set) {
			val m = p.matcher(path)
			if (m.matches()) {
				val parent1 = m.group(4) // user
				val parent2 = m.group(2) // user.package
				if (parent1 === null || !set.contains(parent1)) {
					if (parent2 === null || !set.contains(parent2)) {
						ret.add(path)
					}
				}
			} else {
				logger.severe('''path: «path» did not match «p.toString», this is unexected!''')
			}
		}
		return ret
	}		

	private def getURL(Context context) {
		var URL url
		val element = context.selection.get(0)
		if (element instanceof DatabaseConnection) {
			url = element.URL
		} else if (element instanceof SchemaFolder) {
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
	
	private def void populateGenContext(GenContext genContext, PreferenceModel preferences) {
		genContext.testPackagePrefix = preferences.testPackagePrefix.toLowerCase
		genContext.testPackageSuffix = preferences.testPackageSuffix.toLowerCase
		genContext.testUnitPrefix = preferences.testUnitPrefix.toLowerCase
		genContext.testUnitSuffix = preferences.testUnitSuffix.toLowerCase
		genContext.numberOfTestsPerUnit = preferences.numberOfTestsPerUnit
		genContext.generateComments = preferences.generateComments
		genContext.disableTests = preferences.disableTests
		genContext.suitePath = preferences.suitePath.toLowerCase
		genContext.indentSpaces = preferences.indentSpaces
	}
	
	private def getGenContext(Context context) {
		val connectionName = context.URL.connectionName
		val genContext = new GenContext
		if (Connections.instance.isConnectionOpen(connectionName)) {
			genContext.conn = Connections.instance.getConnection(connectionName)
			val element = context.selection.get(0)
			if (element instanceof PlSqlNode) {
				genContext.objectType = element.objectType.replace(" BODY", "")
				genContext.objectName = element.objectName
				val preferences = PreferenceModel.getInstance(Preferences.preferences)
				populateGenContext(genContext, preferences)
			}
		}
		return genContext
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
				// issue 59 - always use a connection to ensure the utPL/SQL annotation API is used
				val parser = new UtplsqlParser(component.text, Connections.instance.getConnection(connectionName), owner)
				val position = component.caretPosition
				val path = parser.getPathAt(position)
				val utPlsqlWorksheet = new UtplsqlWorksheetRunner(path.pathList, connectionName)
				utPlsqlWorksheet.runTestAsync
			}
		} else if (view instanceof DBNavigatorWindow) {
			val url=context.URL
			if (url !== null) {
				val connectionName = url.connectionName
				logger.fine('''connectionName: «connectionName»''')
				val pathList=context.pathList.dedupPathList
				val utPlsqlWorksheet = new UtplsqlWorksheetRunner(pathList, connectionName)
				utPlsqlWorksheet.runTestAsync
			}
		}
	}
	
	def List<String> dependencies(String name, String connectionName) {
		var List<String> ret = null
		if (connectionName !== null) {
			val dao = new UtplsqlDao(Connections.instance.getConnection(connectionName))
			ret = dao.includes(name)
		}
		return ret
	}
	
	def List<String> dependencies(Context context, String connectionName) {
		val HashSet<String> ret = new HashSet<String>
		for (i : 0 ..< context.selection.length) {
			val element = context.selection.get(i)
			if (element instanceof PlSqlNode) {
				val dep = dependencies(element.objectName, connectionName)
				for (d : dep) {
					ret.add(d)
				}
			} else if (element instanceof ChildObjectElement) {
				val dep = dependencies(element.URL.memberObject, connectionName)
				for (d : dep) {
					ret.add(d)
				}
			}
		}
		return ret.toList.sortBy[it]
	}
	
	def codeCoverage(Context context) {
		val view = context.view
		val node = context.node
		logger.finer('''Code coverage from view «view?.class?.name» and node «node?.class?.name».''')
		if (view instanceof Editor) {
			val component = view.defaultFocusComponent
			if (component instanceof JEditorPane) {
				var String connectionName = null;
				var String owner = null;
				if (node instanceof DatabaseSourceNode) {
					connectionName = node.connectionName
				} else if (view instanceof Worksheet) {
					connectionName = view.connectionName
				}
				logger.fine('''connectionName: «connectionName»''')
				val preferences = PreferenceModel.getInstance(Preferences.preferences)
				val parser = new UtplsqlParser(component.text, if (preferences.checkRunUtplsqlTest) {Connections.instance.getConnection(connectionName)} else {null}, owner)
				val position = component.caretPosition
				val path = parser.getPathAt(position)
				val object = parser.getObjectAt(position)
				val includeObjectList = dependencies(object.name, connectionName)
				val reporter = new CodeCoverageReporter(path.pathList, includeObjectList, connectionName)
				reporter.showParameterWindow
			}
		} else if (view instanceof DBNavigatorWindow) {
			val url=context.URL
			if (url !== null) {
				val connectionName = url.connectionName
				logger.fine('''connectionName: «connectionName»''')
				val pathList=context.pathList.dedupPathList
				val includeObjectList = dependencies(context, connectionName)
				val reporter = new CodeCoverageReporter(pathList, includeObjectList, connectionName)
				reporter.showParameterWindow
			}
		}
	}

	def generateTest(Context context) {
		val view = context.view
		val node = context.node
		logger.finer('''Generate utPLSQL test from view «view?.class?.name» and node «node?.class?.name».''')
		if (view instanceof Editor) {
			val component = view.defaultFocusComponent
			if (component instanceof JEditorPane) {
				var String connectionName = null;
				if (node instanceof DatabaseSourceNode) {
					connectionName = node.connectionName
				} else if (view instanceof Worksheet) {
					connectionName = view.connectionName
				}
				if (connectionName !== null) {
					if (Connections.instance.isConnectionOpen(connectionName)) {
						val genContext = new GenContext
						genContext.conn = Connections.instance.getConnection(connectionName)
						val parser = new UtplsqlParser(component.text)
						val position = component.caretPosition
						val obj = parser.getObjectAt(position)
						if (obj !== null) {
							genContext.objectType = obj.type.toUpperCase
							genContext.objectName = obj.name.toUpperCase
							val preferences = PreferenceModel.getInstance(Preferences.preferences)
							populateGenContext(genContext, preferences)
							val testTemplate = new TestTemplate(genContext)
							val code = testTemplate.generate.toString
							UtplsqlWorksheetRunner.openWithCode(code, connectionName)
						}
					}
				}
			}

		} else if (view instanceof DBNavigatorWindow) {
			val url=context.URL
			if (url !== null) {
				val connectionName = url.connectionName
				val testTemplate = new TestTemplate(context.genContext)
				val code = testTemplate.generate.toString
				UtplsqlWorksheetRunner.openWithCode(code, connectionName)
			}
		}
	}
}
