package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ResourceBundle;

public class LoginWindowController implements Initializable {

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button enterButton;

    @FXML
    private Label errorLabel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enterButton.setOnMouseClicked(event -> {
            if(loginField.getText().length() < 4
                    || passwordField.getText().length() < 6) errorLabel.setText("Проверьте вводимые значения.");
            else{
                Pair<Byte, String> user = Formatting.userSearch(loginField.getText());
                if(user != null){
                    if(passwordField.getText().equals(user.getValue())){
                        Formatting.CURRENT_UID = user.getKey();
                        Formatting.CURRENT_DIR = "/";
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/main_window_fs.fxml"));
                            Parent root1;
                            root1 = (Parent) fxmlLoader.load();
                            Stage stage = new Stage();
                            stage.initStyle(StageStyle.UNDECORATED);
                            stage.setScene(new Scene(root1));
                            Stage currentStage = (Stage) enterButton.getScene().getWindow();
                            currentStage.close();
                            stage.show();
                        } catch(IOException E){
                            E.printStackTrace();
                        }
                    }
                    else {
                        errorLabel.setText("Неверно введенные данные.");
                    }
                }
            }
        });
    }
}
