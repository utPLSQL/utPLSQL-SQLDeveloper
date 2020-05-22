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
package org.utplsql.sqldev.model

import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

class XMLTools {
	val xpathFactory = XPathFactory.newInstance()
	val xpath = xpathFactory.newXPath()
		
	def getNodeList(Node doc, String xpathString) {
		val expr = xpath.compile(xpathString);
		val NodeList nodeList = expr.evaluate(doc, XPathConstants.NODESET) as NodeList
		return nodeList 
	}
	
	def getNode(Node doc, String xpathString) {
		val expr = xpath.compile(xpathString);
		val Node node = expr.evaluate(doc, XPathConstants.NODE) as Node
		return node 
	}
	
	def void trimWhitespace(Node node) {
		val children = node.childNodes
		for (i : 0 ..< children.length) {
			val child = children.item(i)
			if (child.nodeType == Node.TEXT_NODE) {
				child.textContent = child.textContent.trim
			}
			trimWhitespace(child);
		}
	}
	
	def nodeToString(Node node, String cdataSectionElements) {
		node.trimWhitespace
		val writer = new StringWriter()
		val factory = TransformerFactory.newInstance().newTransformer()
		factory.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
		factory.setOutputProperty(OutputKeys.INDENT, "yes")
		factory.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		factory.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements)
		factory.transform(new DOMSource(node), new StreamResult(writer))
		val result = writer.toString()
		val fixedResult = result.replaceAll('''<!\[CDATA\[\s*\]\]>''',"")
		return fixedResult
	}
	
	def getAttributeValue(Node node, String namedItem) {
		var String value = null
		if (node instanceof Element) {
			value = node.attributes?.getNamedItem(namedItem)?.nodeValue;
		}
		return value
	}
	
	def getElementValue(Node node, String tagName) {
		return getElementNode(node, tagName)?.textContent
	}

	def getElementNode(Node node, String tagName) {
		var Node resultNode = null
		if (node instanceof Element) {
			resultNode = node.getElementsByTagName(tagName)?.item(0)
		}
		return resultNode
	}
}
