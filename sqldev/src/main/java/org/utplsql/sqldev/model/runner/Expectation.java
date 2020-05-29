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
package org.utplsql.sqldev.model.runner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.UtplsqlToStringStyler;

public class Expectation {
    private String description;
    private String message;
    private String caller;

    @Override
    public String toString() {
        return new ToStringCreator(this, UtplsqlToStringStyler.INSTANCE)
                .append("description", description)
                .append("message", message)
                .append("caller", caller)
                .append("failureText", getFailureText())
                .append("shortFailureText", getShortFailureText())
                .append("callerLine", getCallerLine())
                .toString();
    }

    public String getFailureText() {
        final StringBuilder sb = new StringBuilder();
        sb.append(message.trim());
        if (caller != null) {
            sb.append('\n');
            sb.append(caller.trim());
        }
        return sb.toString();
    }

    public String getShortFailureText() {
        final StringBuilder sb = new StringBuilder();
        if (description != null) {
            sb.append(description);
            sb.append(" (line ");
            sb.append(getCallerLine());
            sb.append(")");
        } else {
            sb.append("Line ");
            sb.append(getCallerLine());
        }
        return sb.toString();
    }

    public Integer getCallerLine() {
        Integer line = null;
        if (caller != null) {
            final Pattern p = Pattern.compile("(?i)\"[^\\\"]+\",\\s+line\\s*([0-9]+)");
            final Matcher m = p.matcher(caller);
            if (m.find()) {
                line = Integer.valueOf(m.group(1));
            }
        }
        return line;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(final String caller) {
        this.caller = caller;
    }
}
