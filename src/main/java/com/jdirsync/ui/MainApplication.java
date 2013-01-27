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

public class MainApplication extends Application {
    private static MainFormViewModel mainFormViewModel;

    public static void startUIMode(String[] args) {
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
}
