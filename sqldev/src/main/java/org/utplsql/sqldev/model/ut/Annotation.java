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
package org.utplsql.sqldev.model.ut;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.JsonToStringStyler;

public class Annotation {
    private String objectOwner;
    private String objectName;
    private String name;
    private String text;
    private String subobjectName;

    @Override
    public String toString() {
        return new ToStringCreator(this, JsonToStringStyler.getInstance())
                .append("objectOwner", objectOwner)
                .append("objectName", objectName)
                .append("name", name)
                .append("text", text)
                .append("subobjectName", subobjectName)
                .toString();
    }    
    
    public String getObjectOwner() {
        return objectOwner;
    }

    public void setObjectOwner(final String objectOwner) {
        this.objectOwner = objectOwner;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(final String objectName) {
        this.objectName = objectName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getSubobjectName() {
        return subobjectName;
    }

    public void setSubobjectName(final String subobjectName) {
        this.subobjectName = subobjectName;
    }
}
