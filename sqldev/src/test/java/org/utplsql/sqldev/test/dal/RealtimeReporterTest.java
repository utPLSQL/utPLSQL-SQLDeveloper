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

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.utplsql.sqldev.dal.RealtimeReporterDao;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.runner.PostRunEvent;
import org.utplsql.sqldev.model.runner.PostSuiteEvent;
import org.utplsql.sqldev.model.runner.PostTestEvent;
import org.utplsql.sqldev.model.runner.PreRunEvent;
import org.utplsql.sqldev.model.runner.PreSuiteEvent;
import org.utplsql.sqldev.model.runner.PreTestEvent;
import org.utplsql.sqldev.test.AbstractJdbcTest;
import org.utplsql.sqldev.test.coverage.CodeCoverageReporterTest;

public class RealtimeReporterTest extends AbstractJdbcTest {
    private static final Logger logger = Logger.getLogger(RealtimeReporterTest.class.getName());

    @Before
    public void setup() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test1_pkg is\n");
        sb.append("   --%suite(JUnit testing)\n");
        sb.append("   --%suitepath(a)\n\n");

        sb.append("   --%context(test context)\n\n");

        sb.append("   --%test(test 1 - OK)\n");
        sb.append("   PROCEDURE test_1_ok;\n\n");

        sb.append("   --%test(test 2 - NOK)\n");
        sb.append("   PROCEDURE test_2_nok;\n\n");

        sb.append("   --%endcontext\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test1_pkg IS\n");
        sb.append("   PROCEDURE test_1_ok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      ut.expect(1).to_equal(1);\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_2_nok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      ut.expect(1).to_equal(2);\n");
        sb.append("   END;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test2_pkg IS\n");
        sb.append("   --%suite\n");
        sb.append("   --%suitepath(b)\n\n");

        sb.append("   --%test\n");
        sb.append("   PROCEDURE test_3_ok;\n\n");

        sb.append("   --%test\n");
        sb.append("   PROCEDURE test_4_nok;\n\n");

        sb.append("   --%test\n");
        sb.append("   --%disabled\n");
        sb.append("   PROCEDURE test_5;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test2_pkg IS\n");
        sb.append("   PROCEDURE test_3_ok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      ut3.ut.expect(2).to_equal(2);\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_4_nok IS\n");
        sb.append("   BEGIN\n");
        sb.append("      ut3.ut.expect(2).to_equal(3);\n");
        sb.append("      ut3.ut.expect(2).to_equal(4);\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_5 IS\n");
        sb.append("   BEGIN\n");
        sb.append("      NULL;\n");
        sb.append("   END;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_test3_pkg IS\n");
        sb.append("   --%suite\n");
        sb.append("   --%suitepath(b)\n\n");

        sb.append("   --%test\n");
        sb.append("   PROCEDURE test_6_with_runtime_error;\n\n");

        sb.append("   --%test\n");
        sb.append("   PROCEDURE test_7_with_serveroutput;\n\n");

        sb.append("   --%afterall\n");
        sb.append("   PROCEDURE print_and_raise;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_test3_pkg IS\n");
        sb.append("   PROCEDURE test_6_with_runtime_error IS\n");
        sb.append("      l_actual INTEGER;\n");
        sb.append("   BEGIN\n");
        sb.append("      EXECUTE IMMEDIATE 'select 6 from non_existing_table' INTO l_actual;\n");
        sb.append("      ut3.ut.expect(6).to_equal(l_actual);\n");
        sb.append("   END\n\n;");

        sb.append("   PROCEDURE test_7_with_serveroutput IS\n");
        sb.append("   BEGIN\n");
        sb.append("      dbms_output.put_line('before test 7');\n");
        sb.append("      ut3.ut.expect(7).to_equal(7);\n");
        sb.append("      dbms_output.put_line('after test 7');\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE print_and_raise IS\n");
        sb.append("   BEGIN\n");
        sb.append("      dbms_output.put_line('Now, a no_data_found exception is raised');\n");
        sb.append("      dbms_output.put_line('dbms_output and error stack is reported for this suite.');\n");
        sb.append("      dbms_output.put_line('A runtime error in afterall is counted as a warning.');\n");
        sb.append("      RAISE no_data_found;\n");
        sb.append("    END;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());

        new CodeCoverageReporterTest().setup();
    }
    
    @After
    public void teardown() {
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_utplsql_test1_pkg");
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_utplsql_test2_pkg");
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_utplsql_test3_pkg");
        new CodeCoverageReporterTest().teardown();
    }

    @Test
    public void produceAndConsume() {
        final RealtimeReporterDao dao = new RealtimeReporterDao(DatabaseTools.getConnection(dataSource));
        final String reporterId = UUID.randomUUID().toString().replace("-", "");
        final TestRealtimerReporterEventConsumer consumer = new TestRealtimerReporterEventConsumer();
        dao.produceReport(reporterId, Arrays.asList(":a", ":b"));
        dao.consumeReport(reporterId, consumer);
        logger.fine(consumer.getConsumedList().toString());
        Assert.assertEquals(1, consumer.getConsumedList().stream().filter(it -> it instanceof PreRunEvent).count());
        Assert.assertEquals(1, consumer.getConsumedList().stream().filter(it -> it instanceof PostRunEvent).count());
        // 2 suitepaths (a, b), 1 context, 3 packages -> 6 suites
        Assert.assertEquals(6, consumer.getConsumedList().stream().filter(it -> it instanceof PreSuiteEvent).count());
        Assert.assertEquals(6, consumer.getConsumedList().stream().filter(it -> it instanceof PostSuiteEvent).count());
        Assert.assertEquals(7, consumer.getConsumedList().stream().filter(it -> it instanceof PreTestEvent).count());
        Assert.assertEquals(7, consumer.getConsumedList().stream().filter(it -> it instanceof PostTestEvent).count());
        Assert.assertEquals(28, consumer.getConsumedList().size());
    }
    
    @Test
    public void produceAndConsumeWithCoverage() {
        final RealtimeReporterDao dao = new RealtimeReporterDao(DatabaseTools.getConnection(dataSource));
        final String realtimeReporterId = UUID.randomUUID().toString().replace("-", "");
        final String coverageReporterId = UUID.randomUUID().toString().replace("-", "");
        final TestRealtimerReporterEventConsumer consumer = new TestRealtimerReporterEventConsumer();
        dao.produceReportWithCoverage(realtimeReporterId, coverageReporterId, Arrays.asList(":test_f"), null, null, null, null);
        dao.consumeReport(realtimeReporterId, consumer);
        logger.fine(consumer.getConsumedList().toString());
        Assert.assertEquals(6, consumer.getConsumedList().size());
        final String html = dao.getHtmlCoverage(coverageReporterId);
        Assert.assertTrue(html.trim().endsWith("</html>"));
        // default assets accessed via internet
        Assert.assertTrue(html.contains("script src='https://utplsql.github.io"));
    }
}
