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
package org.utplsql.sqldev.test.runner;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.utplsql.sqldev.model.SystemTools;
import org.utplsql.sqldev.model.runner.Run;
import org.utplsql.sqldev.resources.UtplsqlResources;
import org.utplsql.sqldev.ui.runner.RunnerPanel;

public class UtplsqlRunnerPanelTest {
    private Run run;

    @Before
    public void setup() {
        final String reporterId = UUID.randomUUID().toString().replace("-", "");
        run = new Run(null, reporterId, Arrays.asList());
        run.setStartTime("2019-06-09T13:42:42.123456");
        run.getCounter().setDisabled(0);
        run.getCounter().setSuccess(0);
        run.getCounter().setFailure(0);
        run.getCounter().setError(0);
        run.getCounter().setWarning(0);
        run.setTotalNumberOfTests(5);
        run.setCurrentTestNumber(0);
    }

    @Test
    public void showGUI() {
        final long start = System.currentTimeMillis();
        final JFrame frame = new JFrame("utPLSQL Runner Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final RunnerPanel panel = new RunnerPanel();
        final Component gui = panel.getGUI();
        panel.setModel(run);

        SwingUtilities.invokeLater(() -> {
            frame.add(gui);
            frame.pack();
            final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize(600, 600);
            frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
            frame.setVisible(true);
        });

        run.setStatus("starting");
        panel.update(run.getReporterId());
        SystemTools.sleep(3000);

        run.getCounter().setSuccess(run.getCounter().getSuccess() + 1);
        run.setStatus("utplsql.test.a");
        panel.update(run.getReporterId());
        SystemTools.sleep(500);

        run.getCounter().setSuccess(run.getCounter().getSuccess() + 1);
        run.setStatus("utplsql.test.b");
        panel.update(run.getReporterId());
        SystemTools.sleep(500);

        run.getCounter().setSuccess(run.getCounter().getSuccess() + 1);
        run.setStatus("utplsql.test.c");
        panel.update(run.getReporterId());
        SystemTools.sleep(500);

        run.getCounter().setFailure(run.getCounter().getFailure() + 1);
        run.setStatus("utplsql.test.d");
        panel.update(run.getReporterId());
        SystemTools.sleep(500);

        run.getCounter().setSuccess(run.getCounter().getSuccess() + 1);
        run.setStatus("utplsql.test.e");
        final long end = System.currentTimeMillis();
        run.setExecutionTime(Double.valueOf(end - start) / 1000);
        run.setStatus(UtplsqlResources.getString("RUNNER_FINNISHED_TEXT"));
        panel.update(run.getReporterId());
        SystemTools.sleep(2000);
        Assert.assertNotNull(frame);
        frame.dispose();
    }
}
