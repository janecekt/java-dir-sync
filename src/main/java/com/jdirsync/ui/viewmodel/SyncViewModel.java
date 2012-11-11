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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.jdirsync.builder.DiffBuilder;
import com.jdirsync.builder.IndexBuilder;
import com.jdirsync.core.Action1;
import com.jdirsync.core.Action2;
import com.jdirsync.model.DiffRecord;
import com.jdirsync.model.DirectoryNode;
import com.jdirsync.synchronizer.Synchronizer;
import com.jdirsync.task.BuildIndexTask;
import com.jdirsync.task.SynchronizeTask;
import com.jdirsync.ui.task.UITaskUtil;
import com.jdirsync.util.FileUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncViewModel {
    private static final Logger logger = LoggerFactory.getLogger(SyncViewModel.class);

    // Services
    private IndexBuilder indexBuilder;
    private DiffBuilder diffBuilder;
    private Synchronizer synchronizer;
    private ExecutorService executorService;

    // Internal state
    private Path leftPath;
    private Path rightPath;
    private DirectoryNode leftIndex;
    private DirectoryNode rightIndex;

    // UI Properties
    private StringProperty leftNameProperty;
    private StringProperty rightNameProperty;
    private StringProperty leftPathProperty;
    private StringProperty rightPathProperty;
    private StringProperty leftToRightButtonCaptionProperty = new SimpleStringProperty();
    private StringProperty rightToLeftButtonCaptionProperty = new SimpleStringProperty();
    private BooleanProperty isBusyProperty = new SimpleBooleanProperty(false);
    private StringProperty statusProperty = new SimpleStringProperty("Idle");
    private ObservableList<DiffRecordViewModel> diffRecordViewModelList = FXCollections.observableArrayList();
    private SelectionViewModel<DiffRecordViewModel> selectionViewModel;

    public SyncViewModel(String leftName, Path leftPath, String rightName, Path rightPath,
                         ExecutorService executorService,
                         IndexBuilder indexBuilder,
                         DiffBuilder diffBuilder,
                         Synchronizer synchronizer) {

        this.executorService = executorService;
        this.indexBuilder = indexBuilder;
        this.diffBuilder = diffBuilder;
        this.synchronizer = synchronizer;
        this.leftPath = leftPath;
        this.rightPath = rightPath;

        // Initialize properties
        this.selectionViewModel = new SelectionViewModel<DiffRecordViewModel>(diffRecordViewModelList);
        this.leftNameProperty = new SimpleStringProperty(leftName);
        this.leftPathProperty =  new SimpleStringProperty(leftPath.toAbsolutePath().toString());
        this.rightNameProperty = new SimpleStringProperty(rightName);
        this.rightPathProperty = new SimpleStringProperty(rightPath.toAbsolutePath().toString());

        // Initialize binding properties
        leftToRightButtonCaptionProperty.bind(
                Bindings.concat(leftNameProperty, "-To-", rightNameProperty));
        rightToLeftButtonCaptionProperty.bind(
                Bindings.concat(rightNameProperty, "-To-", leftNameProperty));
    }


    public ObservableList<DiffRecordViewModel> getDiffRecordList() {
        return diffRecordViewModelList;
    }

    public SelectionViewModel<DiffRecordViewModel> getSelectionViewModel() {
        return this.selectionViewModel;
    }


    public StringProperty leftNameProperty() {
        return leftNameProperty;
    }

    public StringProperty rightNameProperty() {
        return rightNameProperty;
    }

    public StringProperty leftPathProperty() {
        return leftPathProperty;
    }

    public StringProperty rightPathProperty() {
        return rightPathProperty;
    }

    public StringProperty leftToRightButtonCaptionProperty() {
        return leftToRightButtonCaptionProperty;
    }

    public StringProperty rightToLeftButtonCaptionProperty() {
        return rightToLeftButtonCaptionProperty;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public BooleanProperty isBusyProperty() {
        return isBusyProperty;
    }

    public void leftToRightButtonPressed() {
        logger.info(leftToRightButtonCaptionProperty.getValue() + " Pressed");
        for (DiffRecordViewModel diffRecordVM :  diffRecordViewModelList) {
            diffRecordVM.actionProperty().set(DiffRecord.Action.USE_LEFT);
        }
    }

    public void rightToLeftButtonPressed() {
        logger.info(rightToLeftButtonCaptionProperty.getValue() + " Pressed");
        for (DiffRecordViewModel diffRecordVM :  diffRecordViewModelList) {
            diffRecordVM.actionProperty().set(DiffRecord.Action.USE_RIGHT);
        }
    }

    public void compareButtonPressed() {
        logger.info("Compare Button Pressed");

        this.leftIndex = null;
        this.rightIndex = null;
        this.isBusyProperty.set(true);
        this.statusProperty.set("Building Indexes");

        // Start background tasks to build indexes (in separate thread)
        BuildIndexTask leftBuildIndexTask = BuildIndexTask.start(executorService, indexBuilder, leftPath );
        BuildIndexTask rightBuildIndexTask = BuildIndexTask.start(executorService, indexBuilder, rightPath );

        UITaskUtil.onProgress(executorService, leftBuildIndexTask, rightBuildIndexTask, 500, new Action2<BuildIndexTask, BuildIndexTask>() {
            @Override
            public void invoke(BuildIndexTask left, BuildIndexTask right) {
                statusProperty().set("Comparing: left:"
                        + (left.isDone() ? "DONE" : left.getFileCount())
                        + " / right:"
                        + (right.isDone() ? "DONE" : right.getFileCount()) );
            }
        });

        // Wait for completion in the background
        // ... once complete dispatch to UI thread.
        UITaskUtil.onCompleted(executorService, leftBuildIndexTask, rightBuildIndexTask, new Action2<BuildIndexTask, BuildIndexTask>() {
            @Override
            public void invoke(BuildIndexTask leftIndexTask, BuildIndexTask rightIndexTask) {
                try {
                    // Store indexes
                    leftIndex = leftIndexTask.get();
                    rightIndex = rightIndexTask.get();

                    // Rebuild Diff
                    rebuildDiff();
                } catch (InterruptedException ex) {
                    Thread.interrupted();
                    logger.warn("Comparison action was interrupted");
                } catch (ExecutionException ex) {
                    logger.warn("Index building failed with exception", ex);
                } finally {
                    // Update IsBusy and Status
                    isBusyProperty.set(false);
                    statusProperty.set("Idle");
                }

            }
        });
    }

    public void synchronizeButtonPressed() {
        logger.info("Synchronize Pressed");

        // Update IsBusy and Status
        isBusyProperty.set(true);
        statusProperty.set("Synchronizing files");

        // Unwrap DiffRecords from ViewModels
        List<DiffRecord> diffRecordList = new ArrayList<>();
        for (DiffRecordViewModel viewModel : diffRecordViewModelList) {
            diffRecordList.add(viewModel.getDiffRecord());
        }

        SynchronizeTask synchronizeTask = SynchronizeTask.start(executorService, synchronizer, leftPath, rightPath, diffRecordList);

        // Progress updater task
        UITaskUtil.onProgress(executorService, synchronizeTask, 500, new Action1<SynchronizeTask>() {
            @Override
            public void invoke(SynchronizeTask task) {
                long bytesCopied = task.getBytesCopied();
                long totalBytes = task.getTotalCopyBytes();

                int progressPercentage = (totalBytes == 0) ? 0 : (int) (bytesCopied * 100.0 / totalBytes);

                statusProperty().set("Synchronizing: "
                        + progressPercentage + "% "
                        + FileUtil.formatSize(bytesCopied) + " / " + FileUtil.formatSize(totalBytes) );
            }
        });


        UITaskUtil.onCompleted(executorService, synchronizeTask, new Action1<SynchronizeTask>() {
            @Override
            public void invoke(SynchronizeTask syncTask) {
                try {
                    // Throws exception if any occured
                    syncTask.get();

                    // Rebuild Diff
                    rebuildDiff();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    logger.warn("Synchronization of files was interrupted !");
                } catch (ExecutionException ex) {
                    logger.warn("Synchronization of files failed !", ex.getCause());
                } finally {
                    // Update IsBusy and Status
                    isBusyProperty.set(false);
                    statusProperty.set("Idle");
                }
            }
        });
    }


    private void rebuildDiff() {
        // Build diff
        List<DiffRecord> diffList = diffBuilder.buildDiff(leftIndex, rightIndex);

        // Update diff record list
        diffRecordViewModelList.clear();
        for (DiffRecord diffRecord : diffList) {
            diffRecordViewModelList.add(new DiffRecordViewModel(diffRecord, leftNameProperty, rightNameProperty));
        }
    }


    public void keyPressed(KeyEvent keyEvent) {
        if ("1".equals(keyEvent.getText())) {
            if (selectionViewModel.selectedItemProperty().get() != null) {
                selectionViewModel.selectedItemProperty().get().actionProperty().set(DiffRecord.Action.USE_LEFT);
                selectionViewModel.selectNext();
            }
        } else if ("2".equals(keyEvent.getText())) {
            if (selectionViewModel.selectedItemProperty().get() != null) {
                selectionViewModel.selectedItemProperty().get().actionProperty().set(DiffRecord.Action.USE_RIGHT);
                selectionViewModel.selectNext();
            }
        } else if ("0".equals(keyEvent.getText())) {
            if (selectionViewModel.selectedItemProperty().get() != null) {
                selectionViewModel.selectedItemProperty().get().actionProperty().set(DiffRecord.Action.NONE);
                selectionViewModel.selectNext();
            }
        }
    }
}
