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
package org.utplsql.sqldev.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.snippet.SnippetMerger;

@SuppressWarnings("all")
public class SnippetTest {
  @Test
  public void mergeAsCopy() {
    try {
      String _property = System.getProperty("user.home");
      String _plus = (_property + File.separator);
      String _plus_1 = (_plus + "UserSnippets.xml");
      final File file = new File(_plus_1);
      file.delete();
      final SnippetMerger merger = new SnippetMerger(file);
      merger.merge();
      Assert.assertTrue(file.exists());
      byte[] _readAllBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
      final String userSnippetsXml = new String(_readAllBytes);
      Assert.assertEquals(merger.getTemplate(), userSnippetsXml);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void mergeKeepExisting() {
    try {
      String _property = System.getProperty("user.home");
      String _plus = (_property + File.separator);
      String _plus_1 = (_plus + "UserSnippets.xml");
      final File file = new File(_plus_1);
      file.delete();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<?xml version = \'1.0\' encoding = \'UTF-8\'?>");
      _builder.newLine();
      _builder.append("<snippets>");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("<group category=\"utPLSQL\" language=\"PLSQL\">");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("<snippet name=\"test s\" description=\"test s\">");
      _builder.newLine();
      _builder.append("         ");
      _builder.append("<code>");
      _builder.newLine();
      _builder.append("            ");
      _builder.append("<![CDATA[bla bla bla]]>");
      _builder.newLine();
      _builder.append("         ");
      _builder.append("</code>");
      _builder.newLine();
      _builder.append("      ");
      _builder.append("</snippet>");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("</group>");
      _builder.newLine();
      _builder.append("</snippets>");
      _builder.newLine();
      final String userSnippetsXml = _builder.toString();
      Files.write(Paths.get(file.getAbsolutePath()), userSnippetsXml.getBytes());
      final SnippetMerger merger = new SnippetMerger(file);
      merger.merge();
      Assert.assertTrue(file.exists());
      byte[] _readAllBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
      final String userSnippetsXml2 = new String(_readAllBytes);
      int _length = userSnippetsXml2.length();
      int _length_1 = userSnippetsXml.length();
      boolean _greaterThan = (_length > _length_1);
      Assert.assertTrue(_greaterThan);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("<group category=\"utPLSQL\" language=\"PLSQL\">");
      Assert.assertTrue(userSnippetsXml2.contains(_builder_1));
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("<group category=\"utPLSQL Annotations\" language=\"PLSQL\">");
      Assert.assertTrue(userSnippetsXml2.contains(_builder_2));
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("<group category=\"utPLSQL Expectations\" language=\"PLSQL\">");
      Assert.assertTrue(userSnippetsXml2.contains(_builder_3));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void mergeRemoveExisting() {
    try {
      String _property = System.getProperty("user.home");
      String _plus = (_property + File.separator);
      String _plus_1 = (_plus + "UserSnippets.xml");
      final File file = new File(_plus_1);
      file.delete();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<?xml version = \'1.0\' encoding = \'UTF-8\'?>");
      _builder.newLine();
      _builder.append("<snippets>");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("<group category=\"utPLSQL Annotations\" language=\"XYZ\"/>");
      _builder.newLine();
      _builder.append("   ");
      _builder.append("<group category=\"utPLSQL Expectations\" language=\"XYZ\"/>");
      _builder.newLine();
      _builder.append("</snippets>");
      _builder.newLine();
      final String userSnippetsXml = _builder.toString();
      Files.write(Paths.get(file.getAbsolutePath()), userSnippetsXml.getBytes());
      final SnippetMerger merger = new SnippetMerger(file);
      merger.merge();
      Assert.assertTrue(file.exists());
      byte[] _readAllBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
      final String userSnippetsXml2 = new String(_readAllBytes);
      int _length = userSnippetsXml2.length();
      int _length_1 = userSnippetsXml.length();
      boolean _greaterThan = (_length > _length_1);
      Assert.assertTrue(_greaterThan);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("<group category=\"utPLSQL Annotations\" language=\"XYZ\">");
      Assert.assertFalse(userSnippetsXml2.contains(_builder_1));
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("<group category=\"utPLSQL Expectations\" language=\"XYZ\">");
      Assert.assertFalse(userSnippetsXml2.contains(_builder_2));
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("<group category=\"utPLSQL Annotations\" language=\"PLSQL\">");
      Assert.assertTrue(userSnippetsXml2.contains(_builder_3));
      StringConcatenation _builder_4 = new StringConcatenation();
      _builder_4.append("<group category=\"utPLSQL Expectations\" language=\"PLSQL\">");
      Assert.assertTrue(userSnippetsXml2.contains(_builder_4));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
