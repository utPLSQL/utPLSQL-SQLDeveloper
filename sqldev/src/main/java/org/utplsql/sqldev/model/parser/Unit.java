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
package org.utplsql.sqldev.model.parser;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.JsonToStringStyler;

public class Unit {
    private String name;
    private Integer position;
    private Integer positionOfName;

    @Override
    public String toString() {
        return new ToStringCreator(this, JsonToStringStyler.INSTANCE)
                .append("name", name)
                .append("position", position)
                .append("positionOfName", positionOfName)
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public Integer getPositionOfName() {
        return positionOfName;
    }

    public void setPositionOfName(final Integer positionOfName) {
        this.positionOfName = positionOfName;
    }
}
