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

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import com.jdirsync.ui.viewmodel.AggregateObservable;
import com.jdirsync.ui.viewmodel.DiffRecordViewModel;
import com.jdirsync.ui.viewmodel.MainFormViewModel;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Callback;

/**
 * Helper class which sets up the binding between view (defined in FXML and ViewModel)
 * (ideally it should be possible to remove this completely once bi-directional binding from FXML is supported)
 */
public class MainFormView implements Initializable {
    @FXML
    private Button leftToRightButton;

    @FXML
    private Button rightToLeftButton;

    @FXML
    private Button compareButton;

    @FXML
    private Button synchronizeButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label leftNameLabel;

    @FXML
    private Label leftSummaryLabel;

    @FXML
    private Label rightNameLabel;

    @FXML
    private Label rightSummaryLabel;

    @FXML
    private WebView logWebView;

    @FXML
    private TableView<DiffRecordViewModel> diffTableView;

    @FXML
    private TableColumn<DiffRecordViewModel, String> directoryColumn;

    @FXML
    private TableColumn<DiffRecordViewModel, String> nameColumn;

    @FXML
    private TableColumn<DiffRecordViewModel, String> diffTypeColumn;

    @FXML
    private TableColumn<DiffRecordViewModel, DiffRecordViewModel> leftColumn;

    @FXML
    private TableColumn<DiffRecordViewModel, DiffRecordViewModel> arrowColumn;


    @FXML
    private TableColumn<DiffRecordViewModel, DiffRecordViewModel> rightColumn;

    @FXML
    private TableColumn<DiffRecordViewModel, DiffRecordViewModel> actionColumn;



    private Image leftToRightImage;
    private Image rightToLeftImage;

    private MainFormViewModel viewModel;


    public MainFormView(MainFormViewModel viewModel) {
        this.viewModel = viewModel;
        this.leftToRightImage = new Image(getClass().getResourceAsStream("/com/jdirsync/ui/icon/left-to-right-arrow.png"));
        this.rightToLeftImage = new Image(getClass().getResourceAsStream("/com/jdirsync/ui/icon/right-to-left-arrow.png"));
    }

    @FXML
    public void leftToRightButtonPressed(ActionEvent event) {
        viewModel.getSyncViewModel().leftToRightButtonPressed();
    }

    @FXML
    public void rightToLeftButtonPressed(ActionEvent event) {
        viewModel.getSyncViewModel().rightToLeftButtonPressed();
    }

    @FXML
    public void compareButtonPressed(ActionEvent event) {
        viewModel.getSyncViewModel().compareButtonPressed();
    }

    @FXML
    public void synchronizeButtonPressed(ActionEvent event) {
        viewModel.getSyncViewModel().synchronizeButtonPressed();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Bind isBusy => ENABLED property of UI items
        leftToRightButton.disableProperty().bind(viewModel.getSyncViewModel().isBusyProperty());
        rightToLeftButton.disableProperty().bind(viewModel.getSyncViewModel().isBusyProperty());
        compareButton.disableProperty().bind(viewModel.getSyncViewModel().isBusyProperty());
        synchronizeButton.disableProperty().bind(viewModel.getSyncViewModel().isBusyProperty());
        diffTableView.disableProperty().bind(viewModel.getSyncViewModel().isBusyProperty());

        // Bind status label
        statusLabel.textProperty().bind(viewModel.getSyncViewModel().statusProperty());

        // Bind button captions
        leftToRightButton.textProperty().bind(viewModel.getSyncViewModel().leftToRightButtonCaptionProperty());
        rightToLeftButton.textProperty().bind(viewModel.getSyncViewModel().rightToLeftButtonCaptionProperty());

        // Bind button captions
        leftNameLabel.textProperty().bind(viewModel.getSyncViewModel().leftNameProperty());
        rightNameLabel.textProperty().bind(viewModel.getSyncViewModel().rightNameProperty());
        leftSummaryLabel.textProperty().bind(viewModel.getSyncViewModel().leftPathProperty());
        rightSummaryLabel.textProperty().bind(viewModel.getSyncViewModel().rightPathProperty());

        // Bind column captions
        leftColumn.textProperty().bind(viewModel.getSyncViewModel().leftNameProperty());
        rightColumn.textProperty().bind(viewModel.getSyncViewModel().rightNameProperty());

        // Bind LogView
        BindingHelper.bindWebEngine(
                logWebView.getEngine(),
                viewModel.getLogObservingViewModel().logAsHtmlProperty(),
                null);

        // Bind TableView
        diffTableView.setItems(viewModel.getSyncViewModel().getDiffRecordList());

        diffTableView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                viewModel.getSyncViewModel().keyPressed(keyEvent);
            }
        });


        viewModel.getSyncViewModel().getSelectionViewModel().bind(diffTableView.getSelectionModel());

        directoryColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DiffRecordViewModel, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DiffRecordViewModel, String> p) {
                return p.getValue().pathProperty();
            }
        });

        /*
        nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DiffRecordViewModel, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DiffRecordViewModel, String> p) {
                return p.getValue().nameProperty();
            }
        });
        */

        diffTypeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DiffRecordViewModel, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<DiffRecordViewModel, String> p) {
                return p.getValue().diffTypeProperty();
            }
        });


        leftColumn.setComparator(new Comparator<DiffRecordViewModel>() {
            @Override
            public int compare(DiffRecordViewModel o1, DiffRecordViewModel o2) {
                return (o1.leftSummaryProperty().get() == null)
                        ? -1
                        : o1.leftSummaryProperty().get().compareTo(o2.leftSummaryProperty().get());
            }
        });

        leftColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel>, ObservableValue<DiffRecordViewModel>>() {
            public ObservableValue<DiffRecordViewModel> call(TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel> p) {
                return new AggregateObservable<>(p.getValue(),
                        p.getValue().leftSummaryProperty(),
                        p.getValue().leftStyleProperty());
            }
        });

        leftColumn.setCellFactory(new Callback<TableColumn<DiffRecordViewModel, DiffRecordViewModel>, TableCell<DiffRecordViewModel, DiffRecordViewModel>>() {
            @Override
            public TableCell<DiffRecordViewModel, DiffRecordViewModel> call(TableColumn<DiffRecordViewModel, DiffRecordViewModel> param) {
                return new TableCell<DiffRecordViewModel, DiffRecordViewModel>() {
                    @Override
                    public void updateItem(DiffRecordViewModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            setGraphic( buildTextNode(
                                    item.leftSummaryProperty().getValue(),
                                    item.leftStyleProperty().getValue() ));
                        }
                    }
                };
            }
        });


        arrowColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel>, ObservableValue<DiffRecordViewModel>>() {
            public ObservableValue<DiffRecordViewModel> call(TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel> p) {
                return new AggregateObservable<>(p.getValue(),
                        p.getValue().actionProperty());
            }
        });

        arrowColumn.setCellFactory(new Callback<TableColumn<DiffRecordViewModel, DiffRecordViewModel>, TableCell<DiffRecordViewModel, DiffRecordViewModel>>() {
            @Override
            public TableCell<DiffRecordViewModel, DiffRecordViewModel> call(TableColumn<DiffRecordViewModel, DiffRecordViewModel> param) {
                return new TableCell<DiffRecordViewModel, DiffRecordViewModel>() {
                    @Override
                    public void updateItem(DiffRecordViewModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            ImageView imageView = new ImageView();
                            imageView.setFitHeight(25);
                            imageView.setFitWidth(25);
                            switch (item.actionProperty().getValue()) {
                                case USE_LEFT:
                                    imageView.setImage(leftToRightImage);
                                    setGraphic(imageView);
                                    break;
                                case USE_RIGHT:
                                    imageView.setImage(rightToLeftImage);
                                    setGraphic(imageView);
                                    break;
                                default:
                                    setGraphic(null);
                                    break;
                            }
                        }
                    }
                };
            }
        });


        rightColumn.setComparator(new Comparator<DiffRecordViewModel>() {
            @Override
            public int compare(DiffRecordViewModel o1, DiffRecordViewModel o2) {
                return (o1.rightSummaryProperty().get() == null)
                        ? -1
                        : o1.rightSummaryProperty().get().compareTo(o2.rightSummaryProperty().get());
            }
        });

        rightColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel>, ObservableValue<DiffRecordViewModel>>() {
            public ObservableValue<DiffRecordViewModel> call(TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel> p) {
                return new AggregateObservable<>(p.getValue(),
                        p.getValue().rightSummaryProperty(),
                        p.getValue().rightStyleProperty());
            }
        });

        rightColumn.setCellFactory(new Callback<TableColumn<DiffRecordViewModel, DiffRecordViewModel>, TableCell<DiffRecordViewModel, DiffRecordViewModel>>() {
            @Override
            public TableCell<DiffRecordViewModel, DiffRecordViewModel> call(TableColumn<DiffRecordViewModel, DiffRecordViewModel> param) {
                return new TableCell<DiffRecordViewModel, DiffRecordViewModel>() {
                    @Override
                    public void updateItem(DiffRecordViewModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            setGraphic( buildTextNode(
                                    item.rightSummaryProperty().getValue(),
                                    item.rightStyleProperty().getValue() ));
                        }
                    }
                };
            }
        });


        actionColumn.setComparator(new Comparator<DiffRecordViewModel>() {
            @Override
            public int compare(DiffRecordViewModel o1, DiffRecordViewModel o2) {
                return (o1.actionSummaryProperty().get() == null)
                        ? -1
                        : o1.actionSummaryProperty().get().compareTo(o2.actionSummaryProperty().get());
            }
        });


        actionColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel>, ObservableValue<DiffRecordViewModel>>() {
            public ObservableValue<DiffRecordViewModel> call(TableColumn.CellDataFeatures<DiffRecordViewModel, DiffRecordViewModel> p) {
                return new AggregateObservable<>(p.getValue(),
                        p.getValue().actionSummaryProperty(),
                        p.getValue().actionStyleProperty());
            }
        });

        actionColumn.setCellFactory(new Callback<TableColumn<DiffRecordViewModel, DiffRecordViewModel>, TableCell<DiffRecordViewModel, DiffRecordViewModel>>() {
            @Override
            public TableCell<DiffRecordViewModel, DiffRecordViewModel> call(TableColumn<DiffRecordViewModel, DiffRecordViewModel> param) {
                return new TableCell<DiffRecordViewModel, DiffRecordViewModel>() {
                    @Override
                    public void updateItem(DiffRecordViewModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            setGraphic( buildTextNode(
                                    item.actionSummaryProperty().getValue(),
                                    item.actionStyleProperty().getValue() ));
                        }
                    }
                };
            }
        });

    }

    private static Text buildTextNode(String text, DiffRecordViewModel.Style style) {
        Text textNode = new Text();
        textNode.setFontSmoothingType( FontSmoothingType.LCD );
        textNode.getStyleClass().add(style.toString());
        textNode.setText( text );
        return textNode;
    }
}