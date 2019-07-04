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

import oracle.dbtools.parser.LexerToken
import oracle.dbtools.raptor.navigator.plsql.PlSqlArguments
import oracle.dbtools.raptor.navigator.plsql.PlsqlStructureParser

/*
 * Cannot use this class within SQL Developer because the
 * package oracle.dbtools.parser is not exported in sqldeveloper OSGI bundle (extension)
 * (throws ClassNotFoundException at runtime).
 * 
 * The dbtools-common.jar contains the necessary packages, 
 * but it cannot be distributed with the utPLSQL extension 
 * without violating the Oracle license agreement.
 */
class SqlDevParser {
	def getMembers(String plsql) {
		val tokens = LexerToken.parse(plsql)
		val parser = new PlsqlStructureParser
		parser.parse(tokens, PlSqlArguments.sort)
		return parser.children
	}
	
	private def getStartLine(String plsql, int offset) {
		var int line = 1
		for (var i = 0; i < plsql.length; i++) {
			val c = plsql.substring(i, i+1)
			if (i > offset) {
				return line
			} else if (c == '\n') {
				line = line + 1
			}
		}
		return line
	}
	
	def getMemberStartLine(String plsql, String memberName) {
		val members = plsql.members
		val member = members.findFirst[it.name.equalsIgnoreCase(memberName)]
		if (member !== null) {
			return getStartLine(plsql, member.codeOffset)
		} else {
			1
		}
	}
}