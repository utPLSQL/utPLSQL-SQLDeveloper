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

import oracle.ide.docking.DockStation;
import oracle.ide.docking.Dockable;
import oracle.ide.docking.DockableFactory;
import oracle.ide.docking.DockingParam;
import oracle.ide.layout.ViewId;

public class RunnerFactory implements DockableFactory {
    public static final String FACTORY_NAME = "UTPLSQL_RUNNER_FACTORY";

    private RunnerView dockable;

    @Override
    public void install() {
        final DockStation dockStation = DockStation.getDockStation();
        final DockingParam dp = new DockingParam();
        final ViewId referencedViewId = new ViewId("DatabaseNavigatorWindow", "DatabaseNavigatorWindow");
        final Dockable referencedDockable = dockStation.findDockable(referencedViewId);
        dp.setTabbedWith(referencedDockable);
        dockStation.dock(getLocalDockable(), dp);
    }

    @Override
    public Dockable getDockable(final ViewId viewId) {
        if (viewId == RunnerView.VIEW_ID) {
            return getLocalDockable();
        }
        return null;
    }

    private RunnerView getLocalDockable() {
        if (dockable == null) {
            dockable = new RunnerView();
        }
        return dockable;
    }

    public static RunnerView getDockable() {
        final DockStation dockStation = DockStation.getDockStation();
        final Dockable dockable = dockStation.findDockable(RunnerView.VIEW_ID);
        return (RunnerView) dockable;
    }

    public static void showDockable() {
        final DockStation dockStation = DockStation.getDockStation();
        dockStation.setDockableVisible(getDockable(), true);
    }
}
