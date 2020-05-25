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

import java.util.List;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.UtplsqlToStringStyler;
import org.utplsql.sqldev.model.ut.Annotation;

public class PlsqlObject {
    private String name;
    private String type;
    private Integer position;
    private List<Annotation> annotations;

    @Override
    public String toString() {
        return new ToStringCreator(this, UtplsqlToStringStyler.INSTANCE)
                .append("name", name)
                .append("type", type)
                .append("position", position)
                .append("annotations", annotations)
                .toString();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(final List<Annotation> annotations) {
        this.annotations = annotations;
    }
}
