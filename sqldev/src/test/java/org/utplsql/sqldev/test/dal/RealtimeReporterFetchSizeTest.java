/*
 * Copyright 2019 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.dal.RealtimeReporterDao;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.test.AbstractJdbcTest;

public class RealtimeReporterFetchSizeTest extends AbstractJdbcTest {
    private static final Logger logger = Logger.getLogger(RealtimeReporterFetchSizeTest.class.getName());

    @Before
    public void setup() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE PACKAGE junit_utplsql_fetch_size_pkg is\n");
        sb.append("   --%suite(JUnit testing)\n\n");

        sb.append("   --%test(test 1 - 0 seconds)\n");
        sb.append("   PROCEDURE test_1_0;\n\n");

        sb.append("   --%test(test 2 - 1 seconds)\n");
        sb.append("   PROCEDURE test_2_1;\n\n");

        sb.append("   --%test(test 3 - 2 seconds)\n");
        sb.append("   PROCEDURE test_3_2;\n\n");

        sb.append("   --%test(test 4 - 0 seconds)\n");
        sb.append("   PROCEDURE test_4_0;\n\n");

        sb.append("   --%test(test 5 - 0 seconds)\n");
        sb.append("   PROCEDURE test_5_0;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE BODY junit_utplsql_fetch_size_pkg is\n");
        sb.append("   PROCEDURE test_1_0 IS\n");
        sb.append("   BEGIN\n");
        sb.append("     NULL;\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_2_1 IS\n");
        sb.append("   BEGIN\n");
        sb.append("     dbms_session.sleep(1);\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_3_2 IS\n");
        sb.append("   BEGIN\n");
        sb.append("     dbms_session.sleep(2);\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_4_0 IS\n");
        sb.append("   BEGIN\n");
        sb.append("     NULL;\n");
        sb.append("   END;\n\n");

        sb.append("   PROCEDURE test_5_0 IS\n");
        sb.append("   BEGIN\n");
        sb.append("     NULL;\n");
        sb.append("   END;\n");
        sb.append("END;");
        jdbcTemplate.execute(sb.toString());
    }

    @After
    public void teardown() {
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE junit_utplsql_fetch_size_pkg");
    }

    private void delayFreeStreamingConsumtionProducer(final String reporterId) {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl(dataSource.getUrl());
        ds.setUsername(dataSource.getUsername());
        ds.setPassword(dataSource.getPassword());
        final RealtimeReporterDao dao = new RealtimeReporterDao(DatabaseTools.getConnection(ds));
        dao.produceReport(reporterId, Arrays.asList("junit_utplsql_fetch_size_pkg"));
    }

    @Test
    public void delayFreeStreamingConsumtion() {
        final long TOLERANCE_MS = 600;
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl(dataSource.getUrl());
        ds.setUsername(dataSource.getUsername());
        ds.setPassword(dataSource.getPassword());
        final TestRealtimerReporterEventTimedConsumer consumer = new TestRealtimerReporterEventTimedConsumer();
        final String reporterId = UUID.randomUUID().toString().replace("-", "");
        final RealtimeReporterDao dao = new RealtimeReporterDao(DatabaseTools.getConnection(ds));
        final Thread thread = new Thread(() -> delayFreeStreamingConsumtionProducer(reporterId));
        thread.setName("utPLSQL run test");
        thread.start();
        dao.consumeReport(reporterId, consumer);
        logger.fine(consumer.getPostTestEvents().toString());
        Assert.assertEquals(5, consumer.getPostTestEvents().entrySet().size());
        final Long test_1_0 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_1_0");
        final Long test_2_1 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_2_1");
        final Long test_3_2 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_3_2");
        final Long test_4_0 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_4_0");
        final Long test_5_0 = consumer.getPostTestEvents().get("junit_utplsql_fetch_size_pkg.test_5_0");
        final long test_2_1_time = test_2_1 - test_1_0;
        logger.fine("test_2_1 time [ms]: " + test_2_1_time);
        Assert.assertTrue("test_2_1 runtime was too long", test_2_1_time < 1000 + TOLERANCE_MS);
        Assert.assertTrue("test_2_1 runtime was too short", test_2_1_time > 1000 - TOLERANCE_MS);
        final long test_3_2_time = test_3_2 - test_2_1;
        logger.fine("test_3_2 time [ms]: " + test_3_2_time);
        Assert.assertTrue("test_3_2 runtime was too long", test_3_2_time < 2000 + TOLERANCE_MS);
        Assert.assertTrue("test_3_2 runtime was too short", test_3_2_time > 2000 - TOLERANCE_MS);
        final long test_4_0_time = test_4_0 - test_3_2;
        logger.fine("test_4_0 time [ms]: " + test_4_0_time);
        Assert.assertTrue("test_4_0 runtime was too long", test_4_0_time < TOLERANCE_MS);
        final long test_5_0_time = test_5_0 - test_4_0;
        logger.fine("test_5_0 time [ms]: " + test_5_0_time);
        Assert.assertTrue("test_5_0 runtime was too long", test_5_0_time < TOLERANCE_MS);
    }
}
