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

import javax.swing.Icon;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.JsonToStringStyler;
import org.utplsql.sqldev.resources.UtplsqlResources;

public abstract class Item {
    private String id;
    private String name;
    private String description;
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
                .append("name", name)
                .append("description", description)
                .append("startTime", startTime)
                .append("endTime", endTime)
                .append("executionTime", executionTime)
                .append("counter", counter)
                .append("errorStack", errorStack)
                .append("serverOutput", serverOutput)
                .append("warnings", warnings)
                .append("parentId", getParentId())
                .append("statusIcon", getStatusIcon())
                .append("warningIcon", getWarningIcon())
                .append("infoIcon", getInfoIcon())
                .toString();
    }
    
    public String getParentId() {
        // Works only if id (suitepath) is build based on names delimited with a period
        // that's expected for real utPLSQL runs, but may fail for artificial runs.
        // Returning null is valid, it means this item has no parent and as a 
        // consequence it will be shown on the top level in the runner.
        // A key is required to identify an item since suites can be delivered 
        // multiple times, e.g. when running a chosen list of tests. This way 
        // the tests will shown at the right position in the tree, regardless of the call
        // parameters.
        if (name != null && id != null && name.length() < id.length() && id.endsWith(name)) {
            return id.substring(0, id.length() - name.length() - 1);
        }
        return null;
    }
    
    public Icon getStatusIcon() {
        Icon icon = null;
        if (getStartTime() != null && getEndTime() == null) {
            icon = UtplsqlResources.getIcon("PROGRESS_ICON");
        } else {
            if (getCounter() != null) {
                // Escalation logic as for the color of the progress bar.
                // A suite with errors or failed tests cannot be considered successful,
                // even if some tests completed successfully.
                if (getCounter().getError() > 0) {
                    icon = UtplsqlResources.getIcon("ERROR_ICON");
                } else if (getCounter().getFailure() > 0) {
                    icon = UtplsqlResources.getIcon("FAILURE_ICON");
                } else if (getCounter().getSuccess() > 0) {
                    icon = UtplsqlResources.getIcon("SUCCESS_ICON");
                } else if (getCounter().getDisabled() > 0) {
                    icon = UtplsqlResources.getIcon("DISABLED_ICON");
                }       
            }
        }
        return icon;
    }

    public Icon getWarningIcon() {
        Icon icon = null;
        if (getCounter() != null && getCounter().getWarning() > 0) {
            icon = UtplsqlResources.getIcon("WARNING_ICON");
        }
        return icon;
    }

    public Icon getInfoIcon() {
        Icon icon = null;
        if (getServerOutput() != null && getServerOutput().length() > 0) {
            icon = UtplsqlResources.getIcon("INFO_ICON");
        }
        return icon;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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
