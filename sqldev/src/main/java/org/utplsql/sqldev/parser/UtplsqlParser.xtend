/* Copyright 2018 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
package org.utplsql.sqldev.parser

import java.util.ArrayList
import java.util.regex.Pattern
import org.utplsql.sqldev.model.parser.Unit
import org.utplsql.sqldev.model.parser.PlsqlObject

class UtplsqlParser {
	private String plsql
	private ArrayList<PlsqlObject> objects = new ArrayList<PlsqlObject>
	private ArrayList<Unit> units = new ArrayList<Unit>
	
	new(String plsql) {
		this.plsql = plsql
		populateObjects
		populateUnits
	}
	
	private def populateObjects() {
		val p = Pattern.compile("(?i)(\\s*)(create(\\s+or\\s+replace)?\\s+(package|type)\\s+(body\\s+)?)(.*?)(\\s+)", Pattern.DOTALL)
		val m = p.matcher(plsql)
		while (m.find) {
			val o = new PlsqlObject
			o.name = m.group(6)
			o.position = m.start
			objects.add(o)
		}
	}
	
	private def populateUnits() {
		val p = Pattern.compile("(?i)(\\s*)(function|procedure)(\\s+)(.*?)(\\s+)", Pattern.DOTALL)
		val m = p.matcher(plsql)
		while (m.find) {
			val u = new Unit
			u.name = m.group(4)
			u.position = m.start
			units.add(u)
		}
	}
	
	private def getObjectNameAt(int position) {
		var name = ""
		for (o : objects) {
			if (o.position < position) {
				name = o.name
			}
		}
		return name
	}
	
	private def getUnitNameAt(int position) {
		var name = ""
		for (u : units) {
			if (u.position <= position) {
				name = u.name
			}
		}
		return name
	}
	
	private def removeQuotes(String name) {
		return name.replace("\"", "")
	}
	
	def getObjects() {
		return objects
	}
	
	def getUnits() {
		return units
	}

	def getUtPlsqlCall(int position) {
		var objectName = getObjectNameAt(position)
		if (!objectName.empty) {
			var unitName = getUnitNameAt(position)
			if (unitName.empty) {
				return '''ut.run('«objectName.removeQuotes»');'''
			} else {
				return '''ut.run('«objectName.removeQuotes».«unitName.removeQuotes»');'''
			}
		}
		return ""
	}

}