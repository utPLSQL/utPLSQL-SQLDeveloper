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
package org.utplsql.sqldev.dal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oddgen.sqldev.generators.model.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.model.StringTools;
import org.utplsql.sqldev.model.ut.Annotation;
import org.utplsql.sqldev.model.ut.OutputLines;

public class UtplsqlDao {
    public static final String UTPLSQL_PACKAGE_NAME = "UT";
    public static final int NOT_INSTALLED = 0;
    public static final int FIRST_VERSION_WITH_INTERNAL_ANNOTATION_API = 3000004;
    public static final int FIRST_VERSION_WITH_ANNOTATION_API = 3001003;
    public static final int FIRST_VERSION_WITHOUT_INTERNAL_API = 3001008;
    public static final int FIRST_VERSION_WITH_HAS_SUITES_API = 3001008;
    public static final int FETCH_ROWS = 100;
    private JdbcTemplate jdbcTemplate;
    // cache fields
    private Boolean cachedDbaViewAccessible;
    private String cachedUtplsqlSchema;
    private String cachedUtPlsqlVersion;

    public UtplsqlDao(final Connection conn) {
        jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(conn, true));
        jdbcTemplate.setFetchSize(FETCH_ROWS);
    }

    /**
     * used for testing purposes only
     */
    public void setUtPlsqlVersion(final String utPlsqlVersion) {
        this.cachedUtPlsqlVersion = utPlsqlVersion;
    }

    /**
     * returns a normalized utPLSQL version in format 9.9.9
     */
    public String normalizedUtPlsqlVersion() {
        final String version = this.getUtPlsqlVersion();
        if (version != null) {
            final Pattern p = Pattern.compile("(\\d+\\.\\d+\\.\\d+)");
            final Matcher m = p.matcher(version);
            if (m.find()) {
                return m.group(0);
            }
        }
        return "0.0.0";
    }

    /**
     * get version as number, e.g. 3001004
     */
    public int normalizedUtPlsqlVersionNumber() {
        final Pattern p = Pattern.compile("(\\d+)");
        final String version = this.normalizedUtPlsqlVersion();
        final Matcher m = p.matcher(version);
        m.find();
        final String major = m.group();
        m.find();
        final String minor = m.group();
        m.find();
        final String bugfix = m.group();
        return Integer.valueOf(major) * 1000000 + Integer.valueOf(minor) * 1000 + Integer.valueOf(bugfix);
    }

    /**
     * gets version of installed utPLSQL
     */
    public String getUtPlsqlVersion() {
        if (cachedUtPlsqlVersion == null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("BEGIN\n");
            sb.append("   ? := ut.version;\n");
            sb.append("END;");
            final String sql = sb.toString();
            try {
                cachedUtPlsqlVersion = jdbcTemplate.execute(sql, new CallableStatementCallback<String>() {
                    @Override
                    public String doInCallableStatement(final CallableStatement cs) throws SQLException {
                        cs.registerOutParameter(1, Types.VARCHAR);
                        cs.execute();
                        return cs.getString(1);
                    }
                });
            } catch (DataAccessException e) {
                // ignore error
            }
        }
        return cachedUtPlsqlVersion;
    }

    public boolean isDbaViewAccessible() {
        if (cachedDbaViewAccessible == null) {
            try {
                final StringBuilder sb = new StringBuilder();
                sb.append("SELECT 1 AS dummy\n");
                sb.append("  FROM dba_objects\n");
                sb.append(" WHERE 1=2\n");
                sb.append("UNION ALL\n");
                sb.append("SELECT 1\n");
                sb.append("  FROM dba_synonyms\n");
                sb.append(" WHERE 1=2\n");
                sb.append("UNION ALL\n");
                sb.append("SELECT 1\n");
                sb.append("  FROM dba_dependencies\n");
                sb.append(" WHERE 1=2\n");
                final String sql = sb.toString();
                jdbcTemplate.execute(sql);
                cachedDbaViewAccessible = true;
            } catch (DataAccessException e) {
                cachedDbaViewAccessible = false;
            }
        }
        return cachedDbaViewAccessible.booleanValue();
    }
    
    public String getDbaView(String viewName) {
        StringBuilder sb = new StringBuilder();
        if (isDbaViewAccessible()) {
            sb.append("dba");
        } else {
            sb.append("all");
        }
        sb.append("_");
        sb.append(viewName);
        return sb.toString();
    }

    /**
     * Gets the schema name of the utPLSQL installation.
     * 
     * @return utPLSQL schema or null if no utPLSQL is not installed
     * @throws DataAccessException
     *             if there is a problem
     */
    public String getUtplsqlSchema() {
        if (cachedUtplsqlSchema == null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("SELECT table_owner\n");
            sb.append("  FROM ");
            sb.append(getDbaView("synonyms\n"));
            sb.append(" WHERE owner = 'PUBLIC'\n");
            sb.append("   AND synonym_name = '");
            sb.append(UtplsqlDao.UTPLSQL_PACKAGE_NAME);
            sb.append("'\n");
            sb.append("   AND table_name = '");
            sb.append(UtplsqlDao.UTPLSQL_PACKAGE_NAME);
            sb.append("'");
            final String sql = sb.toString();
            try {
                final String schema = jdbcTemplate.queryForObject(sql, String.class);
                cachedUtplsqlSchema = schema;
            } catch (EmptyResultDataAccessException e) {
                cachedUtplsqlSchema = null;
            }
        }
        return cachedUtplsqlSchema;
    }

    /**
     * Checks if the package ut_annotation_manager is installed. This package has
     * been introduced with utPLSQL 3.0.4. This version is a prerequisite to
     * identify utPLSQL unit test procedures.
     * 
     * @return true if ut_annotation_manager package has been found
     * @throws DataAccessException
     *             if there is a problem
     */
    public boolean isUtAnnotationManagerInstalled() {
        return normalizedUtPlsqlVersionNumber() >= UtplsqlDao.FIRST_VERSION_WITH_INTERNAL_ANNOTATION_API;
    }

    /**
     * Checks if utPLSQL tests exist
     * 
     * @param owner
     *            schema name, mandatory, case-insensitive
     * @param objectName
     *            name of the package or package body, optional, case-insensitive
     * @param subobjectName
     *            name of the procedure, optional, case-insensitive
     * @return true if at least one test has been found
     * @throws DataAccessException
     *             if a utPLSQL version less than 3.0.4 is installed or if there are
     *             other problems
     */
    public boolean containsUtplsqlTest(final String owner, final String objectName, final String subobjectName) {
        try {
            if (normalizedUtPlsqlVersionNumber() >= UtplsqlDao.FIRST_VERSION_WITH_HAS_SUITES_API && objectName != null
                    && subobjectName != null) {
                // use faster check function available since v3.1.3 (reliable in v3.1.8)
                final StringBuilder sb = new StringBuilder();
                sb.append("DECLARE\n");
                sb.append("   l_return VARCHAR2(1) := '0';\n");
                sb.append("BEGIN\n");
                sb.append("   IF ut_runner.is_test(?, ?, ?) THEN\n");
                sb.append("      l_return := '1';\n");
                sb.append("   END IF;\n");
                sb.append("   ? := l_return;\n");
                sb.append("END;");
                final String sql = sb.toString();
                return jdbcTemplate.execute(sql, new CallableStatementCallback<Boolean>() {
                    @Override
                    public Boolean doInCallableStatement(final CallableStatement cs) throws SQLException {
                        cs.setString(1, owner);
                        cs.setString(2, objectName);
                        cs.setString(3, subobjectName);
                        cs.registerOutParameter(4, Types.VARCHAR);
                        cs.execute();
                        final String ret = cs.getString(4);
                        return "1".equals(ret);
                    }
                });
            } else if (normalizedUtPlsqlVersionNumber() >= FIRST_VERSION_WITH_ANNOTATION_API) {
                // using API available since 3.1.3, can handle nulls in objectName and subobjectName
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT count(*)\n");
                sb.append("  FROM TABLE(ut_runner.get_suites_info(upper(?), upper(?)))\n");
                sb.append(" WHERE item_type IN ('UT_TEST', 'UT_SUITE')\n");
                sb.append("   AND (item_name = upper(?) or ? IS NULL)\n");
                final String sql = sb.toString();
                final Object[] binds = new Object[] {owner, objectName, subobjectName, subobjectName};
                final Integer found = jdbcTemplate.queryForObject(sql, Integer.class, binds);
                return found > 0;
            } else {
                // using internal API (deprecated, not accessible in latest version)
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT count(\n");
                sb.append("          CASE\n");
                sb.append("             WHEN a.name = 'test'\n");
                sb.append("                  AND (upper(a.subobject_name) = upper(?) OR ? IS NULL)\n");
                sb.append("             THEN\n");
                sb.append("                1\n");
                sb.append("             ELSE\n");
                sb.append("               NULL\n");
                sb.append("          END\n");
                sb.append("       )\n");
                sb.append("  FROM TABLE(");
                sb.append(getUtplsqlSchema());
                sb.append(".ut_annotation_manager.get_annotated_objects(upper(?), 'PACKAGE')) o\n");
                sb.append(" CROSS JOIN TABLE(o.annotations) a\n");
                sb.append(" WHERE (o.object_name = upper(?) OR ? IS NULL)\n");
                sb.append("   AND a.name IN ('test', 'suite')\n");
                sb.append("HAVING count(\n");
                sb.append("          CASE\n");
                sb.append("             WHEN a.name = 'suite' THEN\n");
                sb.append("                1\n");
                sb.append("             ELSE\n");
                sb.append("                NULL\n");
                sb.append("          END\n");
                sb.append("       ) > 0");
                final String sql = sb.toString();
                final Object[] binds = new Object[] {subobjectName, subobjectName, owner, objectName, objectName};
                final Integer found = jdbcTemplate.queryForObject(sql, Integer.class, binds);
                return found > 0;
            }
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean containsUtplsqlTest(final String owner) {
        if (normalizedUtPlsqlVersionNumber() >= org.utplsql.sqldev.dal.UtplsqlDao.FIRST_VERSION_WITH_HAS_SUITES_API) {
            // use faster check function available since v3.1.3 (reliable in v3.1.8)
            StringBuilder sb = new StringBuilder();
            sb.append("DECLARE\n");
            sb.append("   l_return VARCHAR2(1) := '0';\n");
            sb.append("BEGIN\n");
            sb.append("   IF ut_runner.has_suites(?) THEN\n");
            sb.append("      l_return := '1';\n");
            sb.append("   END IF;\n");
            sb.append("   ? := l_return;\n");
            sb.append("END;");
            final String sql = sb.toString();
            return jdbcTemplate.execute(sql, new CallableStatementCallback<Boolean>() {
                @Override
                public Boolean doInCallableStatement(final CallableStatement cs)
                        throws SQLException {
                    cs.setString(1, owner);
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.execute();
                    final String ret = cs.getString(2);
                    return "1".equals(ret);
                }
            });
        } else {
            return containsUtplsqlTest(owner, null, null);
        }
    }

    public boolean containsUtplsqlTest(final String owner, final String objectName) {
        if (normalizedUtPlsqlVersionNumber() >= org.utplsql.sqldev.dal.UtplsqlDao.FIRST_VERSION_WITH_HAS_SUITES_API) {
            StringBuilder sb = new StringBuilder();
            sb.append("DECLARE\n");
            sb.append("   l_return VARCHAR2(1) := '0';\n");
            sb.append("BEGIN\n");
            sb.append("   IF ut_runner.is_suite(?, ?) THEN\n");
            sb.append("      l_return := '1';\n");
            sb.append("   END IF;\n");
            sb.append("   ? := l_return;\n");
            sb.append("END;");
            final String sql = sb.toString();
            return jdbcTemplate.execute(sql, new CallableStatementCallback<Boolean>() {
                @Override
                public Boolean doInCallableStatement(final CallableStatement cs)
                        throws SQLException {
                    cs.setString(1, owner);
                    cs.setString(2, objectName);
                    cs.registerOutParameter(3, Types.VARCHAR);
                    cs.execute();
                    final String ret = cs.getString(3);
                    return "1".equals(ret);
                }
            });
        } else {
            return containsUtplsqlTest(owner, objectName, null);
        }
    }

    /**
     * Gets a list of utPLSQL annotations for a given PL/SQL package specification
     * 
     * @param owner
     *            schema name, mandatory, case-insensitive
     * @param objectName
     *            name of the package or package body, optional, case-insensitive
     * @return list of Annotation with name 'suite' or 'test'
     * @throws DataAccessException
     *             if a utPLSQL version less than 3.0.4 is installed or if there are
     *             other problems
     */
    public List<Annotation> annotations(final String owner, final String objectName) {
        StringBuilder sb = new StringBuilder();
        if (normalizedUtPlsqlVersionNumber() >= FIRST_VERSION_WITH_ANNOTATION_API) {
            sb.append("SELECT object_owner,\n");
            sb.append("       object_name,\n");
            sb.append("       lower(substr(item_type, 4)) AS name,\n");
            sb.append("       item_name as subobject_name\n");
            sb.append("  FROM TABLE(ut_runner.get_suites_info(upper(?), upper(?)))");
        } else {
            sb.append("SELECT o.object_owner,\n");
            sb.append("       o.object_name,\n");
            sb.append("       a.name,\n");
            sb.append("       a.text,\n");
            sb.append("       coalesce(upper(a.subobject_name), o.object_name) AS subobject_name\n");
            sb.append("  FROM TABLE(");
            sb.append(getUtplsqlSchema());
            sb.append(".ut_annotation_manager.get_annotated_objects(upper(?), 'PACKAGE')) o\n");
            sb.append(" CROSS JOIN TABLE(o.annotations) a\n");
            sb.append(" WHERE o.object_name = upper(?)");
        }
        final String sql = sb.toString();
        final BeanPropertyRowMapper<Annotation> rowMapper = new BeanPropertyRowMapper<>(Annotation.class);
        final Object[] binds = new Object[] {owner, objectName};
        return jdbcTemplate.query(sql, rowMapper, binds);
    }

    /**
     * Gets a list of public units in the object type
     * 
     * @param objectType
     *            expected object types are PACKAGE, TYPE, FUNCTION, PROCEDURE
     * @param objectName
     *            name of the object
     * @return list of the public units in the object type
     * @throws DataAccessException
     *             if there is a problem
     */
    public List<String> units(final String objectType, final String objectName) {
        if ("PACKAGE".equals(objectType) || "TYPE".equals(objectType)) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT procedure_name\n");
            sb.append("  FROM user_procedures\n");
            sb.append(" WHERE object_type = ?\n");
            sb.append("   AND object_name = ?\n");
            sb.append("   AND procedure_name IS NOT NULL\n");
            sb.append(" GROUP BY procedure_name\n");
            sb.append(" ORDER BY min(subprogram_id)");
            final String sql = sb.toString();
            final Object[] binds = new Object[] {objectType, objectName};
            return jdbcTemplate.queryForList(sql, String.class, binds);
        } else {
            return Arrays.asList(objectName);
        }
    }

    /**
     * Gets a list of oddgen's nodes as candidates to create utPLSQL test packages.
     * Candidates are packages, types, functions and procedures in the current user.
     * 
     * This functions must be called from an oddgen generator only, since the Node
     * is not defined in the utPLSQL extension.
     * 
     * @param objectType
     *            expected object types are PACKAGE, TYPE, FUNCTION, PROCEDURE
     * @return list of the oddgen nodes for the requested object type
     * @throws DataAccessException
     *             if there is a problem
     */
    public List<Node> testables(final String objectType) {
        StringBuilder sb = new StringBuilder();
        if ("PACKAGE".equals(objectType)) {
            if (normalizedUtPlsqlVersionNumber() >= FIRST_VERSION_WITH_ANNOTATION_API) {
                // using API available since 3.1.3
                sb.append("SELECT DISTINCT\n");
                sb.append("       object_type || '.' || object_name AS id,\n");
                sb.append("       object_type AS parent_id,\n");
                sb.append("       1 AS leaf,\n");
                sb.append("       1 AS generatable,\n");
                sb.append("       1 AS multiselectable\n");
                sb.append("  FROM user_procedures\n");
                sb.append(" WHERE object_type = ?\n");
                sb.append("   AND procedure_name IS NOT NULL\n");
                sb.append("   AND object_name NOT IN (\n");
                sb.append("          SELECT object_name\n");
                sb.append("            FROM TABLE(ut_runner.get_suites_info(USER))\n");
                sb.append("           WHERE item_type = 'UT_SUITE'\n");
                sb.append("       )");
            } else {
                // using internal API (deprecated, not accessible in latest version)
                sb.append("SELECT DISTINCT\n");
                sb.append("       object_type || '.' || object_name AS id,\n");
                sb.append("       object_type AS parent_id,\n");
                sb.append("       1 AS leaf,\n");
                sb.append("       1 AS generatable,\n");
                sb.append("       1 AS multiselectable\n");
                sb.append("  FROM user_procedures\n");
                sb.append(" WHERE object_type = ?\n");
                sb.append("   AND procedure_name IS NOT NULL\n");
                sb.append("   AND object_name NOT IN (\n");
                sb.append("          SELECT object_name\n");
                sb.append("            FROM TABLE(\n");
                sb.append(getUtplsqlSchema());
                sb.append(".ut_annotation_manager.get_annotated_objects(USER, 'PACKAGE'))\n");
                sb.append("       )");
            }
        } else if ("TYPE".equals(objectType)) {
            sb.append("SELECT DISTINCT\n");
            sb.append("       object_type || '.' || object_name AS id,\n");
            sb.append("       object_type AS parent_id,\n");
            sb.append("       1 AS leaf,\n");
            sb.append("       1 AS generatable,\n");
            sb.append("       1 AS multiselectable\n");
            sb.append("  FROM user_procedures\n");
            sb.append(" WHERE object_type = ?\n");
            sb.append("   AND procedure_name IS NOT NULL");
        } else {
            sb.append("SELECT object_type || '.' || object_name AS id,\n");
            sb.append("       object_type AS parent_id,\n");
            sb.append("       1 AS leaf,\n");
            sb.append("       1 AS generatable,\n");
            sb.append("       1 AS multiselectable\n");
            sb.append("  FROM user_objects\n");
            sb.append(" WHERE object_type = ?\n");
            sb.append("   AND generated = 'N'");
        }
        final String sql = sb.toString();
        final Object[] binds = new Object[] {objectType};
        BeanPropertyRowMapper<Node> rowMapper = new BeanPropertyRowMapper<>(Node.class);
        return jdbcTemplate.query(sql, rowMapper, binds);
    }

    /**
     * Gets a list of oddgen's nodes as candidates to run utPLSQL tests.
     * 
     * This functions must be called from an oddgen generator only, since the Node
     * is not defined in the utPLSQL extension.
     * 
     * @return list of oddgen nodes (complete hierarchy loaded eagerly)
     * @throws DataAccessException
     *             if there is a problem
     */
    public List<Node> runnables() {
        StringBuilder sb = new StringBuilder();
        if (normalizedUtPlsqlVersionNumber() >= FIRST_VERSION_WITH_ANNOTATION_API) {
            // using API available since 3.1.3
            sb.append("WITH\n");
            sb.append("   test AS (\n");
            sb.append("      SELECT object_owner,\n");
            sb.append("             object_name,\n");
            sb.append("             path AS suitepath,\n");
            sb.append("             count(\n");
            sb.append("                CASE\n");
            sb.append("                   WHEN item_type = 'UT_TEST' THEN\n");
            sb.append("                      1\n");
            sb.append("                   ELSE\n");
            sb.append("                      NULL\n");
            sb.append("                   END\n");
            sb.append("             ) over (partition by object_owner, object_name) AS test_count,\n");
            sb.append("             item_type,\n");
            sb.append("             item_name,\n");
            sb.append("             item_description\n");
            sb.append("        FROM TABLE(ut_runner.get_suites_info(user))\n");
            sb.append("   ),\n");
            sb.append("   suite_tree AS (\n");
            sb.append("      SELECT null AS parent_id,\n");
            sb.append("             'SUITE' AS id,\n");
            sb.append("             'All Suites' AS name,\n");
            sb.append("             'All utPLSQL test suites' AS description,\n");
            sb.append("             'PACKAGE_FOLDER_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM dual\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT DISTINCT\n");
            sb.append("             'SUITE' AS parent_id,\n");
            sb.append("             object_owner || '.' || object_name AS id,\n");
            sb.append("             object_name AS name,\n");
            sb.append("             null AS description,\n");
            sb.append("             'PACKAGE_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("       WHERE item_type IN ('UT_TEST', 'UT_SUITE')\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT object_owner || '.' || object_name AS parent_id,\n");
            sb.append("             object_owner || '.' || object_name || '.' || item_name AS id,\n");
            sb.append("             item_name AS name,\n");
            sb.append("             item_description AS description,\n");
            sb.append("             'PROCEDURE_ICON' AS iconName,\n");
            sb.append("             'Yes' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("       WHERE item_type = 'UT_TEST'\n");
            sb.append("   ),\n");
            sb.append("   suitepath_tree AS (\n");
            sb.append("      SELECT NULL AS parent_id,\n");
            sb.append("             'SUITEPATH' AS id,\n");
            sb.append("             'All Suitepaths' AS name\n,");
            sb.append("             'All utPLSQL test suitepathes' AS description,\n");
            sb.append("             'FOLDER_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM dual\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT CASE\n");
            sb.append("                WHEN regexp_replace(suitepath,'\\.?\\w+$','') IS NULL THEN\n");
            sb.append("                   'SUITEPATH'\n");
            sb.append("                ELSE\n");
            sb.append("                   object_owner || ':' || regexp_replace(suitepath,'\\.?\\w+$','')\n");
            sb.append("             END AS parent_id,\n");
            sb.append("             object_owner || ':' || suitepath AS id,\n");
            sb.append("             item_name AS name,\n");
            sb.append("             item_description AS description,\n");
            sb.append("             CASE\n");
            sb.append("                WHEN item_type = 'UT_SUITE' AND test_count > 0 THEN\n");
            sb.append("                   'PACKAGE_ICON'\n");
            sb.append("                WHEN item_type = 'UT_TEST' THEN\n");
            sb.append("                   'PROCEDURE_ICON'\n");
            sb.append("               ELSE\n");
            sb.append("                   'FOLDER_ICON'\n");
            sb.append("             END AS iconName,\n");
            sb.append("             CASE item_type\n");
            sb.append("                WHEN 'UT_TEST' THEN\n");
            sb.append("                   'Yes'\n");
            sb.append("                ELSE\n");
            sb.append("                   'No'\n");
            sb.append("             END AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("   ),\n");
            sb.append("   tree AS (\n");
            sb.append("      SELECT parent_id, id, name, description, iconName, leaf, generatable, multiselectable, relevant\n");
            sb.append("       FROM suite_tree\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT parent_id, id, name, description, iconName, leaf, generatable, multiselectable, relevant\n");
            sb.append("       FROM suitepath_tree\n");
            sb.append("   )\n");
            sb.append("SELECT parent_id, id, initcap(name) AS name, description, iconName, leaf, generatable, multiselectable, relevant\n");
            sb.append("  FROM tree");
        } else {
            // using internal API (deprecated, not accessible in latest version)
            sb.append("WITH\n");
            sb.append("   base AS (\n");
            sb.append("      SELECT rownum AS an_id,\n");
            sb.append("             o.object_owner,\n");
            sb.append("             o.object_type,\n");
            sb.append("             o.object_name,\n");
            sb.append("             lower(a.name) AS name,\n");
            sb.append("             a.text,\n");
            sb.append("             a.subobject_name\n");
            sb.append("        FROM table(");
            sb.append(getUtplsqlSchema());
            sb.append(".ut_annotation_manager.get_annotated_objects(user, 'PACKAGE')) o\n");
            sb.append("       CROSS JOIN table(o.annotations) a\n");
            sb.append("       WHERE lower(a.name) in ('suite', 'suitepath', 'endcontext', 'test')\n");
            sb.append("          OR lower(a.name) = 'context' AND regexp_like(text, '(\\w+)(\\.\\w+)*')\n");
            sb.append("   ),\n");
            sb.append("   suite AS (\n");
            sb.append("      SELECT object_owner, object_type, object_name, text AS suite_description\n");
            sb.append("        FROM base\n");
            sb.append("       WHERE name = 'suite'\n");
            sb.append("   ),\n");
            sb.append("   suitepath as (\n");
            sb.append("      SELECT object_owner, object_type, object_name, lower(text) AS suitepath\n");
            sb.append("        FROM base\n");
            sb.append("       WHERE name = 'suitepath'");
            sb.append("   ),\n");
            sb.append("   context_base AS (\n");
            sb.append("      SELECT an_id,\n");
            sb.append("             lead(an_id) over (partition by object_owner, object_type, object_name order by an_id) AS an_id_end,\n");
            sb.append("             object_owner,\n");
            sb.append("             object_type,\n");
            sb.append("             object_name,\n");
            sb.append("             name,\n");
            sb.append("             lead(name) over (partition by object_owner, object_type, object_name order by an_id) AS name_end,\n");
            sb.append("             text AS context\n");
            sb.append("        FROM base\n");
            sb.append("       WHERE name IN ('context', 'endcontext')\n");
            sb.append("   ),\n");
            sb.append("   context AS (\n");
            sb.append("      SELECT an_id, an_id_end, object_owner, object_type, object_name, context\n");
            sb.append("        FROM context_base\n");
            sb.append("       WHERE name = 'context'\n");
            sb.append("         AND name_end = 'endcontext'\n");
            sb.append("   ),\n");
            sb.append("   test AS (\n");
            sb.append("      SELECT b.an_id,\n");
            sb.append("             b.object_owner,\n");
            sb.append("             b.object_type,\n");
            sb.append("             b.object_name,\n");
            sb.append("             p.suitepath,\n");
            sb.append("             c.context,\n");
            sb.append("             b.subobject_name,\n");
            sb.append("             b.text AS test_description\n");
            sb.append("        FROM base b\n");
            sb.append("        LEFT JOIN suitepath p\n");
            sb.append("          ON p.object_owner = b.object_owner\n");
            sb.append("             AND p.object_type = b.object_type\n");
            sb.append("             AND p.object_name = b.object_name\n");
            sb.append("        LEFT JOIN context c\n");
            sb.append("          ON c.object_owner = b.object_owner\n");
            sb.append("             AND c.object_type = b.object_type\n");
            sb.append("             AND c.object_name = b.object_name\n");
            sb.append("             AND b.an_id BETWEEN c.an_id AND c.an_id_end\n");
            sb.append("       WHERE name = 'test'\n");
            sb.append("         AND (b.object_owner, b.object_type, b.object_name) IN (\n");
            sb.append("                SELECT object_owner, object_type, object_name\n");
            sb.append("                  FROM suite\n");
            sb.append("             )\n");
            sb.append("   ),\n");
            sb.append("   suite_tree AS (\n");
            sb.append("      SELECT null AS parent_id,\n");
            sb.append("             'SUITE' AS id,\n");
            sb.append("             'All Suites' AS name,\n");
            sb.append("             'All utPLSQL test suites' AS description,\n");
            sb.append("             'PACKAGE_FOLDER_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM dual\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT DISTINCT\n");
            sb.append("             'SUITE' AS parent_id,\n");
            sb.append("             'object_owner || '.' || object_name AS id,\n");
            sb.append("             object_name AS name,\n");
            sb.append("             NULL AS description,\n");
            sb.append("             'PACKAGE_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT object_owner || '.' || object_name AS parent_id,\n");
            sb.append("             object_owner || '.' || object_name || '.' || upper(subobject_name) AS id,\n");
            sb.append("             subobject_name AS name,\n");
            sb.append("             NULL AS description,\n");
            sb.append("             'PROCEDURE_ICON' AS iconName,\n");
            sb.append("             'Yes' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("   ),\n");
            sb.append("   suitepath_base AS (\n");
            sb.append("      SELECT DISTINCT\n");
            sb.append("             suitepath\n");
            sb.append("        FROM suitepath\n");
            sb.append("   ),\n");
            sb.append("   gen AS (\n");
            sb.append("      SELECT rownum AS pos\n");
            sb.append("        FROM xmltable('1 to 100')\n");
            sb.append("   ),\n");
            sb.append("   suitepath_part AS (\n");
            sb.append("      SELECT DISTINCT\n");
            sb.append("             lower(substr(suitepath, 1, instr(suitepath || '.', '.', 1, g.pos) -1)) AS suitepath\n");
            sb.append("        FROM suitepath_base b\n");
            sb.append("        JOIN gen g\n");
            sb.append("          ON g.pos <= regexp_count(suitepath, '\\w+')\n");
            sb.append("   ),\n");
            sb.append("   suitepath_tree AS (\n");
            sb.append("      SELECT NULL AS parent_id,\n");
            sb.append("             'SUITEPATH' AS id,\n");
            sb.append("             'All Suitepaths' AS name,\n");
            sb.append("             'All utPLSQL test suitepathes' AS description,\n");
            sb.append("             'FOLDER_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM dual\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT CASE\n");
            sb.append("                WHEN regexp_replace(suitepath,'\\.?\\w+$','') IS NULL THEN\n");
            sb.append("                   'SUITEPATH'\n");
            sb.append("                ELSE\n");
            sb.append("                   USER || ':' || regexp_replace(suitepath,'\\.?\\w+$','')");
            sb.append("             END AS parent_id,\n");
            sb.append("             USER || ':' || suitepath AS id,\n");
            sb.append("             regexp_substr(suitepath, '\\.?(\\w+$)', 1, 1, NULL, 1) AS name,\n");
            sb.append("             NULL AS description,\n");
            sb.append("             'FOLDER_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM suitepath_part\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT DISTINCT\n");
            sb.append("             object_owner || ':' || suitepath AS parent_id,\n");
            sb.append("             object_owner || ':' || suitepath || '.' || lower(object_name) AS id,\n");
            sb.append("             object_name AS name,\n");
            sb.append("             NULL AS description,\n");
            sb.append("             'PACKAGE_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("       WHERE suitepath IS NOT NULL\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT DISTINCT\n");
            sb.append("             object_owner || ':' || suitepath || '.' || lower(object_name) AS parent_id,\n");
            sb.append("             object_owner || ':' || suitepath || '.' || lower(object_name) || '.' || context AS id,\n");
            sb.append("             context AS name,\n");
            sb.append("             NULL AS description,\n");
            sb.append("             'FOLDER_ICON' AS iconName,\n");
            sb.append("             'No' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable,\n");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("       WHERE suitepath IS NOT NULL\n");
            sb.append("         AND context IS NOT NULL\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT object_owner || ':' || suitepath || '.' || lower(object_name) || CASE WHEN context IS NOT NULL THEN '.' || context END AS parent_id,\n");
            sb.append("             object_owner || ':' || suitepath || '.' || lower(object_name) || CASE WHEN context IS NOT NULL THEN '.' || context END || '.' || lower(subobject_name) AS id,\n");
            sb.append("             subobject_name AS name,\n");
            sb.append("             NULL AS description,\n");
            sb.append("             'PROCEDURE_ICON' AS iconName,\n");
            sb.append("             'Yes' AS leaf,\n");
            sb.append("             'Yes' AS generatable,\n");
            sb.append("             'Yes' AS multiselectable\n,");
            sb.append("             'Yes' AS relevant\n");
            sb.append("        FROM test\n");
            sb.append("       WHERE suitepath IS NOT NULL\n");
            sb.append("   ),\n");
            sb.append("   tree AS (\n");
            sb.append("      SELECT parent_id, id, name, description, iconName, leaf, generatable, multiselectable, relevant\n");
            sb.append("       FROM suite_tree\n");
            sb.append("      UNION ALL\n");
            sb.append("      SELECT parent_id, id, name, description, iconName, leaf, generatable, multiselectable, relevant\n");
            sb.append("       FROM suitepath_tree\n");
            sb.append("   )\n");
            sb.append("SELECT parent_id, id, initcap(name) AS name, description, iconName, leaf, generatable, multiselectable, relevant\n");
            sb.append("  FROM tree");
        }
        BeanPropertyRowMapper<Node> rowMapper = new BeanPropertyRowMapper<>(Node.class);
        final String sql = sb.toString();
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * enable DBMS_OUTPUT
     * 
     * @throws DataAccessException
     *             if there is a problem
     */
    public void enableDbmsOutput() {
        // equivalent to "set serveroutput on size unlimited"
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN");
        sb.append("   sys.dbms_output.enable(NULL);\n");
        sb.append("END;");
        jdbcTemplate.update(sb.toString());
    }

    /**
     * disable DBMS_OUTPUT
     * 
     * @throws DataAccessException
     *             if there is a problem
     */
    public void disableDbmsOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN\n");
        sb.append("   sys.dbms_output.disable;\n");
        sb.append("END;");
        jdbcTemplate.update(sb.toString());
    }

    /**
     * return the content of DBMS_OUTPUT as String
     * 
     * @throws DataAccessException
     *             if there is a problem
     */
    public String getDbmsOutput() {
        return getDbmsOutput(1000);
    }

    /**
     * return the content of DBMS_OUTPUT as String
     * 
     * @param bufferSize
     *            maximum number of rows to be read from the DBMS_OUTPUT buffer in
     *            one network round trip
     * @return content of DBMS_OUTPUT as String
     * @throws DataAccessException
     *             if there is a problem
     */
    public String getDbmsOutput(final int bufferSize) {
        final StringBuilder resultSb = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN");
        sb.append("   sys.dbms_output.get_lines(?, ?);\n");
        sb.append("END;");
        final String sql = sb.toString();
        OutputLines ret = null;
        do {
            ret = jdbcTemplate.execute(sql, new CallableStatementCallback<OutputLines>() {
                @Override
                public OutputLines doInCallableStatement(final CallableStatement cs) throws SQLException {
                    cs.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
                    cs.registerOutParameter(2, Types.INTEGER);
                    cs.setInt(2, bufferSize);
                    cs.execute();
                    final OutputLines out = new OutputLines();
                    Object array = cs.getArray(1).getArray();
                    out.setLines((String[]) array);
                    out.setNumlines(cs.getInt(2));
                    return out;
                }
            });
            for (int i = 0; i < ret.getNumlines(); i++) {
                final String line = ret.getLines()[i];
                if (line != null) {
                    resultSb.append(ret.getLines()[i]);
                }
                resultSb.append(System.lineSeparator());
            }
        } while (ret.getNumlines() > 0);
        return resultSb.toString();
    }
    
    /**
     * gets the HTML code coverage report as String
     * 
     * @param pathList
     *            utPLSQL path list
     * @param schemaList
     *            list of schemas under tests. Current schema, if empty
     * @param includeObjectList
     *            list of objects to be included for coverage analysis. All, if
     *            empty
     * @param excludeObjectList
     *            list of objects to be excluded from coverage analysis. None, if
     *            empty
     * @return HTML code coverage report in HTML format
     * @throws DataAccessException
     *             if there is a problem
     */
    public String htmlCodeCoverage(final List<String> pathList, final List<String> schemaList,
            final List<String> includeObjectList, final List<String> excludeObjectList) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT column_value\n");
        sb.append("  FROM table(\n");
        sb.append("          ut.run(\n");
        sb.append("             a_paths => ut_varchar2_list(\n");
        sb.append(StringTools.getCSV(pathList, 16));
        sb.append("             ),\n");
        if (schemaList != null && !schemaList.isEmpty()) {
            sb.append("             a_coverage_schemes => ut_varchar2_list(\n");
            sb.append(StringTools.getCSV(schemaList, 16));
            sb.append("             ),\n");
        }
        if (includeObjectList != null && !includeObjectList.isEmpty()) {
            sb.append("             a_include_objects => ut_varchar2_list(\n");
            sb.append(StringTools.getCSV(includeObjectList, 16));
            sb.append("             ),\n");
        }
        if (excludeObjectList != null && excludeObjectList.isEmpty()) {
            sb.append("             a_exclude_objects => ut_varchar2_list(\n");
            sb.append(StringTools.getCSV(excludeObjectList, 16));
            sb.append("             ),\n");
        }
        sb.append("             a_reporter => ut_coverage_html_reporter()\n");
        sb.append("          )\n");
        sb.append("       )");
        final String sql = sb.toString();
        final List<String> lines = jdbcTemplate.queryForList(sql, String.class);
        final StringBuilder resultSb = new StringBuilder();
        for (String line : lines) {
            if (line != null) {
                resultSb.append(line);
                resultSb.append("\n");
            }
        }
        return resultSb.toString();
    }

    /**
     * gets dependencies of a given object.
     * 
     * The result can be used as input for the includeObjectList in htmlCodeCoverage
     * The scope is reduced to non-oracle maintained schemas.
     * 
     * Oracle introduced the column ORACLE_MAINTAINED in 12.1. To simplify the query
     * and compatibility the result of the following query is included
     * 
     * SELECT '''' || listagg(username, ''', ''') || '''' AS oracle_maintained_users
     * FROM dba_users WHERE oracle_maintained = 'Y' ORDER BY username;
     * 
     * The result may include test packages
     * 
     * @param name
     *            test package name
     * @return list of dependencies in the current schema
     */
    public List<String> includes(final String owner, final String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT referenced_owner || '.' || referenced_name AS dep_name\n");
        sb.append("  FROM ");
        sb.append(getDbaView("dependencies\n"));
        sb.append("  WHERE owner = upper(?)\n");
        sb.append("    AND name = upper(?)\n");
        sb.append("    AND referenced_owner NOT IN (\n");
        sb.append("           'SYS', 'SYSTEM', 'XS$NULL', 'OJVMSYS', 'LBACSYS', 'OUTLN', 'SYS$UMF',\n");
        sb.append("           'DBSNMP', 'APPQOSSYS', 'DBSFWUSER', 'GGSYS', 'ANONYMOUS', 'CTXSYS',\n");
        sb.append("           'SI_INFORMTN_SCHEMA', 'DVF', 'DVSYS', 'GSMADMIN_INTERNAL', 'ORDPLUGINS',\n");
        sb.append("           'MDSYS', 'OLAPSYS', 'ORDDATA', 'XDB', 'WMSYS', 'ORDSYS', 'GSMCATUSER',\n");
        sb.append("           'MDDATA', 'REMOTE_SCHEDULER_AGENT', 'SYSBACKUP', 'GSMUSER', 'APEX_PUBLIC_USER',\n");
        sb.append("           'SYSRAC', 'AUDSYS', 'DIP', 'SYSKM', 'ORACLE_OCM', 'APEX_INSTANCE_ADMIN_USER',\n");
        sb.append("           'SYSDG', 'FLOWS_FILES', 'ORDS_METADATA', 'ORDS_PUBLIC_USER'\n");
        sb.append("        )\n");
        sb.append("    AND referenced_owner NOT LIKE 'APEX\\_______'");
        sb.append("    AND referenced_type IN ('PACKAGE', 'TYPE', 'PROCEDURE', 'FUNCTION', 'TRIGGER')");
        final String sql = sb.toString();
        final Object[] binds = new Object[] {owner, name};
        return jdbcTemplate.queryForList(sql, String.class, binds);
    }

    /**
     * gets source of an object from the database via DBMS_METADATA
     * 
     * @param owner
     *            owner of the object (schema)
     * @param objectType
     *            expected object types are PACKAGE, PACKAGE BODY
     * @param objectName
     *            name of the object
     * @return the source code of the object
     * @throws DataAccessException
     *             if there is a problem
     */
    public String getSource(final String owner, final String objectType, final String objectName) {
        String fixedObjectType;
        if ("PACKAGE".equals(objectType)) {
            fixedObjectType = "PACKAGE_SPEC";
        } else if ("PACKAGE BODY".equals(objectType)) {
            fixedObjectType = "PACKAGE_BODY";
        } else {
            fixedObjectType = objectType;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN\n");
        sb.append("   ? := sys.dbms_metadata.get_ddl(\n");
        sb.append("           schema      => ?,\n");
        sb.append("           object_type => ?,\n");
        sb.append("           name        => ?\n");
        sb.append("        );\n");
        sb.append("END;");
        final String sql = sb.toString();
        return jdbcTemplate.execute(sql, new CallableStatementCallback<String>() {
            @Override
            public String doInCallableStatement(final CallableStatement cs) throws SQLException {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, owner);
                cs.setString(3, fixedObjectType);
                cs.setString(4, objectName);
                cs.execute();
                return cs.getString(1);
            }
        });
    }

    /**
     * gets the object type of a database object
     * 
     * The object types "PACKAGE BODY", "TYPE BODY" have higher priority. "PACKAGE"
     * OR "TYPE" will be returned only when no body exists.
     * 
     * @param owner
     *            owner of the object (schema)
     * @param objectName
     *            name of the object
     * @return the object type, e.g. PACKAGE BODY, TYPE BODY, PROCEDURE, FUNCTION
     */
    public String getObjectType(final String owner, final String objectName) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT object_type\n");
        sb.append("  FROM (\n");
        sb.append("          SELECT object_type\n");
        sb.append("            FROM ");
        sb.append(getDbaView("objects\n"));
        sb.append("           WHERE owner = ?\n");
        sb.append("             AND object_name = ?\n");
        sb.append("           ORDER BY decode(object_type, 'PACKAGE', 10, 'TYPE', 10, 'SYNONYM', 20, 1)\n");
        sb.append("       )\n");
        sb.append(" WHERE rownum = 1");
        final String sql = sb.toString();
        final Object[] binds = new Object[] {owner, objectName};
        return jdbcTemplate.queryForObject(sql, binds, String.class);
    }
}
