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
package org.utplsql.sqldev.test.dal;

import java.util.HashMap;

import org.utplsql.sqldev.dal.RealtimeReporterEventConsumer;
import org.utplsql.sqldev.model.runner.PostTestEvent;
import org.utplsql.sqldev.model.runner.RealtimeReporterEvent;

public class TestRealtimerReporterEventTimedConsumer implements RealtimeReporterEventConsumer {
    private final HashMap<String, Long> postTestEvents = new HashMap<>();

    public HashMap<String, Long> getPostTestEvents() {
        return postTestEvents;
    }

    @Override
    public void process(final RealtimeReporterEvent event) {
        if (event instanceof PostTestEvent) {
            postTestEvents.put(((PostTestEvent) event).getId(), System.currentTimeMillis());
        }
    }
}
