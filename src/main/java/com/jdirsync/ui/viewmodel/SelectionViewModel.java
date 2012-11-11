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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionModel;


public class SelectionViewModel<T> {
    public static int NOTHING_SELECTED = -1;
    private ObservableList<T> list;
    private  ObjectProperty<T> selectedItemProperty = new SimpleObjectProperty<>();


    public SelectionViewModel(ObservableList<T> list) {
        this.list = list;
    }

    public ObjectProperty<T> selectedItemProperty() {
        return selectedItemProperty;
    }

    public int getSelectedIndex() {
        return (selectedItemProperty.get() != null)
                ? list.indexOf(selectedItemProperty.get())
                : -1;
    }

    public void selectedItemAtIndex(int index) {
        if (index >= 0 && index < list.size()) {
            T newItem = list.get(index);
            selectedItemProperty.set(newItem);
        }
    }


    public void selectNext() {
        int index = getSelectedIndex();
        if (index != NOTHING_SELECTED) {
            selectedItemAtIndex(index+1);
        }
    }

    public void selectPrevious() {
        int index = getSelectedIndex();
        if (index != NOTHING_SELECTED) {
            selectedItemAtIndex(index-1);
        }
    }


    public void selectFirst() {
        selectedItemAtIndex(0);
    }


    public void selectLast() {
        selectedItemAtIndex(list.size()-1);
    }


    public void bind(final SelectionModel<T> selectionModel) {
        // Initialize from property
        selectionModel.select(selectedItemProperty.getValue());

        // Bind selectionMode => selectedItemProperty
        selectionModel.selectedItemProperty().addListener(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observableValue, T oldValue, T newValue) {
                selectedItemProperty.set(newValue);
            }
        });

        // Bind selectedItemProperty => selectionModel
        selectedItemProperty.addListener(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observableValue, T oldValue, T newValue) {
                selectionModel.select(newValue);
            }
        });
    }
}
