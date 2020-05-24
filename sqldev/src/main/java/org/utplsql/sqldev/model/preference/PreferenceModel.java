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
package org.utplsql.sqldev.model.preference;

import java.io.File;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.UtplsqlToStringStyler;

import oracle.javatools.data.HashStructure;
import oracle.javatools.data.HashStructureAdapter;
import oracle.javatools.data.PropertyStorage;

public class PreferenceModel extends HashStructureAdapter {
    public static final String DEFAULT_OUTPUT_DIRECTORY = System.getProperty("user.home") + File.separator + "utplsql" + File.separator + "generated";
    private static final String DATA_KEY = "utplsql";

    private PreferenceModel(final HashStructure hash) {
        super(hash);
    }

    public static PreferenceModel getInstance(final PropertyStorage prefs) {
        return new PreferenceModel(findOrCreate(prefs, DATA_KEY));
    }

    private static final String KEY_USE_REALTIME_REPORTER = "useRealtimeRorter";
    private static final String KEY_UNSHARED_WORKSHEET = "unsharedWorksheet";
    private static final String KEY_RESET_PACKAGE = "resetPackage";
    private static final String KEY_CLEAR_SCREEN = "clearScreen";
    private static final String KEY_AUTO_EXECUTE = "autoExecute";
    private static final String KEY_CHECK_RUN_UTPLSQL_TEST = "checkRunUtplsqlTest";
    private static final String KEY_USE_SMART_TIMES = "useSmartTimes";
    private static final String KEY_NUMBER_OF_RUNS_IN_HISTORY = "numberOfRunsInHistory";
    private static final String KEY_SHOW_DISABLED_COUNTER = "showDisabledCounter";
    private static final String KEY_SHOW_WARNINGS_COUNTER = "showWarningsCounter";
    private static final String KEY_SHOW_INFO_COUNTER = "showInfoCounter";
    private static final String KEY_SHOW_WARNING_INDICATOR = "showWarningIndicator";
    private static final String KEY_SHOW_INFO_INDICATOR = "showInfoIndicator";
    private static final String KEY_SHOW_SUCCESSFUL_TESTS = "showSuccessfulTests";
    private static final String KEY_SHOW_DISABLED_TESTS = "showDisabledTests";
    private static final String KEY_SHOW_TEST_DESCRIPTION = "showTestDescription";
    private static final String KEY_SYNC_DETAIL_TAB = "syncDetailTab";
    private static final String KEY_TEST_PACKAGE_PREFIX = "testPackagePrefix";
    private static final String KEY_TEST_PACKAGE_SUFFIX = "testPackageSuffix";
    private static final String KEY_TEST_UNIT_PREFIX = "testUnitPrefix";
    private static final String KEY_TEST_UNIT_SUFFIX = "testUnitSuffix";
    private static final String KEY_NUMBER_OF_TESTS_PER_UNIT = "numberOfTestsPerUnit";
    private static final String KEY_CHECK_GENERATE_UTPLSQL_TEST = "checkGenerateUtplsqlTest";
    private static final String KEY_GENERATE_COMMENTS = "generateComments";
    private static final String KEY_DISABLE_TESTS = "disableTests";
    private static final String KEY_SUITE_PATH = "suitePath";
    private static final String KEY_INDENT_SPACES = "indentSpaces";
    private static final String KEY_GENERATE_FILES = "generateFiles";
    private static final String KEY_OUTPUT_DIRECTORY = "outputDirectory";
    private static final String KEY_DELETE_EXISTING_FILES = "deleteExistingFiles";
    private static final String KEY_ROOT_FOLDER_IN_ODDGEN_VIEW = "rootFolderInOddgenView";

    @Override
    public String toString() {
        return new ToStringCreator(this, UtplsqlToStringStyler.STYLER)
                .append(KEY_USE_REALTIME_REPORTER, isUseRealtimeReporter())
                .append(KEY_UNSHARED_WORKSHEET, isUnsharedWorksheet())
                .append(KEY_RESET_PACKAGE, isResetPackage())
                .append(KEY_CLEAR_SCREEN, isClearScreen())
                .append(KEY_AUTO_EXECUTE, isAutoExecute())
                .append(KEY_CHECK_RUN_UTPLSQL_TEST, isCheckRunUtplsqlTest())
                .append(KEY_USE_SMART_TIMES, isUseSmartTimes())
                .append(KEY_NUMBER_OF_RUNS_IN_HISTORY, getNumberOfRunsInHistory())
                .append(KEY_SHOW_DISABLED_COUNTER, isShowDisabledCounter())
                .append(KEY_SHOW_WARNINGS_COUNTER, isShowWarningsCounter())
                .append(KEY_SHOW_INFO_COUNTER, isShowInfoCounter())
                .append(KEY_SHOW_WARNING_INDICATOR, isShowWarningIndicator())
                .append(KEY_SHOW_INFO_INDICATOR, isShowInfoIndicator())
                .append(KEY_SHOW_SUCCESSFUL_TESTS, isShowSuccessfulTests())
                .append(KEY_SHOW_DISABLED_TESTS, isShowDisabledTests())
                .append(KEY_SHOW_TEST_DESCRIPTION, isShowTestDescription())
                .append(KEY_SYNC_DETAIL_TAB, isSyncDetailTab())
                .append(KEY_TEST_PACKAGE_PREFIX, getTestPackagePrefix())
                .append(KEY_TEST_PACKAGE_SUFFIX, getTestPackageSuffix())
                .append(KEY_TEST_UNIT_PREFIX, getTestUnitPrefix())
                .append(KEY_TEST_UNIT_SUFFIX, getTestUnitSuffix())
                .append(KEY_NUMBER_OF_TESTS_PER_UNIT, getNumberOfTestsPerUnit())
                .append(KEY_CHECK_GENERATE_UTPLSQL_TEST, isCheckGenerateUtplsqlTest())
                .append(KEY_GENERATE_COMMENTS, isGenerateComments())
                .append(KEY_DISABLE_TESTS, isDisableTests())
                .append(KEY_SUITE_PATH, getSuitePath())
                .append(KEY_INDENT_SPACES, getIndentSpaces())
                .append(KEY_GENERATE_FILES, isGenerateFiles())
                .append(KEY_OUTPUT_DIRECTORY, getOutputDirectory())
                .append(KEY_DELETE_EXISTING_FILES, isDeleteExistingFiles())
                .append(KEY_ROOT_FOLDER_IN_ODDGEN_VIEW, getRootFolderInOddgenView())
                .toString();
    }

    public boolean isUseRealtimeReporter() {
        return getHashStructure().getBoolean(KEY_USE_REALTIME_REPORTER, true);
    }

    public void setUseRealtimeReporter(final boolean useRealtimeReporter) {
        getHashStructure().putBoolean(KEY_USE_REALTIME_REPORTER, useRealtimeReporter);
    }

    public boolean isUnsharedWorksheet() {
        return getHashStructure().getBoolean(KEY_UNSHARED_WORKSHEET, true);
    }

    public void setUnsharedWorksheet(final boolean unsharedWorksheet) {
        getHashStructure().putBoolean(KEY_UNSHARED_WORKSHEET, unsharedWorksheet);
    }

    public boolean isResetPackage() {
        return getHashStructure().getBoolean(KEY_RESET_PACKAGE, false);
    }

    public void setResetPackage(final boolean resetPackage) {
        getHashStructure().putBoolean(KEY_RESET_PACKAGE, resetPackage);
    }

    public boolean isClearScreen() {
        return getHashStructure().getBoolean(KEY_CLEAR_SCREEN, false);
    }

    public void setClearScreen(final boolean clearScreen) {
        getHashStructure().putBoolean(KEY_CLEAR_SCREEN, clearScreen);
    }

    public boolean isAutoExecute() {
        return getHashStructure().getBoolean(KEY_AUTO_EXECUTE, true);
    }

    public void setAutoExecute(final boolean autoExecute) {
        getHashStructure().putBoolean(KEY_AUTO_EXECUTE, autoExecute);
    }

    public boolean isCheckRunUtplsqlTest() {
        return getHashStructure().getBoolean(KEY_CHECK_RUN_UTPLSQL_TEST, false);
    }

    public void setCheckRunUtplsqlTest(final boolean checkRunUtplsqlTest) {
        getHashStructure().putBoolean(KEY_CHECK_RUN_UTPLSQL_TEST, checkRunUtplsqlTest);
    }

    public boolean isUseSmartTimes() {
        return getHashStructure().getBoolean(KEY_USE_SMART_TIMES, false);
    }

    public void setUseSmartTimes(final boolean useSmartTimes) {
        getHashStructure().putBoolean(KEY_USE_SMART_TIMES, useSmartTimes);
    }

    public int getNumberOfRunsInHistory() {
        return getHashStructure().getInt(KEY_NUMBER_OF_RUNS_IN_HISTORY, 10);
    }

    public void setNumberOfRunsInHistory(final int runs) {
        getHashStructure().putInt(KEY_NUMBER_OF_RUNS_IN_HISTORY, runs);
    }

    public boolean isShowDisabledCounter() {
        return getHashStructure().getBoolean(KEY_SHOW_DISABLED_COUNTER, false);
    }

    public void setShowDisabledCounter(final boolean showDisabledCounter) {
        getHashStructure().putBoolean(KEY_SHOW_DISABLED_COUNTER, showDisabledCounter);
    }

    public boolean isShowWarningsCounter() {
        return getHashStructure().getBoolean(KEY_SHOW_WARNINGS_COUNTER, false);
    }

    public void setShowWarningsCounter(final boolean showWarningCounter) {
        getHashStructure().putBoolean(KEY_SHOW_WARNINGS_COUNTER, showWarningCounter);
    }

    public boolean isShowInfoCounter() {
        return getHashStructure().getBoolean(KEY_SHOW_INFO_COUNTER, false);
    }

    public void setShowInfoCounter(final boolean showInfoCounter) {
        getHashStructure().putBoolean(KEY_SHOW_INFO_COUNTER, showInfoCounter);
    }

    public boolean isShowWarningIndicator() {
        return getHashStructure().getBoolean(KEY_SHOW_WARNING_INDICATOR, false);
    }

    public void setShowWarningIndicator(final boolean showWarningIndicator) {
        getHashStructure().putBoolean(KEY_SHOW_WARNING_INDICATOR, showWarningIndicator);
    }

    public boolean isShowInfoIndicator() {
        return getHashStructure().getBoolean(KEY_SHOW_INFO_INDICATOR, false);
    }

    public void setShowInfoIndicator(final boolean showInfoIndicator) {
        getHashStructure().putBoolean(KEY_SHOW_INFO_INDICATOR, showInfoIndicator);
    }

    public boolean isShowSuccessfulTests() {
        return getHashStructure().getBoolean(KEY_SHOW_SUCCESSFUL_TESTS, true);
    }

    public void setShowSuccessfulTests(final boolean showSuccessfulTests) {
        getHashStructure().putBoolean(KEY_SHOW_SUCCESSFUL_TESTS, showSuccessfulTests);
    }

    public boolean isShowDisabledTests() {
        return getHashStructure().getBoolean(KEY_SHOW_DISABLED_TESTS, true);
    }

    public void setShowDisabledTests(final boolean showDisabledTests) {
        getHashStructure().putBoolean(KEY_SHOW_DISABLED_TESTS, showDisabledTests);
    }

    public boolean isShowTestDescription() {
        return getHashStructure().getBoolean(KEY_SHOW_TEST_DESCRIPTION, false);
    }

    public void setShowTestDescription(final boolean showTestDescription) {
        getHashStructure().putBoolean(KEY_SHOW_TEST_DESCRIPTION, showTestDescription);
    }

    public boolean isSyncDetailTab() {
        return getHashStructure().getBoolean(KEY_SYNC_DETAIL_TAB, true);
    }

    public void setSyncDetailTab(final boolean syncDetailTab) {
        getHashStructure().putBoolean(KEY_SYNC_DETAIL_TAB, syncDetailTab);
    }

    public String getTestPackagePrefix() {
        return getHashStructure().getString(KEY_TEST_PACKAGE_PREFIX, "test_");
    }

    public void setTestPackagePrefix(final String testPackagePrefix) {
        getHashStructure().putString(KEY_TEST_PACKAGE_PREFIX, testPackagePrefix);
    }

    public String getTestPackageSuffix() {
        return getHashStructure().getString(KEY_TEST_PACKAGE_SUFFIX, "");
    }

    public void setTestPackageSuffix(final String testPackageSuffix) {
        getHashStructure().putString(KEY_TEST_PACKAGE_SUFFIX, testPackageSuffix);
    }

    public String getTestUnitPrefix() {
        return getHashStructure().getString(KEY_TEST_UNIT_PREFIX, "");
    }

    public void setTestUnitPrefix(final String testUnitPrefix) {
        getHashStructure().putString(KEY_TEST_UNIT_PREFIX, testUnitPrefix);
    }

    public String getTestUnitSuffix() {
        return getHashStructure().getString(KEY_TEST_UNIT_SUFFIX, "");
    }

    public void setTestUnitSuffix(final String testUnitSuffix) {
        getHashStructure().putString(KEY_TEST_UNIT_SUFFIX, testUnitSuffix);
    }

    public int getNumberOfTestsPerUnit() {
        return getHashStructure().getInt(KEY_NUMBER_OF_TESTS_PER_UNIT, 1);
    }

    public void setNumberOfTestsPerUnit(final int numberOfTestsPerUnit) {
        getHashStructure().putInt(KEY_NUMBER_OF_TESTS_PER_UNIT, numberOfTestsPerUnit);
    }

    public boolean isCheckGenerateUtplsqlTest() {
        return getHashStructure().getBoolean(KEY_CHECK_GENERATE_UTPLSQL_TEST, false);
    }

    public void setCheckGenerateUtplsqlTest(final boolean checkGenerateUtplsqlTest) {
        getHashStructure().putBoolean(KEY_CHECK_GENERATE_UTPLSQL_TEST, checkGenerateUtplsqlTest);
    }

    public boolean isGenerateComments() {
        return getHashStructure().getBoolean(KEY_GENERATE_COMMENTS, true);
    }

    public void setGenerateComments(final boolean generateComments) {
        getHashStructure().putBoolean(KEY_GENERATE_COMMENTS, generateComments);
    }

    public boolean isDisableTests() {
        return getHashStructure().getBoolean(KEY_DISABLE_TESTS, false);
    }

    public void setDisableTests(final boolean disableTests) {
        getHashStructure().putBoolean(KEY_DISABLE_TESTS, disableTests);
    }

    public String getSuitePath() {
        return getHashStructure().getString(KEY_SUITE_PATH, "alltests");
    }

    public void setSuitePath(final String suitePath) {
        getHashStructure().putString(KEY_SUITE_PATH, suitePath);
    }

    public int getIndentSpaces() {
        return getHashStructure().getInt(KEY_INDENT_SPACES, 3);
    }

    public void setIndentSpaces(final int indentSpaces) {
        getHashStructure().putInt(KEY_INDENT_SPACES, indentSpaces);
    }

    public boolean isGenerateFiles() {
        return getHashStructure().getBoolean(KEY_GENERATE_FILES, true);
    }

    public void setGenerateFiles(final boolean generateFiles) {
        getHashStructure().putBoolean(KEY_GENERATE_FILES, generateFiles);
    }

    public String getOutputDirectory() {
        return getHashStructure().getString(KEY_OUTPUT_DIRECTORY, DEFAULT_OUTPUT_DIRECTORY);
    }

    public void setOutputDirectory(final String outputDirectory) {
        final String dir = outputDirectory.isEmpty() ? DEFAULT_OUTPUT_DIRECTORY : outputDirectory;
        getHashStructure().putString(KEY_OUTPUT_DIRECTORY, dir);
    }

    public boolean isDeleteExistingFiles() {
        return getHashStructure().getBoolean(KEY_DELETE_EXISTING_FILES, false);
    }

    public void setDeleteExistingFiles(final boolean deleteExistingFiles) {
        getHashStructure().putBoolean(KEY_DELETE_EXISTING_FILES, deleteExistingFiles);
    }

    public String getRootFolderInOddgenView() {
        return getHashStructure().getString(KEY_ROOT_FOLDER_IN_ODDGEN_VIEW, "utPLSQL");
    }

    public void setRootFolderInOddgenView(final String rootFolder) {
        final String folder = rootFolder.isEmpty() ? "utPLSQL" : rootFolder;
        getHashStructure().putString(KEY_ROOT_FOLDER_IN_ODDGEN_VIEW, folder);
    }
}
