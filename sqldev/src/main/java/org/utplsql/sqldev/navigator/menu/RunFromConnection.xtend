package org.utplsql.sqldev.navigator.menu

import java.util.logging.Logger
import oracle.dbtools.raptor.dialogs.actions.AbstractMenuAction
import org.utplsql.sqldev.UtplsqlWorksheet

class RunFromConnection extends AbstractMenuAction {
	private static final Logger logger = Logger.getLogger(RunFromConnection.name);

	override launch() {
		logger.finer('''Run utPLSQL from Connection node.''')
		val utPlsqlWorksheet = new UtplsqlWorksheet(DBObject.schemaName, DBObject.connectionName)
		utPlsqlWorksheet.runTestAsync
	}
}