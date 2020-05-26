/**
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
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oracle.dbtools.util.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Extension;
import org.utplsql.sqldev.model.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@SuppressWarnings("all")
public class SnippetMerger {
  @Extension
  private final XMLTools xmlTools = new XMLTools();
  
  private File userSnippetsFile;
  
  private String utplsqlSnippets;
  
  public String getUtplsqlSnippetsAsString() throws IOException {
    final InputStream stream = this.getClass().getResourceAsStream("/org/utplsql/sqldev/resources/UtplsqlSnippets.xml");
    Charset _defaultCharset = Charset.defaultCharset();
    InputStreamReader _inputStreamReader = new InputStreamReader(stream, _defaultCharset);
    final BufferedReader reader = new BufferedReader(_inputStreamReader);
    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
  }
  
  public SnippetMerger() {
    this(new File(((Resource.RAPTOR_USER.getAbsolutePath() + File.separator) + "UserSnippets.xml")));
  }
  
  public SnippetMerger(final File file) {
    try {
      this.utplsqlSnippets = this.getUtplsqlSnippetsAsString();
      this.userSnippetsFile = file;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Path merge() {
    try {
      Path _xblockexpression = null;
      {
        String result = null;
        boolean _exists = this.userSnippetsFile.exists();
        if (_exists) {
          byte[] _readAllBytes = Files.readAllBytes(Paths.get(this.userSnippetsFile.getAbsolutePath()));
          final String userSnippets = new String(_readAllBytes);
          final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          StringReader _stringReader = new StringReader(userSnippets);
          InputSource _inputSource = new InputSource(_stringReader);
          final Document userSnippetsDoc = docBuilder.parse(_inputSource);
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("/snippets/group[not(@category=\"utPLSQL Annotations\" or @category=\"utPLSQL Expectations\")]");
          final NodeList userSnippetsGroups = this.xmlTools.getNodeList(userSnippetsDoc, _builder.toString());
          StringReader _stringReader_1 = new StringReader(this.utplsqlSnippets);
          InputSource _inputSource_1 = new InputSource(_stringReader_1);
          final Document utplsqlSnippetsDoc = docBuilder.parse(_inputSource_1);
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("/snippets/group");
          final NodeList utplsqlSnippetsGroups = this.xmlTools.getNodeList(utplsqlSnippetsDoc, _builder_1.toString());
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("<?xml version = \'1.0\' encoding = \'UTF-8\'?>");
          _builder_2.newLine();
          _builder_2.append("<snippets>");
          _builder_2.newLine();
          {
            int _length = userSnippetsGroups.getLength();
            ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, _length, true);
            for(final Integer i : _doubleDotLessThan) {
              _builder_2.append("   ");
              String _nodeToString = this.xmlTools.nodeToString(userSnippetsGroups.item((i).intValue()), "code");
              _builder_2.append(_nodeToString, "   ");
              _builder_2.newLineIfNotEmpty();
            }
          }
          {
            int _length_1 = utplsqlSnippetsGroups.getLength();
            ExclusiveRange _doubleDotLessThan_1 = new ExclusiveRange(0, _length_1, true);
            for(final Integer i_1 : _doubleDotLessThan_1) {
              _builder_2.append("   ");
              String _nodeToString_1 = this.xmlTools.nodeToString(utplsqlSnippetsGroups.item((i_1).intValue()), "code");
              _builder_2.append(_nodeToString_1, "   ");
              _builder_2.newLineIfNotEmpty();
            }
          }
          _builder_2.append("</snippets>");
          _builder_2.newLine();
          result = _builder_2.toString();
        } else {
          result = this.utplsqlSnippets;
        }
        _xblockexpression = Files.write(Paths.get(this.userSnippetsFile.getAbsolutePath()), result.getBytes());
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public String getTemplate() {
    return this.utplsqlSnippets;
  }
  
  public File getFile() {
    return this.userSnippetsFile;
  }
}
