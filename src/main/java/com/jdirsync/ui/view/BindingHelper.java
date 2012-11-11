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
package com.jdirsync.ui.view;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;

public class BindingHelper {
    public static void bindWebEngine(final WebEngine webEngine,  final StringProperty contentProperty, final StringProperty contentTypeProperty) {
        // Initialize from properties
        refreshWebView(webEngine, contentProperty, contentTypeProperty);

        // Bind content property
        contentProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                refreshWebView(webEngine, contentProperty, contentTypeProperty);
            }
        });

        // Bind content type property
        if (contentTypeProperty != null) {
            contentTypeProperty.addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    refreshWebView(webEngine, contentProperty, contentTypeProperty);
                }
            });
        }


    }

    private static void refreshWebView(final WebEngine webEngine,  final StringProperty contentProperty, final StringProperty contentTypeProperty) {
        if ((contentTypeProperty == null) || (contentTypeProperty.getValue() == null)) {
            webEngine.loadContent(contentProperty.getValue());
        } else {
            webEngine.loadContent(contentProperty.getValue(), contentTypeProperty.getValue());
        }
    }
}