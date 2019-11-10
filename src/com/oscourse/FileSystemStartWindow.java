package com.oscourse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FileSystemStartWindow extends Application
{

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/oscourse/javafxscenes/fs_start_window.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        primaryStage.show();
    }
}


