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
package org.utplsql.sqldev.tests

import org.junit.Assert
import org.junit.Test
import org.utplsql.sqldev.model.preference.PreferenceModel

class PreferenceModelTest {

	@Test
	def testDefaultValues() {
		val PreferenceModel model = PreferenceModel.getInstance(null)
		Assert.assertTrue(model.unsharedWorksheet)
		Assert.assertFalse(model.resetPackage)
		Assert.assertFalse(model.clearScreen)
		Assert.assertTrue(model.autoExecute)
		Assert.assertFalse(model.checkRunUtplsqlTest)
		Assert.assertEquals("test_", model.testPackagePrefix)
		Assert.assertEquals("", model.testPackageSuffix)
		Assert.assertEquals("", model.testUnitPrefix)
		Assert.assertEquals("", model.testUnitSuffix)
		Assert.assertFalse(model.checkGenerateUtplsqlTest)
		Assert.assertTrue(model.generateComments)
		Assert.assertFalse(model.disableTests)
		Assert.assertEquals("alltests", model.suitePath)
		Assert.assertEquals(3, model.indentSpaces)
		Assert.assertTrue(model.generateFiles)
		Assert.assertEquals(PreferenceModel.DEFAULT_OUTPUT_DIRECTORY, model.outputDirectory)
		Assert.assertEquals("utPLSQL", model.rootFolderInOddgenView)
	}	
}
