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
package org.utplsql.sqldev.model

import java.util.List

// converted to Xtend based on Java code on https://www.geeksforgeeks.org/longest-common-prefix-using-binary-search/
class PrefixTools {
	def static int findMinLength(String[] arr, int n) {
		var int min = Integer.MAX_VALUE
		for (var int i = 0; i <= (n - 1); i++) {
			if ({
				val _rdIndx_arr = i
				arr.get(_rdIndx_arr)
			}.length() < min) {
				min = {
					val _rdIndx_arr = i
					arr.get(_rdIndx_arr)
				}.length()
			}
		}
		return min
	}

	def static boolean allContainsPrefix(String[] arr, int n, String str, int start, int end) {
		for (var int i = 0; i <= (n - 1); i++) {
			var String arr_i = {
				val _rdIndx_arr = i
				arr.get(_rdIndx_arr)
			}
			for (var int j = start; j <= end; j++)
				if(arr_i.charAt(j) !== str.charAt(j)) return false
		}
		return true
	}

	def static String commonPrefix(String[] arr, int n) {
		var int index = findMinLength(arr, n)
		var String prefix = ""
		var int low = 0
		var int high = index
		while (low <= high) {
			var int mid = low + (high - low) / 2
			if (allContainsPrefix(arr, n, arr.get(0), low, mid)) {
				prefix = prefix + arr.get(0).substring(low, mid + 1)
				low = mid + 1
			} else {
				high = mid - 1
			}
		}
		return prefix
	}
	
	def static String commonPrefix(List<String> list) {
		if (list.size === 0) {
			return ""
		} else if (list.size === 1) {
			val pos = list.get(0).lastIndexOf(".");
			if (pos > 0) {
				return list.get(0).substring(0, pos + 1)
			} else {
				return ""
			}
		} else {
			var String[] testArray = newArrayOfSize(list.size)
			var prefix = commonPrefix(list.toArray(testArray), list.size)
			return prefix
		}
	}
	
}
