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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.utplsql.sqldev.model.StringTools;
import org.utplsql.sqldev.model.XMLTools;
import org.utplsql.sqldev.model.runner.Counter;
import org.utplsql.sqldev.model.runner.Expectation;
import org.utplsql.sqldev.model.runner.PostEvent;
import org.utplsql.sqldev.model.runner.PostRunEvent;
import org.utplsql.sqldev.model.runner.PostSuiteEvent;
import org.utplsql.sqldev.model.runner.PostTestEvent;
import org.utplsql.sqldev.model.runner.PreRunEvent;
import org.utplsql.sqldev.model.runner.PreSuiteEvent;
import org.utplsql.sqldev.model.runner.PreTestEvent;
import org.utplsql.sqldev.model.runner.RealtimeReporterEvent;
import org.utplsql.sqldev.model.runner.Suite;
import org.utplsql.sqldev.model.runner.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import oracle.jdbc.OracleTypes;

public class RealtimeReporterDao {
    private static final Logger logger = Logger.getLogger(RealtimeReporterDao.class.getName());
    private static final int FIRST_VERSION_WITH_REALTIME_REPORTER = 3001004;
    private final XMLTools xmlTools = new XMLTools();
    private final Connection conn;
    private final JdbcTemplate jdbcTemplate;

    public RealtimeReporterDao(final Connection conn) {
        this.conn = conn;
        jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(conn, true));
        jdbcTemplate.setFetchSize(UtplsqlDao.FETCH_ROWS);
    }

    public boolean isSupported() {
        return new UtplsqlDao(conn)
                .normalizedUtPlsqlVersionNumber() >= RealtimeReporterDao.FIRST_VERSION_WITH_REALTIME_REPORTER;
    }

    // used for execution via PL/SQL Debugger
    public String getProduceReportPlsql(final String reporterId, final List<String> pathList) {
        return getProduceReportPlsql(reporterId, pathList, false);
    }

    private String getProduceReportPlsql(final String reporterId, final List<String> pathList, boolean useBindVariable) {
        StringBuilder sb = new StringBuilder();
        sb.append("DECLARE\n");
        sb.append("   l_reporter ut_realtime_reporter := ut_realtime_reporter();\n");
        sb.append("BEGIN\n");
        if (useBindVariable) {
            sb.append("   l_reporter.set_reporter_id(?);\n");
        } else {
            sb.append("   l_reporter.set_reporter_id('");
            sb.append(reporterId);
            sb.append("');\n");
        }
        sb.append("   l_reporter.output_buffer.init();\n");
        sb.append("   sys.dbms_output.enable(NULL);\n");
        sb.append("   ut_runner.run(\n");
        sb.append("      a_paths     => ut_varchar2_list(\n");
        sb.append(StringTools.getCSV(pathList, 24));
        sb.append("                     ),\n");
        sb.append("      a_reporters => ut_reporters(l_reporter)\n");
        sb.append("   );\n");
        sb.append("   sys.dbms_output.disable;\n");
        sb.append("END;");
        return sb.toString();
    }

    public void produceReport(final String reporterId, final List<String> pathList) {
        final String plsql = getProduceReportPlsql(reporterId, pathList, true);
        final Object[] binds = { reporterId };
        jdbcTemplate.update(plsql, binds);
    }

    public void produceReportWithCoverage(final String realtimeReporterId, final String coverageReporterId,
            final List<String> pathList, final List<String> schemaList, final List<String> includeObjectList,
            final List<String> excludeObjectList, final URL htmlReportAssetPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("DECLARE\n");
        sb.append("   l_rt_rep  ut_realtime_reporter      := ut_realtime_reporter();\n");
        sb.append("   l_cov_rep ut_coverage_html_reporter := ut_coverage_html_reporter(a_html_report_assets_path => ?);\n");
        sb.append("BEGIN\n");
        sb.append("   l_rt_rep.set_reporter_id(?);\n");
        sb.append("   l_rt_rep.output_buffer.init();\n");
        sb.append("   l_cov_rep.set_reporter_id(?);\n");
        sb.append("   l_cov_rep.output_buffer.init();\n");
        sb.append("   sys.dbms_output.enable(NULL);\n");
        sb.append("   ut_runner.run(\n");
        sb.append("      a_paths            => ut_varchar2_list(\n");
        sb.append(StringTools.getCSV(pathList, 31));
        sb.append("                            ),\n");
        if (schemaList != null && !schemaList.isEmpty()) {
            sb.append("      a_coverage_schemes => ut_varchar2_list(\n");
            sb.append(StringTools.getCSV(schemaList, 31));
            sb.append("                            ),\n");
        }
        if (includeObjectList != null && !includeObjectList.isEmpty()) {
            sb.append("     a_include_objects   => ut_varchar2_list(\n");
            sb.append(StringTools.getCSV(includeObjectList, 31));
            sb.append("                            ),\n");
        }
        if (excludeObjectList != null && !excludeObjectList.isEmpty()) {
            sb.append("     a_exclude_objects   => ut_varchar2_list(\n");
            sb.append(StringTools.getCSV(excludeObjectList, 31));
            sb.append("                            ),\n");
        }
        sb.append("      a_reporters        => ut_reporters(l_rt_rep, l_cov_rep)\n");
        sb.append("   );\n");
        sb.append("   sys.dbms_output.disable;\n");
        sb.append("END;");
        final String plsql = sb.toString();
        final Object[] binds = { htmlReportAssetPath == null ? null : htmlReportAssetPath.toExternalForm(), realtimeReporterId, coverageReporterId };
        jdbcTemplate.update(plsql, binds);
    }

    public void consumeReport(final String reporterId, final RealtimeReporterEventConsumer consumer) {
        consumeReport(reporterId, consumer, 60);
    }

    public void consumeReport(final String reporterId, final RealtimeReporterEventConsumer consumer, final int timeoutSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append("DECLARE\n");
        sb.append("   l_reporter ut_realtime_reporter := ut_realtime_reporter();\n");
        sb.append("BEGIN\n");
        sb.append("   l_reporter.set_reporter_id(?);\n");
        sb.append("   ? := l_reporter.get_lines_cursor(a_initial_timeout => ?);\n");
        sb.append("END;");
        final String plsql = sb.toString();
        jdbcTemplate.setFetchSize(1);
        try {
            jdbcTemplate.execute(plsql, (CallableStatementCallback<Void>) cs -> {
                cs.setString(1, reporterId);
                cs.setInt(3, timeoutSeconds);
                cs.registerOutParameter(2, OracleTypes.CURSOR);
                cs.execute();
                final ResultSet rs = (ResultSet) cs.getObject(2);
                while (rs.next()) {
                    final String itemType = rs.getString("item_type");
                    final Clob textClob = rs.getClob("text");
                    final String textString = textClob.getSubString(1, ((int) textClob.length()));
                    final RealtimeReporterEvent event = convert(itemType, textString);
                    if (event != null) {
                        consumer.process(event);
                    }
                }
                rs.close();
                return null;
            });
        } finally {
            jdbcTemplate.setFetchSize(UtplsqlDao.FETCH_ROWS);
        }
    }

    public String getHtmlCoverage(final String reporterId) {
        StringBuilder sb = new StringBuilder();
        sb.append("DECLARE\n");
        sb.append("   l_reporter ut_coverage_html_reporter := ut_coverage_html_reporter();\n");
        sb.append("BEGIN\n");
        sb.append("   l_reporter.set_reporter_id(?);\n");
        sb.append("   ? := l_reporter.get_lines_cursor();\n");
        sb.append("END;");
        final String plsql = sb.toString();
        return jdbcTemplate.execute(plsql, (CallableStatementCallback<String>) cs -> {
            cs.setString(1, reporterId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();
            final StringBuilder sb1 = new StringBuilder();
            final ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                final String text = rs.getString("text");
                if (text != null) {
                    sb1.append(text);
                    sb1.append('\n');
                }
            }
            rs.close();
            return sb1.toString();
        });
    }

    private RealtimeReporterEvent convert(final String itemType, final String text) {
        logger.fine(() -> "\n---- " + itemType + " ----\n" + text);
        try {
            final DocumentBuilder docBuilder = xmlTools.createDocumentBuilder();
            final Document doc = docBuilder.parse(new InputSource(new StringReader(text)));
            RealtimeReporterEvent event = null;
            if ("pre-run".equals(itemType)) {
                event = convertToPreRunEvent(doc);
            } else if ("post-run".equals(itemType)) {
                event = convertToPostRunEvent(doc);
            } else if ("pre-suite".equals(itemType)) {
                event = convertToPreSuiteEvent(doc);
            } else if ("post-suite".equals(itemType)) {
                event = convertToPostSuiteEvent(doc);
            } else if ("pre-test".equals(itemType)) {
                event = convertToPreTestEvent(doc);
            } else if ("post-test".equals(itemType)) {
                event = convertToPostTestEvent(doc);
            }
            return event;
        } catch (SAXException e) {
            // continue processing, see https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/107
            final String msg = "Parse error while processing " + itemType + " with content: " + text;
            logger.severe(() -> msg);
            return null;
        } catch (IOException e) {
            // continue processing, see https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/107
            final String msg = "I/O error while processing " + itemType + " with content: " + text;
            logger.severe(() -> msg);
            return null;
        }
    }

    private RealtimeReporterEvent convertToPreRunEvent(final Document doc) {
        final PreRunEvent event = new PreRunEvent();
        final Node totalNumberOfTestsNode = xmlTools.getNode(doc, "/event/totalNumberOfTests");
        String totalNumberOfTestsTextContent = null;
        if (totalNumberOfTestsNode != null) {
            totalNumberOfTestsTextContent = totalNumberOfTestsNode.getTextContent();
        }
        event.setTotalNumberOfTests(Integer.valueOf(totalNumberOfTestsTextContent != null ? totalNumberOfTestsTextContent : "0"));
        final NodeList nodes = xmlTools.getNodeList(doc, "/event/items/*");
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            final String nodeName = node.getNodeName();
            if ("suite".equals(nodeName)) {
                final Suite suite = new Suite();
                event.getItems().add(suite);
                populate(suite, node);
            } else if ("test".equals(nodeName)) {
                final Test test = new Test();
                event.getItems().add(test);
                populate(test, node);
            }
        }
        return event;
    }

    private RealtimeReporterEvent convertToPostRunEvent(final Document doc) {
        final PostRunEvent event = new PostRunEvent();
        populate(event, xmlTools.getNode(doc, "/event/run"));
        return event;
    }

    private RealtimeReporterEvent convertToPreSuiteEvent(final Document doc) {
        final PreSuiteEvent event = new PreSuiteEvent();
        final Node node = xmlTools.getNode(doc, "/event/suite");
        if (node instanceof Element) {
            event.setId(xmlTools.getAttributeValue(node, "id"));
        }
        return event;
    }

    private RealtimeReporterEvent convertToPostSuiteEvent(final Document doc) {
        final PostSuiteEvent event = new PostSuiteEvent();
        final Node node = xmlTools.getNode(doc, "/event/suite");
        if (node instanceof Element) {
            event.setId(xmlTools.getAttributeValue(node, "id"));
            populate(event, node);
        }
        return event;
    }

    private RealtimeReporterEvent convertToPreTestEvent(final Document doc) {
        final PreTestEvent event = new PreTestEvent();
        final Node node = xmlTools.getNode(doc, "/event/test");
        if (node instanceof Element) {
            event.setId(xmlTools.getAttributeValue(node, "id"));
            event.setTestNumber(Integer.valueOf(xmlTools.getElementValue(node, "testNumber")));
            event.setTotalNumberOfTests(Integer.valueOf(xmlTools.getElementValue(node, "totalNumberOfTests")));
        }
        return event;
    }

    private RealtimeReporterEvent convertToPostTestEvent(final Document doc) {
        final PostTestEvent event = new PostTestEvent();
        final Node node = xmlTools.getNode(doc, "/event/test");
        if (node instanceof Element) {
            event.setId(xmlTools.getAttributeValue(node, "id"));
            event.setTestNumber(Integer.valueOf(xmlTools.getElementValue(node, "testNumber")));
            event.setTotalNumberOfTests(Integer.valueOf(xmlTools.getElementValue(node, "totalNumberOfTests")));
            populate(event, node);
            final NodeList failedExpectations = xmlTools.getNodeList(node, "failedExpectations/expectation");
            for (int i = 0; i < failedExpectations.getLength(); i++) {
                final Node expectationNode = failedExpectations.item(i);
                final Expectation expectation = new Expectation();
                event.getFailedExpectations().add(expectation);
                populate(expectation, expectationNode);
            }
        }
        return event;
    }

    private void populate(final Suite suite, final Node node) {
        if (node instanceof Element) {
            suite.setId(xmlTools.getAttributeValue(node, "id"));
            suite.setName(xmlTools.getElementValue(node, "name"));
            suite.setDescription(xmlTools.getElementValue(node, "description"));
            final NodeList nodeList = xmlTools.getNodeList(node, "items/*");
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node childNode = nodeList.item(i);
                final String nodeName = childNode.getNodeName();
                if ("suite".equals(nodeName)) {
                    final Suite childSuite = new Suite();
                    suite.getItems().add(childSuite);
                    populate(childSuite, childNode);
                } else if ("test".equals(nodeName)) {
                    final Test childTest = new Test();
                    suite.getItems().add(childTest);
                    populate(childTest, childNode);
                }
            }
        }
    }

    private void populate(final Test test, final Node node) {
        if (node instanceof Element) {
            test.setId(xmlTools.getAttributeValue(node, "id"));
            test.setExecutableType(xmlTools.getElementValue(node, "executableType"));
            test.setOwnerName(xmlTools.getElementValue(node, "ownerName"));
            test.setObjectName(xmlTools.getElementValue(node, "objectName"));
            test.setProcedureName(xmlTools.getElementValue(node, "procedureName"));
            test.setDisabled("true".equals(xmlTools.getElementValue(node, "disabled")));
            test.setDisabledReason(xmlTools.getElementValue(node, "disabledReason"));
            test.setName(xmlTools.getElementValue(node, "name"));
            test.setDescription(xmlTools.getElementValue(node, "description"));
            test.setTestNumber(Integer.valueOf(xmlTools.getElementValue(node, "testNumber")));
        }
    }

    private void populate(final PostEvent event, final Node node) {
        if (node instanceof Element) {
            event.setStartTime(xmlTools.getElementValue(node, "startTime"));
            event.setEndTime(xmlTools.getElementValue(node, "endTime"));
            event.setExecutionTime(Double.valueOf(xmlTools.getElementValue(node, "executionTime")));
            populate(event.getCounter(), node);
            event.setErrorStack(xmlTools.getElementValue(node, "errorStack"));
            event.setServerOutput(xmlTools.getElementValue(node, "serverOutput"));
            event.setWarnings(xmlTools.getElementValue(node, "warnings"));
        }
    }

    private void populate(final Counter counter, final Node node) {
        if (node instanceof Element) {
            final Node counterNode = xmlTools.getElementNode(node, "counter");
            if (counterNode instanceof Element) {
                counter.setDisabled(Integer.valueOf(xmlTools.getElementValue(counterNode, "disabled")));
                counter.setSuccess(Integer.valueOf(xmlTools.getElementValue(counterNode, "success")));
                counter.setFailure(Integer.valueOf(xmlTools.getElementValue(counterNode, "failure")));
                counter.setError(Integer.valueOf(xmlTools.getElementValue(counterNode, "error")));
                counter.setWarning(Integer.valueOf(xmlTools.getElementValue(counterNode, "warning")));
            }
        }
    }

    private void populate(final Expectation expectation, final Node node) {
        if (node instanceof Element) {
            expectation.setDescription(xmlTools.getElementValue(node, "description"));
            expectation.setMessage(xmlTools.getElementValue(node, "message"));
            expectation.setCaller(xmlTools.getElementValue(node, "caller"));
        }
    }
}
