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
package org.utplsql.sqldev.model.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.utplsql.sqldev.resources.UtplsqlResources;

public class ItemNode extends DefaultMutableTreeNode implements Comparable<ItemNode> {

    private static final long serialVersionUID = -4053143673822661743L;

    public ItemNode(Item userObject) {
        super(userObject, userObject instanceof Suite);
    }
    
    @Override
    public int compareTo(ItemNode other) {
        return getId().compareTo(other.getId());
    }

    public String getId() {
        return ((Item) getUserObject()).getId();
    }

    public String getName() {
        return ((Item) getUserObject()).getName();
    }

    public String getDescription() {
        return ((Item) getUserObject()).getDescription();
    }

    public Double getExecutionTime() {
        return ((Item) getUserObject()).getExecutionTime();
    }
    
    public Set<String> getTestPackages() {
        HashSet<String> testPackages = new HashSet<>();
        Enumeration<?> orderedNodes = preorderEnumeration();
        while (orderedNodes.hasMoreElements()) {
            ItemNode node = (ItemNode) orderedNodes.nextElement();
            if (node.getUserObject() instanceof Test) {
                Test test = (Test) node.getUserObject();
                testPackages.add(test.getOwnerName() + "." + test.getObjectName());
            }
        }
        return testPackages;
    }
    
    public Set<String> getOwners() {
        HashSet<String> owners = new HashSet<>();
        Enumeration<?> children = children();
        while (children.hasMoreElements()) {
            ItemNode child = (ItemNode) children.nextElement();
            owners.add(child.getOwnerName());
        }
        return owners;
    }

    public String getOwnerName() {
        String ownerName = null;
        Enumeration<?> orderedNodes = preorderEnumeration();
        while (orderedNodes.hasMoreElements()) {
            ItemNode node = (ItemNode) orderedNodes.nextElement();
            if (node.getUserObject() instanceof Test) {
                Test test = (Test) node.getUserObject();
                if (ownerName == null) {
                    ownerName = test.getOwnerName();
                } else if (!ownerName.equals(test.getOwnerName())) {
                    ownerName = "***";
                    break;
                }
            }
        }
        return ownerName;
    }
    
    public String getPackageName() {
        String packageName = null;
        Enumeration<?> orderedNodes = preorderEnumeration();
        while (orderedNodes.hasMoreElements()) {
            ItemNode node = (ItemNode) orderedNodes.nextElement();
            if (node.getUserObject() instanceof Test) {
                Test test = (Test) node.getUserObject();
                if (packageName == null) {
                    packageName = test.getObjectName();
                } else if (!packageName.equals(test.getObjectName())) {
                    packageName = "***";
                    break;
                }
            }
        }
        return packageName;
    }

    public String getProcedureName() {
        String procedureName = null;
        Enumeration<?> orderedNodes = preorderEnumeration();
        while (orderedNodes.hasMoreElements()) {
            ItemNode node = (ItemNode) orderedNodes.nextElement();
            if (node.getUserObject() instanceof Test) {
                Test test = (Test) node.getUserObject();
                if (procedureName == null) {
                    procedureName = test.getProcedureName();
                } else if (!procedureName.equals(test.getProcedureName())) {
                    procedureName = "***";
                    break;
                }
            }
        }
        return procedureName;
    }

    public Icon getStatusIcon() {
        Item item = (Item) getUserObject();
        Icon icon = item.getStatusIcon();
        if (icon == null) {
            if (item.getId() != null) {
                if (item instanceof Test) {
                    icon = UtplsqlResources.getIcon("PROCEDURE_ICON");
                } else if (item.getId().contains("context_#")) {
                    icon = UtplsqlResources.getIcon("PROCEDURE_FOLDER_ICON");
                } else {
                    if (item.getName().equals(getPackageName())) {
                        icon = UtplsqlResources.getIcon("PACKAGE_ICON");
                    } else {
                        icon = UtplsqlResources.getIcon("PACKAGE_FOLDER_ICON");
                    }
                }
            }
        }
        return icon;
    }

    public Icon getWarningIcon() {
        return ((Item) getUserObject()).getWarningIcon();
    }

    public Icon getInfoIcon() {
        return ((Item) getUserObject()).getInfoIcon();
    }
    
    /**
     * Calculates non-overlapping items.
     * 
     * This can be used to build a list of suites to be started by utPLSQL while ensuring that 
     * 
     *   - all requested tests are executed, but not more than once 
     *   - the test execution is efficient by ensuring that the list is as short as possible
     * 
     * This means if all tests of a suite shall be executed that the suit should be
     * part of the result list and not all of its tests.
     * 
     * In other words, top-level nodes are preferred to produce an optimal result.
     * 
     * @param selectedNodes all selected nodes must be part of the same tree
     * @return non-overlapping set of nodes
     */
    public static Set<ItemNode> createNonOverlappingSet(List<ItemNode> selectedNodes) {
        HashSet<ItemNode> result = new HashSet<>();
        if (selectedNodes != null && selectedNodes.size() > 0) {
            HashSet<ItemNode> expandedResult = new HashSet<>();
            List<ItemNode> sortedNodes = new ArrayList<>(selectedNodes);
            Collections.sort(sortedNodes);
            for (ItemNode sortedNode : sortedNodes) {
                if (!expandedResult.contains(sortedNode)) {
                    result.add(sortedNode);
                    Enumeration<?> expandedNodes = sortedNode.preorderEnumeration();
                    while (expandedNodes.hasMoreElements()) {
                        ItemNode expandedNode = (ItemNode) expandedNodes.nextElement();
                        expandedResult.add(expandedNode);
                    }
                }
            }
        }
        return result;
    }

}
