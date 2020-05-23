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
package org.utplsql.sqldev.coverage;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.ui.coverage.CodeCoverageReporterDialog;

import oracle.dbtools.raptor.utils.Connections;
import oracle.javatools.db.DBException;
import oracle.jdeveloper.db.ConnectionException;

public class CodeCoverageReporter {
    private static final Logger logger = Logger.getLogger(CodeCoverageReporter.class.getName());

    private Connection conn;
    private List<String> pathList;
    private List<String> includeObjectList;
    private CodeCoverageReporterDialog frame;
    private String schemas;
    private String includeObjects;
    private String excludeObjects;

    public CodeCoverageReporter(final List<String> pathList, final List<String> includeObjectList,
            final String connectionName) {
        this.pathList = pathList;
        this.includeObjectList = includeObjectList;
        setConnection(connectionName);
    }

    public CodeCoverageReporter(final List<String> pathList, final List<String> includeObjectList,
            final Connection conn) {
        this.pathList = pathList;
        this.includeObjectList = includeObjectList;
        this.conn = conn;
    }

    private void setConnection(final String connectionName) {
        if (connectionName == null) {
            final String msg = "Cannot initialize a CodeCoverageReporter without a ConnectionName";
            logger.severe(() -> msg);
            throw new RuntimeException(msg);
        } else {
            try {
                // must be closed manually
                conn = Connections.getInstance()
                        .cloneConnection(Connections.getInstance().getConnection(connectionName));
            } catch (ConnectionException e) {
                logger.severe(() -> "ConnectionException while setting connection: " + e.getMessage());
                throw new RuntimeException(e);
            } catch (DBException e) {
                logger.severe(() -> "DBException while setting connection: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private ArrayList<String> toStringList(final String s) {
        final ArrayList<String> list = new ArrayList<>();
        if (s != null && !s.isEmpty()) {
            for (final String item : s.split(",")) {
                if (!item.isEmpty()) {
                    list.add(item.trim());
                }
            }
        }
        return list;
    }

    private void run() {
        logger.fine(() -> "Running code coverage reporter for " + pathList + "...");
        try {
            final UtplsqlDao dal = new UtplsqlDao(conn);
            final String content = dal.htmlCodeCoverage(pathList, toStringList(schemas),
                    toStringList(includeObjects), toStringList(excludeObjects));
            final File file = File.createTempFile("utplsql_", ".html");
            logger.fine(() -> "Writing result to " + file + "...");
            Files.write(file.toPath(), Arrays.asList(content.split(System.lineSeparator())), StandardCharsets.UTF_8);
            final URL url = file.toURI().toURL();
            logger.fine(() -> "Opening " + url.toExternalForm() + " in browser...");
            final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE) && url != null) {
                desktop.browse(url.toURI());
                logger.fine(() -> url.toExternalForm() + " opened in browser.");
            } else {
                logger.severe(
                        () -> "Could not launch " + file + "in browser. No default browser defined on this system.");
            }
        } catch (Exception e) {
            logger.severe(() -> "Error while running code coverage: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
            if (frame != null) {
                frame.exit();
            }
        }
    }

    public void setFrame(final CodeCoverageReporterDialog frame) {
        this.frame = frame;
    }

    public CodeCoverageReporterDialog getFrame() {
        return frame;
    }

    public Connection getConnection() {
        return conn;
    }

    public List<String> getPathList() {
        return pathList;
    }

    public List<String> getIncludeObjectList() {
        if ((includeObjectList == null)) {
            return new ArrayList<>();
        } else {
            return includeObjectList;
        }
    }

    public void setSchemas(final String schemas) {
        this.schemas = schemas;
    }

    public void setIncludeObjects(final String includeObjects) {
        this.includeObjects = includeObjects;
    }

    public void setExcludeObjects(final String excludeObjects) {
        this.excludeObjects = excludeObjects;
    }

    public Thread runAsync() {
        final Thread thread = new Thread(() -> {
            run();
        });
        thread.setName("code coverage reporter");
        thread.start();
        return thread;
    }

    public void showParameterWindow() {
        CodeCoverageReporterDialog.createAndShow(this);
    }
}
