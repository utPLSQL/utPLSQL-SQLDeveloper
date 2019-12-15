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

import java.util.regex.Pattern
import org.eclipse.xtend.lib.annotations.Accessors
import org.utplsql.sqldev.model.AbstractModel

@Accessors
class Expectation extends AbstractModel {
	String description
	String message
	String caller
	
	def getFailureText() {
		return '''
			«message.trim»
			«caller?.trim»
		'''.toString.trim
	}
	
	def getShortFailureText() {
		return '''«IF description !== null»«description» (line «callerLine»)«ELSE»Line «callerLine»«ENDIF»'''.toString
	}
	
	def getCallerLine() {
		var Integer line = null
		if (caller !== null) {
			val p = Pattern.compile("(?i)\"[^\\\"]+\",\\s+line\\s*([0-9]+)")
			val m = p.matcher(caller)
			if (m.find) {
				line = Integer.valueOf(m.group(1))
			}
		}
		return line
	}
}