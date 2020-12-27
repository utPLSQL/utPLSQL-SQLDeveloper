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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;

import org.utplsql.sqldev.model.FileTools;
import org.utplsql.sqldev.model.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import oracle.dbtools.util.Resource;

public class SnippetMerger {
    private final XMLTools xmlTools = new XMLTools();
    private final File userSnippetsFile;
    private final String utplsqlSnippets;

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
    
    public void merge() {
        String result;
        if (userSnippetsFile.exists()) {
            // file exists, proper merge required
            final String userSnippets = new String(FileTools.readFile(Paths.get(userSnippetsFile.getAbsolutePath())));
            final DocumentBuilder docBuilder = xmlTools.createDocumentBuilder();
            final Document userSnippetsDoc = xmlTools.parse(docBuilder, new InputSource(new StringReader(userSnippets)));
            final NodeList userSnippetsGroups = xmlTools.getNodeList(userSnippetsDoc,
                    "/snippets/group[not(@category=\"utPLSQL Annotations\" or @category=\"utPLSQL Expectations\")]");
            final Document utplsqlSnippetsDoc = xmlTools.parse(docBuilder, new InputSource(new StringReader(utplsqlSnippets)));
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
        FileTools.writeFile(Paths.get(userSnippetsFile.getAbsolutePath()), result.getBytes());
    }

    public String getTemplate() {
        return utplsqlSnippets;
    }

    public File getFile() {
        return userSnippetsFile;
    }
}
