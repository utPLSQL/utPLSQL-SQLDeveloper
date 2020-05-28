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

import java.awt.Component;
import javax.swing.Icon;
import oracle.ide.docking.DockableWindow;
import oracle.ide.layout.ViewId;
import org.utplsql.sqldev.resources.UtplsqlResources;
import org.utplsql.sqldev.ui.runner.RunnerFactory;
import org.utplsql.sqldev.ui.runner.RunnerPanel;

@SuppressWarnings("all")
public class RunnerView extends DockableWindow {
  private static final String VIEW_NAME = "UTPLSQL_RUNNER_VIEW";
  
  public static final ViewId VIEW_ID = new ViewId(RunnerFactory.FACTORY_NAME, RunnerView.VIEW_NAME);
  
  private RunnerPanel panel;
  
  @Override
  public String getTitleName() {
    return UtplsqlResources.getString("RUNNER_VIEW_TITLE");
  }
  
  @Override
  public Component getGUI() {
    if ((this.panel == null)) {
      RunnerPanel _runnerPanel = new RunnerPanel();
      this.panel = _runnerPanel;
    }
    return this.panel.getGUI();
  }
  
  @Override
  public Icon getTabIcon() {
    return UtplsqlResources.getIcon("UTPLSQL_ICON");
  }
  
  public RunnerPanel getRunnerPanel() {
    this.getGUI();
    return this.panel;
  }
}
