/*
 * Copyright 2021 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
package org.utplsql.sqldev.test.runner;

import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.SystemTools;
import org.utplsql.sqldev.model.runner.ItemNode;
import org.utplsql.sqldev.runner.UtplsqlRunner;
import org.utplsql.sqldev.test.AbstractJdbcTest;

public class UtplsqlRunnerAggregationTest extends AbstractJdbcTest {
    static final int SHOW_GUI_AFTER_RUN_COMPLETION_IN_SECONDS = 0;
    
    @Before
    public void setup() {
        // based on https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/126
        jdbcTemplate.execute(
                  "create or replace package x is\n"
                + "\n"
                + "   --%suite(suite x)\n"
                + "   --%suitepath(foo.bar)\n"
                + "\n"
                + "   --%test(feature a)\n"
                + "   --%disabled\n"
                + "   procedure test_a;\n"
                + "\n"
                + "   --%test(feature b)\n"
                + "   --%disabled\n"
                + "   procedure test_b;\n"
                + "\n"
                + "end;");
        jdbcTemplate.execute(
                  "create or replace package y is\n"
                + "\n"
                + "   --%suite(suite y)\n"
                + "   --%suitepath(foo.bar)\n"
                + "\n"
                + "   --%test(feature c)\n"
                + "   --%disabled\n"
                + "   procedure test_c;\n"
                + "\n"
                + "   --%test(feature d)\n"
                + "   --%disabled\n"
                + "   procedure test_d;\n"
                + "\n"
                + "end;");
    }

    @After
    public void teardown() {
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE x");
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE y");
    }
    
    private Connection getNewConnection() {
        final SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl(dataSource.getUrl());
        ds.setUsername(dataSource.getUsername());
        ds.setPassword(dataSource.getPassword());
        return DatabaseTools.getConnection(ds);
    }

    @Test
    public void aggregateDescription() {
        UtplsqlRunner runner = new UtplsqlRunner(Collections.singletonList(":foo"), getNewConnection(), getNewConnection());
        runner.runTestAsync();
        SystemTools.waitForThread(runner.getProducerThread(), 10000);
        SystemTools.waitForThread(runner.getConsumerThread(), 10000);
        Assert.assertNotNull(runner);
        LinkedHashMap<String, ItemNode> nodes = runner.getRun().getItemNodes();
        Assert.assertEquals(9, nodes.size()); // 8 + 1 for the run node 
        Assert.assertNotNull(nodes.get(runner.getRun().getReporterId()));
        Assert.assertNull(nodes.get("foo").getDescription());
        Assert.assertNull(nodes.get("foo.bar").getDescription());
        Assert.assertEquals("suite y", nodes.get("foo.bar.y").getDescription());
        Assert.assertEquals("suite x", nodes.get("foo.bar.x").getDescription());
        Assert.assertEquals("feature c", nodes.get("foo.bar.y.test_c").getDescription());
        Assert.assertEquals("feature d", nodes.get("foo.bar.y.test_d").getDescription());
        Assert.assertEquals("feature a", nodes.get("foo.bar.x.test_a").getDescription());
        Assert.assertEquals("feature b", nodes.get("foo.bar.x.test_b").getDescription());
        SystemTools.sleep(SHOW_GUI_AFTER_RUN_COMPLETION_IN_SECONDS * 1000);
        runner.dispose();
    }

}
