package com.oscourse.controllers;

import com.oscourse.filesystem.File;
import com.oscourse.filesystem.Formatting;
import com.oscourse.filesystem.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class UsersListWindowController implements Initializable {

    @FXML
    private TableView<User> usersTv;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> typeColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<User> users = FXCollections.observableArrayList(Formatting.getAllUsers());

        usernameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<User, String>("type"));

        usersTv.setItems(users);
    }
}
