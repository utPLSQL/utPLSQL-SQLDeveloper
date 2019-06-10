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
package org.utplsql.sqldev.model.runner

import java.util.LinkedHashMap
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import org.utplsql.sqldev.model.AbstractModel

@Accessors
class Run extends AbstractModel {
	String reporterId
	String connectionName
	Integer totalNumberOfTests
	String startTime
	String endTime
	Double executionTime
	Counter counter
	String errorStack
	String serverOutput
	LinkedHashMap<String, Test> tests
	String status
	
	new(String reporterId, String connectionName) {
		this.reporterId = reporterId
		this.connectionName = connectionName
		this.counter = new Counter
		this.tests = new LinkedHashMap<String, Test>
	}
	
	def getName() {
		return '''«startTime» («connectionName»)'''
	}
	
	def void put(List<Item> items) {
		for (item : items) {
			if (item instanceof Test) {
				this.tests.put(item.id, item)
			}
			if (item instanceof Suite) {
				item.items.put
			}
		}
	}
	
	def getTest(String id) {
		return tests.get(id)
	}

	def getTotalNumberOfCompletedTests() {
		return counter.disabled + counter.success + counter.failure + counter.error
	}
	
}
