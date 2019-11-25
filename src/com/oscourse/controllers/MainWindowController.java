package com.oscourse.controllers;

import com.oscourse.filesystem.File;
import com.oscourse.filesystem.Formatting;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.text.Normalizer;
import java.util.Date;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable{

    @FXML TableView<File> tv;
    @FXML TableColumn<File, String> filenameColumn;
    @FXML TableColumn<File, String> extensionColumn;
    @FXML TableColumn<File, String> dateColumn;
    @FXML TableColumn<File, String> typeColumn;
    @FXML TableColumn<File, String> sizeColumn;
    @FXML Label powerBtn;
    @FXML Button createFileBtn;
    @FXML TextField pathField;
    @FXML Label backBtn;

    static ObservableList<File> files;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Formatting.CURRENT_DIR = "/";

        files = FXCollections.observableArrayList();
        refreshInfo();

        filenameColumn.setCellValueFactory(new PropertyValueFactory<File, String>("name"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<File, String>("size"));
        extensionColumn.setCellValueFactory(new PropertyValueFactory<File, String>("extension"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<File, String>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<File, String>("type"));

        tv.setItems(files);

        powerBtn.setOnMouseClicked(event -> {
            ((Stage)powerBtn.getScene().getWindow()).close();
        });

        backBtn.setOnMouseClicked(event -> {
            if(!Formatting.CURRENT_DIR.equals("/")){
                if(Formatting.CURRENT_DIR.indexOf("/") == Formatting.CURRENT_DIR.lastIndexOf("/")){
                    Formatting.CURRENT_DIR = "/";
                } else Formatting.CURRENT_DIR = Formatting.CURRENT_DIR.substring(0, Formatting.CURRENT_DIR.lastIndexOf("/"));
            }
            refreshInfo();
        });

        createFileBtn.setOnMouseClicked(event -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/create_file_window.fxml"));
                Parent root1;
                root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.show();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        });

        tv.setRowFactory( tv -> {
            TableRow<File> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    File file = row.getItem();
                    if(file.getType().equals("Папка")){
                        if(Formatting.CURRENT_DIR.equals("/"))Formatting.CURRENT_DIR += file.getName();
                        else Formatting.CURRENT_DIR += "/" + file.getName();
                    }
                    refreshInfo();
                }
            });
            return row ;
        });
    }

    public void refreshInfo(){
        pathField.setText(Formatting.CURRENT_DIR);
        updateInfoAboutFiles();
    }

    public static void updateInfoAboutFiles(){
        files.clear();
        files.addAll(Formatting.getFilesFromFolder(Formatting.CURRENT_DIR));
    }


}
