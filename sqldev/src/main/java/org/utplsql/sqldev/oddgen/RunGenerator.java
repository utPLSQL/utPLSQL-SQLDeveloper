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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.oddgen.sqldev.generators.OddgenGenerator2;
import org.oddgen.sqldev.generators.model.Node;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.exception.GenericDatabaseAccessException;
import org.utplsql.sqldev.model.StringTools;
import org.utplsql.sqldev.model.preference.PreferenceModel;
import org.utplsql.sqldev.resources.UtplsqlResources;

import oracle.ide.config.Preferences;

public class RunGenerator implements OddgenGenerator2 {
    private static final Logger logger = Logger.getLogger(RunGenerator.class.getName());

    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String RESET_PACKAGE = UtplsqlResources.getString("PREF_RESET_PACKAGE_LABEL");
    public static final String CLEAR_SCREEN = UtplsqlResources.getString("PREF_CLEAR_SCREEN_LABEL");
    public static final String INDENT_SPACES = UtplsqlResources.getString("PREF_INDENT_SPACES_LABEL");

    // oddgen node cache
    private List<Node> runnables = null;

    @Override
    public boolean isSupported(final Connection conn) {
        try {
            boolean ret = false;
            if (conn != null && conn.getMetaData().getDatabaseProductName().startsWith("Oracle")
                    && (conn.getMetaData().getDatabaseMajorVersion() == 11
                            && conn.getMetaData().getDatabaseMinorVersion() >= 2
                            || conn.getMetaData().getDatabaseMajorVersion() > 11)) {
                ret = true;
            }
            return ret;
        } catch (SQLException e) {
            final String msg = "SQLException during connection check due to " + e.getMessage();
            logger.severe(() -> msg);
            throw new GenericDatabaseAccessException(msg, e);
        }
    }

    @Override
    public String getName(final Connection conn) {
        return "Run test";
    }

    @Override
    public String getDescription(final Connection conn) {
        return "Runs utPLSQL test packages in the current user.";
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
        // oddgen asks for children for each parent node, regardless of load strategy (eager/lazy)
        // oddgen does not know about the load strategy, hence caching is the responsibility of the generator
        if (runnables == null) {
            final PreferenceModel preferences = PreferenceModel.getInstance(Preferences.getPreferences());
            final LinkedHashMap<String, String> params = new LinkedHashMap<>();
            params.put(RESET_PACKAGE, preferences.isResetPackage() ? "YES" : "NO");
            params.put(CLEAR_SCREEN, preferences.isClearScreen() ? "YES" : "NO");
            params.put(INDENT_SPACES, String.valueOf(preferences.getIndentSpaces()));
            final UtplsqlDao dao = new UtplsqlDao(conn);
            // load node tree eagerly (all nodes in one go)
            runnables = dao.runnables();
            for (final Node node : runnables) {
                node.setParams(params);
            }
        }
        return runnables;
    }

    @Override
    public HashMap<String, List<String>> getLov(final Connection conn, final LinkedHashMap<String, String> params, final List<Node> nodes) {
        final HashMap<String, List<String>> lov = new HashMap<>();
        lov.put(RESET_PACKAGE, Arrays.asList(YES, NO));
        lov.put(CLEAR_SCREEN, Arrays.asList(YES, NO));
        lov.put(INDENT_SPACES, Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"));
        return lov;
    }

    @Override
    public HashMap<String, Boolean> getParamStates(final Connection conn, final LinkedHashMap<String, String> params, final List<Node> nodes) {
        return new HashMap<>();
    }

    private String getPath(final Node node, final Connection conn) {
        if ("SUITE".equals(node.getId()) || "SUITEPATH".equals(node.getId())) {
            try {
                return conn.getMetaData().getUserName();
            } catch (SQLException e) {
                final String msg = "SQLException during getUserName() due to " + e.getMessage();
                logger.severe(() -> msg);
                throw new GenericDatabaseAccessException(msg, e);
            }
        } else {
            return node.getId();
        }
    }

    private String replaceTabsWithSpaces(final CharSequence input, final int indentSpaces) {
        final String spaces = String.format((("%1$" + Integer.valueOf(indentSpaces)) + "s"), "");
        return input.toString().replace("\t", spaces);
    }

    public ArrayList<Node> dedup(final List<Node> nodes) {
        final HashSet<String> set = new HashSet<>();
        for (final Node node : nodes) {
            set.add(node.getId());
        }
        final ArrayList<Node> ret = new ArrayList<>();
        for (final Node node : nodes) {
            if (!set.contains(node.getParentId())) {
                ret.add(node);
            }
        }
        return ret;
    }

    @Override
    public String generateProlog(final Connection conn, final List<Node> nodes) {
        final ArrayList<Node> dedupNodes = dedup(nodes);
        final LinkedHashMap<String, String> params = dedupNodes.get(0).getParams();
        final StringBuilder sb = new StringBuilder();
        if ("YES".equals(params.get(RESET_PACKAGE))) {
            sb.append("EXECUTE dbms_session.reset_package;\n");
        }
        sb.append("SET SERVEROUTPUT ON SIZE UNLIMITED\n");
        if ("YES".equals(params.get(CLEAR_SCREEN))) {
            sb.append("CLEAR SCREEN\n");
        }
        if (dedupNodes.size() == 1) {
            sb.append("EXECUTE ut.run('");
            sb.append(getPath(dedupNodes.get(0), conn));
            sb.append("');\n");
        } else {
            final List<String> paths = nodes.stream().map(node -> getPath(node, conn)).collect(Collectors.toList());
            sb.append("BEGIN\n");
            sb.append("\tut.run(\n");
            sb.append("\t\tut_varchar2_list(\n");
            sb.append(StringTools.getCSV(paths, "\t\t\t"));
            sb.append("\t\t)\n");
            sb.append("\t);\n");
            sb.append("END;\n");
            sb.append("/\n");
        }
        final String ret = sb.toString();
        return replaceTabsWithSpaces(ret, (Integer.valueOf(params.get(INDENT_SPACES))));
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
