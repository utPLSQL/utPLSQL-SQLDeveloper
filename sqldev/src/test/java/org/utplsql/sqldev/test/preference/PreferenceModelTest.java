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
package org.utplsql.sqldev.test.preference;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.preference.PreferenceModel;

public class PreferenceModelTest {

    @Test
    public void defaultValues() {
        final PreferenceModel model = PreferenceModel.getInstance(null);
        Assert.assertTrue(model.isUseRealtimeReporter());
        Assert.assertTrue(model.isUnsharedWorksheet());
        Assert.assertFalse(model.isResetPackage());
        Assert.assertFalse(model.isClearScreen());
        Assert.assertTrue(model.isAutoExecute());
        Assert.assertFalse(model.isCheckRunUtplsqlTest());
        Assert.assertFalse(model.isUseSmartTimes());
        Assert.assertEquals(10, model.getNumberOfRunsInHistory());
        Assert.assertFalse(model.isShowDisabledCounter());
        Assert.assertFalse(model.isShowWarningsCounter());
        Assert.assertFalse(model.isShowInfoCounter());
        Assert.assertFalse(model.isShowWarningIndicator());
        Assert.assertFalse(model.isShowInfoIndicator());
        Assert.assertTrue(model.isShowSuccessfulTests());
        Assert.assertTrue(model.isShowDisabledTests());
        Assert.assertFalse(model.isShowTestDescription());
        Assert.assertTrue(model.isSyncDetailTab());
        Assert.assertEquals("test_", model.getTestPackagePrefix());
        Assert.assertEquals("", model.getTestPackageSuffix());
        Assert.assertEquals("", model.getTestUnitPrefix());
        Assert.assertEquals("", model.getTestUnitSuffix());
        Assert.assertEquals(1, model.getNumberOfTestsPerUnit());
        Assert.assertFalse(model.isCheckGenerateUtplsqlTest());
        Assert.assertTrue(model.isGenerateComments());
        Assert.assertFalse(model.isDisableTests());
        Assert.assertEquals("alltests", model.getSuitePath());
        Assert.assertEquals(3, model.getIndentSpaces());
        Assert.assertTrue(model.isGenerateFiles());
        Assert.assertEquals(PreferenceModel.DEFAULT_OUTPUT_DIRECTORY, model.getOutputDirectory());
        Assert.assertEquals(Boolean.valueOf(false), Boolean.valueOf(model.isDeleteExistingFiles()));
        Assert.assertEquals("utPLSQL", model.getRootFolderInOddgenView());
    }
}
