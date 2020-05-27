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

import java.util.AbstractMap;

public class ComboBoxItem<K extends Object, V extends Object> extends AbstractMap.SimpleEntry<K, V> {
    private static final long serialVersionUID = 7869442222989031548L;

    public ComboBoxItem(final K key, final V value) {
        super(key, value);
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }
}
