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
package org.utplsql.sqldev.model.runner

import java.util.List
import javax.swing.Icon
import org.eclipse.xtend.lib.annotations.Accessors
import org.utplsql.sqldev.resources.UtplsqlResources

@Accessors
class Test extends Item {
	String executionType
	String ownerName
	String objectName
	String procedureName
	Boolean disabled
	String name
	String description
	Integer testNumber
	List<Expectation> failedExpectations
	
	def getStatusIcon() {
		var Icon icon = null
		if (counter !== null) {
			if (counter.success > 0) {
				icon = UtplsqlResources.getIcon("SUCCESS_ICON")
			} else if (counter.error > 0) {
				icon = UtplsqlResources.getIcon("ERROR_ICON")
			} else if (counter.failure > 0) {
				icon = UtplsqlResources.getIcon("FAILURE_ICON")
			} else if (counter.disabled > 0) {
				icon = UtplsqlResources.getIcon("DISABLED_ICON")
			}		
		}
		return icon
	}
}