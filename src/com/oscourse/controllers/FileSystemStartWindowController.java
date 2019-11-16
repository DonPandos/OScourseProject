package com.oscourse.controllers;

import com.oscourse.parameters.FileSystemParameters;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ResourceBundle;

public class FileSystemStartWindowController implements Initializable {

    @FXML
    private ListView<String> fsList;

    @FXML
    private Button enterButton;

    @FXML
    private Button createNewFs;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File folder = new File("/Users/bogdan/Desktop/OScourse");
        File[] listOfFiles = folder.listFiles();
        ObservableList<String> observableArray = FXCollections.observableArrayList();
        for(int i = 0; i < listOfFiles.length; i++){
            if(listOfFiles[i].getName().equals(".DS_Store")) continue;
            observableArray.add(listOfFiles[i].getName());
        }
        fsList.setItems(observableArray);
        createNewFs.setOnMouseClicked(event ->  {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/create_fs_window.fxml"));
                Parent root1;
                root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                Stage currentStage = (Stage) createNewFs.getScene().getWindow();
                currentStage.close();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        enterButton.setOnMouseClicked(event -> {
            FileSystemParameters.currentFsName = fsList.getSelectionModel().getSelectedItem();
            try {
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + FileSystemParameters.currentFsName, "rw");
                raf.seek(FileSystemParameters.clusterSizeOffset);
                FileSystemParameters.clusterSize = raf.readShort();
                raf.seek(FileSystemParameters.clustersCountOffset);
                int countOfClusters = raf.readInt();
                if(countOfClusters < Math.pow(2, 16) - 1) FileSystemParameters.fatGap = 2; // кол-во байт в таблице FAT
                else FileSystemParameters.fatGap = 4;
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/login_window.fxml"));
                Parent root1;
                root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                Stage currentStage = (Stage) createNewFs.getScene().getWindow();
                currentStage.close();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
