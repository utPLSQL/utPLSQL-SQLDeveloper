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

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.JsonToStringStyler;

public class PostTestEvent extends PostEvent {
    private String id;
    private Integer testNumber;
    private Integer totalNumberOfTests;
    private List<Expectation> failedExpectations;

    @Override
    public String toString() {
        return new ToStringCreator(this, JsonToStringStyler.getInstance())
                // ancestor
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("executionTime", getExecutionTime())
                .append("counter", getCounter())
                .append("errorStack", getErrorStack())
                .append("serverOutput", getServerOutput())
                .append("warnings", getWarnings())
                // local
                .append("id", id)
                .append("testNumber", testNumber)
                .append("totalNumberOfTests", totalNumberOfTests)
                .append("failedExpectations", failedExpectations)
                .toString();
    }

    public PostTestEvent() {
        failedExpectations = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(final Integer testNumber) {
        this.testNumber = testNumber;
    }

    public Integer getTotalNumberOfTests() {
        return totalNumberOfTests;
    }

    public void setTotalNumberOfTests(final Integer totalNumberOfTests) {
        this.totalNumberOfTests = totalNumberOfTests;
    }

    public List<Expectation> getFailedExpectations() {
        return failedExpectations;
    }

    public void setFailedExpectations(final List<Expectation> failedExpectations) {
        this.failedExpectations = failedExpectations;
    }
}
