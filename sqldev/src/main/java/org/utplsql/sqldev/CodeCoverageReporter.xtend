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

import java.awt.Desktop
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.util.ArrayList
import java.util.List
import java.util.logging.Logger
import oracle.dbtools.raptor.utils.Connections
import org.utplsql.sqldev.dal.UtplsqlDao

class CodeCoverageReporter {
	static val Logger logger = Logger.getLogger(CodeCoverageReporter.name);

	var Connection conn
	var List<String> pathList
	var List<String> includeObjectList
	var CodeCoverageReporterWindow frame
	var String schemas
	var String includeObjects
	var String excludeObjects

	new(List<String> pathList, List<String> includeObjectList, String connectionName) {
		this.pathList = pathList
		this.includeObjectList = includeObjectList
		setConnection(connectionName)
	}

	new(List<String> pathList, List<String> includeObjectList, Connection conn) {
		this.pathList = pathList
		this.includeObjectList = includeObjectList
		this.conn = conn
	}

	private def setConnection(String connectionName) {
		if (connectionName === null) {
			throw new RuntimeException("Cannot initialize a CodeCoverageReporter without a ConnectionName")
		} else {
			// must be closed manually
			this.conn = Connections.instance.cloneConnection(Connections.instance.getConnection(connectionName))
		}
	}
	
	private def toStringList(String s) {
		val list = new ArrayList<String>
		if (s !== null && !s.empty) {
			for (item : s.split(",")) {
				if (!item.empty) {
					list.add(item.trim)
				}
			}
		}
		return list
	}

	private def void run() {
		try {
			logger.fine('''Running code coverage reporter for «pathList»...''')
			val dal = new UtplsqlDao(conn)
			val content = dal.htmlCodeCoverage(pathList, toStringList(schemas), toStringList(includeObjects), toStringList(excludeObjects))
			val file = File.createTempFile("utplsql_", ".html")
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
			if (frame !== null) {
				frame.exit
			}
		}
	}
	
	def setFrame(CodeCoverageReporterWindow frame) {
		this.frame = frame;
	}
	
	def getFrame() {
		return this.frame
	}
	
	def getConnection() {
		return conn
	}
	
	def getPathList() {
		return pathList
	}
	
	def getIncludeObjectList() {
		if (includeObjectList === null) {
			return new ArrayList<String>
		} else {
			return includeObjectList
		}
	}
	
	def setSchemas(String schemas) {
		this.schemas = schemas
	}
	
	def setIncludeObjects(String includeObjects) {
		this.includeObjects = includeObjects
	}
	
	def setExcludeObjects(String excludeObjects) {
		this.excludeObjects = excludeObjects
	}	

	def runAsync() {
		val Runnable runnable = [|run]
		val thread = new Thread(runnable)
		thread.name = "code coverage reporter"
		thread.start
	}
	
	def showParameterWindow() {
		CodeCoverageReporterWindow.createAndShow(this)
	}
	
}