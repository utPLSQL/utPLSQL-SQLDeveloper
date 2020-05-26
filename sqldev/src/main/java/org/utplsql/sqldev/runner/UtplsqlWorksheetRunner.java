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
package org.utplsql.sqldev.runner;

import java.awt.Container;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JSplitPane;

import org.utplsql.sqldev.exception.GenericDatabaseAccessException;
import org.utplsql.sqldev.model.StringTools;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.resources.UtplsqlResources;

import oracle.dbtools.raptor.utils.Connections;
import oracle.dbtools.worksheet.WorksheetResultPanel;
import oracle.dbtools.worksheet.editor.OpenWorksheetWizard;
import oracle.dbtools.worksheet.editor.Worksheet;
import oracle.dbtools.worksheet.utils.WorksheetUtil;
import oracle.ide.Ide;
import oracle.ide.config.Preferences;
import oracle.ide.controller.IdeAction;
import oracle.jdeveloper.db.ConnectionException;

public class UtplsqlWorksheetRunner {
    private static final Logger logger = Logger.getLogger(UtplsqlWorksheetRunner.class.getName());

    private PreferenceModel preferences;
    private String connectionName;
    private List<String> pathList;

    public UtplsqlWorksheetRunner(final List<String> pathList, final String connectionName) {
        this.pathList = pathList;
        preferences = PreferenceModel.getInstance(Preferences.getPreferences());
        setConnection(connectionName);
    }

    private void setConnection(final String connectionName) {
        if (connectionName != null && preferences.isUnsharedWorksheet()) {
            // fix for issue #47 - private connections are not closed in SQLDev >= 17.4.0
            try {
                this.connectionName = Connections.getInstance().createTemporaryConnection(connectionName);
            } catch (Throwable t) {
                // private connection is closed when worksheet is closed in SQLDev < 17.4.0
                try {
                    this.connectionName = Connections.getInstance().createPrivateConnection(connectionName);
                } catch (ConnectionException e) {
                    final String msg = "failed to create private connection due to " + e.getMessage();
                    logger.severe(() -> msg);
                    throw new GenericDatabaseAccessException(msg, e);
                }
            }
        } else {
            this.connectionName = connectionName;
        }
    }

    private CharSequence getCode() {
        StringBuilder sb = new StringBuilder();
        if (preferences.isResetPackage()) {
            sb.append("EXECUTE dbms_session.reset_package;\n");
        }
        sb.append("SET SERVEROUTPUT ON SIZE UNLIMITED\n");
        if (preferences.isClearScreen()) {
            sb.append("CLEAR SCREEN\n");
        }
        final List<String> paths = pathList;
        if (paths.size() == 1) {
            sb.append("EXECUTE ut.run('");
            sb.append(paths.get(0));
            sb.append("');\n");
        } else {
            // we want a horizontal dense output because we resize the worksheet to fit the command in common cases
            sb.append("EXECUTE ut.run(ut_varchar2_list(");
            sb.append(StringTools.getCSV(pathList, "").replace("\n",""));
            sb.append("));\n");
        }
        return sb;
    }

    private Worksheet openWorksheet() {
        final Worksheet worksheet = (Worksheet) OpenWorksheetWizard.openNewTempWorksheet(connectionName,
                getCode().toString());
        if (connectionName == null) {
            worksheet.setComboConnection(null);
        }
        WorksheetUtil.setWorksheetTabName(worksheet.getContext().getNode().getURL(),
                UtplsqlResources.getString("WORKSHEET_TITLE"));
        worksheet.getContext().getNode().markDirty(false);
        return worksheet;
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }        
    }

    private void resizeResultPanel(final Worksheet worksheet) {
        sleep(200);
        Container splitPane = null;
        WorksheetResultPanel selectedResultPanel = worksheet.getSelectedResultPanel();
        if (selectedResultPanel != null && selectedResultPanel.getGUI() != null && selectedResultPanel.getGUI().getParent() != null
                && selectedResultPanel.getGUI().getParent().getParent() != null && selectedResultPanel.getGUI().getParent().getParent().getParent() != null) {
            splitPane = selectedResultPanel.getGUI().getParent().getParent().getParent();
        }
        if ((splitPane instanceof JSplitPane)) {
            ((JSplitPane) splitPane).setDividerLocation(0.15);
        } else {
            final String msg = "Could not adjust size of worksheet. Expected JSplitPane but got "
                    + (splitPane != null ? splitPane.getClass().getName() : "null") + ".";
            logger.severe(msg);
        }
    }

    private void runScript(final Worksheet worksheet) {
        if (preferences.isAutoExecute()) {
            sleep(100);
            final IdeAction action = ((IdeAction) Ide.getIdeActionMap().get(Ide.findCmdID("Worksheet.RunScript")));
            if ((action != null)) {
                try {
                    action.performAction(worksheet.getContext());
                } catch (Exception e) {
                    logger.severe(() -> "Could not run script due to " + (e != null ? e.getMessage() : "???") + ".");
                }
                resizeResultPanel(worksheet);
            }
        }
    }

    private void runTest() {
        final Worksheet worksheet = openWorksheet();
        runScript(worksheet);
        logger.fine(() -> "utPLSQL test called for " + pathList + " in " + connectionName + ".");
    }

    public void runTestAsync() {
        final Thread thread = new Thread(() -> runTest());
        thread.setName("utPLSQL run test");
        thread.start();
    }

    public static void openWithCode(final String code, final String connectionName) {
        final Worksheet worksheet = (Worksheet) OpenWorksheetWizard.openNewTempWorksheet(connectionName, code);
        if (connectionName == null) {
            worksheet.setComboConnection(null);
        }
        WorksheetUtil.setWorksheetTabName(worksheet.getContext().getNode().getURL(),
                UtplsqlResources.getString("WORKSHEET_TITLE"));
    }
}
