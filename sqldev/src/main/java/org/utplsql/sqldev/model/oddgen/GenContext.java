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
package org.utplsql.sqldev.model.oddgen;

import java.sql.Connection;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.AbstractModel;

public class GenContext extends AbstractModel {
    private Connection conn;
    private String objectType;
    private String objectName;
    private String testPackagePrefix;
    private String testPackageSuffix;
    private String testUnitPrefix;
    private String testUnitSuffix;
    private int numberOfTestsPerUnit;
    private boolean generateComments;
    private boolean disableTests;
    private String suitePath;
    private int indentSpaces;

    public Connection getConn() {
        return conn;
    }

    @Override 
    public String toString() {
        return new ToStringCreator(this, getStyler())
                .append("conn", conn)
                .append("objectType", objectType)
                .append("objectName", objectName)
                .append("testPackagePrefix", testPackagePrefix)
                .append("testPackageSuffix", testPackageSuffix)
                .append("testUnitPrefix", testUnitPrefix)
                .append("testUnitSuffix", testUnitSuffix)
                .append("numberOfTestsPerUnit", numberOfTestsPerUnit)
                .append("generateComments", generateComments)
                .append("disableTests", disableTests)
                .append("suitePath", suitePath)
                .append("indentSpaces", indentSpaces)
                .toString();
    }
    
    public void setConn(final Connection conn) {
        this.conn = conn;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(final String objectType) {
        this.objectType = objectType;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    public String getTestPackagePrefix() {
        return testPackagePrefix;
    }

    public void setTestPackagePrefix(final String testPackagePrefix) {
        this.testPackagePrefix = testPackagePrefix;
    }

    public String getTestPackageSuffix() {
        return testPackageSuffix;
    }

    public void setTestPackageSuffix(final String testPackageSuffix) {
        this.testPackageSuffix = testPackageSuffix;
    }

    public String getTestUnitPrefix() {
        return testUnitPrefix;
    }

    public void setTestUnitPrefix(final String testUnitPrefix) {
        this.testUnitPrefix = testUnitPrefix;
    }

    public String getTestUnitSuffix() {
        return testUnitSuffix;
    }

    public void setTestUnitSuffix(final String testUnitSuffix) {
        this.testUnitSuffix = testUnitSuffix;
    }

    public int getNumberOfTestsPerUnit() {
        return numberOfTestsPerUnit;
    }

    public void setNumberOfTestsPerUnit(final int numberOfTestsPerUnit) {
        this.numberOfTestsPerUnit = numberOfTestsPerUnit;
    }

    public boolean isGenerateComments() {
        return generateComments;
    }

    public void setGenerateComments(final boolean generateComments) {
        this.generateComments = generateComments;
    }

    public boolean isDisableTests() {
        return disableTests;
    }

    public void setDisableTests(final boolean disableTests) {
        this.disableTests = disableTests;
    }

    public String getSuitePath() {
        return suitePath;
    }

    public void setSuitePath(final String suitePath) {
        this.suitePath = suitePath;
    }

    public int getIndentSpaces() {
        return indentSpaces;
    }

    public void setIndentSpaces(final int indentSpaces) {
        this.indentSpaces = indentSpaces;
    }
}
