/*
 *  Copyright (c) 2008 - Tomas Janecek.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.jdirsync.ui.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MainFormViewModel {
    private StringProperty windowTitleProperty = new SimpleStringProperty();
    private SyncViewModel syncViewModel;
    private LogObservingViewModel logObservingViewModel;



    public MainFormViewModel(SyncViewModel syncViewModel, LogObservingViewModel logObservingViewModel) {
        this.syncViewModel = syncViewModel;
        this.logObservingViewModel = logObservingViewModel;

        windowTitleProperty.bind(
                Bindings.concat("JDirSync - ",
                        syncViewModel.leftNameProperty(), " - ", syncViewModel.leftPathProperty(),
                        "           /       ",
                        syncViewModel.rightNameProperty(), " -  ", syncViewModel.rightPathProperty()) );
    }

    public LogObservingViewModel getLogObservingViewModel() {
        return logObservingViewModel;
    }

    public SyncViewModel getSyncViewModel() {
        return syncViewModel;
    }

    public StringProperty windowTitleProperty() {
        return windowTitleProperty;
    }
}

