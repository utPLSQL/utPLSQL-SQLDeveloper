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

import org.springframework.core.style.ToStringCreator;
import org.utplsql.sqldev.model.UtplsqlToStringStyler;

public class Counter {
    private Integer disabled;
    private Integer success;
    private Integer failure;
    private Integer error;
    private Integer warning;

    @Override
    public String toString() {
        return new ToStringCreator(this, UtplsqlToStringStyler.INSTANCE)
                .append("disabled", disabled)
                .append("success", success)
                .append("failure", failure)
                .append("error", error)
                .append("warning", warning)
                .toString();
    }

    public Integer getDisabled() {
        return disabled;
    }

    public void setDisabled(final Integer disabled) {
        this.disabled = disabled;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(final Integer success) {
        this.success = success;
    }

    public Integer getFailure() {
        return failure;
    }

    public void setFailure(final Integer failure) {
        this.failure = failure;
    }

    public Integer getError() {
        return error;
    }

    public void setError(final Integer error) {
        this.error = error;
    }

    public Integer getWarning() {
        return warning;
    }

    public void setWarning(final Integer warning) {
        this.warning = warning;
    }
}
