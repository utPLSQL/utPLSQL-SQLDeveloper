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
package org.utplsql.sqldev.test.dal;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oddgen.sqldev.generators.model.Node;
import org.utplsql.sqldev.dal.UtplsqlDao;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.test.AbstractJdbcTest;

public class DalBugFixTest extends AbstractJdbcTest {
    @Before
    @After
    public void setupAndTeardown() {
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_utplsql_test_pkg");
    }

    @Test
    // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/54
    public void issue54FolderIconForSuitesWithoutTests() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        final List<Node> actualNodes = dao.runnables();
        final Node pkg = actualNodes.stream().filter(it -> it.getId().equals("SCOTT:junit_utplsql_test_pkg"))
                .findFirst().get();
        Assert.assertEquals("FOLDER_ICON", pkg.getIconName());
    }

    @Test
    // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/54
    public void issue54PackageIconForSuitesWithTests() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("   -- %test\n");
        sb.append("   PROCEDURE t1;\n\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        final List<Node> actualNodes = dao.runnables();
        final Node pkg = actualNodes.stream().filter(it -> it.getId().equals("SCOTT:junit_utplsql_test_pkg"))
                .findFirst().get();
        Assert.assertEquals("PACKAGE_ICON", pkg.getIconName());
    }

    @Test
    // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/55
    public void issue55SuiteWithoutTests() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        final List<Node> actualNodes = dao.runnables();
        Assert.assertEquals(4, actualNodes.size());
    }

    @Test
    public void issue56SuiteWithoutTests() {
        // https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/56
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test_pkg IS\n");
        sb.append("   -- %suite\n\n");
        sb.append("END junit_utplsql_test_pkg;");
        jdbcTemplate.execute(sb.toString());
        final UtplsqlDao dao = new UtplsqlDao(DatabaseTools.getConnection(dataSource));
        Assert.assertTrue(dao.containsUtplsqlTest("scott", "junit_utplsql_test_pkg"));
    }
}
