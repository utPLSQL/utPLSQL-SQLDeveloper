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
package org.utplsql.sqldev.navigator.menu

import java.net.URL
import java.util.logging.Logger
import java.util.regex.Pattern
import oracle.dbtools.raptor.dialogs.actions.AbstractMenuAction
import oracle.dbtools.raptor.navigator.impl.ChildObjectElement
import org.utplsql.sqldev.UtplsqlWorksheet

class UtplsqlNavigatorAction extends AbstractMenuAction {
	private static final Logger logger = Logger.getLogger(UtplsqlNavigatorAction.name);
	
	private def getSchema(URL url) {
		val p = Pattern.compile("(//)([^/]+)")
		val m = p.matcher(url.toString)
		if (m.find) {
			return m.group(2)
		} else {
			return ""
		}
	}
	
	private def getMemberObject(URL url) {
		val p = Pattern.compile("(/)([^/]+)(#MEMBER)")
		val m = p.matcher(url.toString)
		if (m.find) {
			return m.group(2)
		} else {
			return ""
		}		
	}

	private def getPath() {
		var String path
		if (DBObject.objectType == "MEMBER") {
			val element = DBObject.element as ChildObjectElement
			path = '''«element.URL.schema».«element.URL.memberObject».«DBObject.childName»'''
		} else if (DBObject.objectType == "CONNECTION" || DBObject.objectName === null) {
			path = DBObject.schemaName
		} else {
			path = '''«DBObject.schemaName».«DBObject.objectName»'''
		}
		return path
	}

	override launch() {
		logger.finer('''Run utPLSQL from a navigator node.''')
		val utPlsqlWorksheet = new UtplsqlWorksheet(path, DBObject.connectionName)
		utPlsqlWorksheet.runTestAsync
	}
}