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
package org.utplsql.sqldev.test.runner;

import java.sql.Connection;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.SystemTools;
import org.utplsql.sqldev.runner.UtplsqlRunner;
import org.utplsql.sqldev.test.AbstractJdbcTest;
import org.utplsql.sqldev.test.coverage.CodeCoverageReporterTest;

public class UtplsqlRunnerTest extends AbstractJdbcTest {

    @Before
    public void setup() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg is\n");
        sb.append("   --%suite(JUnit testing)\n");
        sb.append("   --%suitepath(a)\n");
        sb.append("   /* tags annotation without parameter will raise a warning */\n");
        sb.append("   --%tags\n\n");

        sb.append("   --%context(test context)\n\n");

        sb.append("   --%test(test 1 - OK)\n");
        sb.append("   PROCEDURE test_1_ok;\n\n");

        sb.append("   --%test(test 2 - NOK)\n");
        sb.append("   PROCEDURE test_2_nok;\n\n");

        sb.append("   --%test(test 3 - disabled)\n");
        sb.append("   --%disabled\n");
        sb.append("   PROCEDURE test_3_disabled;\n\n");

        sb.append("   --%test(test 4 - errored)\n");
        sb.append("   PROCEDURE test_4_errored;\n\n");

        sb.append("   --%test(test 5 - warnings)\n");
        sb.append("   PROCEDURE test_5_warnings;\n\n");

        sb.append("   --%endcontext\n\n");

        sb.append("   --%afterall\n");
        sb.append("   procedure print_and_raise;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());

        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test1_pkg IS\n");
        sb.append("   PROCEDURE test_1_ok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      dbms_output.put_line('start test 1');\n");
        sb.append("      dbms_session.sleep(1);\n");
        sb.append("      ut.expect(1).to_equal(1);\n");
        sb.append("      dbms_output.put_line('end test 1');\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_2_nok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      dbms_output.put_line('start test 2');\n");
        sb.append("      dbms_session.sleep(2);\n");
        sb.append("      ut.expect(1, 'first assert.').to_equal(2);\n");
        sb.append("      ut.expect(1, 'second assert.').to_equal(2);\n");
        sb.append("      dbms_output.put_line('end test 2');\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_3_disabled IS\n");
        sb.append("   BEGIN\n");
        sb.append("      NULL;\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_4_errored IS\n");
        sb.append("   BEGIN\n");
        sb.append("      EXECUTE IMMEDIATE 'bla bla';\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_5_warnings IS\n");
        sb.append("   BEGIN\n");
        sb.append("      COMMIT; -- will raise a warning\n");
        sb.append("      ut.expect(1).to_equal(1);\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE print_and_raise IS\n");
        sb.append("   BEGIN\n");
        sb.append("      dbms_output.put_line('Now, a no_data_found exception is raised');\n");
        sb.append("      dbms_output.put_line('dbms_output and error stack is reported for this suite.');\n");
        sb.append("      dbms_output.put_line('A runtime error in afterall is counted as a warning.');\n");
        sb.append("      RAISE no_data_found;\n");
        sb.append("   END;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        new CodeCoverageReporterTest().setup();
    }

    @After
    public void teardown() {
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_utplsql_test1_pkg");
        new CodeCoverageReporterTest().teardown();
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
    public void runTestsWithMaxTime() {
        UtplsqlRunner runner = new UtplsqlRunner(Collections.singletonList(":a"), getNewConnection(), getNewConnection());
        runner.runTestAsync();

        SystemTools.waitForThread(runner.getProducerThread(), 200000);
        SystemTools.waitForThread(runner.getConsumerThread(), 200000);
        SystemTools.sleep(4 * 1000);
        Assert.assertNotNull(runner);
        runner.dispose();
    }

    @Test
    public void runTestsWithCodeCoverage() {
        UtplsqlRunner runner = new UtplsqlRunner(Collections.singletonList(":test_f"), null, null, null, getNewConnection(), getNewConnection());
        runner.runTestAsync();

        SystemTools.waitForThread(runner.getProducerThread(), 200000);
        SystemTools.waitForThread(runner.getConsumerThread(), 200000);
        SystemTools.sleep(4 * 1000);
        Assert.assertNotNull(runner);
        runner.dispose();
    }
        runner.runTestAsync();

        SystemTools.waitForThread(runner.getProducerThread(), 200000);
        SystemTools.waitForThread(runner.getConsumerThread(), 200000);
        SystemTools.sleep(4 * 1000);
        Assert.assertNotNull(runner);
        runner.dispose();
    }
}
