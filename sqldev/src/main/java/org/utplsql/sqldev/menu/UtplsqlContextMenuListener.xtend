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

import java.util.logging.Logger
import oracle.dbtools.raptor.navigator.db.DatabaseConnection
import oracle.dbtools.raptor.navigator.impl.ChildObjectElement
import oracle.dbtools.raptor.navigator.impl.ObjectFolder
import oracle.dbtools.raptor.navigator.plsql.PlSqlNode
import oracle.ide.Context
import oracle.ide.controller.ContextMenu
import oracle.ide.controller.ContextMenuListener
import org.utplsql.sqldev.model.URLTools

class UtplsqlContextMenuListener implements ContextMenuListener {
	private static final Logger logger = Logger.getLogger(UtplsqlContextMenuListener.name);
	private val extension URLTools urlTools = new URLTools


	override handleDefaultAction(Context context) {
		return false
	}
	
	override menuWillHide(ContextMenu contextMenu) {
	}
	
	override menuWillShow(ContextMenu contextMenu) {
		val element = contextMenu.context.selection.get(0)
		var boolean showMenu = false
		logger.fine('''selected object is of type «element.class.name»''')
		if (element instanceof DatabaseConnection) {
			showMenu = true
		} else if (element instanceof ObjectFolder) {
			if (element.objectType == "PACKAGE" || element.objectType == "TYPE") {
				showMenu = true
			}
		} else if (element instanceof PlSqlNode) {
			if (element.objectType == "PACKAGE" || element.objectType == "PACKAGE BODY" ||
				element.objectType == "TYPE" || element.objectType == "TYPE BODY") {
				showMenu = true
			}
		} else if (element instanceof ChildObjectElement) {
			if (element.URL.objectType == "PACKAGE" || element.URL.objectType == "TYPE") {
				showMenu = true
			}
		}
		if (showMenu) {
			val menuItem = contextMenu.createMenuItem(UtplsqlController.UTLPLSQL_TEST_ACTION, 1.0f)
			contextMenu.add(menuItem, 12.1f)
			logger.finer("context menu created.")
		}
	}
}