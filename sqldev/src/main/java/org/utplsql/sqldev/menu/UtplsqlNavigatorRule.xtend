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
package org.utplsql.sqldev.menu

import java.util.Map
import java.util.logging.Logger
import oracle.dbtools.raptor.navigator.db.DatabaseConnection
import oracle.dbtools.raptor.navigator.impl.ChildObjectElement
import oracle.dbtools.raptor.navigator.impl.ObjectFolder
import oracle.dbtools.raptor.navigator.plsql.PlSqlNode
import oracle.ide.^extension.rules.RuleEvaluationContext
import oracle.ide.^extension.rules.RuleEvaluationException
import oracle.ide.^extension.rules.RuleFunction
import oracle.ide.^extension.rules.RuleFunctionParameter
import org.utplsql.sqldev.model.URLTools

class UtplsqlNavigatorRule extends RuleFunction {
	private static final Logger logger = Logger.getLogger(UtplsqlNavigatorRule.name);
	private val extension URLTools urlTools = new URLTools

	override evaluate(RuleEvaluationContext ruleContext,
		Map<String, RuleFunctionParameter> parameters) throws RuleEvaluationException {
		val element = ruleContext.ideContext.selection.get(0)
		var boolean enable = false
		logger.fine('''selected object is of type «element.class.name»''')
		if (element instanceof DatabaseConnection) {
			enable = true
		} else if (element instanceof ObjectFolder) {
			if (element.objectType == "PACKAGE") {
				enable = true
			}
		} else if (element instanceof PlSqlNode) {
			if (element.objectType == "PACKAGE" || element.objectType == "PACKAGE BODY") {
				enable = true
			}
		} else if (element instanceof ChildObjectElement) {
			if (element.URL.objectType == "PACKAGE") {
				enable = true
			}
		}
		return enable
	}
}
