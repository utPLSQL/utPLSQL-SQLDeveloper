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

import java.text.DecimalFormat;
import java.util.Locale;

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

    public void setSeconds(final Double seconds) {
        this.seconds = seconds;
    }

    public void setSmart(final boolean smart) {
        this.smart = smart;
    }

    public Double getSeconds() {
        return seconds;
    }

    @Override
    public String toString() {
        Locale.setDefault(new Locale("en", "US"));
        String ret;
        if (seconds == null) {
            ret = null;
        } else if (smart) {
            if (seconds >= 60 * 60) {
                final DecimalFormat formatter = new DecimalFormat("#0.00");
                ret = formatter.format(seconds / 60 / 60) + " h";
            } else if (seconds >= 60) {
                final DecimalFormat formatter = new DecimalFormat("#0.00");
                ret = formatter.format(seconds / 60) + " min";
            } else if (seconds >= 1) {
                final DecimalFormat formatter = new DecimalFormat("#0.000");
                ret = formatter.format(seconds) + " s";
            } else {
                final DecimalFormat formatter = new DecimalFormat("##0");
                ret = formatter.format(seconds * 1000) + " ms";
            }
        } else {
            final DecimalFormat formatter = new DecimalFormat("##,##0.000");
            ret = formatter.format(seconds);
        }
        return ret;
    }
}
