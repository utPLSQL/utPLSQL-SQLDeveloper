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

import java.util.List;

import javax.swing.Icon;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.JsonToStringStyler;
import org.utplsql.sqldev.resources.UtplsqlResources;

@SuppressWarnings("unused")
public class Test extends Item {
    private String executableType;
    private String ownerName;
    private String objectName;
    private String procedureName;
    private Boolean disabled;
    private String name;
    private String description;
    private Integer testNumber;
    private List<Expectation> failedExpectations;

    @Override
    public String toString() {
        return new ToStringCreator(this, JsonToStringStyler.getInstance())
                // ancestor
                .append("id", getId())
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("executionTime", getExecutionTime())
                .append("counter", getCounter())
                .append("errorStack", getErrorStack())
                .append("serverOutput", getServerOutput())
                .append("warnings", getWarnings())
                // local
                .append("executableType", executableType)
                .append("ownerName", ownerName)
                .append("objectName", objectName)
                .append("procedureName", procedureName)
                .append("disabled", disabled)
                .append("name", name)
                .append("description", description)
                .append("testNumber", testNumber)
                .append("failedExpectations", failedExpectations)
                .append("statusIcon", getStatusIcon())
                .append("warningIcon", getWarningIcon())
                .append("infoIcon", getInfoIcon())
                .toString();
    }

    public Icon getStatusIcon() {
        Icon icon = null;
        if (getStartTime() != null && getEndTime() == null) {
            icon = UtplsqlResources.getIcon("PROGRESS_ICON");
        } else {
            if (getCounter() != null) {
                if (getCounter().getSuccess() > 0) {
                    icon = UtplsqlResources.getIcon("SUCCESS_ICON");
                } else if (getCounter().getError() > 0) {
                    icon = UtplsqlResources.getIcon("ERROR_ICON");
                } else if (getCounter().getFailure() > 0) {
                    icon = UtplsqlResources.getIcon("FAILURE_ICON");
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

    public String getExecutableType() {
        return executableType;
    }

    public void setExecutableType(final String executableType) {
        this.executableType = executableType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(final String procedureName) {
        this.procedureName = procedureName;
    }

    public Boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(final Boolean disabled) {
        this.disabled = disabled;
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

    public Integer getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(final Integer testNumber) {
        this.testNumber = testNumber;
    }

    public List<Expectation> getFailedExpectations() {
        return failedExpectations;
    }

    public void setFailedExpectations(final List<Expectation> failedExpectations) {
        this.failedExpectations = failedExpectations;
    }
}
