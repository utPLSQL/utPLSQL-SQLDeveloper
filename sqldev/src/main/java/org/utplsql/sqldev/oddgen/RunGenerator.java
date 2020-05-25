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
package org.utplsql.sqldev.oddgen

import java.sql.Connection
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.List
import oracle.ide.config.Preferences
import org.oddgen.sqldev.generators.OddgenGenerator2
import org.oddgen.sqldev.generators.model.Node
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.resources.UtplsqlResources

class RunGenerator implements OddgenGenerator2 {

	public static val YES = "Yes"
	public static val NO = "No"
	
	public static var RESET_PACKAGE = UtplsqlResources.getString("PREF_RESET_PACKAGE_LABEL")
	public static var CLEAR_SCREEN = UtplsqlResources.getString("PREF_CLEAR_SCREEN_LABEL")
	public static var INDENT_SPACES = UtplsqlResources.getString("PREF_INDENT_SPACES_LABEL")
	
	// oddgen node cache
	var List<Node> runnables = null;

	override isSupported(Connection conn) {
		var ret = false
		if (conn !== null) {
			if (conn.metaData.databaseProductName.startsWith("Oracle")) {
				if (conn.metaData.databaseMajorVersion == 11) {
					if (conn.metaData.databaseMinorVersion >= 2) {
						ret = true
					}
				} else if (conn.metaData.databaseMajorVersion > 11) {
					ret = true
				}
			}
		}
		return ret
	}

	override getName(Connection conn) {
		return "Run test"
	}

	override getDescription(Connection conn) {
		return "Runs utPLSQL test packages in the current user."
	}

	override getFolders(Connection conn) {
		val preferences = PreferenceModel.getInstance(Preferences.preferences)
		val folders = new ArrayList<String>
		for (f : preferences.rootFolderInOddgenView.split(",").filter[!it.empty]) {
			folders.add(f.trim)
		}
		return folders
	}

	override getHelp(Connection conn) {
		return "<p>not yet available</p>"
	}
	
	override getNodes(Connection conn, String parentNodeId) {
		// oddgen asks for children for each parent node, regardless of load strategy (eager/lazy)
		// oddgen does not know about the load strategy, hence caching is the responsibility of the generator
		if (runnables === null) {
			val preferences = PreferenceModel.getInstance(Preferences.preferences)
			val params = new LinkedHashMap<String, String>()
			params.put(RESET_PACKAGE, if (preferences.resetPackage) {YES} else {NO})
			params.put(CLEAR_SCREEN, if (preferences.clearScreen) {YES} else {NO})
			params.put(INDENT_SPACES, String.valueOf(preferences.indentSpaces))
			val UtplsqlDao dao = new UtplsqlDao(conn)
			// load node tree eagerly (all nodes in one go)
			runnables = dao.runnables
			for (node : runnables) {
				node.params = params
			}
		}
		return runnables
	}

	override getLov(Connection conn, LinkedHashMap<String, String> params, List<Node> nodes) {
		val lov = new HashMap<String, List<String>>()
		lov.put(RESET_PACKAGE, #[YES, NO])
		lov.put(CLEAR_SCREEN, #[YES, NO])
		lov.put(INDENT_SPACES, #["1", "2", "3", "4", "5", "6", "7", "8"])
		return lov
	}

	override getParamStates(Connection conn, LinkedHashMap<String, String> params, List<Node> nodes) {
		return new HashMap<String, Boolean>
	}
	
	private def getPath(Node node, Connection conn) {
		if (node.id == "SUITE" || node.id == "SUITEPATH") {
			return conn.metaData.userName
		} else {
			return node.id
		}
	}

	private def replaceTabsWithSpaces(CharSequence input, int indentSpaces) {
		val spaces = String.format("%1$"+indentSpaces+"s", "")
		return input.toString.replace("\t", spaces)
	}
	
	def dedup(List<Node> nodes) {
		val set = new HashSet<String>
		for (node : nodes) {
			set.add(node.id)
		}
		val ret = new ArrayList<Node>
		for (node : nodes) {
			if (!set.contains(node.parentId)) {
				ret.add(node)
			}
		}
		return ret
	}
	
	override generateProlog(Connection conn, List<Node> nodes) {
		val dedupNodes = nodes.dedup
		val params = dedupNodes.get(0).params
		val ret = '''
			«IF params.get(RESET_PACKAGE) == YES»
				EXECUTE dbms_session.reset_package;
			«ENDIF»
			SET SERVEROUTPUT ON SIZE UNLIMITED
			«IF params.get(CLEAR_SCREEN) == YES»
				CLEAR SCREEN
			«ENDIF»
			«IF dedupNodes.size == 1»
				EXECUTE ut.run('«dedupNodes.get(0).getPath(conn)»');
			«ELSE»
				BEGIN
					ut.run(
						ut_varchar2_list(
							«FOR node : dedupNodes SEPARATOR ","»
								'«node.getPath(conn)»'
							«ENDFOR»
						)
					);
				END;
				/
			«ENDIF»
		'''
		return ret.replaceTabsWithSpaces(Integer.valueOf(params.get(INDENT_SPACES)))
	}

	override generateSeparator(Connection conn) {
		return ""
	}
	
	override generateEpilog(Connection conn, List<Node> nodes) {
		return ""
	}
	
	override generate(Connection conn, Node node) {
		return ""
	}
	
}
