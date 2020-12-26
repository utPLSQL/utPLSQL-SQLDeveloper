/*
 * Copyright 2018 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.JsonToStringStyler;

public abstract class Item {
    private String id;
    private String startTime;
    private String endTime;
    private Double executionTime;
    private Counter counter;
    private String errorStack;
    private String serverOutput;
    private String warnings;

    public Item() {
        counter = new Counter();
    }

    @Override
    public String toString() {
        return new ToStringCreator(this, JsonToStringStyler.getInstance())
                .append("id", id)
                .append("startTime", startTime)
                .append("endTime", endTime)
                .append("executionTime", executionTime)
                .append("counter", counter)
                .append("errorStack", errorStack)
                .append("serverOutput", serverOutput)
                .append("warnings", warnings)
                .toString();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
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

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(final String warnings) {
        this.warnings = warnings;
    }
}
