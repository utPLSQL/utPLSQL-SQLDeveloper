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
package org.utplsql.sqldev.ui.runner;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;

import org.utplsql.sqldev.model.PrefixTools;
import org.utplsql.sqldev.model.runner.Test;
import org.utplsql.sqldev.resources.UtplsqlResources;

public class TestOverviewTableModel extends DefaultTableModel {
    private static final long serialVersionUID = -4087082648970132657L;

    private LinkedHashMap<String, Test> tests;
    private String commonPrefix;
    private boolean commonPrefixCalculated;
    private boolean showDescription;
    private boolean useSmartTimes;

    public TestOverviewTableModel() {
        super();
    }

    private void calcCommonPrefix() {
        if (!commonPrefixCalculated && tests != null && tests.size() > 0) {
            commonPrefix = PrefixTools.commonPrefix(new ArrayList<>(tests.keySet()));
            fireTableDataChanged();
            commonPrefixCalculated = true;
        }
    }

    public void setModel(final LinkedHashMap<String, Test> tests, final boolean showDescription,
            final boolean useSmartTimes) {
        commonPrefixCalculated = false;
        this.tests = tests;
        this.showDescription = showDescription;
        this.useSmartTimes = useSmartTimes;
        calcCommonPrefix();
        fireTableDataChanged();
    }

    public void updateModel(final boolean showDescription) {
        this.showDescription = showDescription;
        fireTableDataChanged();
    }

    public String getTestIdColumnName() {
        StringBuilder sb = new StringBuilder();
        calcCommonPrefix();
        if (commonPrefix == null || commonPrefix.isEmpty()) {
            if (showDescription) {
                sb.append(UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL"));
            } else {
                sb.append(UtplsqlResources.getString("RUNNER_TEST_ID_COLUMN"));
            }
        } else {
            if (showDescription) {
                sb.append(UtplsqlResources.getString("RUNNER_DESCRIPTION_LABEL"));
                sb.append(" (");
                sb.append(commonPrefix);
                sb.append(")");
            } else {
                sb.append(commonPrefix);
            }
        }
        return sb.toString();
    }

    public String getTimeColumnName() {
        return UtplsqlResources.getString("RUNNER_TEST_EXECUTION_TIME_COLUMN") + (!useSmartTimes ? " [s]" : "");
    }

    public Test getTest(final int row) {
        return new ArrayList<>(tests.entrySet()).get(row).getValue();
    }

    @Override
    public int getRowCount() {
        if (tests == null) {
            return 0;
        }
        return tests.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        final Test test = getTest(row);
        switch (col) {
            case 0:
                return test.getStatusIcon();
            case 1:
                if (showDescription && test.getDescription() != null) {
                    return test.getDescription();
                } else {
                    return test.getId().substring(commonPrefix == null ? 0 : commonPrefix.length());
                }
            case 2:
                return test.getWarningIcon();
            case 3:
                return test.getInfoIcon();
            case 4:
                return test.getExecutionTime();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(final int col) {
        switch (col) {
            case 1:
                return UtplsqlResources.getString(showDescription ? "RUNNER_DESCRIPTION_LABEL" : "RUNNER_TEST_ID_COLUMN");
            case 0:
            case 2:
            case 3:
                return ""; // icons are used instead of descriptions
            case 4:
                return getTimeColumnName();
            default:
                return null;
        }
    
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(final int col) {
        switch (col) {
            case 0:
            case 2:
            case 3:
                return Icon.class;
            case 4:
                return Double.class;
            default:
                return String.class;
        }
    }
}
