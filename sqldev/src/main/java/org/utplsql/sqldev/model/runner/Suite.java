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

public class Suite extends Item {
    private List<Item> items;

    public Suite() {
        items = new ArrayList<>();
    }

    @Override
    public String toString() {
        return new ToStringCreator(this, JsonToStringStyler.getInstance())
                // ancestor
                .append("id", getId())
                .append("name", getName())
                .append("description", getDescription())
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("executionTime", getExecutionTime())
                .append("counter", getCounter())
                .append("errorStack", getErrorStack())
                .append("serverOutput", getServerOutput())
                .append("warnings", getWarnings())
                .append("parentId", getParentId())
                .append("statusIcon", getStatusIcon())
                .append("warningIcon", getWarningIcon())
                .append("infoIcon", getInfoIcon())
                // local
                .append("items", items)
                .toString();
    }
    
    public List<Item> getItems() {
        return items;
    }

    public void setItems(final List<Item> items) {
        this.items = items;
    }
}
