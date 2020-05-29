/**
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
import java.util.Collections;
import java.util.UUID;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Before;
import org.junit.Test;
import org.utplsql.sqldev.model.runner.Counter;
import org.utplsql.sqldev.model.runner.Run;
import org.utplsql.sqldev.resources.UtplsqlResources;
import org.utplsql.sqldev.ui.runner.RunnerPanel;

@SuppressWarnings("all")
public class UtplsqlRunnerPanelTest {
  private Run run;
  
  @Before
  public void setup() {
    final String reporterId = UUID.randomUUID().toString().replace("-", "");
    Run _run = new Run(null, reporterId, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList()));
    this.run = _run;
    this.run.setStartTime("2019-06-09T13:42:42.123456");
    Counter _counter = this.run.getCounter();
    _counter.setDisabled(Integer.valueOf(0));
    Counter _counter_1 = this.run.getCounter();
    _counter_1.setSuccess(Integer.valueOf(0));
    Counter _counter_2 = this.run.getCounter();
    _counter_2.setFailure(Integer.valueOf(0));
    Counter _counter_3 = this.run.getCounter();
    _counter_3.setError(Integer.valueOf(0));
    Counter _counter_4 = this.run.getCounter();
    _counter_4.setWarning(Integer.valueOf(0));
    this.run.setTotalNumberOfTests(Integer.valueOf(5));
    this.run.setCurrentTestNumber(Integer.valueOf(0));
  }
  
  @Test
  public void showGUI() {
    try {
      final long start = System.currentTimeMillis();
      final JFrame frame = new JFrame("utPLSQL Runner Panel");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final RunnerPanel panel = new RunnerPanel();
      final Component gui = panel.getGUI();
      panel.setModel(this.run);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          frame.add(gui);
          frame.pack();
          final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
          frame.setLocation(((dim.width / 2) - (frame.getSize().width / 2)), ((dim.height / 2) - (frame.getSize().height / 2)));
          frame.setVisible(true);
        }
      });
      this.run.setStatus("starting");
      panel.update(this.run.getReporterId());
      Thread.sleep(3000);
      Counter _counter = this.run.getCounter();
      Integer _success = this.run.getCounter().getSuccess();
      int _plus = ((_success).intValue() + 1);
      _counter.setSuccess(Integer.valueOf(_plus));
      this.run.setStatus("utplsql.test.a");
      panel.update(this.run.getReporterId());
      Thread.sleep(500);
      Counter _counter_1 = this.run.getCounter();
      Integer _success_1 = this.run.getCounter().getSuccess();
      int _plus_1 = ((_success_1).intValue() + 1);
      _counter_1.setSuccess(Integer.valueOf(_plus_1));
      this.run.setStatus("utplsql.test.b");
      panel.update(this.run.getReporterId());
      Thread.sleep(500);
      Counter _counter_2 = this.run.getCounter();
      Integer _success_2 = this.run.getCounter().getSuccess();
      int _plus_2 = ((_success_2).intValue() + 1);
      _counter_2.setSuccess(Integer.valueOf(_plus_2));
      this.run.setStatus("utplsql.test.c");
      panel.update(this.run.getReporterId());
      Thread.sleep(500);
      Counter _counter_3 = this.run.getCounter();
      Integer _failure = this.run.getCounter().getFailure();
      int _plus_3 = ((_failure).intValue() + 1);
      _counter_3.setFailure(Integer.valueOf(_plus_3));
      this.run.setStatus("utplsql.test.d");
      panel.update(this.run.getReporterId());
      Thread.sleep(500);
      Counter _counter_4 = this.run.getCounter();
      Integer _success_3 = this.run.getCounter().getSuccess();
      int _plus_4 = ((_success_3).intValue() + 1);
      _counter_4.setSuccess(Integer.valueOf(_plus_4));
      this.run.setStatus("utplsql.test.e");
      final long end = System.currentTimeMillis();
      Double _double = new Double((end - start));
      double _divide = ((_double).doubleValue() / 1000);
      this.run.setExecutionTime(Double.valueOf(_divide));
      this.run.setStatus(UtplsqlResources.getString("RUNNER_FINNISHED_TEXT"));
      panel.update(this.run.getReporterId());
      Thread.sleep(2000);
      frame.dispose();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
