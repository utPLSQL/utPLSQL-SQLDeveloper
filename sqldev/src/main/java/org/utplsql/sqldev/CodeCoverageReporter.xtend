package org.utplsql.sqldev

import java.awt.Desktop
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.util.List
import java.util.logging.Logger
import oracle.dbtools.raptor.utils.Connections
import org.utplsql.sqldev.dal.UtplsqlDao

class CodeCoverageReporter {
	static val Logger logger = Logger.getLogger(CodeCoverageReporter.name);

	var Connection conn
	var List<String> pathList

	new(List<String> pathList, String connectionName) {
		this.pathList = pathList
		setConnection(connectionName)
	}

	private def setConnection(String connectionName) {
		if (connectionName === null) {
			throw new RuntimeException("Cannot initialize a CodeCoverageReporter without a ConnectionName")
		} else {
			// must be closed manually
			this.conn = Connections.instance.cloneConnection(Connections.instance.getConnection(connectionName))
		}
	}

	private def run() {
		try {
			logger.fine('''Running code coverage reporter for «pathList»...''')
			val dal = new UtplsqlDao(conn)
			val content = dal.htmlCodeCoverage(pathList)
			val file = File.createTempFile("utplsql_", "html")
			logger.fine('''Writing result to «file.absolutePath»...''')
			Files.write(Paths.get(file.absolutePath), content.split(System.lineSeparator), StandardCharsets.UTF_8);
			val url = file.toURI().toURL().toExternalForm()
			logger.fine('''Opening «url» in browser...''')
			val Desktop desktop = if (Desktop.isDesktopSupported()) {Desktop.getDesktop()} else {null}
			if (desktop !== null && desktop.isSupported(Desktop.Action.BROWSE) && url !== null) {
				desktop.browse((new URL(url)).toURI)
				logger.fine(url + " opened in browser.");
			} else {
				logger.severe('''Could not launch «file» in browser. No default browser defined on this system.''')
			}
		} catch (Exception e) {
			logger.severe('''Error when running code coverage: «e?.message»''')
		}
		finally {
			conn.close
		}
	}

	def runAsync() {
		val Runnable runnable = [|run]
		val thread = new Thread(runnable)
		thread.name = "code coverage reporter"
		thread.start
	}
	
}