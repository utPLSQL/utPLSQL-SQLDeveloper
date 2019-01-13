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

import java.sql.Connection
import java.util.List
import java.util.UUID
import java.util.logging.Logger
import oracle.dbtools.raptor.utils.Connections
import org.utplsql.sqldev.dal.RealtimeReporterDao
import org.utplsql.sqldev.dal.RealtimeReporterEventConsumer
import org.utplsql.sqldev.model.runner.RealtimeReporterEvent

class UtPlsqlRunner implements RealtimeReporterEventConsumer {

	static val Logger logger = Logger.getLogger(UtPlsqlRunner.name);

	var List<String> pathList
	var Connection producerConn
	var Connection consumerConn
	var String reporterId = UUID.randomUUID().toString.replace("-", "");

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
	}
	
	def dispose() {
		producerConn.close;
		consumerConn.close;
	}
	
	override void process(RealtimeReporterEvent event) {
		logger.fine(event.toString)
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
	}
	
	def runAsync() {
		// the producer
		val Runnable producer = [|produce]
		val producerThread = new Thread(producer)
		producerThread.name = "realtime producer"
		producerThread.start
		// the consumer
		val Runnable consumer = [|consume]
		val consumerThread = new Thread(consumer)
		consumerThread.name = "realtime consumer"
		consumerThread.start
	}
}
