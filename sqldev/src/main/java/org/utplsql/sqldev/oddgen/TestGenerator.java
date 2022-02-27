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
package org.utplsql.sqldev.oddgen;

import java.io.File;
import java.sql.Connection;
import java.util.*;
import java.util.logging.Logger;

import org.oddgen.sqldev.generators.OddgenGenerator2;
import org.oddgen.sqldev.generators.model.Node;
import org.oddgen.sqldev.generators.model.NodeTools;
import org.oddgen.sqldev.plugin.templates.TemplateTools;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.oddgen.GenContext;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.resources.UtplsqlResources;

import oracle.ide.config.Preferences;

public class TestGenerator implements OddgenGenerator2 {
    private static final Logger logger = Logger.getLogger(TestGenerator.class.getName());

    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final String GENERATE_FILES = UtplsqlResources.getString("PREF_GENERATE_FILES_LABEL");
    public static final String OUTPUT_DIRECTORY = UtplsqlResources.getString("PREF_OUTPUT_DIRECTORY_LABEL");
    public static final String DELETE_EXISTING_FILES = UtplsqlResources.getString("PREF_DELETE_EXISTING_FILES_LABEL");
    public static final String TEST_PACKAGE_PREFIX = UtplsqlResources.getString("PREF_TEST_PACKAGE_PREFIX_LABEL");
    public static final String TEST_PACKAGE_SUFFIX = UtplsqlResources.getString("PREF_TEST_PACKAGE_SUFFIX_LABEL");
    public static final String TEST_UNIT_PREFIX = UtplsqlResources.getString("PREF_TEST_UNIT_PREFIX_LABEL");
    public static final String TEST_UNIT_SUFFIX = UtplsqlResources.getString("PREF_TEST_UNIT_SUFFIX_LABEL");
    public static final String NUMBER_OF_TESTS_PER_UNIT = UtplsqlResources.getString("PREF_NUMBER_OF_TESTS_PER_UNIT_LABEL");
    public static final String GENERATE_COMMENTS = UtplsqlResources.getString("PREF_GENERATE_COMMENTS_LABEL");
    public static final String DISABLE_TESTS = UtplsqlResources.getString("PREF_DISABLE_TESTS_LABEL");
    public static final String SUITE_PATH = UtplsqlResources.getString("PREF_SUITE_PATH_LABEL");
    public static final String INDENT_SPACES = UtplsqlResources.getString("PREF_INDENT_SPACES_LABEL");

    private final NodeTools nodeTools = new NodeTools();
    private final TemplateTools templateTools = new TemplateTools();
    private final ArrayList<String> consoleOutput = new ArrayList<>();

    private GenContext toContext(final Node node) {
        final GenContext context = new GenContext();
        context.setObjectType(nodeTools.toObjectType(node));
        context.setObjectName(nodeTools.toObjectName(node));
        context.setTestPackagePrefix(node.getParams().get(TEST_PACKAGE_PREFIX).toLowerCase());
        context.setTestPackageSuffix(node.getParams().get(TEST_PACKAGE_SUFFIX).toLowerCase());
        context.setTestUnitPrefix(node.getParams().get(TEST_UNIT_PREFIX).toLowerCase());
        context.setTestUnitSuffix(node.getParams().get(TEST_UNIT_SUFFIX).toLowerCase());
        context.setNumberOfTestsPerUnit(Integer.parseInt(node.getParams().get(NUMBER_OF_TESTS_PER_UNIT)));
        context.setGenerateComments(YES.equals(node.getParams().get(GENERATE_COMMENTS)));
        context.setDisableTests(YES.equals(node.getParams().get(DISABLE_TESTS)));
        context.setSuitePath(node.getParams().get(SUITE_PATH).toLowerCase());
        context.setIndentSpaces(Integer.parseInt(node.getParams().get(INDENT_SPACES)));
        return context;
    }

    private void resetConsoleOutput() {
        consoleOutput.clear();
    }

    private void saveConsoleOutput(final String s) {
        if (s != null) {
            consoleOutput.addAll(Arrays.asList(s.split("[\\n\\r]+")));
        }
    }

    private void logConsoleOutput() {
        for (final String line : consoleOutput) {
            if (line.contains("error") || line.startsWith("Cannot")) {
                logger.severe(line);
            } else {
                logger.fine(line);
            }
        }
    }

    private String deleteFile(final File file) {
        String ret;
        if (file.delete()) {
            StringBuilder sb = new StringBuilder();
            sb.append(file.getAbsoluteFile());
            sb.append(" deleted.");
            ret = sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot delete file ");
            sb.append(file.getAbsoluteFile());
            sb.append(".");
            ret = sb.toString();
        }
        return ret;
    }

    private CharSequence deleteFiles(final String directory) {
        StringBuilder sb = new StringBuilder();
        final File dir = new File(directory);
        try {
            for (final File file : Objects.requireNonNull(dir.listFiles())) {
                if (!file.isDirectory() && (file.getName().endsWith(".pks") || file.getName().endsWith(".pkb"))) {
                    sb.append(deleteFile(file));
                    sb.append('\n');
                }
            }
        } catch (NullPointerException e) {
            // ignore
        }
        return sb;
    }

    @Override
    public boolean isSupported(final Connection conn) {
        return DatabaseTools.isSupported(conn);
    }

    @Override
    public String getName(final Connection conn) {
        return "Generate test";
    }

    @Override
    public String getDescription(final Connection conn) {
        return "Generates utPLSQL test packages for public units in packages, types, functions and procedures found in the current schema.";
    }

    @Override
    public List<String> getFolders(final Connection conn) {
        final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
        final ArrayList<String> folders = new ArrayList<>();
        for (String f : preferences.getRootFolderInOddgenView().split(",")) {
            if (f != null) {
                folders.add(f.trim());
            }
        }
        return folders;
    }
    
    @Override
    public String getHelp(final Connection conn) {
        return "<p>not yet available</p>";
    }

    @Override
    public List<Node> getNodes(final Connection conn, final String parentNodeId) {
        final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
        final LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put(GENERATE_FILES, preferences.isGenerateFiles() ? YES : NO);
        params.put(OUTPUT_DIRECTORY, preferences.getOutputDirectory());
        params.put(DELETE_EXISTING_FILES, preferences.isDeleteExistingFiles() ? YES : NO);
        params.put(TEST_PACKAGE_PREFIX, preferences.getTestPackagePrefix());
        params.put(TEST_PACKAGE_SUFFIX, preferences.getTestPackageSuffix());
        params.put(TEST_UNIT_PREFIX, preferences.getTestUnitPrefix());
        params.put(TEST_UNIT_SUFFIX, preferences.getTestUnitSuffix());
        params.put(NUMBER_OF_TESTS_PER_UNIT, String.valueOf(preferences.getNumberOfTestsPerUnit()));
        params.put(GENERATE_COMMENTS, preferences.isGenerateComments() ? YES : NO);
        params.put(DISABLE_TESTS, preferences.isDisableTests() ? YES : NO);
        params.put(SUITE_PATH, preferences.getSuitePath());
        params.put(INDENT_SPACES, String.valueOf(preferences.getIndentSpaces()));
        if (parentNodeId == null || parentNodeId.isEmpty()) {
            final Node packageNode = new Node();
            packageNode.setId("PACKAGE");
            packageNode.setParams(params);
            packageNode.setLeaf(false);
            packageNode.setGeneratable(true);
            packageNode.setMultiselectable(true);
            final Node typeNode = new Node();
            typeNode.setId("TYPE");
            typeNode.setParams(params);
            typeNode.setLeaf(false);
            typeNode.setGeneratable(true);
            typeNode.setMultiselectable(true);
            final Node functionNode = new Node();
            functionNode.setId("FUNCTION");
            functionNode.setParams(params);
            functionNode.setLeaf(false);
            functionNode.setGeneratable(true);
            functionNode.setMultiselectable(true);
            final Node procedureNode = new Node();
            procedureNode.setId("PROCEDURE");
            procedureNode.setParams(params);
            procedureNode.setLeaf(false);
            procedureNode.setGeneratable(true);
            procedureNode.setMultiselectable(true);
            return Arrays.asList(packageNode, typeNode, functionNode, procedureNode);
        } else {
            final UtplsqlDao dao = new UtplsqlDao(conn);
            final List<Node> nodes = dao.testables(parentNodeId);
            for (final Node node : nodes) {
                node.setParams(params);
            }
            return nodes;
        }
    }

    @Override
    public HashMap<String, List<String>> getLov(final Connection conn, final LinkedHashMap<String, String> params,
            final List<Node> nodes) {
        final HashMap<String, List<String>> lov = new HashMap<>();
        lov.put(NUMBER_OF_TESTS_PER_UNIT, Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
        lov.put(INDENT_SPACES, Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"));
        lov.put(GENERATE_COMMENTS, Arrays.asList(YES, NO));
        lov.put(DISABLE_TESTS, Arrays.asList(YES, NO));
        lov.put(GENERATE_FILES, Arrays.asList(YES, NO));
        lov.put(DELETE_EXISTING_FILES, Arrays.asList(YES, NO));
        return lov;
    }

    @Override
    public HashMap<String, Boolean> getParamStates(final Connection conn, final LinkedHashMap<String, String> params,
            final List<Node> nodes) {
        final HashMap<String, Boolean> paramStates = new HashMap<>();
        paramStates.put(OUTPUT_DIRECTORY, YES.equals(params.get(GENERATE_FILES)));
        paramStates.put(DELETE_EXISTING_FILES, YES.equals(params.get(GENERATE_FILES)));
        return paramStates;
    }

    @Override
    public String generateProlog(final Connection conn, final List<Node> nodes) {
        StringBuilder sb = new StringBuilder();
        final boolean generateFiles = YES.equals(nodes.get(0).getParams().get(GENERATE_FILES));
        final String outputDirectory = nodes.get(0).getParams().get(OUTPUT_DIRECTORY);
        final boolean deleteExistingfiles = YES.equals(nodes.get(0).getParams().get(DELETE_EXISTING_FILES));
        if (generateFiles) {
            resetConsoleOutput();
            saveConsoleOutput(templateTools.mkdirs(outputDirectory));
            if (deleteExistingfiles) {
                saveConsoleOutput(deleteFiles(outputDirectory).toString());
            }
            sb.append("--\n");
            sb.append("-- install generated utPLSQL test packages\n");
            sb.append("--\n");
        }
        for (final Node node : nodes) {
            final GenContext context = toContext(node);
            context.setConn(conn);
            final TestTemplate testTemplate = new TestTemplate(context);
            if (generateFiles) {
                final String packageName = context.getTestPackagePrefix().toLowerCase()
                        + nodeTools.toObjectName(node).toLowerCase()
                        + context.getTestPackageSuffix().toLowerCase();
                final String packagePath = outputDirectory + File.separator + packageName;
                saveConsoleOutput(templateTools.writeToFile(packagePath + ".pks", testTemplate.generateSpec()));
                saveConsoleOutput(templateTools.writeToFile(packagePath + ".pkb", testTemplate.generateBody()));
                sb.append('@');
                sb.append(packagePath);
                sb.append(".pks\n");
                sb.append('@');
                sb.append(packagePath);
                sb.append(".pkb\n");
            } else {
                sb.append(testTemplate.generate());
                sb.append('\n');
            }
        }
        logConsoleOutput();
        if (generateFiles && consoleOutput.stream().anyMatch(it -> it.contains("error"))) {
            sb.append('\n');
            sb.append("--\n");
            sb.append("-- console output produced during the generation of this script (errors found)\n");
            sb.append("--\n");
            sb.append("/*\n\n");
                for (final String line : consoleOutput) {
                    sb.append(line);
                    sb.append('\n');
                }
            sb.append('\n');
            sb.append("*/\n");
        }
        return sb.toString();
    }

    @Override
    public String generateSeparator(final Connection conn) {
        return "";
    }

    @Override
    public String generateEpilog(final Connection conn, final List<Node> nodes) {
        return "";
    }

    @Override
    public String generate(final Connection conn, final Node node) {
        return "";
    }
}
