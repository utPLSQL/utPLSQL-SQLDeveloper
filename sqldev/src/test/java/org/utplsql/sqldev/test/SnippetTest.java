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
package org.utplsql.sqldev.test

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.junit.Assert
import org.junit.Test
import org.utplsql.sqldev.snippet.SnippetMerger

class SnippetTest {

	@Test
	def void mergeAsCopy() {
		val file = new File(System.getProperty("user.home") + File.separator + "UserSnippets.xml")
		file.delete
		val merger = new SnippetMerger(file)
		merger.merge
		Assert.assertTrue(file.exists)
		val userSnippetsXml = new String(Files.readAllBytes(Paths.get(file.absolutePath)))
		Assert.assertEquals(merger.template, userSnippetsXml )
	}

	@Test
	def void mergeKeepExisting() {
		val file = new File(System.getProperty("user.home") + File.separator + "UserSnippets.xml")
		file.delete
		val userSnippetsXml = '''
			<?xml version = '1.0' encoding = 'UTF-8'?>
			<snippets>
			   <group category="utPLSQL" language="PLSQL">
			      <snippet name="test s" description="test s">
			         <code>
			            <![CDATA[bla bla bla]]>
			         </code>
			      </snippet>
			   </group>
			</snippets>
		'''.toString
		Files.write(Paths.get(file.absolutePath), userSnippetsXml.bytes)
		val merger = new SnippetMerger(file)
		merger.merge
		Assert.assertTrue(file.exists)
		val userSnippetsXml2 = new String(Files.readAllBytes(Paths.get(file.absolutePath)))
		Assert.assertTrue(userSnippetsXml2.length > userSnippetsXml.length)
		Assert.assertTrue(userSnippetsXml2.contains('''<group category="utPLSQL" language="PLSQL">'''))
		Assert.assertTrue(userSnippetsXml2.contains('''<group category="utPLSQL Annotations" language="PLSQL">'''))
		Assert.assertTrue(userSnippetsXml2.contains('''<group category="utPLSQL Expectations" language="PLSQL">'''))
	} 

	@Test
	def void mergeRemoveExisting() {
		val file = new File(System.getProperty("user.home") + File.separator + "UserSnippets.xml")
		file.delete
		val userSnippetsXml = '''
			<?xml version = '1.0' encoding = 'UTF-8'?>
			<snippets>
			   <group category="utPLSQL Annotations" language="XYZ"/>
			   <group category="utPLSQL Expectations" language="XYZ"/>
			</snippets>
		'''.toString
		Files.write(Paths.get(file.absolutePath), userSnippetsXml.bytes)
		val merger = new SnippetMerger(file)
		merger.merge
		Assert.assertTrue(file.exists)
		val userSnippetsXml2 = new String(Files.readAllBytes(Paths.get(file.absolutePath)))
		Assert.assertTrue(userSnippetsXml2.length > userSnippetsXml.length)
		Assert.assertFalse(userSnippetsXml2.contains('''<group category="utPLSQL Annotations" language="XYZ">'''))
		Assert.assertFalse(userSnippetsXml2.contains('''<group category="utPLSQL Expectations" language="XYZ">'''))
		Assert.assertTrue(userSnippetsXml2.contains('''<group category="utPLSQL Annotations" language="PLSQL">'''))
		Assert.assertTrue(userSnippetsXml2.contains('''<group category="utPLSQL Expectations" language="PLSQL">'''))
	} 


}
