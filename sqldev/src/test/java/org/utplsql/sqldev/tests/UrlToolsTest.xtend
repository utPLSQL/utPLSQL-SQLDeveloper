package org.utplsql.sqldev.tests

import org.junit.Assert
import org.junit.Test
import org.utplsql.sqldev.model.URLTools

class UrlToolsTest {
	private val extension URLTools urlTools = new URLTools

	@Test
	def void testReplacePlusSign() {
		Assert.assertEquals("+", "%2B".replaceHexChars)
		Assert.assertEquals("++", "%2B%2B".replaceHexChars)
		Assert.assertEquals("abc+%xyz", "abc%2B%xyz".replaceHexChars)
	}

	@Test
	def void testReplaceAtSign() {
		Assert.assertEquals("@", "%40".replaceHexChars)
		Assert.assertEquals("@@", "%40%40".replaceHexChars)
		Assert.assertEquals("abc@%xyz", "abc%40%xyz".replaceHexChars)
	}

	@Test
	def void testReplaceAtAndPlusSign() {
		Assert.assertEquals("@+", "%40%2B".replaceHexChars)
		Assert.assertEquals("@+@+", "%40%2B%40%2B".replaceHexChars)
		Assert.assertEquals("abc@+%xyz", "abc%40%2B%xyz".replaceHexChars)
	}

}
