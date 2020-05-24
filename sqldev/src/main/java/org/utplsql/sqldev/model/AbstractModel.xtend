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
package org.utplsql.sqldev.model

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder
import org.springframework.core.style.ToStringStyler

abstract class AbstractModel {
	static final ToStringStyler STYLER = new UtplsqlToStringStyler();
	
	override toString() {
		new ToStringBuilder(this).addAllFields.toString
	}
	
	def getStyler() {
		return STYLER;
	}
}
