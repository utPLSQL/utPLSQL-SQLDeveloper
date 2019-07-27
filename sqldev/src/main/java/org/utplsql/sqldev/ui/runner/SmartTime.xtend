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
package org.utplsql.sqldev.ui.runner

import java.text.DecimalFormat

class SmartTime {
	var Double seconds
	var boolean smart = false
	
	new() {
		super()
	}
	
	new(Double seconds, boolean smart) {
		super()
		this.seconds = seconds
		this.smart = smart
	}
	
	def setMillis(Double seconds) {
		this.seconds = seconds
	}
	
	def setSmart(boolean smart) {
		this.smart = smart
	}
	
	def getSeconds() {
		return seconds
	} 
	
	override toString() {
		var String ret;
		if (seconds === null) {
			ret = null
		} else if (smart) {
			if (seconds >= 60*60) {
				val DecimalFormat formatter = new DecimalFormat("#0.00")
				ret = formatter.format(seconds / 60 / 60) + " h"
			} else if (seconds >= 60) {
				val DecimalFormat formatter = new DecimalFormat("#0.00")
				ret = formatter.format(seconds / 60) + " min"
			} else if (seconds >= 1) {
				val DecimalFormat formatter = new DecimalFormat("#0.000")
				ret = formatter.format(seconds) + " s"
			} else {
				val DecimalFormat formatter = new DecimalFormat("##0")
				ret = formatter.format(seconds * 1000) + " ms"				
			}
			
		} else {
			val DecimalFormat formatter = new DecimalFormat("##,##0.000")
			ret = formatter.format(seconds)
		}
		return ret
	}

}