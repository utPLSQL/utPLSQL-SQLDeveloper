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

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.sqldev.ui.runner.SmartTime;

@SuppressWarnings("all")
public class SmartTimeTest {
  @Test
  public void null_default() {
    final String effective = new SmartTime(null, false).toString();
    Assert.assertEquals(null, effective);
  }
  
  @Test
  public void null_smart() {
    final String effective = new SmartTime(null, true).toString();
    Assert.assertEquals(null, effective);
  }
  
  @Test
  public void ms_0_default() {
    final String effective = new SmartTime(Double.valueOf(0.0), false).toString();
    Assert.assertEquals("0.000", effective);
  }
  
  @Test
  public void ms_0_smart() {
    final String effective = new SmartTime(Double.valueOf(0.0), true).toString();
    Assert.assertEquals("0 ms", effective);
  }
  
  @Test
  public void ms_999_default() {
    final String effective = new SmartTime(Double.valueOf(0.999), false).toString();
    Assert.assertEquals("0.999", effective);
  }
  
  @Test
  public void ms_999_smart() {
    final String effective = new SmartTime(Double.valueOf(0.999), true).toString();
    Assert.assertEquals("999 ms", effective);
  }
  
  @Test
  public void s_1_default() {
    final String effective = new SmartTime(Double.valueOf(1.0), false).toString();
    Assert.assertEquals("1.000", effective);
  }
  
  @Test
  public void s_1_smart() {
    final String effective = new SmartTime(Double.valueOf(1.0), true).toString();
    Assert.assertEquals("1.000 s", effective);
  }
  
  @Test
  public void s_59_default() {
    final String effective = new SmartTime(Double.valueOf(59.999), false).toString();
    Assert.assertEquals("59.999", effective);
  }
  
  @Test
  public void s_59_smart() {
    final String effective = new SmartTime(Double.valueOf(59.999), true).toString();
    Assert.assertEquals("59.999 s", effective);
  }
  
  @Test
  public void min_1_default() {
    final String effective = new SmartTime(Double.valueOf(60.0), false).toString();
    Assert.assertEquals("60.000", effective);
  }
  
  @Test
  public void min_1_smart() {
    final String effective = new SmartTime(Double.valueOf(60.0), true).toString();
    Assert.assertEquals("1.00 min", effective);
  }
  
  @Test
  public void min_59_default() {
    final String effective = new SmartTime(Double.valueOf(3599.999), false).toString();
    Assert.assertEquals("3,599.999", effective);
  }
  
  @Test
  public void min_59_smart_and_rounded() {
    final String effective = new SmartTime(Double.valueOf(3599.999), true).toString();
    Assert.assertEquals("60.00 min", effective);
  }
  
  @Test
  public void h_1_default() {
    final String effective = new SmartTime(Double.valueOf(3600.0), false).toString();
    Assert.assertEquals("3,600.000", effective);
  }
  
  @Test
  public void h_1_smart() {
    final String effective = new SmartTime(Double.valueOf(3600.0), true).toString();
    Assert.assertEquals("1.00 h", effective);
  }
  
  @Test
  public void h_max_default() {
    final String effective = new SmartTime(Double.valueOf(99999.999), false).toString();
    Assert.assertEquals("99,999.999", effective);
  }
  
  @Test
  public void h_max_smart() {
    final String effective = new SmartTime(Double.valueOf(99999.999), true).toString();
    Assert.assertEquals("27.78 h", effective);
  }
  
  @Test
  public void h_higher_than_max_default() {
    final String effective = new SmartTime(Double.valueOf(100000000.0), false).toString();
    Assert.assertEquals("100,000,000.000", effective);
  }
  
  @Test
  public void h_higher_than_max_smart() {
    final String effective = new SmartTime(Double.valueOf(100000000.0), true).toString();
    Assert.assertEquals("27777.78 h", effective);
  }
}
