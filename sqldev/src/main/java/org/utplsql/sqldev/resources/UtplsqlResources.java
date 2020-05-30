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
package org.utplsql.sqldev.resources;

import java.awt.Image;

import javax.swing.Icon;

import oracle.dbtools.raptor.utils.MessagesBase;

public class UtplsqlResources extends MessagesBase {
    private static final ClassLoader CLASS_LOADER = UtplsqlResources.class.getClassLoader();
    private static final String CLASS_NAME = UtplsqlResources.class.getCanonicalName();
    private static final UtplsqlResources INSTANCE = new UtplsqlResources();

    private UtplsqlResources() {
        super(CLASS_NAME, CLASS_LOADER);
    }

    public static String getString(final String paramString) {
        return INSTANCE.getStringImpl(paramString);
    }

    public static String get(final String paramString) {
        return getString(paramString);
    }

    public static Image getImage(final String paramString) {
        return INSTANCE.getImageImpl(paramString);
    }

    public static String format(final String paramString, final Object... paramVarArgs) {
        return INSTANCE.formatImpl(paramString, paramVarArgs);
    }

    public static Icon getIcon(final String paramString) {
        return INSTANCE.getIconImpl(paramString);
    }

    public static Integer getInteger(final String paramString) {
        return INSTANCE.getIntegerImpl(paramString);
    }
}
