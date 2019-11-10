package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateFsWindowController implements Initializable{

    @FXML
    private TextField fsNameField;

    @FXML
    private TextField hddSizeField;

    @FXML
    private ChoiceBox<Integer> clusterSizeChoiceBox;

    @FXML
    private TextField userNameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button createButton;

    @FXML
    private Label errorField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clusterSizeChoiceBox.getItems().addAll(512, 1024, 2048, 4096);
        clusterSizeChoiceBox.setValue(512);
        createButton.setOnMouseClicked(event -> {
            errorField.setText("");
            String error = "";
            if( fsNameField.getText().length() == 0 || fsNameField.getText().length() > 10 || fsNameField.getText().charAt(0) == ' '){
                error = "Неправильное название файловой системы.\n";
            }
            File folder = new File("/Users/bogdan/Desktop/OScourse");
            File[] listOfFiles = folder.listFiles();
            for(int i = 1; i < listOfFiles.length; i++){
                if(listOfFiles[i].getName().equals(fsNameField.getText())){
                    error += "Данное название файловой системы занято.\n";
                }
            }
            try{
                if(Integer.parseInt(hddSizeField.getText()) < 10 || Integer.parseInt(hddSizeField.getText()) > 150){
                    error = "HDD должен быть в пределах 10-150 Мб.\n";
                }

            } catch (Exception e){
                error += "HDD должен быть задан в числовом формате.\n";
            }
            if(userNameField.getText().length() > 12 || userNameField.getText().length() < 4){
                error += "Имя пользователя 4-12 cимволов.\n";
            }
            if(passwordField.getText().length() > 14 || passwordField.getText().length() < 6 ){
                error += "Пароль 6-14 символов.";
            }

            if(error != "") errorField.setText(error);
            else {
                try {
                    Formatting.formattingFS(clusterSizeChoiceBox.getValue().shortValue(), Integer.parseInt(hddSizeField.getText()) * 1024 * 1024, fsNameField.getText(), userNameField.getText(), passwordField.getText());
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/fs_start_window.fxml"));
                    Parent root1;
                    root1 = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    Stage currentStage = (Stage) fsNameField.getScene().getWindow();
                    currentStage.close();
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
