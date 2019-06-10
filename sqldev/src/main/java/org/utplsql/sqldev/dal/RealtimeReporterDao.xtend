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
 package org.utplsql.sqldev.dal

import java.io.StringReader
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.List
import java.util.logging.Logger
import javax.xml.parsers.DocumentBuilderFactory
import oracle.jdbc.OracleTypes
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.CallableStatementCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.utplsql.sqldev.model.XMLTools
import org.utplsql.sqldev.model.runner.Counter
import org.utplsql.sqldev.model.runner.Expectation
import org.utplsql.sqldev.model.runner.PostEvent
import org.utplsql.sqldev.model.runner.PostRunEvent
import org.utplsql.sqldev.model.runner.PostSuiteEvent
import org.utplsql.sqldev.model.runner.PostTestEvent
import org.utplsql.sqldev.model.runner.PreRunEvent
import org.utplsql.sqldev.model.runner.PreSuiteEvent
import org.utplsql.sqldev.model.runner.PreTestEvent
import org.utplsql.sqldev.model.runner.RealtimeReporterEvent
import org.utplsql.sqldev.model.runner.Suite
import org.utplsql.sqldev.model.runner.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource

class RealtimeReporterDao {
	static val Logger logger = Logger.getLogger(RealtimeReporterDao.name);
	static val FIRST_VERSION_WITH_REALTIME_REPORTER = 3001004
	val extension XMLTools xmlTools = new XMLTools
	var Connection conn
	var JdbcTemplate jdbcTemplate

	new(Connection connection) {
		conn = connection
		jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(conn, true))
		jdbcTemplate.fetchSize = 1
	}
	
	def isSupported() {
		return new UtplsqlDao(conn).normalizedUtPlsqlVersionNumber >= FIRST_VERSION_WITH_REALTIME_REPORTER
	}
	
	def produceReport(String reporterId, List<String> pathList) {
		var plsql = '''
			DECLARE
			   l_reporter ut_realtime_reporter := ut_realtime_reporter();
			BEGIN
			   l_reporter.set_reporter_id(?);
			   l_reporter.output_buffer.init();
			   sys.dbms_output.enable(NULL);
			   ut_runner.run(
			      a_paths     => ut_varchar2_list(
			                        «FOR path : pathList SEPARATOR ","»
			                           '«path»'
			                        «ENDFOR»
			                     ),
			      a_reporters => ut_reporters(l_reporter)
			   );
			   sys.dbms_output.disable;
			END;
		'''
		jdbcTemplate.update(plsql, #[reporterId])
	}
		
	def consumeReport(String reporterId, RealtimeReporterEventConsumer consumer) {
		val plsql = '''
			DECLARE
			   l_reporter ut_realtime_reporter := ut_realtime_reporter();
			BEGIN
			   l_reporter.set_reporter_id(?);
			   OPEN ? FOR
			      SELECT t.item_type, t.text
			        FROM table(l_reporter.get_lines()) t;
			END;
		'''
		jdbcTemplate.execute(plsql, new CallableStatementCallback<Void>() {
			override doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
				cs.setString(1, reporterId)
				cs.registerOutParameter(2, OracleTypes.CURSOR)
				cs.execute
				val rs = cs.getObject(2) as ResultSet
				while(rs.next) {
					val itemType = rs.getString("item_type")
					val textClob = rs.getClob("text")
					val textString = textClob.getSubString(1, textClob.length as int)
					val event = convert(itemType, textString)
					if (event !== null) {
						consumer.process(event)
					}
				}
				rs.close
				return null
			}
		})
	}
	
	private def RealtimeReporterEvent convert(String itemType, String text) {
		logger.fine('''
			---- «itemType» ----
			«text»
		''')
		val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		val	doc = docBuilder.parse(new InputSource(new StringReader(text)))
		var RealtimeReporterEvent event
		if (itemType == "pre-run") {
			event = doc.convertToPreRunEvent
		} else if (itemType == "post-run") {
			event = doc.convertToPostRunEvent
		} else if (itemType == "pre-suite") {
			event = doc.convertToPreSuiteEvent
		} else if (itemType == "post-suite") {
			event = doc.convertToPostSuiteEvent
		} else if (itemType == "pre-test") {
			event = doc.convertToPreTestEvent
		} else if (itemType == "post-test") {
			event = doc.convertToPostTestEvent
		}
		return event
	}
	
	private def RealtimeReporterEvent convertToPreRunEvent(Document doc) {
		val event = new PreRunEvent
		event.totalNumberOfTests = Integer.valueOf(doc.getNode("/event/totalNumberOfTests")?.textContent)
		val nodeList = doc.getNodeList("/event/items/*")
		for (i : 0 ..< nodeList.length) {
			val node = nodeList.item(i)
			if (node.nodeName == "suite") {
				val suite = new Suite
				event.items.add(suite)
				suite.populate(node)
			} else if (node.nodeName == "test") {
				val test = new Test
				event.items.add(test)
				test.populate(node)
			}
		}
		return event
	}

	private def RealtimeReporterEvent convertToPostRunEvent(Document doc) {
		val event = new PostRunEvent
		event.populate(doc.getNode("/event/run"))
		return event
	}

	private def RealtimeReporterEvent convertToPreSuiteEvent(Document doc) {
		val event = new PreSuiteEvent
		val node = doc.getNode("/event/suite")
		if (node instanceof Element) {
			event.id = node.attributes?.getNamedItem("id")?.nodeValue
		}
		return event
	}
	
	private def RealtimeReporterEvent convertToPostSuiteEvent(Document doc) {
		val event = new PostSuiteEvent
		val node = doc.getNode("/event/suite")
		if (node instanceof Element) {
			event.id = node.attributes?.getNamedItem("id")?.nodeValue
			event.populate(node)
		}
		return event
	}

	private def RealtimeReporterEvent convertToPreTestEvent(Document doc) {
		val event = new PreTestEvent
		val node = doc.getNode("/event/test")
		if (node instanceof Element) {
			event.id = node.attributes?.getNamedItem("id")?.nodeValue
			event.testNumber = Integer.valueOf(node.getElementsByTagName("testNumber")?.item(0)?.textContent)
			event.totalNumberOfTests = Integer.valueOf(node.getElementsByTagName("totalNumberOfTests")?.item(0)?.textContent)
		}
		return event
	}

	private def RealtimeReporterEvent convertToPostTestEvent(Document doc) {
		val event = new PostTestEvent
		val node = doc.getNode("/event/test")
		if (node instanceof Element) {
			event.id = node.attributes?.getNamedItem("id")?.nodeValue
			event.testNumber = Integer.valueOf(node.getElementsByTagName("testNumber")?.item(0)?.textContent)
			event.totalNumberOfTests = Integer.valueOf(node.getElementsByTagName("totalNumberOfTests")?.item(0)?.textContent)
			event.populate(node)
			val failedExpectations = node.getNodeList("failedExpectations/expectation")
			for (i : 0 ..< failedExpectations.length) {
				val expectationNode = failedExpectations.item(i)
				val expectation = new Expectation
				event.failedExpectations.add(expectation)
				expectation.populate(expectationNode)
			}
		}
		return event
	}

	private def void populate(Suite suite, Node node) {
		if (node instanceof Element) {
			suite.id = node.attributes?.getNamedItem("id")?.nodeValue
			suite.name = node.getElementsByTagName("name")?.item(0)?.textContent
			suite.description = node.getElementsByTagName("description")?.item(0)?.textContent
			val nodeList = node.getNodeList("items/*")
			for (i : 0 ..< nodeList.length) {
				val childNode = nodeList.item(i)
				if (childNode.nodeName == "suite") {
					val childSuite = new Suite
					suite.items.add(childSuite)
					childSuite.populate(childNode)
				} else if (childNode.nodeName == "test") {
					val childTest = new Test
					suite.items.add(childTest)
					childTest.populate(childNode)
				}
			}
		}
	}
	
	private def void populate(Test test, Node node) {
		if (node instanceof Element) {
			test.id = node.attributes?.getNamedItem("id")?.nodeValue
			test.executionType = node.getElementsByTagName("executionType")?.item(0)?.textContent
			test.ownerName = node.getElementsByTagName("ownerName")?.item(0)?.textContent
			test.objectName = node.getElementsByTagName("objectName")?.item(0)?.textContent
			test.procedureName = node.getElementsByTagName("procedureName")?.item(0)?.textContent
			test.disabled = node.getElementsByTagName("disabled")?.item(0)?.textContent == "true"
			test.name = node.getElementsByTagName("name")?.item(0)?.textContent
			test.description = node.getElementsByTagName("description")?.item(0)?.textContent
			test.testNumber = Integer.valueOf(node.getElementsByTagName("testNumber")?.item(0)?.textContent)
		}
	}
	
	private def void populate(PostEvent event, Node node) {
		if (node instanceof Element) {
			event.startTime = node.getElementsByTagName("startTime")?.item(0)?.textContent
			event.endTime = node.getElementsByTagName("endTime")?.item(0)?.textContent
			event.executionTime = Double.valueOf(node.getElementsByTagName("executionTime")?.item(0)?.textContent)
			event.counter.populate(node)
			event.errorStack = node.getElementsByTagName("errorStack")?.item(0)?.textContent
			event.serverOutput = node.getElementsByTagName("serverOutput")?.item(0)?.textContent
		}
	}
	
	private def void populate(Counter counter, Node node) {
		if (node instanceof Element) {
			val counterNode = node.getElementsByTagName("counter")?.item(0)
			if (counterNode instanceof Element) {
				counter.disabled = Integer.valueOf(counterNode.getElementsByTagName("disabled")?.item(0)?.textContent)
				counter.success = Integer.valueOf(counterNode.getElementsByTagName("success")?.item(0)?.textContent)
				counter.failure = Integer.valueOf(counterNode.getElementsByTagName("failure")?.item(0)?.textContent)
				counter.error = Integer.valueOf(counterNode.getElementsByTagName("error")?.item(0)?.textContent)
				counter.warning = Integer.valueOf(counterNode.getElementsByTagName("warning")?.item(0)?.textContent)
			}
		}
	}

	private def void populate(Expectation expectation, Node node) {
		if (node instanceof Element) {
			expectation.description = node.getElementsByTagName("description")?.item(0)?.textContent
			expectation.message = node.getElementsByTagName("message")?.item(0)?.textContent
			expectation.caller = node.getElementsByTagName("caller")?.item(0)?.textContent
		}
	}
}