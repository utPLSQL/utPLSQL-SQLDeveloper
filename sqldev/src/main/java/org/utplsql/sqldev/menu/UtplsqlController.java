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
package org.utplsql.sqldev.menu;

import java.awt.Component;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JEditorPane;

import org.utplsql.sqldev.coverage.CodeCoverageReporter;
import org.utplsql.sqldev.dal.RealtimeReporterDao;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.exception.GenericRuntimeException;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.StringTools;
import org.utplsql.sqldev.model.URLTools;
import org.utplsql.sqldev.model.oddgen.GenContext;
import org.utplsql.sqldev.model.parser.PlsqlObject;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.oddgen.TestTemplate;
import org.utplsql.sqldev.parser.UtplsqlParser;
import org.utplsql.sqldev.runner.UtplsqlRunner;
import org.utplsql.sqldev.runner.UtplsqlWorksheetRunner;

import oracle.dbtools.raptor.navigator.db.DBNavigatorWindow;
import oracle.dbtools.raptor.navigator.db.DatabaseConnection;
import oracle.dbtools.raptor.navigator.impl.ChildObjectElement;
import oracle.dbtools.raptor.navigator.impl.DatabaseSourceNode;
import oracle.dbtools.raptor.navigator.impl.ObjectFolder;
import oracle.dbtools.raptor.navigator.impl.SchemaFolder;
import oracle.dbtools.raptor.navigator.plsql.PlSqlNode;
import oracle.dbtools.raptor.utils.Connections;
import oracle.dbtools.worksheet.editor.Worksheet;
import oracle.ide.Context;
import oracle.ide.Ide;
import oracle.ide.config.Preferences;
import oracle.ide.controller.Controller;
import oracle.ide.controller.IdeAction;
import oracle.ide.editor.Editor;
import oracle.ide.model.Node;
import oracle.ide.view.View;

@SuppressWarnings("all")
public class UtplsqlController implements Controller {
    private static final Logger logger = Logger.getLogger(UtplsqlController.class.getName());

    public static int UTPLSQL_TEST_CMD_ID = (Ide.findCmdID("utplsql.test")).intValue();
    public static int UTPLSQL_COVERAGE_CMD_ID = (Ide.findCmdID("utplsql.coverage")).intValue();
    public static int UTPLSQL_GENERATE_CMD_ID = (Ide.findCmdID("utplsql.generate")).intValue();
    public static final IdeAction UTPLSQL_TEST_ACTION = IdeAction.get(UTPLSQL_TEST_CMD_ID);
    public static final IdeAction UTPLSQL_COVERAGE_ACTION = IdeAction.get(UTPLSQL_COVERAGE_CMD_ID);
    public static final IdeAction UTPLSQL_GENERATE_ACTION = IdeAction.get(UTPLSQL_GENERATE_CMD_ID);

    @Override
    public boolean handleEvent(final IdeAction action, final Context context) {
        try {
            if (action.getCommandId() == UTPLSQL_TEST_CMD_ID) {
                logger.finer(() -> "handle utplsql.test");
                runTest(context);
                return true;
            } else if (action.getCommandId() == UTPLSQL_COVERAGE_CMD_ID) {
                logger.finer(() -> "handle utplsql.coverage");
                codeCoverage(context);
                return true;
            } else if (action.getCommandId() == UTPLSQL_GENERATE_CMD_ID) {
                logger.finer(() -> "handle utplsql.generate");
                generateTest(context);
                return true;
            }
        } catch (Exception e) {
            final String msg = "Failed to handle event for action " +  action.toString() + ".";
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
        return false;
    }

    @Override
    public boolean update(final IdeAction action, final Context context) {
        if (action.getCommandId() == UTPLSQL_TEST_CMD_ID || action.getCommandId() == UTPLSQL_COVERAGE_CMD_ID) {
            final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
            action.setEnabled(false);
            final View view = context.getView();
            if (view instanceof Editor) {
                final Component component = ((Editor) view).getDefaultFocusComponent();
                if (component instanceof JEditorPane) {
                    if (preferences.isCheckRunUtplsqlTest()) {
                        final Node node = context.getNode();
                        String connectionName = null;
                        String owner = null;
                        if (node instanceof DatabaseSourceNode) {
                            connectionName = ((DatabaseSourceNode) node).getConnectionName();
                            owner = ((DatabaseSourceNode) node).getOwner();
                        } else {
                            if ((view instanceof Worksheet)) {
                                connectionName = ((Worksheet) view).getConnectionName();
                            }
                        }
                        logger.fine("connectionName: " + connectionName);
                        final String text = ((JEditorPane) component).getText();
                        final Connection conn = DatabaseTools.getConnection(connectionName);
                        final UtplsqlParser parser = new UtplsqlParser(text, conn, owner);
                        if (!parser.getPathAt(((JEditorPane) component).getCaretPosition()).isEmpty()) {
                            action.setEnabled(true);
                        }
                    } else {
                        action.setEnabled(true);
                    }
                }
            } else if (view instanceof DBNavigatorWindow) {
                action.setEnabled(true);
                // disable action if a node in the selection is not runnable
                for (int i = 0; i < context.getSelection().length; i++) {
                    logger.fine("section " + i + " is " + context.getSelection()[i].toString() + " of class "
                            + context.getSelection()[i].getClass().getName());
                    if (action.isEnabled()) {
                        final Object element = context.getSelection()[i];
                        final String connectionName = URLTools.getConnectionName(getURL(context));
                        if (Connections.getInstance().isConnectionOpen(connectionName)) {
                            Connection conn = DatabaseTools.getConnection(connectionName);
                            final UtplsqlDao dao = new UtplsqlDao(conn);
                            if (preferences.isCheckRunUtplsqlTest() && dao.isUtAnnotationManagerInstalled()) {
                                if (element instanceof DatabaseConnection) {
                                    final String schema = DatabaseTools.getSchema((DatabaseConnection) element);
                                    action.setEnabled(dao.containsUtplsqlTest(schema));
                                } else if ((element instanceof SchemaFolder)) {
                                    final String schema = ((SchemaFolder) element).getSchemaName();
                                    action.setEnabled(dao.containsUtplsqlTest(schema));
                                } else if ((element instanceof ObjectFolder)) {
                                    final String schema = URLTools.getSchema(((ObjectFolder) element).getURL());
                                    action.setEnabled(dao.containsUtplsqlTest(schema));
                                } else if ((element instanceof PlSqlNode)) {
                                    final String schema = ((PlSqlNode) element).getOwner();
                                    final String objectName = ((PlSqlNode) element).getObjectName();
                                    action.setEnabled(dao.containsUtplsqlTest(schema, objectName));
                                } else if ((element instanceof ChildObjectElement)) {
                                    final String schema = URLTools.getSchema(((ChildObjectElement) element).getURL());
                                    final String objectName = URLTools.getMemberObject(((ChildObjectElement) element).getURL());
                                    final String subObjectName = ((ChildObjectElement) element).getShortLabel();
                                    action.setEnabled(dao.containsUtplsqlTest(schema, objectName, subObjectName));
                                }
                            }
                        } else {
                            action.setEnabled(false);
                        }
                    }
                }
            }
            return true;
        } else if (action.getCommandId() == UTPLSQL_GENERATE_CMD_ID) {
            action.setEnabled(false);
            // enable if generation is possible
            final View view = context.getView();
            if (view instanceof Editor) {
                final Component component = ((Editor) view).getDefaultFocusComponent();
                if (component instanceof JEditorPane) {
                    final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
                    if (preferences.isCheckGenerateUtplsqlTest()) {
                        final String text = ((JEditorPane) component).getText();
                        final UtplsqlParser parser = new UtplsqlParser(text);
                        PlsqlObject plsqlObject = parser.getObjectAt(((JEditorPane) component).getCaretPosition());
                        action.setEnabled(plsqlObject != null);
                    } else {
                        action.setEnabled(true);
                    }
                }
            } else if (view instanceof DBNavigatorWindow) {
                // multiselection is not supported, use oddgen to generte tests for multiple objects
                if (context.getSelection().length == 1) {
                    final Object element = context.getSelection()[0];
                    if (element instanceof PlSqlNode) {
                        final String ot = ((PlSqlNode) element).getObjectType();
                        if (ot.startsWith("PACKAGE") || ot.startsWith("TYPE") || "FUNCTION".equals(ot) || "PROCEDURE".equals(ot)) {
                            action.setEnabled(true);
                        }
                    }
                }
            }
        }
        return false;
    }

    private String getPath(final Object element) {
        String path = null;
        if (element instanceof DatabaseConnection) {
            path = DatabaseTools.getSchema((DatabaseConnection) element);
        } else if (element instanceof SchemaFolder) {
            path = ((SchemaFolder) element).getSchemaName();
        } else if (element instanceof ObjectFolder) {
            path = URLTools.getSchema(((ObjectFolder) element).getURL());
        } else if (element instanceof PlSqlNode) {
            final StringBuilder sb = new StringBuilder();
            sb.append(((PlSqlNode) element).getOwner());
            sb.append(".");
            sb.append(((PlSqlNode) element).getObjectName());
            path = sb.toString();
        } else if (element instanceof ChildObjectElement) {
            StringBuilder sb = new StringBuilder();
            sb.append(URLTools.getSchema(((ChildObjectElement) element).getURL()));
            sb.append(".");
            sb.append(URLTools.getMemberObject(((ChildObjectElement) element).getURL()));
            sb.append(".");
            sb.append( ((ChildObjectElement) element).getShortLabel());
            path = sb.toString();
        } else {
            path = "";
        }
        logger.fine("path: " + path);
        return path;
    }

    private ArrayList<String> getPathList(final Context context) {
        final ArrayList<String> pathList = new ArrayList<>();
        for (int i = 0; i < context.getSelection().length; i++) {
            final Object element = context.getSelection()[i];
            pathList.add(getPath(element));
        }
        return pathList;
    }

    private ArrayList<String> getPathList(final String path) {
        final ArrayList<String> pathList = new ArrayList<>();
        pathList.add(path);
        return pathList;
    }

    private ArrayList<String> dedupPathList(final List<String> pathList) {
        final HashSet<String> set = new HashSet<>();
        for (final String path : pathList) {
            set.add(path);
        }
        final ArrayList<String> ret = new ArrayList<>();
        final Pattern p = Pattern.compile("(((([^\\.]+)\\.)?[^\\.]+)\\.)?[^\\.]+");
        for (final String path : set) {
            final Matcher m = p.matcher(path);
            if (m.matches()) {
                final String parent1 = m.group(4); // user
                final String parent2 = m.group(2); // user.package
                if (parent1 == null || !set.contains(parent1)) {
                    if (parent2 == null || !set.contains(parent2)) {
                        ret.add(path);
                    }
                }
            } else {
                logger.severe("path: " + path + " did not match " + p.toString() + ", this is unexected!");
            }
        }
        return ret;
    }

    private URL getURL(final Context context) {
        URL url = null;
        final Object element = context.getSelection()[0];
        if (element instanceof DatabaseConnection) {
            url = ((DatabaseConnection) element).getURL();
        } else if (element instanceof SchemaFolder) {
            url = ((SchemaFolder) element).getURL();
        } else if (element instanceof ObjectFolder) {
            url = ((ObjectFolder) element).getURL();
        } else if (element instanceof PlSqlNode) {
            url = ((PlSqlNode) element).getURL();
        } else if (element instanceof ChildObjectElement) {
            url = ((ChildObjectElement) element).getURL();
        }
        logger.fine("url: " + url);
        return url;
    }

    private void populateGenContext(final GenContext genContext, final PreferenceModel preferences) {
        genContext.setTestPackagePrefix(preferences.getTestPackagePrefix().toLowerCase());
        genContext.setTestPackageSuffix(preferences.getTestPackageSuffix().toLowerCase());
        genContext.setTestUnitPrefix(preferences.getTestUnitPrefix().toLowerCase());
        genContext.setTestUnitSuffix(preferences.getTestUnitSuffix().toLowerCase());
        genContext.setNumberOfTestsPerUnit(preferences.getNumberOfTestsPerUnit());
        genContext.setGenerateComments(preferences.isGenerateComments());
        genContext.setDisableTests(preferences.isDisableTests());
        genContext.setSuitePath(preferences.getSuitePath().toLowerCase());
        genContext.setIndentSpaces(preferences.getIndentSpaces());
    }

    private GenContext getGenContext(final Context context) {
        final String connectionName = URLTools.getConnectionName(getURL(context));
        final GenContext genContext = new GenContext();
        if (Connections.getInstance().isConnectionOpen(connectionName)) {
            genContext.setConn(DatabaseTools.getConnection(connectionName));
            final Object element = context.getSelection()[0];
            if ((element instanceof PlSqlNode)) {
                genContext.setObjectType(((PlSqlNode) element).getObjectType().replace(" BODY", ""));
                genContext.setObjectName(((PlSqlNode) element).getObjectName());
                final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
                populateGenContext(genContext, preferences);
            }
        }
        return genContext;
    }
    
    public void runTest(final Context context) {
        final View view = context.getView();
        final Node node = context.getNode();
        final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
        final StringBuilder sb = new StringBuilder();
        sb.append("Run utPLSQL from view ");
        sb.append(view != null ? view.getClass().getName() : "???");
        sb.append(" and node ");
        sb.append(node != null ? node.getClass().getName() : "???");
        sb.append(".");
        logger.finer(() -> sb.toString());
        if ((view instanceof Editor)) {
            final Component component = ((Editor) view).getDefaultFocusComponent();
            if (component instanceof JEditorPane) {
                String connectionName = null;
                String owner = null;
                if (node instanceof DatabaseSourceNode) {
                    connectionName = ((DatabaseSourceNode) node).getConnectionName();
                    owner = ((DatabaseSourceNode) node).getOwner();
                } else {
                    if (view instanceof Worksheet) {
                        connectionName = ((Worksheet) view).getConnectionName();
                    }
                }
                logger.fine("connectionName: " + connectionName);
                final Connection conn = DatabaseTools.getConnection(connectionName);
                String text = ((JEditorPane) component).getText();
                final UtplsqlParser parser = new UtplsqlParser(text, conn, owner);
                final int position = ((JEditorPane) component).getCaretPosition();
                final String path = parser.getPathAt(position);
                final RealtimeReporterDao rrDao = new RealtimeReporterDao(conn);
                if (preferences.isUseRealtimeReporter() && rrDao.isSupported()) {
                    final UtplsqlRunner runner = new UtplsqlRunner(getPathList(path), connectionName);
                    runner.runTestAsync();
                } else {
                    final UtplsqlWorksheetRunner worksheet = new UtplsqlWorksheetRunner(getPathList(path), connectionName);
                    worksheet.runTestAsync();
                }
            }
        } else if (view instanceof DBNavigatorWindow) {
            final URL url = getURL(context);
            if ((url != null)) {
                final String connectionName = URLTools.getConnectionName(url);
                logger.fine("connectionName: " + connectionName);
                final Connection conn = DatabaseTools.getConnection(connectionName);
                final RealtimeReporterDao rrDao = new RealtimeReporterDao(conn);
                final ArrayList<String> pathList = dedupPathList(getPathList(context));
                if (preferences.isUseRealtimeReporter() && rrDao.isSupported()) {
                    final UtplsqlRunner runner = new UtplsqlRunner(pathList, connectionName);
                    runner.runTestAsync();
                } else {
                    final UtplsqlWorksheetRunner worksheet = new UtplsqlWorksheetRunner(pathList, connectionName);
                    worksheet.runTestAsync();
                }
            }
        }
    }

    public List<String> dependencies(final String name, final String connectionName) {
        List<String> ret = null;
        if (connectionName != null) {
            final String owner = DatabaseTools.getSchema(connectionName);
            ret = dependencies(owner, name, connectionName);
        }
        return ret;
    }

    public List<String> dependencies(final String owner, final String name, final String connectionName) {
        List<String> ret = null;
        if (connectionName != null) {
            Connection conn = DatabaseTools.getConnection(connectionName);
            final UtplsqlDao dao = new UtplsqlDao(conn);
            ret = dao.includes(owner, name);
        }
        return ret;
    }

    public List<String> dependencies(final Context context, final String connectionName) {
        final HashSet<String> ret = new HashSet<String>();
        for (int i = 0; i < context.getSelection().length; i++) {
            final Object element = context.getSelection()[i];
            if (element instanceof PlSqlNode) {
                final String owner = ((PlSqlNode) element).getOwner();
                final String objectName = ((PlSqlNode) element).getObjectName();
                final List<String> dep = dependencies(owner, objectName, connectionName);
                ret.addAll(dep);
            } else {
                if (element instanceof ChildObjectElement) {
                    final String owner = URLTools.getSchema(((ChildObjectElement) element).getURL());
                    final String objectName = URLTools.getMemberObject(((ChildObjectElement) element).getURL());
                    final List<String> dep = dependencies(owner, objectName, connectionName);
                    ret.addAll(dep);
                }
            }
        }
        return ret.stream().sorted().collect(Collectors.toList());
    }

    public void codeCoverage(final Context context) {
        final View view = context.getView();
        final Node node = context.getNode();
        final StringBuilder sb = new StringBuilder();
        sb.append("Code coverage from view ");
        sb.append(view != null ? view.getClass().getName() : "???");
        sb.append(" and node ");
        sb.append(node != null ? node.getClass().getName() : "???");
        sb.append(".");
        logger.finer(() -> sb.toString());
        if (view instanceof Editor) {
            final Component component = ((Editor) view).getDefaultFocusComponent();
            if (component instanceof JEditorPane) {
                String connectionName = null;
                String owner = null;
                if (node instanceof DatabaseSourceNode) {
                    connectionName = ((DatabaseSourceNode) node).getConnectionName();
                } else if (view instanceof Worksheet) {
                    connectionName = ((Worksheet) view).getConnectionName();
                }
                logger.fine("connectionName: " + connectionName);
                final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
                String text = ((JEditorPane) component).getText();
                Connection conn = null;
                if (preferences.isCheckRunUtplsqlTest()) {
                    conn = DatabaseTools.getConnection(connectionName);
                } else {
                    conn = null;
                }
                final UtplsqlParser parser = new UtplsqlParser(text, conn, owner);
                final int position = ((JEditorPane) component).getCaretPosition();
                final String path = parser.getPathAt(position);
                final PlsqlObject object = parser.getObjectAt(position);
                final List<String> includeObjectList = dependencies(object.getName(), connectionName);
                final CodeCoverageReporter reporter = new CodeCoverageReporter(getPathList(path), includeObjectList, connectionName);
                reporter.showParameterWindow();
            }
        } else if ((view instanceof DBNavigatorWindow)) {
            logger.finer("Code coverage from DB navigator");
            final URL url = getURL(context);
            if (url != null) {
                final String connectionName = URLTools.getConnectionName(url);
                logger.fine(() -> "connectionName: " + connectionName);
                final ArrayList<String> pathList = dedupPathList(getPathList(context));
                logger.fine(() -> "pathlist: " + StringTools.getSimpleCSV(pathList));
                final List<String> includeObjectList = dependencies(context, connectionName);
                logger.finer(() -> "includeObjectList: " + StringTools.getSimpleCSV(includeObjectList));
                final CodeCoverageReporter reporter = new CodeCoverageReporter(pathList, includeObjectList, connectionName);
                logger.finer(() -> "showing code coverage dialog");
                reporter.showParameterWindow();
                logger.finer(() -> "code coverage dialog shown");
            } else {
                logger.warning("url is null");
            }
        }
    }

    public void generateTest(final Context context) {
        final View view = context.getView();
        final Node node = context.getNode();
        final StringBuilder sb = new StringBuilder();
        sb.append("Generate utPLSQL test from view ");
        sb.append(view != null ? view.getClass().getName() : "???");
        sb.append(" and node ");
        sb.append(node != null ? node.getClass().getName() : "???");
        sb.append(".");
        logger.finer(() -> sb.toString());
        if (view instanceof Editor) {
            final Component component = ((Editor) view).getDefaultFocusComponent();
            if (component instanceof JEditorPane) {
                String connectionName = null;
                if (node instanceof DatabaseSourceNode) {
                    connectionName = ((DatabaseSourceNode) node).getConnectionName();
                } else if (view instanceof Worksheet) {
                    connectionName = ((Worksheet) view).getConnectionName();
                }
                if (connectionName != null) {
                    if (Connections.getInstance().isConnectionOpen(connectionName)) {
                        final GenContext genContext = new GenContext();
                        genContext.setConn(DatabaseTools.getConnection(connectionName));
                        String text = ((JEditorPane) component).getText();
                        final UtplsqlParser parser = new UtplsqlParser(text);
                        final int position = ((JEditorPane) component).getCaretPosition();
                        final PlsqlObject obj = parser.getObjectAt(position);
                        if (obj != null) {
                            genContext.setObjectType(obj.getType().toUpperCase());
                            genContext.setObjectName(obj.getName().toUpperCase());
                            final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
                            populateGenContext(genContext, preferences);
                            final TestTemplate testTemplate = new TestTemplate(genContext);
                            final String code = testTemplate.generate().toString();
                            UtplsqlWorksheetRunner.openWithCode(code, connectionName);
                        }
                    }
                }
            }
        } else {
            if (view instanceof DBNavigatorWindow) {
                final URL url = getURL(context);
                if (url != null) {
                    final String connectionName = URLTools.getConnectionName(url);
                    GenContext genContext = getGenContext(context);
                    final TestTemplate testTemplate = new TestTemplate(genContext);
                    final String code = testTemplate.generate().toString();
                    UtplsqlWorksheetRunner.openWithCode(code, connectionName);
                }
            }
        }
    }
}
