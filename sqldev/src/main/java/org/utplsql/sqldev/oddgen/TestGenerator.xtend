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

import java.io.File
import java.sql.Connection
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.List
import oracle.ide.config.Preferences
import org.oddgen.sqldev.generators.OddgenGenerator2
import org.oddgen.sqldev.generators.model.Node
import org.oddgen.sqldev.generators.model.NodeTools
import org.oddgen.sqldev.plugin.templates.TemplateTools
import org.utplsql.sqldev.dal.UtplsqlDao
import org.utplsql.sqldev.model.preference.PreferenceModel
import org.utplsql.sqldev.resources.UtplsqlResources
import org.utplsql.sqldev.oddgen.model.GenContext

class TestGenerator implements OddgenGenerator2 {

	public static val YES = "Yes"
	public static val NO = "No"
	
	public static var GENERATE_FILES = "Generate files?"
	public static var OUTPUT_DIRECTORY = "Output directory"
	public static var TEST_PACKAGE_PREFIX = UtplsqlResources.getString("PREF_TEST_PACKAGE_PREFIX_LABEL")
	public static var TEST_PACKAGE_SUFFIX = UtplsqlResources.getString("PREF_TEST_PACKAGE_SUFFIX_LABEL")
	public static var TEST_UNIT_PREFIX = UtplsqlResources.getString("PREF_TEST_UNIT_PREFIX_LABEL")
	public static var TEST_UNIT_SUFFIX = UtplsqlResources.getString("PREF_TEST_UNIT_SUFFIX_LABEL")
	public static var NUMBER_OF_TESTS_PER_UNIT = UtplsqlResources.getString("PREF_NUMBER_OF_TESTS_PER_UNIT_LABEL")
	public static var GENERATE_COMMENTS = "Generate comments?"
	public static var DISABLE_TESTS = "Disable tests?"
	public static var SUITE_PATH = "Suite Path"
	public static var INDENT_SPACES = "Indent Spaces"
	
	val extension NodeTools nodeTools = new NodeTools
	val extension TemplateTools templateTools = new TemplateTools
	val consoleOutput = new ArrayList<String>();

	private def toContext(Node node) {
		val context = new GenContext()
		context.objectType = node.toObjectType
		context.objectName = node.toObjectName
        context.generateFiles = node.params.get(GENERATE_FILES) == YES
        context.outputDirectory = node.params.get(OUTPUT_DIRECTORY)
		context.testPackagePrefix = node.params.get(TEST_PACKAGE_PREFIX).toLowerCase
		context.testPackageSuffix = node.params.get(TEST_PACKAGE_SUFFIX).toLowerCase
		context.testUnitPrefix = node.params.get(TEST_UNIT_PREFIX).toLowerCase
		context.testUnitSuffix = node.params.get(TEST_UNIT_SUFFIX).toLowerCase
        context.numberOfTestsPerUnit = Integer.valueOf(node.params.get(NUMBER_OF_TESTS_PER_UNIT))
        context.generateComments = node.params.get(GENERATE_COMMENTS) == YES
        context.disableTests = node.params.get(DISABLE_TESTS) == YES
        context.suitePath = node.params.get(SUITE_PATH).toLowerCase
        context.indentSpaces = Integer.valueOf(node.params.get(INDENT_SPACES))
		return context
	}

	private def void resetConsoleOutput() {
		consoleOutput.clear
	}

	private def void saveConsoleOutput(String s) {
		consoleOutput.add(s)
	}
	
	private def String deleteFile(File file) {
		var String ret
		try {
			if (file.delete) {
				ret = '''«file.absoluteFile» deleted.'''
			} else {
				ret = '''Cannot delete file «file.absoluteFile».'''
			}
		} catch (Exception e) {
			ret = '''Cannot delete file «file.absoluteFile». Got the following error message: «e.message».'''
		}
		return ret
	}
	
	private def deleteFiles(String directory) '''
		«val dir = new File(directory)»
		«FOR file: dir.listFiles»
			«IF !file.directory»
				«IF file.name.endsWith(".pks") || file.name.endsWith(".pkb")»
					«file.deleteFile»
				«ENDIF»
			«ENDIF»
		«ENDFOR»
    '''

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
		return "Generate test"
	}

	override getDescription(Connection conn) {
		return "Generates utPLSQL test packages for public units in packages, types, functions and procedures found in the current schema."
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
		val preferences = PreferenceModel.getInstance(Preferences.preferences)
		val params = new LinkedHashMap<String, String>()
		params.put(GENERATE_FILES, if (preferences.generateFiles) {YES} else {NO})
		params.put(OUTPUT_DIRECTORY, preferences.outputDirectory)
		params.put(TEST_PACKAGE_PREFIX, preferences.testPackagePrefix)
		params.put(TEST_PACKAGE_SUFFIX, preferences.testPackageSuffix)
		params.put(TEST_UNIT_PREFIX, preferences.testUnitPrefix)
		params.put(TEST_UNIT_SUFFIX, preferences.testUnitSuffix)
		params.put(NUMBER_OF_TESTS_PER_UNIT, String.valueOf(preferences.numberOfTestsPerUnit))
		params.put(GENERATE_COMMENTS, if(preferences.generateComments) {YES} else {NO})
		params.put(DISABLE_TESTS, if (preferences.disableTests) {YES} else {NO})
		params.put(SUITE_PATH, preferences.suitePath)
		params.put(INDENT_SPACES, String.valueOf(preferences.indentSpaces))
		if (parentNodeId === null || parentNodeId.empty) {
			val packageNode = new Node
			packageNode.id = "PACKAGE"
			packageNode.params = params
			packageNode.leaf = false
			packageNode.generatable = true
			packageNode.multiselectable = true
			val typeNode = new Node
			typeNode.id = "TYPE"
			typeNode.params = params
			typeNode.leaf = false
			typeNode.generatable = true
			typeNode.multiselectable = true
			val functionNode = new Node
			functionNode.id = "FUNCTION"
			functionNode.params = params
			functionNode.leaf = false
			functionNode.generatable = true
			functionNode.multiselectable = true
			val procedureNode = new Node
			procedureNode.id = "PROCEDURE"
			procedureNode.params = params
			procedureNode.leaf = false
			procedureNode.generatable = true
			procedureNode.multiselectable = true
			return #[packageNode, typeNode, functionNode, procedureNode]
		} else {
			val UtplsqlDao dao = new UtplsqlDao(conn)
			val nodes = dao.testables(parentNodeId)
			for (node : nodes) {
				node.params = params
			}
			return nodes
		}
	}

	override getLov(Connection conn, LinkedHashMap<String, String> params, List<Node> nodes) {
		val lov = new HashMap<String, List<String>>()
		lov.put(NUMBER_OF_TESTS_PER_UNIT, #["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"])
		lov.put(INDENT_SPACES, #["1", "2", "3", "4", "5", "6", "7", "8"])
		lov.put(GENERATE_COMMENTS, #[YES, NO])
		lov.put(DISABLE_TESTS, #[YES, NO])
		lov.put(GENERATE_FILES, #[YES, NO])
		return lov
	}

	override getParamStates(Connection conn, LinkedHashMap<String, String> params, List<Node> nodes) {
		val paramStates = new HashMap<String, Boolean>
		paramStates.put(OUTPUT_DIRECTORY, params.get(GENERATE_FILES) == YES)
		return paramStates
	}
	
	override generateProlog(Connection conn, List<Node> nodes) '''
		«val generateFiles = nodes.get(0).params.get(GENERATE_FILES) == YES»
		«val outputDirectory = nodes.get(0).params.get(OUTPUT_DIRECTORY)»
		«IF generateFiles»
			«resetConsoleOutput»
			«outputDirectory.mkdirs.saveConsoleOutput»
			«deleteFiles(outputDirectory).toString.saveConsoleOutput»
			--
			-- install generated utPLSQL test packages
			--
		«ENDIF»
		«FOR node : nodes»
			«val context = node.toContext»
			«context.conn = conn»
			«val testTemplate = new TestTemplate(context)»
			«IF generateFiles»
				«val packageName = '''«context.testPackagePrefix»«node.toObjectName»«context.testPackageSuffix»'''»
				«writeToFile('''«outputDirectory»«File.separator»«packageName».pks'''.toString,testTemplate.generateSpec).saveConsoleOutput»
				«writeToFile('''«outputDirectory»«File.separator»«packageName».pkb'''.toString,testTemplate.generateBody).saveConsoleOutput»
				@«outputDirectory»«File.separator»«packageName».pks
				@«outputDirectory»«File.separator»«packageName».pkb
			«ELSE»
				«testTemplate.generate»

			«ENDIF»
		«ENDFOR»
		«IF generateFiles && consoleOutput.size > 0»

			--
			-- console output produced during the generation of this script
			--
			/*
			
			«FOR line : consoleOutput»
				«line»
			«ENDFOR»
			
			*/
		«ENDIF»
	'''

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
