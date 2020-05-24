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
package org.utplsql.sqldev.test

import org.junit.Assert
import org.junit.Test
import org.utplsql.sqldev.model.URLTools

class UrlToolsTest {

	@Test
	def void replacePlusSign() {
		Assert.assertEquals("+", URLTools.replaceHexChars("%2B"))
		Assert.assertEquals("++", URLTools.replaceHexChars("%2B%2B"))
		Assert.assertEquals("abc+%xyz", URLTools.replaceHexChars("abc%2B%xyz"))
	}

	@Test
	def void replaceAtSign() {
		Assert.assertEquals("@", URLTools.replaceHexChars("%40"))
		Assert.assertEquals("@@", URLTools.replaceHexChars("%40%40"))
		Assert.assertEquals("abc@%xyz", URLTools.replaceHexChars("abc%40%xyz"))
	}

	@Test
	def void replaceAtAndPlusSign() {
		Assert.assertEquals("@+", URLTools.replaceHexChars("%40%2B"))
		Assert.assertEquals("@+@+", URLTools.replaceHexChars("%40%2B%40%2B"))
		Assert.assertEquals("abc@+%xyz", URLTools.replaceHexChars("abc%40%2B%xyz"))
	}

}
