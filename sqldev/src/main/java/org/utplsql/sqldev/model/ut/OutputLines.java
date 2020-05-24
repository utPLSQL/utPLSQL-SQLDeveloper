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
import org.utplsql.sqldev.model.AbstractModel;

public class OutputLines extends AbstractModel {
    private String[] lines;
    private Integer numlines;

    @Override
    public String toString() {
        return new ToStringCreator(this, getStyler())
                .append("lines", lines)
                .append("numlines", numlines)
                .toString();
    }

    public String[] getLines() {
        return this.lines;
    }

    public void setLines(final String[] lines) {
        this.lines = lines;
    }

    public Integer getNumlines() {
        return this.numlines;
    }

    public void setNumlines(final Integer numlines) {
        this.numlines = numlines;
    }
}
