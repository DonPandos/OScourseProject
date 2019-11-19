package com.oscourse.controllers;

import com.oscourse.filesystem.File;
import com.oscourse.filesystem.Formatting;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable{

    @FXML TableView<File> tv;
    @FXML TableColumn<File, String> filenameColumn;
    @FXML TableColumn<File, String> extensionColumn;
    @FXML TableColumn<File, String> dateColumn;
    @FXML TableColumn<File, String> typeColumn;
    @FXML TableColumn<File, String> sizeColumn;

    ObservableList<File> files;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

         files = FXCollections.observableArrayList();
         files.addAll(Formatting.readFilesInRootDirectory());
         filenameColumn.setCellValueFactory(new PropertyValueFactory<File, String>("name"));
         sizeColumn.setCellValueFactory(new PropertyValueFactory<File, String>("size"));
         extensionColumn.setCellValueFactory(new PropertyValueFactory<File, String>("extension"));
         dateColumn.setCellValueFactory(new PropertyValueFactory<File, String>("date"));
         typeColumn.setCellValueFactory(new PropertyValueFactory<File, String>("type"));

         tv.setItems(files);
    }
}
