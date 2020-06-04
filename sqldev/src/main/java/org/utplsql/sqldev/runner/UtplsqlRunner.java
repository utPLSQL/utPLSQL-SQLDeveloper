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
package org.utplsql.sqldev.runner;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.swing.JFrame;

import oracle.dbtools.raptor.runner.DBStarterFactory;
import oracle.ide.Context;
import oracle.jdevimpl.runner.debug.DebuggingProcess;
import oracle.jdevimpl.runner.run.JRunner;
import org.utplsql.sqldev.coverage.CodeCoverageReporter;
import org.utplsql.sqldev.dal.RealtimeReporterDao;
import org.utplsql.sqldev.dal.RealtimeReporterEventConsumer;
import org.utplsql.sqldev.exception.GenericRuntimeException;
import org.utplsql.sqldev.model.DatabaseTools;
import org.utplsql.sqldev.model.SystemTools;
import org.utplsql.sqldev.model.runner.PostRunEvent;
import org.utplsql.sqldev.model.runner.PostSuiteEvent;
import org.utplsql.sqldev.model.runner.PostTestEvent;
import org.utplsql.sqldev.model.runner.PreRunEvent;
import org.utplsql.sqldev.model.runner.PreSuiteEvent;
import org.utplsql.sqldev.model.runner.PreTestEvent;
import org.utplsql.sqldev.model.runner.RealtimeReporterEvent;
import org.utplsql.sqldev.model.runner.Run;
import org.utplsql.sqldev.model.runner.Test;
import org.utplsql.sqldev.resources.UtplsqlResources;
import org.utplsql.sqldev.ui.runner.RunnerFactory;
import org.utplsql.sqldev.ui.runner.RunnerPanel;
import org.utplsql.sqldev.ui.runner.RunnerView;

public class UtplsqlRunner implements RealtimeReporterEventConsumer {
    private static final Logger logger = Logger.getLogger(UtplsqlRunner.class.getName());
    private static final int DEBUG_TIMEOUT_SECONDS = 60*60;

    private final boolean withCodeCoverage;
    private final List<String> pathList;
    private final List<String> schemaList;
    private final List<String> includeObjectList;
    private final List<String> excludeObjectList;
    private Context context;
    private String connectionName;
    private Connection producerConn;
    private Connection consumerConn;
    private final String realtimeReporterId = UUID.randomUUID().toString().replace("-", "");
    private final String coverageReporterId =  UUID.randomUUID().toString().replace("-", "");
    private Run run;
    private RunnerPanel panel;
    private JFrame frame; // for testing purposes only (outside of SQL Developer)
    private Thread producerThread;
    private Thread consumerThread;
    private boolean debug = false;

    public UtplsqlRunner(final List<String> pathList, final String connectionName) {
        this.withCodeCoverage = false;
        this.pathList = pathList;
        this.schemaList = null;
        this.includeObjectList = null;
        this.excludeObjectList = null;
        setConnection(connectionName);
        this.context = Context.newIdeContext();
    }
    
    public UtplsqlRunner(final List<String> pathList, final List<String> schemaList,
            final List<String> includeObjectList, final List<String> excludeObjectList, final String connectionName) {
        this.withCodeCoverage = true;
        this.pathList = pathList;
        this.schemaList = schemaList;
        this.includeObjectList = includeObjectList;
        this.excludeObjectList = excludeObjectList;
        setConnection(connectionName);
        this.context = Context.newIdeContext();
    }

    /**
     * this constructor is intended for tests only (without code coverage)
     */
    public UtplsqlRunner(final List<String> pathList, final Connection producerConn, final Connection consumerConn) {
        this.withCodeCoverage = false;
        this.pathList = pathList;
        this.schemaList = null;
        this.includeObjectList = null;
        this.excludeObjectList = null;
        this.producerConn = producerConn;
        this.consumerConn = consumerConn;
    }

    /**
     * this constructor is intended for tests only (with code coverage)
     */
    public UtplsqlRunner(final List<String> pathList, final List<String> schemaList,
            final List<String> includeObjectList, final List<String> excludeObjectList, final Connection producerConn,
            final Connection consumerConn) {
        this.withCodeCoverage = true;
        this.pathList = pathList;
        this.schemaList = schemaList;
        this.includeObjectList = includeObjectList;
        this.excludeObjectList = excludeObjectList;
        this.producerConn = producerConn;
        this.consumerConn = consumerConn;
    }

    private void setConnection(final String connectionName) {
        if (connectionName == null) {
            throw new NullPointerException("Cannot initialize a RealtimeConsumer without a ConnectionName");
        } else {
            producerConn = DatabaseTools.cloneConnection(connectionName);
            consumerConn = DatabaseTools.cloneConnection(connectionName);
        }
        this.connectionName = connectionName;
    }

    public void enableDebugging() {
        this.debug = true;
    }

    public void dispose() {
        // running in SQL Developer
        DatabaseTools.closeConnection(producerConn);
        DatabaseTools.closeConnection(consumerConn);
        if (frame != null) {
            frame.setVisible(false);
        }
        run.setConsumerConn(null);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void process(final RealtimeReporterEvent event) {
        logger.fine(event::toString);
        // dynamic dispatching code originally generated by Xtend
        if (event instanceof PostRunEvent) {
            doProcess((PostRunEvent) event);
        } else if (event instanceof PostSuiteEvent) {
            doProcess((PostSuiteEvent) event);
        } else if (event instanceof PostTestEvent) {
            doProcess((PostTestEvent) event);
        } else if (event instanceof PreRunEvent) {
            doProcess((PreRunEvent) event);
        } else if (event instanceof PreSuiteEvent) {
            // not processed
        } else if (event instanceof PreTestEvent) {
            doProcess((PreTestEvent) event);
        } else {
            throw new IllegalArgumentException("Unhandled event: " + event.toString());
        }
    }

    public static String getSysdate() {
        final Date dateTime = new Date(System.currentTimeMillis());
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000'");
        return df.format(dateTime);
    }

    public boolean isRunning() {
        return run != null && run.getEndTime() == null;
    }

    private void initRun() {
        run = new Run(realtimeReporterId, connectionName, pathList);
        run.setStartTime(getSysdate());
        run.getCounter().setDisabled(0);
        run.getCounter().setSuccess(0);
        run.getCounter().setFailure(0);
        run.getCounter().setError(0);
        run.getCounter().setWarning(0);
        run.setInfoCount(0);
        run.setTotalNumberOfTests(-1);
        run.setCurrentTestNumber(0);
        run.setStatus(UtplsqlResources.getString("RUNNER_INITIALIZING_TEXT"));
        run.setConsumerConn(consumerConn);
        panel.setModel(run);
        panel.update(realtimeReporterId);
    }
    
    private void doProcess(final PreRunEvent event) {
        run.setTotalNumberOfTests(event.getTotalNumberOfTests());
        run.put(event.getItems());
        run.setStatus(UtplsqlResources.getString("RUNNER_RUNNING_TEXT"));
        panel.update(realtimeReporterId);
    }

    private void doProcess(final PostRunEvent event) {
        run.setStartTime(event.getStartTime());
        run.setEndTime(event.getEndTime());
        run.setExecutionTime(event.getExecutionTime());
        run.setErrorStack(event.getErrorStack());
        run.setServerOutput(event.getServerOutput());
        run.setStatus(UtplsqlResources.getString("RUNNER_FINISHED_TEXT"));
        panel.update(realtimeReporterId);
    }

    private void doProcess(final PostSuiteEvent event) {
        final Test test = run.getCurrentTest();
        // Errors on suite levels are reported as warnings by the utPLSQL framework, 
        // since an error on suite level does not affect a status of a test.
        // It is possible that the test is OK, but contains error messages on suite level(s)
        // Populating test.errorStack would be a) wrong and b) redundant
        if (event.getWarnings() != null) {
            if (test.getCounter().getWarning() == 0) {
                test.getCounter().setWarning(1);
                test.getCounter().setWarning(run.getCounter().getWarning() + 1);
            }
            StringBuilder sb = new StringBuilder();
            if (test.getWarnings() != null) {
                sb.append(test.getWarnings());
                sb.append("\n\n");
            }
            sb.append("For suite ");
            sb.append(event.getId());
            sb.append(":\n\n");
            sb.append(event.getWarnings());
            test.setWarnings(sb.toString());
        }
        if (event.getServerOutput() != null) {
            if (test.getServerOutput() == null) {
                run.setInfoCount(run.getInfoCount() + 1);
            }
            StringBuilder sb = new StringBuilder();
            if (test.getServerOutput() != null) {
                sb.append(test.getServerOutput());
                sb.append("\n\n");
            }
            sb.append("For suite ");
            sb.append(event.getId());
            sb.append(":\n\n");
            sb.append(event.getServerOutput());
            test.setServerOutput(sb.toString());
        }
        panel.update(realtimeReporterId);
    }

    private void doProcess(final PreTestEvent event) {
        final Test test = run.getTest(event.getId());
        if (test == null) {
            logger.severe(() -> "Could not find test id \"" + event.getId() + "\" when processing PreTestEvent "
                    + event.toString() + ".");
        } else {
            test.setStartTime(getSysdate());
        }
        run.setStatus(event.getId() + "...");
        run.setCurrentTestNumber(event.getTestNumber());
        run.setCurrentTest(test);
        panel.update(realtimeReporterId);
    }

    private void doProcess(final PostTestEvent event) {
        final Test test = run.getTest(event.getId());
        if (test == null) {
            logger.severe(() -> "Could not find test id \"" + event.getId() + "\" when processing PostTestEvent "
                    + event.toString() + ".");
        } else {
            test.setStartTime(event.getStartTime());
            test.setEndTime(event.getEndTime());
            test.setExecutionTime(event.getExecutionTime());
            test.setCounter(event.getCounter());
            test.setErrorStack(event.getErrorStack());
            test.setServerOutput(event.getServerOutput());
            if (test.getServerOutput() != null) {
                run.setInfoCount(run.getInfoCount() + 1);
            }
            test.setFailedExpectations(event.getFailedExpectations());
            test.setWarnings(event.getWarnings());
            if (test.getWarnings() != null) {
                test.getCounter().setWarning(1);
            } else {
                test.getCounter().setWarning(0);
            }
            run.getCounter().setWarning(run.getCounter().getWarning() + test.getCounter().getWarning());
        }
        run.getCounter().setDisabled(run.getCounter().getDisabled() + event.getCounter().getDisabled());
        run.getCounter().setSuccess(run.getCounter().getSuccess() + event.getCounter().getSuccess());
        run.getCounter().setFailure(run.getCounter().getFailure() + event.getCounter().getFailure());
        run.getCounter().setError(run.getCounter().getError() + event.getCounter().getError());
        panel.update(realtimeReporterId);
    }

    private void produceReportWithDebugger(String anonymousPlsqlBlock) {
        try {
            Context processContext = JRunner.prepareProcessContext(context, false);
            DebuggingProcess process = new DebuggingProcess(processContext);
            DBStarterFactory.PlSqlStarter starter = new DBStarterFactory.PlSqlStarter(process, anonymousPlsqlBlock, connectionName, context);
            starter.start();
        } catch (Throwable t) {
            String msg = t.getClass().getName() + " while debugging utPLSQL test.";
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg);
        }
    }

    private void produce() {
        try {
            logger.fine(() -> "Running utPLSQL tests and producing events via reporter id " + realtimeReporterId + "...");
            final RealtimeReporterDao dao = new RealtimeReporterDao(producerConn);
            if (withCodeCoverage) {
                dao.produceReportWithCoverage(realtimeReporterId, coverageReporterId, pathList, schemaList, includeObjectList, excludeObjectList);
            } else {
                if (!debug) {
                    dao.produceReport(realtimeReporterId, pathList);
                } else {
                    produceReportWithDebugger(dao.getProduceReportPlsql(realtimeReporterId, pathList));
                }
            }
            logger.fine(() -> "All events produced for reporter id " + realtimeReporterId + ".");
        } catch (Exception e) {
            logger.severe(() -> "Error while producing events for reporter id " + realtimeReporterId + ": " + e.getMessage() + ".");
        }
    }

    private void consume() {
        try {
            try {
                logger.fine(() -> "Consuming events from reporter id " + realtimeReporterId + " in realtime...");
                final RealtimeReporterDao dao = new RealtimeReporterDao(consumerConn);
                if (!debug) {
                    dao.consumeReport(realtimeReporterId, this);
                } else {
                    dao.consumeReport(realtimeReporterId, this, DEBUG_TIMEOUT_SECONDS);
                }
                logger.fine(() -> "All events consumed.");
                if (withCodeCoverage) {
                    String html = dao.getHtmlCoverage(coverageReporterId);
                    CodeCoverageReporter.openInBrowser(html);
                }
            } catch (Exception e) {
                logger.severe(() -> "Error while consuming events for reporter id " + realtimeReporterId + ": " + e.getMessage() + ".");
            }
        } finally {
            if (run.getTotalNumberOfTests() < 0) {
                run.setStatus(UtplsqlResources.getString("RUNNER_NO_TESTS_FOUND_TEXT"));
                run.setExecutionTime((System.currentTimeMillis() - Double.valueOf(run.getStart())) / 1000);
                run.setEndTime(getSysdate());
                run.setTotalNumberOfTests(0);
                panel.update(realtimeReporterId);
            }
            if (isRunningInSqlDeveloper()) {
                dispose();
            }
        }
    }

    private boolean isRunningInSqlDeveloper() {
        return (connectionName != null);
    }

    private boolean initGUI() {
        RunnerView dockable = null;
        if (isRunningInSqlDeveloper() && (dockable = RunnerFactory.getDockable()) == null) {
            logger.severe(() -> "Error getting utPLSQL dockable. Cannot run utPLSQL test.");
            return false;
        } else {
            if (isRunningInSqlDeveloper()) {
                RunnerFactory.showDockable();
                panel = dockable.getRunnerPanel();
                context.setView(dockable);
            } else {
                frame = new JFrame("utPLSQL Runner Panel");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                panel = new RunnerPanel();
                frame.add(panel.getGUI());
                frame.setPreferredSize(new Dimension(600, 800));
                frame.pack();
                final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setLocation(dim.width / 2 - frame.getSize().width / 2,
                        dim.height / 2 - frame.getSize().height / 2);
                frame.setVisible(true);
            }
            initRun();
            return true;
        }
    }

    public void runTestAsync() {
        // start tests when the GUI has been successfully initialized.
        if (initGUI()) {
            // the consumer
            consumerThread = new Thread(this::consume);
            consumerThread.setName("realtime consumer");
            consumerThread.start();
            // avoid concurrency on output header table to fix issue #80
            SystemTools.sleep(100);
            // the producer
            producerThread = new Thread(this::produce);
            producerThread.setName("realtime producer");
            producerThread.start();
        }
    }

    public Thread getProducerThread() {
        return producerThread;
    }

    public Thread getConsumerThread() {
        return consumerThread;
    }
}
