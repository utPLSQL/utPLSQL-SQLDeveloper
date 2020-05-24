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
package org.utplsql.sqldev.model;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.utplsql.sqldev.exception.GenericRuntimeException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLTools {
    private static final Logger logger = Logger.getLogger(XMLTools.class.getName());
    private final XPathFactory xpathFactory = XPathFactory.newInstance();
    private final XPath xpath = xpathFactory.newXPath();

    public NodeList getNodeList(final Node doc, final String xpathString) {
        try {
            final XPathExpression expr = xpath.compile(xpathString);
            return ((NodeList) expr.evaluate(doc, XPathConstants.NODESET));
        } catch (XPathExpressionException e) {
            final String msg = "XPathExpressionException for " + xpathString + " due to " + e.getMessage();
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
    }

    public Node getNode(final Node doc, final String xpathString) {
        try {
            final XPathExpression expr = xpath.compile(xpathString);
            return ((Node) expr.evaluate(doc, XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            final String msg = "XPathExpressionException for " + xpathString + " due to " + e.getMessage();
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
    }

    public void trimWhitespace(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                child.setTextContent(child.getTextContent().trim());
            }
            trimWhitespace(child);
        }
    }

    public String nodeToString(final Node node, final String cdataSectionElements) {
        try {
            trimWhitespace(node);
            final StringWriter writer = new StringWriter();
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
            transformer.transform( new DOMSource(node), new StreamResult(writer));
            final String result = writer.toString();
            return result.replaceAll("<!\\[CDATA\\[\\s*\\]\\]>", "");
        } catch (TransformerException  e) {
            final String msg = "TransformerException for " + cdataSectionElements + " due to " + e.getMessage();
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
    }

    public String getAttributeValue(final Node node, final String namedItem) {
        String value = null;
        if (node instanceof Element) {
            final NamedNodeMap attributes = ((Element) node).getAttributes();
            if (attributes != null) {
                final Node item = attributes.getNamedItem(namedItem);
                if (item != null) {
                    value = item.getNodeValue();
                }
            }
        }
        return value;
    }

    public String getElementValue(final Node node, final String tagName) {
        String value = null;
        final Node item = getElementNode(node, tagName);
        if (item != null) {
            value = item.getTextContent();
        }
        return value;
    }

    public Node getElementNode(final Node node, final String tagName) {
        Node resultNode = null;
        if (node instanceof Element) {
            NodeList list = ((Element) node).getElementsByTagName(tagName);
            if (list != null && list.getLength() > 0) {
                resultNode = list.item(0);
            }
        }
        return resultNode;
    }
}
