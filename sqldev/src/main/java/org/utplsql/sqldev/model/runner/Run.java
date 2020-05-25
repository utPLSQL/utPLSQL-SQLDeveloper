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

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.UtplsqlToStringStyler;

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
    private LinkedHashMap<String, Test> tests;
    private String status;
    private Long start;

    @Override
    public String toString() {
        return new ToStringCreator(this, UtplsqlToStringStyler.INSTANCE)
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
        tests = new LinkedHashMap<>();
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
        start = Long.valueOf(System.currentTimeMillis());
    }

    public String getName() {
        final String time = startTime.substring(11, 19);
        final String conn = connectionName != null ? connectionName.substring(15) : null;
        final StringBuilder sb = new StringBuilder();
        sb.append(time);
        sb.append(" (");
        sb.append(conn);
        sb.append(")");
        return sb.toString();
    }

    public void put(final List<Item> items) {
        for (final Item item : items) {
            if (item instanceof Test) {
                tests.put(((Test) item).getId(), (Test) item);
            }
            if (item instanceof Suite) {
                put(((Suite) item).getItems());
            }
        }
    }

    public Test getTest(final String id) {
        return tests.get(id);
    }

    public int getTotalNumberOfCompletedTests() {
        return counter.getDisabled() + counter.getSuccess() + counter.getFailure() + counter.getError();
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
        return tests;
    }

    public void setTests(final LinkedHashMap<String, Test> tests) {
        this.tests = tests;
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
}
