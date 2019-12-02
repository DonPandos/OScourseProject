package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FileEditWindowController implements Initializable{

    @FXML
    private TextArea textField;

    @FXML
    private Button saveButton;

    @FXML
    private Button closeButton;

    private String filePath;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        closeButton.sceneProperty().addListener(((observable, oldValue, newValue) -> {
            if(!Formatting.IS_ADMIN && !Formatting.getFileRights(filePath)[1]){
                saveButton.setVisible(false);
                textField.setEditable(false);
            }

            textField.setText(Formatting.getDataFromFile(filePath));

        }));

        saveButton.setOnMouseClicked(event -> {
            Formatting.saveFileData(textField.getText(), filePath);
            ((Stage)closeButton.getScene().getWindow()).close();
            MainWindowController.updateInfoAboutFiles();
        });

        closeButton.setOnMouseClicked(event -> {
            ((Stage)closeButton.getScene().getWindow()).close();
        });
    }
}
