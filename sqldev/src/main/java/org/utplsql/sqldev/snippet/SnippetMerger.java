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
package org.utplsql.sqldev.snippet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.utplsql.sqldev.exception.GenericRuntimeException;
import org.utplsql.sqldev.model.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import oracle.dbtools.util.Resource;

public class SnippetMerger {
    private static final Logger logger = Logger.getLogger(SnippetMerger.class.getName());

    private final XMLTools xmlTools = new XMLTools();
    private File userSnippetsFile;
    private String utplsqlSnippets;

    public String getUtplsqlSnippetsAsString() {
        final InputStream stream = getClass()
                .getResourceAsStream("/org/utplsql/sqldev/resources/UtplsqlSnippets.xml");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    public SnippetMerger() {
        // works in SQL Developer only, otherwise a ExceptionInInitializerError is thrown
        this(new File(Resource.RAPTOR_USER.getAbsolutePath() + File.separator + "UserSnippets.xml"));
    }

    public SnippetMerger(final File file) {
        utplsqlSnippets = getUtplsqlSnippetsAsString();
        userSnippetsFile = file;
    }
    
    private byte[] readFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            final String msg = "Cannot read file " + path.toString() + " due to " + e.getMessage() + ".";
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
    }
    
    private void writeFile(Path path, byte[] bytes) {
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            final String msg = "Cannot write file " + path.toString() + " due to " + e.getMessage() + ".";
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
    }
    
    private DocumentBuilder createDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            final String msg = "Could not create no document builder due to " + e.getMessage() + ".";
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
    }
    
    private Document parse(final DocumentBuilder builder, final InputSource inputSource) {
        try {
            return builder.parse(inputSource);
        } catch (SAXException | IOException e) {
            final String msg = "Could not parse XML input due to " + e.getMessage() + ".";
            logger.severe(() -> msg);
            throw new GenericRuntimeException(msg, e);
        }
    }

    public void merge() {
        String result = null;
        if (userSnippetsFile.exists()) {
            // file exists, proper merge required
            final String userSnippets = new String(readFile(Paths.get(userSnippetsFile.getAbsolutePath())));
            final DocumentBuilder docBuilder = createDocumentBuilder();
            final Document userSnippetsDoc = parse(docBuilder, new InputSource(new StringReader(userSnippets)));
            final NodeList userSnippetsGroups = xmlTools.getNodeList(userSnippetsDoc,
                    "/snippets/group[not(@category=\"utPLSQL Annotations\" or @category=\"utPLSQL Expectations\")]");
            final Document utplsqlSnippetsDoc = parse(docBuilder, new InputSource(new StringReader(utplsqlSnippets)));
            final NodeList utplsqlSnippetsGroups = xmlTools.getNodeList(utplsqlSnippetsDoc, "/snippets/group");
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version = '1.0' encoding = 'UTF-8'?>\n");
            sb.append("<snippets>\n");
            for (int i = 0; i < userSnippetsGroups.getLength(); i++) {
                sb.append("   ");
                sb.append(xmlTools.nodeToString(userSnippetsGroups.item(i), "code"));
                sb.append('\n');
            }
            for (int i = 0; i < utplsqlSnippetsGroups.getLength(); i ++) {
                sb.append("   ");
                sb.append(xmlTools.nodeToString(utplsqlSnippetsGroups.item(i), "code"));
                sb.append('\n');
            }
            sb.append("</snippets>\n");
            result = sb.toString();
        } else {
            // just copy
            result = utplsqlSnippets;
        }
        writeFile(Paths.get(userSnippetsFile.getAbsolutePath()), result.getBytes());
    }

    public String getTemplate() {
        return utplsqlSnippets;
    }

    public File getFile() {
        return userSnippetsFile;
    }
}
