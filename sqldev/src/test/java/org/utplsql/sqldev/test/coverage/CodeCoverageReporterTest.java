/**
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
package org.utplsql.sqldev.test.coverage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.coverage.CodeCoverageReporter;
import org.utplsql.sqldev.exception.GenericRuntimeException;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.FileTools;
import org.utplsql.sqldev.model.SystemTools;
import org.utplsql.sqldev.test.AbstractJdbcTest;

public class CodeCoverageReporterTest extends AbstractJdbcTest {

    @BeforeClass
    public static void setup() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE FUNCTION f RETURN INTEGER IS\n");
        sb.append("BEGIN\n");
        sb.append("   RETURN 1;\n");
        sb.append("END f;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE test_f IS\n");
        sb.append("   --%suite\n\n");
        sb.append("   --%test\n");
        sb.append("   PROCEDURE f;\n");
        sb.append("END test_f;");
        jdbcTemplate.execute(sb.toString());
        sb.setLength(0);
        sb.append("CREATE OR REPLACE PACKAGE BODY test_f IS\n");
        sb.append("   --%test\n");
        sb.append("   PROCEDURE f IS\n");
        sb.append("      l_expected INTEGER := 1;\n");
        sb.append("      l_actual   INTEGER;\n");
        sb.append("   BEGIN\n");
        sb.append("      l_actual := scott.f();\n");
        sb.append("      ut.expect(l_actual).to_equal(l_expected);\n");
        sb.append("   END f;\n");
        sb.append("END test_f;");
        jdbcTemplate.execute(sb.toString());
    }
    
    private File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile("test", ".txt");
        } catch (IOException e) {
            final String msg = "Cannot create temporary file with prefix '" + prefix + "' and suffix '" + suffix + "'.";
            throw new GenericRuntimeException(msg, e);
        }
    }

    private Path getNewestOutputFile() {
            final File file = createTempFile("test", ".txt");
            final File dir = file.getParentFile();
            file.delete();
            Optional<Path> last;
            try {
                last = Files.list(dir.toPath())
                        .filter(f -> !f.toFile().isDirectory() && f.getFileName().toString().startsWith("utplsql_")
                                && f.getFileName().toString().endsWith(".html"))
                        .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
            } catch (IOException e) {
                final String msg = "Cannot get newest output file in " + dir.getAbsolutePath() + ".";
                throw new GenericRuntimeException(msg, e);
            }
            return last.get();
    }

    @Test
    public void produceReportAndCloseConnection() {
        // create temporary dataSource, closed by reporter
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl(dataSource.getUrl());
        ds.setUsername(dataSource.getUsername());
        ds.setPassword(dataSource.getPassword());
        final Connection conn = DatabaseTools.getConnection(ds);
        final List<String> pathList = Arrays.asList(":test_f");
        final List<String> includeObjectList = Arrays.asList("f");
        final CodeCoverageReporter reporter = new CodeCoverageReporter(pathList, includeObjectList, conn);
        final Thread run = reporter.runAsync();
        SystemTools.waitForThread(run, 20000);
        Assert.assertTrue(DatabaseTools.isConnectionClosed(conn));
        final Path outputFile = this.getNewestOutputFile();
        Assert.assertNotNull(outputFile);
        final String content = new String(FileTools.readFile(outputFile), StandardCharsets.UTF_8);
        Assert.assertTrue(
                content.contains("<h3>SCOTT.F</h3><h4><span class=\"green\">100 %</span> lines covered</h4>"));
    }

    @AfterClass
    public static void teardown() {
        executeAndIgnore(jdbcTemplate, "DROP PACKAGE test_f");
        executeAndIgnore(jdbcTemplate, "DROP FUNCTION f");
    }
}
