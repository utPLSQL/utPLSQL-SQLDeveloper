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
package org.utplsql.sqldev.model.runner;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.JsonToStringStyler;
import org.utplsql.sqldev.model.URLTools;

public class Run {
    private String reporterId;
    private String connectionName;
    private List<String> pathList;
    private Integer currentTestNumber;
    private Test currentTest;
    private Integer totalNumberOfTests;
    private String startTime;
    private String endTime;
    private Double executionTime;
    private Counter counter;
    private Integer infoCount;
    private String errorStack;
    private String serverOutput;
    private final Set<Item> items;
    private Map<String, Test> tests;
    private Map<String, ItemNode> itemNodes;
    private String status;
    private Long start;
    // to abort connections, producerConn is handled by UtplsqlRunner
    private Connection consumerConn;

    @Override
    public String toString() {
        return new ToStringCreator(this, JsonToStringStyler.getInstance())
                .append("reporterId", reporterId)
                .append("connectionName", connectionName)
                .append("pathList", pathList)
                .append("currentTestNumber", currentTestNumber)
                .append("currentTest", currentTest)
                .append("totalNumberOfTests", totalNumberOfTests)
                .append("startTime", startTime)
                .append("endTime", endTime)
                .append("executionTime", executionTime)
                .append("counter", counter)
                .append("infoCount", infoCount)
                .append("errorStack", errorStack)
                .append("serverOutput", serverOutput)
                .append("tests", tests)
                .append("rootNode", itemNodes.get(reporterId))
                .append("status", status)
                .append("start", start)
                .append("endTime", endTime)
                .append("totalNumberOfCompletedTests", getTotalNumberOfCompletedTests())
                .toString();
    }

    public Run(final String reporterId, final String connectionName, final List<String> pathList) {
        this.reporterId = reporterId;
        this.connectionName = connectionName;
        this.pathList = pathList;
        counter = new Counter();
        items = new LinkedHashSet<>();
        tests = new LinkedHashMap<>();
        itemNodes = new LinkedHashMap<>();
        createRootNode();
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
        start = System.currentTimeMillis();
    }

    public String getName() {
        final String time = startTime.substring(11, 19);
        final String conn = connectionName != null ? URLTools.replaceHexChars(connectionName.substring(15)) : "n/a";
        return time + " (" + conn + ")";
    }

    /**
     * Is called after consuming the pre-run event to populate all items of a run.
     * It's expected to be called only once.
     * 
     * @param items items of a run, to be shown in the runner right after starting a run.
     */
    public void put(final List<Item> items) {
        populateItems(items);
        populateItemNodes();
        populateItemNodeChildren();
    }
    
    private void createRootNode() {
        // Create pseudo root node as suite. 
        // The TreeTableModel requires a single root node, but it will not be displayed.
        final Suite rootSuite = new Suite();
        rootSuite.setId(getReporterId());
        rootSuite.setName(getReporterId());
        ItemNode rootNode = new ItemNode(rootSuite);
        itemNodes.put(rootSuite.getId(), rootNode);
    }
    
    private void populateItems(List<Item> items) {
        for (final Item item : items) {
            this.items.add(item);
            if (item instanceof Suite) {
                populateItems(((Suite) item).getItems());
            } else if (item instanceof Test) {
                this.tests.put(item.getId(), (Test) item);
            }
        }
    }
    
    private void populateItemNodes() {
        for (final Item item : items) {
            itemNodes.put(item.getId(), new ItemNode(item));
        }
    }
    
    private void populateItemNodeChildren() {
        for (Item item : items) {
            String parentId = item.getParentId();
            ItemNode node = itemNodes.get(item.getId());
            ItemNode parent = itemNodes.get(parentId == null ? reporterId : parentId);
            parent.add(node);
        }
    }
    
    public Test getTest(final String id) {
        return tests.get(id);
    }

    public int getTotalNumberOfCompletedTests() {
        if (counter.getDisabled() == null || counter.getSuccess() == null || counter.getFailure() == null
                || counter.getError() == null) {
            return -1;
        }
        int total = counter.getDisabled() + counter.getSuccess() + counter.getFailure() + counter.getError();
        if (totalNumberOfTests != null && total > totalNumberOfTests) {
            // can happen when run is cancelled and two processes are updating the run in parallel
            // not worth to ensure consistency for this case, using synchronized will not be enough
            total = totalNumberOfTests;
        }
        return total;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(final String reporterId) {
        this.reporterId = reporterId;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(final String connectionName) {
        this.connectionName = connectionName;
    }

    public List<String> getPathList() {
        return pathList;
    }

    public void setPathList(final List<String> pathList) {
        this.pathList = pathList;
    }

    public Integer getCurrentTestNumber() {
        return currentTestNumber;
    }

    public void setCurrentTestNumber(final Integer currentTestNumber) {
        this.currentTestNumber = currentTestNumber;
    }

    public Test getCurrentTest() {
        return currentTest;
    }

    public void setCurrentTest(final Test currentTest) {
        this.currentTest = currentTest;
    }

    public Integer getTotalNumberOfTests() {
        return totalNumberOfTests;
    }

    public void setTotalNumberOfTests(final Integer totalNumberOfTests) {
        this.totalNumberOfTests = totalNumberOfTests;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(final String endTime) {
        this.endTime = endTime;
    }

    public Double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(final Double executionTime) {
        this.executionTime = executionTime;
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(final Counter counter) {
        this.counter = counter;
    }

    public Integer getInfoCount() {
        return infoCount;
    }

    public void setInfoCount(final Integer infoCount) {
        this.infoCount = infoCount;
    }

    public String getErrorStack() {
        return errorStack;
    }

    public void setErrorStack(final String errorStack) {
        this.errorStack = errorStack;
    }

    public String getServerOutput() {
        return serverOutput;
    }

    public void setServerOutput(final String serverOutput) {
        this.serverOutput = serverOutput;
    }

    public LinkedHashMap<String, Test> getTests() {
        return (LinkedHashMap<String, Test>) tests;
    }

    public void setTests(final LinkedHashMap<String, Test> tests) {
        this.tests = tests;
    }
    
    public LinkedHashMap<String, ItemNode> getItemNodes() {
        return (LinkedHashMap<String, ItemNode>) itemNodes;
    }

    public void setItemNodes(LinkedHashMap<String, ItemNode> itemNodes) {
        this.itemNodes = itemNodes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(final Long start) {
        this.start = start;
    }

    public Connection getConsumerConn() {
        return consumerConn;
    }

    public void setConsumerConn(Connection consumerConn) {
        this.consumerConn = consumerConn;
    }

}
