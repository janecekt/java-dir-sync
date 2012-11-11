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
package com.jdirsync.ui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import com.jdirsync.builder.DiffBuilder;
import com.jdirsync.builder.IndexBuilderNio;
import com.jdirsync.synchronizer.SynchronizerImpl;
import com.jdirsync.ui.view.MainFormView;
import com.jdirsync.ui.viewmodel.LogObservingViewModel;
import com.jdirsync.ui.viewmodel.MainFormViewModel;
import com.jdirsync.ui.viewmodel.SyncViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Wrapper class for main function.
 */
public class Main extends Application {
    private static MainFormViewModel mainFormViewModel;

    @Override
    public void start(Stage stage) throws Exception {
        stage.titleProperty().bindBidirectional(mainFormViewModel.windowTitleProperty());

        // Set Scene
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/jdirsync/ui/view/MainForm.fxml"),
                null,
                new JavaFXBuilderFactory(),
                new Callback<Class<?>, Object>() {
                    @Override
                    public Object call(Class<?> aClass) {
                        return new MainFormView(mainFormViewModel);
                    }
                });
        loader.load();
        Parent parent = (Parent) loader.getRoot();

        Scene scene = new Scene(parent);
        scene.getStylesheets().add(getClass().getResource("/com/jdirsync/ui/view/MainForm.css").toExternalForm());
        stage.setScene(scene);

        // Show stage
        stage.show();
    }




    private static void printUsage() {
        System.err.println("Usage: java -jar ./jdirsync.jar -ui <leftName> <leftPath> <rightName> <rightPath>");
    }


    /**
     * Program main function.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length <= 1) {
                printUsage();
                System.exit(1);
            }

            if ("-ui".equals(args[0]) && args.length == 5) {
                startUIMode(args);
            } else {
                printUsage();
                System.exit(1);
            }
        } catch (Exception ex) {
            System.err.println("Exception occurred during initialization :" + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private static void startUIMode(String[] args) {
        // Input arguments
        String leftName = args[1] + "(L)";
        Path leftPath = Paths.get(args[2]);
        String rightName = args[3] + "(R)";
        Path rightPath = Paths.get(args[4]);

        // Main Presentation Model
        mainFormViewModel = new MainFormViewModel(
                new SyncViewModel(leftName, leftPath, rightName, rightPath,
                        Executors.newCachedThreadPool(),
                        new IndexBuilderNio(),
                        new DiffBuilder(),
                        new SynchronizerImpl()),
                new LogObservingViewModel()
        );

        // Start application
        launch(args);

    }
}
