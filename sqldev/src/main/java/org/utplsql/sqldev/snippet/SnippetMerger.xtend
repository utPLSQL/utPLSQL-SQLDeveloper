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
package org.utplsql.sqldev.snippet

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.xml.parsers.DocumentBuilderFactory
import oracle.dbtools.util.Resource
import org.utplsql.sqldev.model.XMLTools
import org.xml.sax.InputSource

class SnippetMerger {
	val extension XMLTools xmlTools = new XMLTools
	File userSnippetsFile 
	String utplsqlSnippets 

	def getUtplsqlSnippetsAsString() throws IOException {
		val stream = class.getResourceAsStream("/org/utplsql/sqldev/resources/UtplsqlSnippets.xml")
		val reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset))
		return reader.lines.collect(Collectors.joining(System.lineSeparator))
	}
	
	new() {
		// works in SQL Developer only, otherwise a ExceptionInInitializerError is thrown
		this (new File(Resource.RAPTOR_USER.absolutePath + File.separator + "UserSnippets.xml"))
	}
	
	new(File file) {
		utplsqlSnippets = utplsqlSnippetsAsString 
		userSnippetsFile = file		
	}
	
	def merge() {
		var String result
		if (userSnippetsFile.exists) {
			// file exists, proper merge required
			val userSnippets = new String(Files.readAllBytes(Paths.get(userSnippetsFile.absolutePath)))
			val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
			val	userSnippetsDoc = docBuilder.parse(new InputSource(new StringReader(userSnippets)))
			val userSnippetsGroups = userSnippetsDoc.getNodeList('''/snippets/group[not(@category="utPLSQL Annotations" or @category="utPLSQL Expectations")]''')
			val	utplsqlSnippetsDoc = docBuilder.parse(new InputSource(new StringReader(utplsqlSnippets)))
			val utplsqlSnippetsGroups = utplsqlSnippetsDoc.getNodeList('''/snippets/group''')			
			result = '''
				<?xml version = '1.0' encoding = 'UTF-8'?>
				<snippets>
				   «FOR i : 0 ..< userSnippetsGroups.length»
				      «userSnippetsGroups.item(i).nodeToString("code")»
				   «ENDFOR»
				   «FOR i : 0 ..< utplsqlSnippetsGroups.length»
				      «utplsqlSnippetsGroups.item(i).nodeToString("code")»
				   «ENDFOR»
				</snippets>
			'''
		} else {
			// just copy
			result = utplsqlSnippets
			
		}
		Files.write(Paths.get(userSnippetsFile.absolutePath), result.bytes)
	}
	
	def getTemplate() {
		return utplsqlSnippets
	}
	
	def getFile() {
		return userSnippetsFile
	}
	
}