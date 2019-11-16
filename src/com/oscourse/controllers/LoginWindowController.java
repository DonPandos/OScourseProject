package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Pair;

import java.net.URL;
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
                    }
                    else {
                        errorLabel.setText("Неверно введенные данные.");
                    }
                }
            }
        });
    }
}
