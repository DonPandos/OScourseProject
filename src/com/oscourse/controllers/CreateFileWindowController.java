package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ResourceBundle;

public class CreateFileWindowController implements Initializable {

    @FXML TextField fileName;
    @FXML ChoiceBox<String> typeChoiceBox;
    @FXML Button addBtn;
    @FXML Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeChoiceBox.setItems(FXCollections.observableArrayList("Файл", "Папка"));
        typeChoiceBox.setValue("Файл");

        addBtn.setOnMouseClicked(event -> {
            if(Formatting.createFileInFolder(typeChoiceBox.getValue().equals("Папка"), Formatting.CURRENT_DIR, fileName.getText()) == 1){
                ((Stage) addBtn.getScene().getWindow()).close();
                MainWindowController.updateInfoAboutFiles();
            } else {
                errorLabel.setText("Имя занято");
            }
        });

        MainWindowController.updateInfoAboutFiles();
    }
}
