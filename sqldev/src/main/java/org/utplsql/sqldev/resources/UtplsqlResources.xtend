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
package org.utplsql.sqldev.resources

import oracle.dbtools.raptor.utils.MessagesBase

class UtplsqlResources extends MessagesBase {
	private static final ClassLoader CLASS_LOADER = UtplsqlResources.classLoader
	private static final String CLASS_NAME = UtplsqlResources.canonicalName
	private static final UtplsqlResources INSTANCE = new UtplsqlResources()

	private new() {
		super(CLASS_NAME, CLASS_LOADER)
	}

	def static getString(String paramString) {
		return INSTANCE.getStringImpl(paramString)
	}

	def static get(String paramString) {
		return getString(paramString)
	}

	def static getImage(String paramString) {
		return INSTANCE.getImageImpl(paramString)
	}

	def static format(String paramString, Object... paramVarArgs) {
		return INSTANCE.formatImpl(paramString, paramVarArgs)
	}

	def static getIcon(String paramString) {
		return INSTANCE.getIconImpl(paramString)
	}

	def static getInteger(String paramString) {
		return INSTANCE.getIntegerImpl(paramString)
	}
}