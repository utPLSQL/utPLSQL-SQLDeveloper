/*
 * Copyright 2021 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.utplsql.sqldev.model.runner.ItemNode;

public class ItemNodeTest {
    private final HashMap<String, ItemNode> model = new HashMap<>();
    private final List<ItemNode> selection = new ArrayList<>();
    
    private void addItem(String id, String parentId) {
        // using suite only
        org.utplsql.sqldev.model.runner.Suite suite = new org.utplsql.sqldev.model.runner.Suite();
        suite.setId(id);
        ItemNode node = new ItemNode(suite);
        model.put(id, node);
        ItemNode parent;
        if (parentId !=  null) {
            parent = model.get(parentId);
            parent.add(node);
        }
    }
    
    @Before
    public void setup() { 
        /*
         * Setup model for all test cases:
         *  
         * a
         * +- a.a
         *    +- a.a.a
         *    +- a.a.b
         *       +- a.a.b.a
         *       +- a.a.b.b
         * +- a.b
         *    +- a.b.a
         *    +- a.b.b
         * b
         * +- b.a
         * +- b.b
         */
        model.clear();
        addItem("a"      , null);
        addItem("a.a"    , "a");
        addItem("a.a.a"  , "a.a");
        addItem("a.a.b"  , "a.a");
        addItem("a.a.b.a", "a.a.b");
        addItem("a.a.b.b", "a.a.b");
        addItem("a.b"    , "a");
        addItem("a.b.a"  , "a.b");
        addItem("a.b.b"  , "a.b");
        addItem("b"      , null);
        addItem("b.a"    , "b");
        addItem("b.b"    , "b");
        selection.clear();
    }

    
    @Test
    public void null_input() {
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(null);
        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void empty_input() {
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(selection);
        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void one_top_node() {
        selection.add(model.get("a"));
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(selection);
        Assert.assertEquals(1, actual.size());
        Assert.assertTrue(actual.contains(model.get("a")));
    }
    
    @Test
    public void one_top_node_one_child() {
        selection.add(model.get("a"));
        selection.add(model.get("a.a.b.a"));
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(selection);
        Assert.assertEquals(1, actual.size());
        Assert.assertTrue(actual.contains(model.get("a")));
    }

    @Test
    public void one_top_node_two_chidren() {
        selection.add(model.get("a.b"));
        selection.add(model.get("a"));
        selection.add(model.get("a.a.b.a"));
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(selection);
        Assert.assertEquals(1, actual.size());
        Assert.assertTrue(actual.contains(model.get("a")));
    }

    @Test
    public void one_top_node_three_chidren() {
        selection.add(model.get("a"));
        selection.add(model.get("a.a.b.a"));
        selection.add(model.get("a.b"));
        selection.add(model.get("a.b.a"));
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(selection);
        Assert.assertEquals(1, actual.size());
        Assert.assertTrue(actual.contains(model.get("a")));
    }
    
    @Test
    public void three_chidren() {
        selection.add(model.get("a.a.b.a"));
        selection.add(model.get("a.b"));
        selection.add(model.get("a.b.a"));
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(selection);
        Assert.assertEquals(2, actual.size());
        Assert.assertTrue(actual.contains(model.get("a.a.b.a")));
        Assert.assertTrue(actual.contains(model.get("a.b")));
    }
    
    @Test
    public void two_top_nodes() {
        selection.add(model.get("a"));
        selection.add(model.get("b"));
        Set<ItemNode> actual = ItemNode.createNonOverlappingSet(selection);
        Assert.assertEquals(2, actual.size());
        Assert.assertTrue(actual.contains(model.get("a")));
        Assert.assertTrue(actual.contains(model.get("b")));
    }
 
}
