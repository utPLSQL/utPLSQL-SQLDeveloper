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
package org.utplsql.sqldev.test;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.FileTools;
import org.utplsql.sqldev.snippet.SnippetMerger;

public class SnippetTest {
    private static final File USER_SNIPPETS_FILE = new File(
            System.getProperty("user.home") + File.separator + "UserSnippets.xml");

    @Test
    public void mergeAsCopy() {
        USER_SNIPPETS_FILE.delete();
        final SnippetMerger merger = new SnippetMerger(USER_SNIPPETS_FILE);
        merger.merge();
        Assert.assertTrue(USER_SNIPPETS_FILE.exists());
        final String userSnippetsXml = new String(FileTools.readFile(USER_SNIPPETS_FILE.toPath()));
        Assert.assertEquals(merger.getTemplate(), userSnippetsXml);
    }

    @Test
    public void mergeKeepExisting() {
        USER_SNIPPETS_FILE.delete();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version = '1.0' encoding = 'UTF-8'?>\n");
        sb.append("<snippets>\n");
        sb.append("   <group category=\"utPLSQL\" language=\"PLSQL\">\n");
        sb.append("      <snippet name=\"test s\" description=\"test s\">\n");
        sb.append("         <code>\n");
        sb.append("            <![CDATA[bla bla bla]]>\n");
        sb.append("         </code>\n");
        sb.append("      </snippet>\n");
        sb.append("   </group>\n");
        sb.append("</snippets>\n");
        final String userSnippetsXml = sb.toString();
        FileTools.writeFile(Paths.get(USER_SNIPPETS_FILE.getAbsolutePath()), userSnippetsXml.getBytes());
        final SnippetMerger merger = new SnippetMerger(USER_SNIPPETS_FILE);
        merger.merge();
        Assert.assertTrue(USER_SNIPPETS_FILE.exists());
        final String userSnippetsXml2 = new String(FileTools.readFile(USER_SNIPPETS_FILE.toPath()));
        Assert.assertTrue(userSnippetsXml2.length() > userSnippetsXml.length());
        Assert.assertTrue(userSnippetsXml2.contains("<group category=\"utPLSQL\" language=\"PLSQL\">"));
        Assert.assertTrue(userSnippetsXml2.contains("<group category=\"utPLSQL Annotations\" language=\"PLSQL\">"));
        Assert.assertTrue(userSnippetsXml2.contains("<group category=\"utPLSQL Expectations\" language=\"PLSQL\">"));
    }

    @Test
    public void mergeRemoveExisting() {
        USER_SNIPPETS_FILE.delete();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version = '1.0' encoding = 'UTF-8'?>\n");
        sb.append("<snippets>\n");
        sb.append("   <group category=\"utPLSQL Annotations\" language=\"XYZ\"/>\n");
        sb.append("   <group category=\"utPLSQL Expectations\" language=\"XYZ\"/>\n");
        sb.append("</snippets>\n");
        final String userSnippetsXml = sb.toString();
        FileTools.writeFile(Paths.get(USER_SNIPPETS_FILE.getAbsolutePath()), userSnippetsXml.getBytes());
        final SnippetMerger merger = new SnippetMerger(USER_SNIPPETS_FILE);
        merger.merge();
        Assert.assertTrue(USER_SNIPPETS_FILE.exists());
        final String userSnippetsXml2 = new String(FileTools.readFile(Paths.get(USER_SNIPPETS_FILE.getAbsolutePath())));
        Assert.assertTrue(userSnippetsXml2.length() > userSnippetsXml.length());
        Assert.assertFalse(userSnippetsXml2.contains("<group category=\"utPLSQL Annotations\" language=\"XYZ\">"));
        Assert.assertFalse(userSnippetsXml2.contains("<group category=\"utPLSQL Expectations\" language=\"XYZ\">"));
        Assert.assertTrue(userSnippetsXml2.contains("<group category=\"utPLSQL Annotations\" language=\"PLSQL\">"));
        Assert.assertTrue(userSnippetsXml2.contains("<group category=\"utPLSQL Expectations\" language=\"PLSQL\">"));
    }
}
