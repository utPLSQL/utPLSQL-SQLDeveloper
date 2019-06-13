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
package org.utplsql.sqldev.runner

import java.awt.Dimension
import java.awt.Toolkit
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.List
import java.util.UUID
import java.util.logging.Logger
import javax.swing.JFrame
import oracle.dbtools.raptor.utils.Connections
import org.utplsql.sqldev.dal.RealtimeReporterDao
import org.utplsql.sqldev.dal.RealtimeReporterEventConsumer
import org.utplsql.sqldev.model.runner.PostRunEvent
import org.utplsql.sqldev.model.runner.PostSuiteEvent
import org.utplsql.sqldev.model.runner.PostTestEvent
import org.utplsql.sqldev.model.runner.PreRunEvent
import org.utplsql.sqldev.model.runner.PreSuiteEvent
import org.utplsql.sqldev.model.runner.PreTestEvent
import org.utplsql.sqldev.model.runner.RealtimeReporterEvent
import org.utplsql.sqldev.model.runner.Run
import org.utplsql.sqldev.resources.UtplsqlResources
import org.utplsql.sqldev.ui.runner.RunnerFactory
import org.utplsql.sqldev.ui.runner.RunnerPanel
import org.utplsql.sqldev.ui.runner.RunnerView

class UtplsqlRunner implements RealtimeReporterEventConsumer {

	static val Logger logger = Logger.getLogger(UtplsqlRunner.name);

	var List<String> pathList
	var String connectionName
	var Connection producerConn
	var Connection consumerConn
	val String reporterId = UUID.randomUUID().toString.replace("-", "")
	var Run run
	var RunnerPanel panel
	var Thread producerThread
	var Thread consumerThread

	new(List<String> pathList, String connectionName) {
		this.pathList = pathList
		setConnection(connectionName)
	}

	/**
	 * this constructor is intended for tests only
	 */
	new(List<String> pathList, Connection producerConn, Connection consumerConn) {
		this.pathList = pathList
		this.producerConn = producerConn
		this.consumerConn = consumerConn
	}

	private def setConnection(String connectionName) {
		if (connectionName === null) {
			throw new RuntimeException("Cannot initialize a RealtimeConsumer without a ConnectionName")
		} else {
			this.producerConn = Connections.instance.cloneConnection(Connections.instance.getConnection(connectionName))
			this.consumerConn = Connections.instance.cloneConnection(Connections.instance.getConnection(connectionName))
		}
		this.connectionName = connectionName
	}
	
	def dispose() {
		producerConn.close;
		consumerConn.close;
	}
	
	override void process(RealtimeReporterEvent event) {
		logger.fine(event.toString)
		event.doProcess
	}
	
	private def getSysdate() {
		val dateTime = new Date(System.currentTimeMillis);
		val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000'");
		return df.format(dateTime)
      
	}
	
	private def initRun() {
		run = new Run(reporterId, connectionName, pathList)
		run.startTime = sysdate
		run.counter.disabled = 0
		run.counter.success = 0
		run.counter.failure = 0
		run.counter.error = 0
		run.counter.warning = 0
		run.infoCount = 0
		run.totalNumberOfTests = -1
		run.status = UtplsqlResources.getString("RUNNER_INITIALIZING_TEXT")
		panel.model = run
		panel.update(reporterId)
	}
	
	private def dispatch doProcess(PreRunEvent event) {
		run.totalNumberOfTests = event.totalNumberOfTests
		run.put(event.items)
		run.status = UtplsqlResources.getString("RUNNER_RUNNING_TEXT")
		panel.update(reporterId)	
	}

	private def dispatch doProcess(PostRunEvent event) {
		run.startTime = event.startTime
		run.endTime = event.endTime
		run.executionTime = event.executionTime
		run.errorStack = event.errorStack
		run.serverOutput = event.serverOutput
		run.status = String.format(UtplsqlResources.getString("RUNNER_FINNISHED_TEXT"), event.executionTime)
		panel.update(reporterId)
	}
	
	private def dispatch doProcess(PreSuiteEvent event) {
		// ignore
	}
	
	private def dispatch doProcess(PostSuiteEvent event) {
		// ignore
	}
	
	private def dispatch doProcess(PreTestEvent event) {
		val test = run.getTest(event.id)
		if (test === null) {
			logger.severe('''Could not find test id "«event.id»" when processing PreTestEvent «event.toString».''')
		} else {
			test.startTime = sysdate
		}
		run.status = event.id
		panel.update(reporterId)
	}

	private def dispatch doProcess(PostTestEvent event) {
		val test = run.getTest(event.id)
		if (test === null) {
			logger.severe('''Could not find test id "«event.id»"" when processing PostTestEvent «event.toString».''')
		} else {
			test.startTime = event.startTime
			test.endTime = event.endTime
			test.executionTime = event.executionTime
			test.counter = event.counter
			test.errorStack = event.errorStack
			test.serverOutput = event.serverOutput
			if (test.serverOutput !== null) {
				run.infoCount = run.infoCount + 1
			}
			test.failedExpectations = event.failedExpectations
			test.warnings = event.warnings
			if (test.warnings !== null) {
				// it does not matter how many rows are used by utPLSQL to store a warning event
				test.counter.warning = 1
			} else {
				test.counter.warning = 0
			}
		}
		run.counter.disabled = run.counter.disabled + event.counter.disabled
		run.counter.success = run.counter.success + event.counter.success
		run.counter.failure = run.counter.failure + event.counter.failure
		run.counter.error = run.counter.error + event.counter.error
		run.counter.warning = run.counter.warning + test.counter.warning
		panel.update(reporterId)
	}

	private def void produce() {
		try {
			logger.fine('''Running utPLSQL tests and producing events via reporter id «reporterId»...''')
			val dao = new RealtimeReporterDao(producerConn)
			dao.produceReport(reporterId, pathList)
			logger.fine('''All events produced for reporter id «reporterId».''')
		} catch (Exception e) {
			logger.severe('''Error while producing events for reporter id «reporterId»: «e?.message»''')
		}
	}

	private def void consume() {
		try {
			logger.fine('''Consuming events from reporter id «reporterId» in realtime...''')
			val dao = new RealtimeReporterDao(consumerConn)
			dao.consumeReport(reporterId, this)
			logger.fine('''All events consumed.''')
		} catch (Exception e) {
			logger.severe('''Error while consuming events for reporter id «reporterId»: «e?.message»''')
		}
		if (run.totalNumberOfTests < 0) {
			run.status = UtplsqlResources.getString("RUNNER_NO_TESTS_FOUND_TEXT")
			run.totalNumberOfTests = 0
			panel.update(reporterId)
		}
	}
	
	private def isRunningInSqlDeveloper() {
		return connectionName !== null
	}
	
	private def initGUI() {
		var RunnerView dockable = null
		if (runningInSqlDeveloper && (dockable = RunnerFactory.dockable as RunnerView) === null) {
			logger.severe('''Error getting utPLSQL dockable. Cannot run utPLSQL test.''')
			return false
		} else {
			if (runningInSqlDeveloper) {
				RunnerFactory.showDockable;
				panel = dockable.runnerPanel
			} else {
				val frame = new JFrame("utPLSQL Runner Panel")
				frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
				panel = new RunnerPanel
				frame.add(panel.getGUI)
				frame.preferredSize = new Dimension(600, 800)
				frame.pack
				val dim = Toolkit.getDefaultToolkit().getScreenSize();
				frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
				frame.setVisible(true)
			}
			initRun
		}
		return true
	}
	
	def runTestAsync() {
		// start tests when the GUI has been successfully initialized.
		if (initGUI) {
			// the producer
			val Runnable producer = [|produce]
			producerThread = new Thread(producer)
			producerThread.name = "realtime producer"
			producerThread.start
			// the consumer
			val Runnable consumer = [|consume]
			consumerThread = new Thread(consumer)
			consumerThread.name = "realtime consumer"
			consumerThread.start
		}
	}
	
	def getProducerThread() {
		return producerThread
	}
	
	def getConsumerThread() {
		return consumerThread
	}
	
}
