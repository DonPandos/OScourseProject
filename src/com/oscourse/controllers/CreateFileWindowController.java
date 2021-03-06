package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ResourceBundle;

public class CreateFileWindowController implements Initializable {

    @FXML
    TextField fileName;
    @FXML
    ChoiceBox<String> typeChoiceBox;
    @FXML
    Button addBtn;
    @FXML
    Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeChoiceBox.setItems(FXCollections.observableArrayList("File", "Folder"));
        typeChoiceBox.setValue("File");

        fileName.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                char c = event.getCharacter().charAt(0);
                if(c >= 'А' && c <= 'я'){
                    event.consume();
                }
                }
        });

        addBtn.setOnMouseClicked(event -> {
            String filename = fileName.getText();
            System.out.println("value " + typeChoiceBox.getValue().equals("Folder"));
            System.out.println("value " + filename.contains("."));
            if (filename.equals("")) {
                errorLabel.setText("File name cannot be empty");
            } else if (typeChoiceBox.getValue().equals("Folder") && filename.contains(".")) {
                errorLabel.setText("Folder cannot have extensions");
            } else if (filename.contains(".") && filename.indexOf(".") != filename.lastIndexOf(".")) {
                errorLabel.setText("Name cannot contain .");
            } else if (filename.contains(".") && filename.substring(0, filename.indexOf(".")).length() > 12) {
                errorLabel.setText("Maximum name length 12");
            } else if (filename.contains(".") && filename.substring(filename.indexOf(".") + 1, filename.length()).length() > 3) {
                errorLabel.setText("Maximum extension length 3");
            } else if (Formatting.createFileInFolder(typeChoiceBox.getValue().equals("Folder"), Formatting.CURRENT_DIR, fileName.getText()) == 1) {
                ((Stage) addBtn.getScene().getWindow()).close();
                MainWindowController.updateInfoAboutFiles();
            } else {
                errorLabel.setText("Name not available");
            }
        });

        MainWindowController.updateInfoAboutFiles();
    }
}
