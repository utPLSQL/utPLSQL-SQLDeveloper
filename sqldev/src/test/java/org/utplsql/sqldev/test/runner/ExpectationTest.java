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
package org.utplsql.sqldev.test.runner;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.model.runner.Expectation;

public class ExpectationTest {
    
    private Expectation getExpectationWithDescription() {
        Expectation ex = new Expectation();
        ex.setDescription("This assert must fail");
        ex.setMessage("at: 1 (number) was expected to equal: 2 (number)");
        ex.setCaller("\"SCOTT.JUNIT_UTPLSQL_TEST1_PKG.TEST_2_NOK\", line 14 ut.expect(1, 'This assert must fail').to_equal(2);");
        return ex;
    }

    private Expectation getExpectationWithoutDescription() {
        Expectation ex = new Expectation();
        ex.setMessage("at: 1 (number) was expected to equal: 2 (number)");
        ex.setCaller("\"SCOTT.JUNIT_UTPLSQL_TEST1_PKG.TEST_3_NOK\", line 42 ut.expect(1).to_equal(2);");
        return ex;
    }
  
    @Test
    public void failedExpectationCallerLine() {
        final Integer actual = getExpectationWithDescription().getCallerLine();
        final Integer expected = Integer.valueOf(14);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shortFailureTextWithDescription() {
        final String actual = getExpectationWithDescription().getShortFailureText();
        final String expected = "This assert must fail (line 14)";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shortFailureTextWithoutDescription() {
        final String actual = getExpectationWithoutDescription().getShortFailureText();
        final String expected = "Line 42";
        Assert.assertEquals(expected, actual);
    }
}
