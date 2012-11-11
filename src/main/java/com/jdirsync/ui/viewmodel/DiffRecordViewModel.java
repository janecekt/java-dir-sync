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

import com.jdirsync.model.DiffRecord;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class DiffRecordViewModel {
    public enum Style { NONE, DELETE, OVERWRITE, CREATE, USE, DESTRUCTIVE }

    private DiffRecord diffRecord;
    private StringProperty leftNameProperty;
    private StringProperty rightNameProperty;
    private ReadOnlyStringWrapper pathProperty;
    private ReadOnlyStringWrapper nameProperty;
    private ReadOnlyStringWrapper diffTypeProperty;
    private ReadOnlyObjectWrapper<Style> leftStyleProperty;
    private ReadOnlyStringWrapper leftSummaryProperty;
    private ReadOnlyObjectWrapper<Style> rightStyleProperty;
    private ReadOnlyStringWrapper rightSummaryProperty;
    private ReadOnlyObjectWrapper<Style> actionStyleProperty;
    private StringProperty actionSummaryProperty;
    private ObjectProperty<DiffRecord.Action> actionProperty;

    public DiffRecordViewModel(DiffRecord diffRecord, StringProperty leftNameProperty, StringProperty rightNameProperty) {
        this.diffRecord = diffRecord;
        this.leftNameProperty = leftNameProperty;
        this.rightNameProperty = rightNameProperty;
        this.pathProperty = new ReadOnlyStringWrapper(diffRecord.getPathString());
        this.nameProperty = new ReadOnlyStringWrapper(
                (diffRecord.getLeftNode() != null)
                        ? diffRecord.getLeftNode().getName()
                        : diffRecord.getRightNode().getName());
        this.diffTypeProperty = new ReadOnlyStringWrapper();

        this.leftSummaryProperty = new ReadOnlyStringWrapper();
        this.leftStyleProperty = new ReadOnlyObjectWrapper<>();

        this.rightSummaryProperty = new ReadOnlyStringWrapper();
        this.rightStyleProperty = new ReadOnlyObjectWrapper<>();

        this.actionSummaryProperty = new ReadOnlyStringWrapper();
        this.actionStyleProperty = new ReadOnlyObjectWrapper<>();

        this.actionProperty = new SimpleObjectProperty<>(diffRecord.getAction());
        this.actionProperty.addListener(new ChangeListener<DiffRecord.Action>() {
            @Override
            public void changed(ObservableValue<? extends DiffRecord.Action> observableValue, DiffRecord.Action action, DiffRecord.Action action1) {
                onActionPropertyChanged();
            }
        });

        switch (diffRecord.getDiffType()) {
            case MISSING_LEFT:
                diffTypeProperty.bind(Bindings.concat("MISSING on ", leftNameProperty));
                leftSummaryProperty.set( "---" );
                rightSummaryProperty.set( diffRecord.getRightNode().getName() );
                break;
            case MISSING_RIGHT:
                diffTypeProperty.bind(Bindings.concat("MISSING on ", rightNameProperty));
                leftSummaryProperty.set( diffRecord.getLeftNode().getName() );
                rightSummaryProperty.set("---");
                break;
            case SIZE:
                diffTypeProperty.set("Different SIZE");
                leftSummaryProperty.set( diffRecord.getLeftNode().toFormattedString("{name} ({size})") );
                rightSummaryProperty.set( diffRecord.getRightNode().toFormattedString("{name} ({size})") );
                break;
            case LEFT_NEWER:
                diffTypeProperty.bind(Bindings.concat("Newer on ", leftNameProperty));
                leftSummaryProperty.set( diffRecord.getLeftNode().toFormattedString("{name} ({date})") );
                rightSummaryProperty.set( diffRecord.getRightNode().toFormattedString("{name} ({date})") );
                break;
            case RIGHT_NEWER:
                diffTypeProperty.bind(Bindings.concat("Newer on ", rightNameProperty));
                leftSummaryProperty.set( diffRecord.getLeftNode().toFormattedString("{name} ({date})") );
                rightSummaryProperty.set(diffRecord.getRightNode().toFormattedString("{name} ({date})"));
                break;
            case LEFT_DIR_RIGHT_FILE:
                diffTypeProperty.bind(Bindings.concat(leftNameProperty, " FILE vs ", rightNameProperty, " DIR"));
                leftSummaryProperty.set( diffRecord.getLeftNode().toFormattedString("{name} ({type})") );
                rightSummaryProperty.set(diffRecord.getRightNode().toFormattedString("{name} ({type})"));
                break;
            case LEFT_FILE_RIGHT_DIR:
                diffTypeProperty.bind(Bindings.concat(leftNameProperty, " DIR vs ", rightNameProperty, " FILE"));
                leftSummaryProperty.set( diffRecord.getLeftNode().toFormattedString("{name} ({type})") );
                rightSummaryProperty.set(diffRecord.getRightNode().toFormattedString("{name} ({type})"));
                break;
            default:
                throw new RuntimeException("Unsupported diff type");
        }

        // Call this to initialize Action dependant properties
        onActionPropertyChanged();
    }

    public void onActionPropertyChanged() {
        // Set action to diff record
        diffRecord.setAction(actionProperty.get());

        // Update action summary accordingly
        if (diffRecord.getAction() == DiffRecord.Action.NONE) {
            actionSummaryProperty().unbind();
            actionSummaryProperty.set("None");
            actionStyleProperty.set(Style.NONE);
            leftStyleProperty.set(Style.NONE);
            rightStyleProperty.set(Style.NONE);
        } else {
            switch (diffRecord.getDiffType()) {
                case MISSING_LEFT:
                    if (diffRecord.getAction() == DiffRecord.Action.USE_LEFT ) {
                        actionSummaryProperty.bind(Bindings.concat("Delete on ", rightNameProperty));
                        actionStyleProperty.set(Style.DESTRUCTIVE);
                        leftStyleProperty.set(Style.USE);
                        rightStyleProperty.set(Style.DELETE);
                    } else {
                        actionSummaryProperty.bind(Bindings.concat("Copy to ", leftNameProperty));
                        actionStyleProperty.set(Style.NONE);
                        leftStyleProperty.set(Style.CREATE);
                        rightStyleProperty.set(Style.USE);
                    }
                    break;
                case MISSING_RIGHT:
                    if (diffRecord.getAction() == DiffRecord.Action.USE_RIGHT ) {
                        actionSummaryProperty.bind(Bindings.concat("Delete on ", leftNameProperty));
                        actionStyleProperty.set(Style.DESTRUCTIVE);
                        leftStyleProperty.set(Style.DELETE);
                        rightStyleProperty.set(Style.USE);
                    } else {
                        actionSummaryProperty.bind(Bindings.concat("Copy to ", rightNameProperty));
                        actionStyleProperty.set(Style.NONE);
                        leftStyleProperty.set(Style.USE);
                        rightStyleProperty.set(Style.CREATE);
                    }
                    break;
                case LEFT_FILE_RIGHT_DIR:
                case LEFT_DIR_RIGHT_FILE:
                case RIGHT_NEWER:
                case LEFT_NEWER:
                case SIZE:
                    actionStyleProperty.set(Style.DESTRUCTIVE);

                    if (diffRecord.getAction() == DiffRecord.Action.USE_LEFT ) {
                        actionSummaryProperty.bind(Bindings.concat("Overwrite on ", rightNameProperty));
                        leftStyleProperty.set(Style.USE);
                        rightStyleProperty.set(Style.OVERWRITE);
                    } else {
                        actionSummaryProperty.bind(Bindings.concat("Overwrite on ", leftNameProperty));
                        leftStyleProperty.set(Style.OVERWRITE);
                        rightStyleProperty.set(Style.USE);
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported diff type");
            }
        }
    }

    public DiffRecord getDiffRecord() {
        return diffRecord;
    }

    public ReadOnlyStringWrapper pathProperty() {
        return pathProperty;
    }


    public ReadOnlyStringWrapper nameProperty() {
        return nameProperty;
    }


    public ReadOnlyStringWrapper diffTypeProperty() {
        return diffTypeProperty;
    }


    public ReadOnlyStringWrapper leftSummaryProperty() {
        return leftSummaryProperty;
    }


    public ReadOnlyStringWrapper rightSummaryProperty() {
        return rightSummaryProperty;
    }


    public StringProperty actionSummaryProperty() {
        return actionSummaryProperty;
    }


    public ObjectProperty<DiffRecord.Action> actionProperty() {
        return actionProperty;
    }


    public ReadOnlyObjectWrapper<Style> leftStyleProperty() {
        return leftStyleProperty;
    }


    public ReadOnlyObjectWrapper<Style> rightStyleProperty() {
        return rightStyleProperty;
    }


    public ReadOnlyObjectWrapper<Style> actionStyleProperty() {
        return actionStyleProperty;
    }
}
