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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.utplsql.sqldev.dal.RealtimeReporterDao;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.exception.GenericDatabaseAccessException;
import org.utplsql.sqldev.exception.GenericRuntimeException;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.FileTools;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.runner.UtplsqlRunner;
import org.utplsql.sqldev.ui.coverage.CodeCoverageReporterDialog;

import oracle.ide.config.Preferences;

public class CodeCoverageReporter {
    private static final Logger logger = Logger.getLogger(CodeCoverageReporter.class.getName());
    private static final String ASSETS_PATH = "coverage/assets/";

    private String connectionName;
    private Connection conn;
    private final List<String> pathList;
    private final List<String> includeObjectList;
    private CodeCoverageReporterDialog frame;
    private String schemas;
    private String includeObjects;
    private String excludeObjects;
    private Path assetDir;

    public CodeCoverageReporter(final List<String> pathList, final List<String> includeObjectList,
            final String connectionName) {
        this.pathList = pathList;
        this.includeObjectList = includeObjectList;
        setDefaultSchema();
        setConnection(connectionName);
        setAssetDir();
    }

    // constructor for testing purposes only
    public CodeCoverageReporter(final List<String> pathList, final List<String> includeObjectList,
            final Connection conn) {
        this.pathList = pathList;
        this.includeObjectList = includeObjectList;
        this.conn = conn;
        setDefaultSchema();
        setAssetDir();
    }

    private void setConnection(final String connectionName) {
        if (connectionName == null) {
            final String msg = "Cannot initialize a CodeCoverageReporter without a ConnectionName";
            logger.severe(() -> msg);
            throw new NullPointerException();
        } else {
            // must be closed manually
            this.connectionName = connectionName;
            this.conn = DatabaseTools.getConnection(connectionName);
        }
    }
    
    private void setDefaultSchema() {
        if (includeObjectList != null && !includeObjectList.isEmpty()) {
            // use the owner with the most hits in includeObjectList
            HashMap<String, Integer> owners = new HashMap<>();
            for (String entry : includeObjectList) {
                String[] obj = entry.toUpperCase().split("\\.");
                if (obj.length == 2) {
                    // only if objectOwner and objectName are available
                    Integer count = owners.get(obj[0]);
                    if (count == null) {
                        count = 1;
                    } else {
                        count++;
                    }
                    owners.put(obj[0], count);
                }
            }
            List<String> sortedOwners = owners.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            schemas = String.join(", ", sortedOwners);
        }
    }

    private void setAssetDir() {
        try {
            assetDir = Files.createTempDirectory("utplsql_assets_");
        } catch (IOException e) {
            throw new GenericRuntimeException("Cannot create temporary directory for code coverage report assets.", e);
        }
        populateCoverageAssets();
    }

    // public for testing purposes only
    public URL getHtmlReportAssetPath() {
        try {
            return Paths.get(assetDir.toString()).toUri().toURL();
        } catch (MalformedURLException e) {
            throw new GenericRuntimeException("Cannot convert code coverage asset path to URL.", e);
        }
    }

    private void copyStreamToFile(InputStream inputStream, Path file) throws IOException {
        file.toFile().mkdirs();
        Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private void populateCoverageAssets() {
        logger.fine(() -> "Copying code coverage report assets to " + assetDir.toString() + "...");
        try {
            final File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            if (file.isFile()) {
                // class loaded from a JAR file
                final JarFile jar = new JarFile(file);
                final List<JarEntry> entries = jar.stream().filter(entry -> !entry.isDirectory() && entry.getName().startsWith(ASSETS_PATH)).collect(Collectors.toList());
                for (JarEntry entry : entries) {
                    Path f = Paths.get(assetDir.toString() + File.separator + entry.getName().substring(ASSETS_PATH.length()));
                    copyStreamToFile(jar.getInputStream(entry), f);
                }
                jar.close();
            } else {
                // class loaded from file system (IDE or during test/build)
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + ASSETS_PATH + "**");
                for (Resource resource : resources) {
                    if (Objects.requireNonNull(resource.getFilename()).contains(".")) {
                        // process files but not directories, assume that directories do not contain a period
                        String path = resource.getURL().getPath();
                        Path f = Paths.get(assetDir.toString() + File.separator + path.substring(path.lastIndexOf(ASSETS_PATH) + ASSETS_PATH.length()));
                        copyStreamToFile(resource.getInputStream(), f);
                    }
                }
            }
        } catch (IOException e) {
            throw new GenericRuntimeException("Error while copying coverage report assets to temporary directory.", e);
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
            final RealtimeReporterDao dao = new RealtimeReporterDao(conn);
            PreferenceModel preferences;
            try {
                preferences = PreferenceModel.getInstance(Preferences.getPreferences());
            } catch (NoClassDefFoundError | ExceptionInInitializerError error) {
                // not running in SQL Developer (in tests)
                preferences = PreferenceModel.getInstance(null);
            }
            if (preferences.isUseRealtimeReporter() && dao.isSupported() && connectionName != null) {
                runCodeCoverageWithRealtimeReporter();
            } else {
                runCodeCoverageStandalone();
            }
        } finally {
            if (frame != null) {
                frame.exit();
            }
        }
    }
    
    private void runCodeCoverageWithRealtimeReporter() {
        final UtplsqlRunner runner = new UtplsqlRunner(pathList, toStringList(schemas), toStringList(includeObjects),
                toStringList(excludeObjects), getHtmlReportAssetPath(), connectionName);
        runner.runTestAsync();
    }
    
    private void runCodeCoverageStandalone() {
        Connection coverageConn = null;
        try {
            coverageConn = conn != null ? conn : DatabaseTools.cloneConnection(connectionName);
            final UtplsqlDao dao = new UtplsqlDao(coverageConn);
            final String html = dao.htmlCodeCoverage(pathList, toStringList(schemas),
                    toStringList(includeObjects), toStringList(excludeObjects), getHtmlReportAssetPath());
            openInBrowser(html);
        } finally {
            try {
                if (coverageConn != null && conn == null) {
                    // close only if connection has been cloned
                    DatabaseTools.closeConnection(coverageConn);
                }
            } catch (GenericDatabaseAccessException e) {
                // ignore
            }
        }
    }
    
    public static void openInBrowser(String html) {
        try {
            final File file = File.createTempFile("utplsql_", ".html");
            logger.fine(() -> "Writing result to " + file + "...");
            FileTools.writeFile(file.toPath(), Arrays.asList(html.split(System.lineSeparator())), StandardCharsets.UTF_8);
            final URL url = file.toURI().toURL();
            logger.fine(() -> "Opening " + url.toExternalForm() + " in browser...");
            final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(url.toURI());
                logger.fine(() -> url.toExternalForm() + " opened in browser.");
            } else {
                logger.severe(
                        () -> "Could not launch " + file + " in browser. No default browser defined on this system.");
            }
        } catch (Exception e) {
            final String msg = "Error while opening code coverage HTML report in browser.";
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
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
        if (includeObjectList == null) {
            return new ArrayList<>();
        } else {
            return includeObjectList;
        }
    }

    public void setSchemas(final String schemas) {
        this.schemas = schemas;
    }

    public String getSchemas() {
        return schemas;
    }

    public void setIncludeObjects(final String includeObjects) {
        this.includeObjects = includeObjects;
    }

    public void setExcludeObjects(final String excludeObjects) {
        this.excludeObjects = excludeObjects;
    }

    public Thread runAsync() {
        final Thread thread = new Thread(this::run);
        thread.setName("code coverage reporter");
        thread.start();
        return thread;
    }

    public void showParameterWindow() {
        CodeCoverageReporterDialog.createAndShow(this);
    }
}
