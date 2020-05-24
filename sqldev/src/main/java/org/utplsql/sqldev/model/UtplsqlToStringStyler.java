/*
 * Copyright 2020 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
package org.utplsql.sqldev.model;

import org.springframework.core.style.DefaultToStringStyler;

public class UtplsqlToStringStyler extends DefaultToStringStyler {

    public UtplsqlToStringStyler() {
        super(new UtplsqlValueStyler());
    }
    
    @Override
    public void styleFieldSeparator(StringBuilder buffer) {
        buffer.append(',');
        buffer.append('\n');
    }
}