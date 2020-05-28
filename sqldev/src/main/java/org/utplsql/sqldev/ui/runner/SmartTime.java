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
package org.utplsql.sqldev.ui.runner;

import java.text.DecimalFormat;

@SuppressWarnings("all")
public class SmartTime {
  private Double seconds;
  
  private boolean smart = false;
  
  public SmartTime() {
    super();
  }
  
  public SmartTime(final Double seconds, final boolean smart) {
    super();
    this.seconds = seconds;
    this.smart = smart;
  }
  
  public Double setSeconds(final Double seconds) {
    return this.seconds = seconds;
  }
  
  public boolean setSmart(final boolean smart) {
    return this.smart = smart;
  }
  
  public Double getSeconds() {
    return this.seconds;
  }
  
  @Override
  public String toString() {
    String ret = null;
    if ((this.seconds == null)) {
      ret = null;
    } else {
      if (this.smart) {
        if (((this.seconds).doubleValue() >= (60 * 60))) {
          final DecimalFormat formatter = new DecimalFormat("#0.00");
          String _format = formatter.format((((this.seconds).doubleValue() / 60) / 60));
          String _plus = (_format + " h");
          ret = _plus;
        } else {
          if (((this.seconds).doubleValue() >= 60)) {
            final DecimalFormat formatter_1 = new DecimalFormat("#0.00");
            String _format_1 = formatter_1.format(((this.seconds).doubleValue() / 60));
            String _plus_1 = (_format_1 + " min");
            ret = _plus_1;
          } else {
            if (((this.seconds).doubleValue() >= 1)) {
              final DecimalFormat formatter_2 = new DecimalFormat("#0.000");
              String _format_2 = formatter_2.format(this.seconds);
              String _plus_2 = (_format_2 + " s");
              ret = _plus_2;
            } else {
              final DecimalFormat formatter_3 = new DecimalFormat("##0");
              String _format_3 = formatter_3.format(((this.seconds).doubleValue() * 1000));
              String _plus_3 = (_format_3 + " ms");
              ret = _plus_3;
            }
          }
        }
      } else {
        final DecimalFormat formatter_4 = new DecimalFormat("##,##0.000");
        ret = formatter_4.format(this.seconds);
      }
    }
    return ret;
  }
}
