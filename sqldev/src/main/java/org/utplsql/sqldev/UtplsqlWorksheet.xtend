package org.utplsql.sqldev

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
	private static val Logger logger = Logger.getLogger(UtplsqlWorksheet.name);

	private var PreferenceModel preferences
	private var String connectionName
	private var String path

	new(String path, String connectionName) {
		this.preferences = PreferenceModel.getInstance(Preferences.preferences);
		this.path = path
		setConnection(connectionName)
	}

	private def setConnection(String connectionName) {
		if (connectionName !== null && preferences.unsharedWorksheet) {
			this.connectionName = Connections.instance.createPrivateConnection(connectionName)
		} else {
			this.connectionName = connectionName;
		}
	}

	private def getCode() '''
		«IF preferences.resetPackage»
			EXECUTE dbms_session.reset_package;
		«ENDIF»
		SET SERVEROUTPUT ON SIZE 1000000
		«IF preferences.clearScreen»
			CLEAR SCREEN
		«ENDIF»
		EXECUTE ut.run('«path»');
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

	def runTest() {
		val worksheet = openWorksheet
		worksheet.runScript
		logger.fine('''utPLSQL test called for «path» in «connectionName».''')
	}

	def runTestAsync() {
		val Runnable runnable = [|runTest]
		val thread = new Thread(runnable)
		thread.name = "utPLSQL run test"
		thread.start
	}


}
