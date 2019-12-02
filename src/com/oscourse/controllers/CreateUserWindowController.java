package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.RandomAccessFile;
import java.net.URL;
import java.text.Format;
import java.util.ResourceBundle;

import static com.oscourse.filesystem.Formatting.CURRENT_FS_NAME;

public class CreateUserWindowController implements Initializable{

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private CheckBox adminCheck;

    @FXML
    private Button createButton;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(!Formatting.IS_ADMIN) adminCheck.setVisible(false);
        createButton.setOnMouseClicked(event -> {
            if(usernameField.getText().equals("") || passwordField.getText().equals("")){
                errorLabel.setText("One or more fields are empty");
            } else  if(usernameField.getText().length() > 12 || usernameField.getText().length() < 4){
                errorLabel.setText("Username length 4-12");
            } else if(passwordField.getText().length() > 14 || passwordField.getText().length() < 6 ){
                errorLabel.setText("Password length 6-14");
            } else if(Formatting.userSearch(usernameField.getText()) != null){
                errorLabel.setText("Username is busy");
            } else {
                Formatting.createUser(usernameField.getText(), passwordField.getText(),adminCheck.isSelected());
                ((Stage) errorLabel.getScene().getWindow()).close();
            }

        });
    }
}
