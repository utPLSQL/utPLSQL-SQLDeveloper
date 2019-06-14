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
package org.utplsql.sqldev.model.preference

import java.io.File
import oracle.javatools.data.HashStructure
import oracle.javatools.data.HashStructureAdapter
import oracle.javatools.data.PropertyStorage
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder

class PreferenceModel extends HashStructureAdapter {
	public static final String DEFAULT_OUTPUT_DIRECTORY = '''«System.getProperty("user.home")»«File.separator»utplsql«File.separator»generated'''
	static final String DATA_KEY = "utplsql"

	private new(HashStructure hash) {
		super(hash)
	}

	def static getInstance(PropertyStorage prefs) {
		return new PreferenceModel(findOrCreate(prefs, DATA_KEY))
	}

	static final String KEY_USE_REALTIME_REPORTER = "useRealtimeRorter"
	static final String KEY_UNSHARED_WORKSHEET = "unsharedWorksheet"
	static final String KEY_RESET_PACKAGE = "resetPackage"
	static final String KEY_CLEAR_SCREEN = "clearScreen"
	static final String KEY_AUTO_EXECUTE = "autoExecute"
	static final String KEY_CHECK_RUN_UTPLSQL_TEST = "checkRunUtplsqlTest"
	static final String KEY_SHOW_DISABLED_COUNTER = "showDisabledCounter"
	static final String KEY_SHOW_WARNINGS_COUNTER = "showWarningsCounter"
	static final String KEY_SHOW_INFO_COUNTER = "showInfoCounter"
	static final String KEY_SHOW_WARNING_INDICATOR = "showWarningIndicator"
	static final String KEY_SHOW_INFO_INDICATOR = "showInfoIndicator"
	static final String KEY_TEST_PACKAGE_PREFIX = "testPackagePrefix"
	static final String KEY_TEST_PACKAGE_SUFFIX = "testPackageSuffix"
	static final String KEY_TEST_UNIT_PREFIX = "testUnitPrefix"
	static final String KEY_TEST_UNIT_SUFFIX = "testUnitSuffix"
	static final String KEY_NUMBER_OF_TESTS_PER_UNIT = "numberOfTestsPerUnit"
	static final String KEY_CHECK_GENERATE_UTPLSQL_TEST = "checkGenerateUtplsqlTest"
	static final String KEY_GENERATE_COMMENTS = "generateComments"
	static final String KEY_DISABLE_TESTS = "disableTests"
	static final String KEY_SUITE_PATH="suitePath"
	static final String KEY_INDENT_SPACES="indentSpaces"
	static final String KEY_GENERATE_FILES="generateFiles"
	static final String KEY_OUTPUT_DIRECTORY = "outputDirectory"
	static final String KEY_DELETE_EXISTING_FILES="deleteExistingFiles"
	static final String KEY_ROOT_FOLDER_IN_ODDGEN_VIEW = "rootFolderInOddgenView"

	def isUseRealtimeReporter() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_USE_REALTIME_REPORTER, true)
	}

	def setUseRealtimeReporter(boolean useRealtimeReporter) {
		getHashStructure.putBoolean(PreferenceModel.KEY_USE_REALTIME_REPORTER, useRealtimeReporter)
	}
	
	def isUnsharedWorksheet() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_UNSHARED_WORKSHEET, true)
	}

	def setUnsharedWorksheet(boolean unsharedWorksheet) {
		getHashStructure.putBoolean(PreferenceModel.KEY_UNSHARED_WORKSHEET, unsharedWorksheet)
	}

	def isResetPackage() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_RESET_PACKAGE, false)
	}

	def setResetPackage(boolean resetPackage) {
		getHashStructure.putBoolean(PreferenceModel.KEY_RESET_PACKAGE, resetPackage)
	}
	
	def isClearScreen() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_CLEAR_SCREEN, false)
	}

	def setClearScreen(boolean clearScreen) {
		getHashStructure.putBoolean(PreferenceModel.KEY_CLEAR_SCREEN, clearScreen)
	}

	def isAutoExecute() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_AUTO_EXECUTE, true)
	}

	def setAutoExecute(boolean autoExecute) {
		getHashStructure.putBoolean(PreferenceModel.KEY_AUTO_EXECUTE, autoExecute)
	}

	def isCheckRunUtplsqlTest() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_CHECK_RUN_UTPLSQL_TEST, false)
	}

	def setCheckRunUtplsqlTest(boolean checkRunUtplsqlTest) {
		getHashStructure.putBoolean(PreferenceModel.KEY_CHECK_RUN_UTPLSQL_TEST, checkRunUtplsqlTest)
	}

	def isShowDisabledCounter() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_SHOW_DISABLED_COUNTER, false)
	}

	def setShowDisabledCounter(boolean showDisabledCounter) {
		getHashStructure.putBoolean(PreferenceModel.KEY_SHOW_DISABLED_COUNTER, showDisabledCounter)
	}

	def isShowWarningsCounter() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_SHOW_WARNINGS_COUNTER, false)
	}

	def setShowWarningsCounter(boolean showWarningCounter) {
		getHashStructure.putBoolean(PreferenceModel.KEY_SHOW_WARNINGS_COUNTER, showWarningCounter)
	}

	def isShowInfoCounter() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_SHOW_INFO_COUNTER, false)
	}

	def setShowInfoCounter(boolean showInfoCounter) {
		getHashStructure.putBoolean(PreferenceModel.KEY_SHOW_INFO_COUNTER, showInfoCounter)
	}

	def isShowWarningIndicator() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_SHOW_WARNING_INDICATOR, false)
	}

	def setShowWarningIndicator(boolean showWarningIndicator) {
		getHashStructure.putBoolean(PreferenceModel.KEY_SHOW_WARNING_INDICATOR, showWarningIndicator)
	}

	def isShowInfoIndicator() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_SHOW_INFO_INDICATOR, false)
	}

	def setShowInfoIndicator(boolean showInfoIndicator) {
		getHashStructure.putBoolean(PreferenceModel.KEY_SHOW_INFO_INDICATOR, showInfoIndicator)
	}

	def getTestPackagePrefix() {
		return getHashStructure.getString(PreferenceModel.KEY_TEST_PACKAGE_PREFIX, "test_")
	}
	
	def setTestPackagePrefix(String testPackagePrefix) {
		getHashStructure.putString(PreferenceModel.KEY_TEST_PACKAGE_PREFIX, testPackagePrefix)
	}

	def getTestPackageSuffix() {
		return getHashStructure.getString(PreferenceModel.KEY_TEST_PACKAGE_SUFFIX, "")
	}
	
	def setTestPackageSuffix(String testPackageSuffix) {
		getHashStructure.putString(PreferenceModel.KEY_TEST_PACKAGE_SUFFIX, testPackageSuffix)
	}

	def getTestUnitPrefix() {
		return getHashStructure.getString(PreferenceModel.KEY_TEST_UNIT_PREFIX, "")
	}
	
	def setTestUnitPrefix(String testUnitPrefix) {
		getHashStructure.putString(PreferenceModel.KEY_TEST_UNIT_PREFIX, testUnitPrefix)
	}

	def getTestUnitSuffix() {
		return getHashStructure.getString(PreferenceModel.KEY_TEST_UNIT_SUFFIX, "")
	}
	
	def setTestUnitSuffix(String testUnitSuffix) {
		getHashStructure.putString(PreferenceModel.KEY_TEST_UNIT_SUFFIX, testUnitSuffix)
	}

	def getNumberOfTestsPerUnit() {
		return getHashStructure.getInt(PreferenceModel.KEY_NUMBER_OF_TESTS_PER_UNIT, 1)
	}

	def setNumberOfTestsPerUnit(int numberOfTestsPerUnit) {
		getHashStructure.putInt(PreferenceModel.KEY_NUMBER_OF_TESTS_PER_UNIT, numberOfTestsPerUnit)
	}

	def isCheckGenerateUtplsqlTest() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_CHECK_GENERATE_UTPLSQL_TEST, false)
	}

	def setCheckGenerateUtplsqlTest(boolean checkGenerateUtplsqlTest) {
		getHashStructure.putBoolean(PreferenceModel.KEY_CHECK_GENERATE_UTPLSQL_TEST, checkGenerateUtplsqlTest)
	}

	def isGenerateComments() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_GENERATE_COMMENTS, true)
	}

	def setGenerateComments(boolean generateComments) {
		getHashStructure.putBoolean(PreferenceModel.KEY_GENERATE_COMMENTS, generateComments)
	}

	def isDisableTests() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_DISABLE_TESTS, false)
	}

	def setDisableTests(boolean disableTests) {
		getHashStructure.putBoolean(PreferenceModel.KEY_DISABLE_TESTS, disableTests)
	}

	def getSuitePath() {
		return getHashStructure.getString(PreferenceModel.KEY_SUITE_PATH, "alltests")
	}
	
	def setSuitePath(String suitePath) {
		getHashStructure.putString(PreferenceModel.KEY_SUITE_PATH, suitePath)
	}

	def getIndentSpaces() {
		return getHashStructure.getInt(PreferenceModel.KEY_INDENT_SPACES, 3)
	}

	def setIndentSpaces(int indentSpaces) {
		getHashStructure.putInt(PreferenceModel.KEY_INDENT_SPACES, indentSpaces)
	}

	def isGenerateFiles() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_GENERATE_FILES, true)
	}

	def setGenerateFiles(boolean generateFiles) {
		getHashStructure.putBoolean(PreferenceModel.KEY_GENERATE_FILES, generateFiles)
	}

	def getOutputDirectory() {
		return getHashStructure.getString(PreferenceModel.KEY_OUTPUT_DIRECTORY, DEFAULT_OUTPUT_DIRECTORY)
	}
	
	def setOutputDirectory(String outputDirectory) {
		val dir = if (outputDirectory.empty) {DEFAULT_OUTPUT_DIRECTORY} else {outputDirectory}
		getHashStructure.putString(PreferenceModel.KEY_OUTPUT_DIRECTORY, dir)
	}

	def isDeleteExistingFiles() {
		return getHashStructure.getBoolean(PreferenceModel.KEY_DELETE_EXISTING_FILES, false)
	}

	def setDeleteExistingFiles(boolean deleteExistingFiles) {
		getHashStructure.putBoolean(PreferenceModel.KEY_DELETE_EXISTING_FILES, deleteExistingFiles)
	}

	def getRootFolderInOddgenView() {
		return getHashStructure.getString(PreferenceModel.KEY_ROOT_FOLDER_IN_ODDGEN_VIEW, "utPLSQL")
	}
	
	def setRootFolderInOddgenView(String rootFolder) {
		val folder = if (rootFolder.empty) {"utPLSQL"} else {rootFolder}
		getHashStructure.putString(PreferenceModel.KEY_ROOT_FOLDER_IN_ODDGEN_VIEW, folder)
	}

	override toString() {
		new ToStringBuilder(this).addAllFields.toString
	}
}
