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
package org.utplsql.sqldev.ui.runner;

import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.swing.Icon;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.utplsql.sqldev.model.runner.Counter;
import org.utplsql.sqldev.model.runner.Item;
import org.utplsql.sqldev.model.runner.ItemNode;
import org.utplsql.sqldev.model.runner.Run;
import org.utplsql.sqldev.model.runner.Suite;
import org.utplsql.sqldev.model.runner.Test;
import org.utplsql.sqldev.resources.UtplsqlResources;

import oracle.javatools.ui.treetable.TreeTableModel;

public class TestOverviewTreeTableModel implements TreeTableModel {
    private boolean showDescription;
    private boolean useSmartTimes;
    private boolean showSuccessfulTests;
    private boolean showDisabledTests;
    private String rootId;
    private LinkedHashMap<String, ItemNode> sources = new LinkedHashMap<>();
    private final LinkedHashMap<String, ItemNode> nodes = new LinkedHashMap<>();
    protected final EventListenerList listenerList = new EventListenerList();

    public TestOverviewTreeTableModel() {
        super();
    }
    
    private boolean hasVisibleDisabledTests(ItemNode startNode) {
        if (!showDisabledTests || startNode.getUserObject() instanceof Test) {
            return false;
        }
        Enumeration<?> children = sources.get(startNode.getId()).preorderEnumeration();
        while (children.hasMoreElements()) {
            ItemNode child = (ItemNode) children.nextElement();
            Item item = (Item) child.getUserObject();
            if (item.getStatusIcon() == UtplsqlResources.getIcon("DISABLED_ICON")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a node should be shown in the tree.
     */
    private boolean isVisible(ItemNode node) {
        if (node != null) {
            Item item = (Item) node.getUserObject();
            if (item.getStatusIcon() == UtplsqlResources.getIcon("SUCCESS_ICON") && !showSuccessfulTests
                    && !hasVisibleDisabledTests(node)) {
                return false;
            }
            if (item.getStatusIcon() == UtplsqlResources.getIcon("DISABLED_ICON") && !showDisabledTests) {
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Copies the original nodes determined by the run to the local nodes. 
     * Keeps references to items in the run, so changes in the run are automatically applied.
     * However, the listeners need to by notified about the changes to display them
     * in the underlying tree and table. 
     */
    private void setClonedItemNodes() {
        nodes.clear();
        for (ItemNode source : sources.values()) {
            ItemNode node = new ItemNode((Item) source.getUserObject());
            nodes.put(node.getId(), node);
        }
        for (ItemNode source : sources.values()) {
            if (source.getUserObject() instanceof Suite) {
                ItemNode parent = nodes.get(source.getId());
                Enumeration<?> sourceChildren = source.children();
                while (sourceChildren.hasMoreElements()) {
                    ItemNode sourceChild = (ItemNode) sourceChildren.nextElement();
                    ItemNode child = nodes.get(sourceChild.getId());
                    if (isVisible(child)) {
                        parent.add(child);
                    }
                }
            }
        }
        reload();
    }

    /**
     * Sets the complete model. For example when changing a run.
     */
    public void setModel(final Run run, final boolean showDescription, final boolean useSmartTimes,
            final boolean showSuccessfulTests, final boolean showDisabledTests) {
        this.showDescription = showDescription;
        this.useSmartTimes = useSmartTimes;
        this.showSuccessfulTests = showSuccessfulTests;
        this.showDisabledTests = showDisabledTests;
        this.rootId = run.getReporterId();
        this.sources = run.getItemNodes();
        setClonedItemNodes();
    }

    /**
     * Updates the description only.
     */
    public void updateModel(final boolean showDescription) {
        this.showDescription = showDescription;
    }

    /**
     * Updates filter criteria. If a change is detected the model is re-created from scratch.
     */
    public void updateModel(final boolean showSuccessfulTests, final boolean showDisabledTests) {
        if (this.showSuccessfulTests != showSuccessfulTests || this.showDisabledTests != showDisabledTests) {
            this.showSuccessfulTests = showSuccessfulTests;
            this.showDisabledTests = showDisabledTests;
            setClonedItemNodes();
        }
    }

    /**
     * Re-creates the model from scratch.
     */
    public void updateModel() {
        setClonedItemNodes();
    }
    
    /**
     * Applies the filter criteria for a part of the tree after an update. 
     * Technically it will remove nodes from the tree.
     */
    private void removeInvisibleNodes(ItemNode startNode) {
        TreeNode[] path = startNode.getPath();
        for (TreeNode node : path) {
            ItemNode parent = (ItemNode) node.getParent();
            if (parent != null) {
                if (!isVisible((ItemNode) node)) {
                    int childIndex = parent.getIndex(node);
                    if (childIndex >= 0) {
                        parent.remove(childIndex);
                        // Delaying the fireTreeNodesRemove call would avoid the following exception:
                        //   Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: n >= m
                        //   at java.util.Vector.elementAt(Vector.java:479)
                        //   at javax.swing.tree.DefaultMutableTreeNode.getChildAt(DefaultMutableTreeNode.java:245)
                        //   ...
                        // This exception is raised in the event dispatching thread (another thread). The line numbers
                        // may differ based on the JDK you are using. I suspect that events are not processed 
                        // fast enough.
                        //
                        // However, it looks like these errors can be ignored. At least there seems to be no negative 
                        // side effect in the runner GUI. In any case the user could always use the refresh action 
                        // to get a clean state, should something look wrong. But I've never experienced this.
                        //
                        // These exceptions are thrown only when filtering is enabled, but then it happens quite often.
                        // This means it happens for 5-10% of the nodes.
                        //
                        // Calling fireTreeNodesRemoved in another thread, e.g. via SwingUtilities.invokeLater()
                        // will reduce the number of exceptions significantly (almost zero). However, in this case
                        // the subsequent updates might fail and this will cause an exception in this thread
                        // (in the method {@link #nodeChanged(String id)}. I tried the following:
                        //    - calling all fireTreeNode... methods via SwingUtilities.invokeLater().
                        //    - catching the exception, trying to re-fire or ignore it
                        // In the end the user experience was always worse. Even if I've got no exceptions
                        // anymore the result in the TreeTable was wrong. I've got wrong rows, even empty rows. 
                        //
                        // Therefore I decided to live with some exceptions in the event dispatching thread.
                        fireTreeNodesRemoved(this, parent.getPath(), new int[] { childIndex }, new Object[] { node });
                        // removing the parent removes also all its children, hence no need for further processing
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Updates a node and its parents and then apply the filter criteria.
     */
    public void updateModel(final String id) {
        nodeChanged(id);
        ItemNode startNode = nodes.get(id);
        if (startNode != null) {
            removeInvisibleNodes(startNode);
        }
    }

    /**
     * Notifies all listeners that the complete tree has changed.
     * For that a root node must be available.
     * However, a root node does not mean the model {@link #isComplete()}.
     */
    public void reload() {
        if (getRoot() != null) {
            fireTreeStructureChanged(this, getRoot().getPath(), null, null);
        }
    }
    
    /**
     * Notifies all listeners that a node and its parents have changed.
     */
    public void nodeChanged(String id) {
        ItemNode startNode = nodes.get(id);
        if (startNode != null) {
            TreeNode[] path = startNode.getPath();
            for (TreeNode node : path) {
                ItemNode parent = (ItemNode) node.getParent();
                if (parent != null) {
                    int childIndex = parent.getIndex(node);
                    if (childIndex >= 0) {
                        fireTreeNodesChanged(this, parent.getPath(), new int[] { childIndex }, new Object[] { node });
                    }
                }
            }
        }
    }

    /**
     * Determines if the model is fully initialized and can be used.
     * For that it the pseudo root must contain a child.
     */
    public boolean isComplete() {
        return nodes.size() > 1; // return sources.size() != nodes.size();
    }

    /**
     * Calculates the row of the underlying table when the tree is fully expanded.
     */
    public int getRow(final String id) {
        // do not count root
        int i = -1;

        // The order of orderedNodes can differ to nodes.values()
        // when run is based on list of tests.
        Enumeration<?> orderedNodes = getRoot().preorderEnumeration();
        while (orderedNodes.hasMoreElements()) {
            ItemNode node = (ItemNode) orderedNodes.nextElement();
            if (((Item) node.getUserObject()).getId().equals(id)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    
    public Test getTestOf(final ItemNode startNode) {
        Enumeration<?> orderedNodes = startNode.preorderEnumeration();
        while (orderedNodes.hasMoreElements()) {
            ItemNode node = (ItemNode) orderedNodes.nextElement();
            Item item = (Item) node.getUserObject();
            if (item instanceof Test) {
                return (Test) item;
            }
        }
        return null;
    }
    
    public ItemNode getItemNode(final String id) {
        return nodes.get(id);
    }
    
    private interface CounterChecker {
        boolean matchedStatus (Counter counter);
    }
 
    private boolean ItemNodeStatus(final String id, final CounterChecker checker) {
        ItemNode startNode = sources.get(id);
        if (startNode != null) {
            Enumeration<?> orderedNodes = startNode.preorderEnumeration();
            while (orderedNodes.hasMoreElements()) {
                ItemNode node = (ItemNode) orderedNodes.nextElement();
                Item item = (Item) node.getUserObject();
                if (checker.matchedStatus(item.getCounter())) {
                    return true;
                }
            }
        }
        return false;
    }   
    
    /**
     * Returns true if a node or one of its children have errors.
     */
    public boolean ItemNodeHasErrors(final String id) {
        return ItemNodeStatus(id, counter -> counter.getError() > 0);
    }

    /** 
     * Returns true if a node or one of its children have failed tests.
     */
    public boolean ItemNodeHasFailedTests(final String id) {
        return ItemNodeStatus(id, counter -> counter.getFailure() > 0);
    }

    /**
     * Returns true if a node or one of its children have successful tests.
     */
    public boolean ItemNodeHasSuccessfulTests(final String id) {
        return ItemNodeStatus(id, counter -> counter.getSuccess() > 0);
    }
    
    public String getTreeColumnName() {
        return UtplsqlResources.getString(showDescription ? "RUNNER_DESCRIPTION_LABEL" : "RUNNER_TEST_ID_COLUMN");
    }

    public String getTimeColumnName() {
        return UtplsqlResources.getString("RUNNER_TEST_EXECUTION_TIME_COLUMN") + (!useSmartTimes ? " [s]" : "");
    }
    
    @Override
    public ItemNode getRoot() {
        return nodes.get(rootId);
    }

    @Override
    public ItemNode getChild(Object parent, int index) {
        return (ItemNode) ((ItemNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((ItemNode) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return !((ItemNode) node).getAllowsChildren();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // ignore, no implementation required
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((ItemNode) parent).getIndex((ItemNode) child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Copied from DefaultTreeModel
     */
    protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }
                // might fail with IndexOutOfBoundsException
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Copied from DefaultTreeModel
     */
    protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }

                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /**
     * Copied from DefaultTreeModel
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }

                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Copied from DefaultTreeModel
     */
    protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        Object[] listeners = this.listenerList.getListenerList();
        TreeModelEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }

                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return getTreeColumnName();
            case 1:
            case 2:
                return ""; // icons are used instead of descriptions
            case 3:
                return getTimeColumnName();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
                return TreeTableModel.class;
            case 1:
            case 2:
                return Icon.class;
            case 3:
                return Double.class;
            default:
                return String.class;
        }
    }

    @Override
    public Object getValueAt(Object node, int col) {
        final ItemNode itemNode = (ItemNode) node;
        switch (col) {
            case 0:
                if (showDescription && itemNode.getDescription() != null) {
                    return itemNode.getDescription();
                } else {
                    return itemNode.getName();
                }
            case 1:
                return itemNode.getWarningIcon();
            case 2:
                return itemNode.getInfoIcon();
            case 3:
                return itemNode.getExecutionTime();
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(Object node, int col) {
        // make the tree column editable to forward mouse events for collapse/expand
        return getColumnClass(col) == TreeTableModel.class;
    }

    @Override
    public void setValueAt(Object value, Object node, int col) {
        // ignore, no implementation required
    }

}
