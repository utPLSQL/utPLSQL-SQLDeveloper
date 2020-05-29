/**
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

import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.utplsql.sqldev.model.runner.Expectation;

@SuppressWarnings("all")
public class ExpectationTest {
  private Expectation exceptionWithDescription;
  
  private Expectation exceptionWithoutDescription;
  
  @Before
  public void setup() {
    Expectation _expectation = new Expectation();
    this.exceptionWithDescription = _expectation;
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("This assert must fail");
    this.exceptionWithDescription.setDescription(_builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("at: 1 (number) was expected to equal: 2 (number)");
    this.exceptionWithDescription.setMessage(_builder_1.toString());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("\"SCOTT.JUNIT_UTPLSQL_TEST1_PKG.TEST_2_NOK\", line 14 ut.expect(1, \'This assert must fail\').to_equal(2);");
    this.exceptionWithDescription.setCaller(_builder_2.toString());
    Expectation _expectation_1 = new Expectation();
    this.exceptionWithoutDescription = _expectation_1;
    this.exceptionWithoutDescription.setMessage(this.exceptionWithDescription.getMessage());
    this.exceptionWithoutDescription.setCaller(this.exceptionWithDescription.getCaller());
    StringConcatenation _builder_3 = new StringConcatenation();
    _builder_3.append("at: 1 (number) was expected to equal: 2 (number)");
    this.exceptionWithoutDescription.setMessage(_builder_3.toString());
    StringConcatenation _builder_4 = new StringConcatenation();
    _builder_4.append("\"SCOTT.JUNIT_UTPLSQL_TEST1_PKG.TEST_3_NOK\", line 42 ut.expect(1).to_equal(2);");
    this.exceptionWithoutDescription.setCaller(_builder_4.toString());
  }
  
  @Test
  public void failedExpectationCallerLine() {
    final Integer actual = this.exceptionWithDescription.getCallerLine();
    final Integer expected = new Integer(14);
    Assert.assertEquals(expected, actual);
  }
  
  @Test
  public void shortFailureTextWithDescription() {
    final String actual = this.exceptionWithDescription.getShortFailureText();
    final String expected = "This assert must fail (line 14)";
    Assert.assertEquals(expected, actual);
  }
  
  @Test
  public void shortFailureTextWithoutDescription() {
    final String actual = this.exceptionWithoutDescription.getShortFailureText();
    final String expected = "Line 42";
    Assert.assertEquals(expected, actual);
  }
}
