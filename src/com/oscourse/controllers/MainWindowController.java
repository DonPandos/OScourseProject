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
import java.text.Format;
import java.text.Normalizer;
import java.util.Date;
import java.util.ResourceBundle;

import static com.oscourse.filesystem.Formatting.IS_ADMIN;

public class MainWindowController implements Initializable{

    @FXML TableView<File> tv;
    @FXML TableColumn<File, String> filenameColumn;
    @FXML TableColumn<File, String> extensionColumn;
    @FXML TableColumn<File, String> dateColumn;
    @FXML TableColumn<File, String> typeColumn;
    @FXML TableColumn<File, String> sizeColumn;
    @FXML Label powerBtn;
    @FXML Button createFileBtn;
    @FXML Button propertiesButton;
    @FXML Button renameButton;
    @FXML Button deleteButton;
    @FXML Button duplicateButton;
    @FXML Button moveButton;
    @FXML Button cancelButton;
    @FXML Button moveHereButton;
    @FXML Button duplicateHereButton;
    @FXML TextField pathField;
    @FXML Label backBtn;
    @FXML MenuItem addUserButton;
    @FXML MenuItem usersListButton;

    String pathOfFileToMove;
    String pathOfFileToDuplicate;

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
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/fs_start_window.fxml"));
                Parent root1;
                root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.show();
                ((Stage) powerBtn.getScene().getWindow()).close();
            } catch(Exception e){
                e.printStackTrace();
            }
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
            String pageName;
            if(IS_ADMIN || Formatting.getFileRights(Formatting.CURRENT_DIR)[1]) pageName = "create_file_window.fxml";
            else pageName = "havent_right_window.fxml";
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/" + pageName));
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

        propertiesButton.setOnMouseClicked(event -> {
            String filePath = Formatting.CURRENT_DIR;
            if(!Formatting.CURRENT_DIR.equals("/")) filePath += "/";
            filePath += tv.getSelectionModel().getSelectedItem().getFullFileName();
            System.out.println(filePath);
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/file_info_window.fxml"));
                Parent root1;
                root1 = (Parent) fxmlLoader.load();
                ((FileInfoWindowController)fxmlLoader.getController()).setStartByteOfFile(Formatting.getStartByteOfFile(filePath));
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.show();
            } catch(Exception e){
                e.printStackTrace();
            }
        });

        renameButton.setOnMouseClicked(event -> {
            String filePath = Formatting.CURRENT_DIR;
            if(!Formatting.CURRENT_DIR.equals("/")) filePath += "/";
            String pathToController = filePath;
            String fileName = tv.getSelectionModel().getSelectedItem().getFullFileName();;
            filePath += fileName;
            System.out.println(filePath);
            if(IS_ADMIN || Formatting.getFileRights(filePath)[1]){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/rename_file_window.fxml"));
                    Parent root1 = (Parent) fxmlLoader.load();
                    ((RenameFileWindowController) fxmlLoader.getController()).setFileName(fileName);
                    ((RenameFileWindowController) fxmlLoader.getController()).setStartByte(Formatting.getStartByteOfFile(filePath));
                    ((RenameFileWindowController) fxmlLoader.getController()).setFullPath(pathToController);
                    ((RenameFileWindowController) fxmlLoader.getController()).setFileType(tv.getSelectionModel().getSelectedItem().getType());
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            else {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                    Parent root1 = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        });

        deleteButton.setOnMouseClicked(event -> {
            File file = tv.getSelectionModel().getSelectedItem();
            System.out.println(file.getFullFileName() + " " + file.isSystem());
            if(Formatting.CURRENT_UID != 1 && file.isSystem()){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/file_is_system.fxml"));
                    Parent root1 = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            else {
                String filePath = Formatting.CURRENT_DIR.equals("/") ? Formatting.CURRENT_DIR + file.getFullFileName() : Formatting.CURRENT_DIR + "/" + file.getFullFileName();
                System.out.println("file Path" + filePath);
                if (file != null) {
                    if (IS_ADMIN || Formatting.getFileRights(filePath)[1]) {
                        if (file.getType().equals("File")) {
                            Formatting.deleteFile(filePath);
                        } else {
                            if (Formatting.haveRightsToDeleteFolder(filePath)) Formatting.deleteFolder(filePath);
                            else {
                                try {
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                                    Parent root1 = (Parent) fxmlLoader.load();
                                    Stage stage = new Stage();
                                    stage.setScene(new Scene(root1));
                                    stage.show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                            Parent root1 = (Parent) fxmlLoader.load();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(root1));
                            stage.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                refreshInfo();
            }
        });

        addUserButton.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/create_user_window.fxml"));
                Parent root1;
                root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.show();
            } catch(Exception e){
                e.printStackTrace();
            }
        });

        usersListButton.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/users_list_window.fxml"));
                Parent root1;
                root1 = (Parent) fxmlLoader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.show();
            } catch(Exception e){
                e.printStackTrace();
            }
        });

        duplicateButton.setOnMouseClicked(event -> {
            File file = tv.getSelectionModel().getSelectedItem();
            pathOfFileToDuplicate = Formatting.CURRENT_DIR.equals("/") ? Formatting.CURRENT_DIR + file.getFullFileName() : Formatting.CURRENT_DIR + "/" + file.getFullFileName();
            if(!Formatting.getFileRights(pathOfFileToDuplicate)[1] && !IS_ADMIN){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                    Parent root1 = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if(file != null){
                createFileBtn.setVisible(false);
                propertiesButton.setVisible(false);
                renameButton.setVisible(false);
                deleteButton.setVisible(false);
                moveButton.setVisible(false);
                duplicateButton.setVisible(false);

                duplicateHereButton.setVisible(true);
                cancelButton.setVisible(true);

            }
        });

        moveButton.setOnMouseClicked(event -> {
            File file = tv.getSelectionModel().getSelectedItem();
            pathOfFileToMove = Formatting.CURRENT_DIR.equals("/") ? Formatting.CURRENT_DIR + file.getFullFileName() : Formatting.CURRENT_DIR + "/" + file.getFullFileName();
            if(!Formatting.getFileRights(pathOfFileToMove)[1] && !IS_ADMIN){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                    Parent root1 = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if(file != null){
                createFileBtn.setVisible(false);
                propertiesButton.setVisible(false);
                renameButton.setVisible(false);
                deleteButton.setVisible(false);
                moveButton.setVisible(false);
                duplicateButton.setVisible(false);

                moveHereButton.setVisible(true);
                cancelButton.setVisible(true);

            }
        });

        cancelButton.setOnMouseClicked(event -> {
            createFileBtn.setVisible(true);
            propertiesButton.setVisible(true);
            renameButton.setVisible(true);
            deleteButton.setVisible(true);
            moveButton.setVisible(true);
            duplicateButton.setVisible(true);

            moveHereButton.setVisible(false);
            duplicateHereButton.setVisible(false);
            cancelButton.setVisible(false);
        });

        moveHereButton.setOnMouseClicked(event -> {
            if(!Formatting.getFileRights(Formatting.CURRENT_DIR)[1] && !IS_ADMIN){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                    Parent root1 = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                String fileName = pathOfFileToMove.substring(pathOfFileToMove.lastIndexOf("/") + 1, pathOfFileToMove.length());
                String newFilePath = Formatting.CURRENT_DIR.equals("/") ? Formatting.CURRENT_DIR + fileName : Formatting.CURRENT_DIR + "/" + fileName;
                System.out.println(newFilePath);
                if(Formatting.getFileStartCluster(newFilePath) == -1){
                    Formatting.moveFile(pathOfFileToMove, Formatting.CURRENT_DIR);
                    createFileBtn.setVisible(true);
                    propertiesButton.setVisible(true);
                    renameButton.setVisible(true);
                    deleteButton.setVisible(true);
                    moveButton.setVisible(true);
                    duplicateButton.setVisible(true);

                    moveHereButton.setVisible(false);
                    cancelButton.setVisible(false);
                } else {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/filename_unavailable.fxml"));
                        Parent root1 = (Parent) fxmlLoader.load();
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root1));
                        stage.show();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            refreshInfo();
        });

        duplicateHereButton.setOnMouseClicked(event -> {
            if(!Formatting.getFileRights(Formatting.CURRENT_DIR)[1] && !IS_ADMIN){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                    Parent root1 = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                String fileName = pathOfFileToDuplicate.substring(pathOfFileToDuplicate.lastIndexOf("/") + 1, pathOfFileToDuplicate.length());
                String newFilePath = Formatting.CURRENT_DIR.equals("/") ? Formatting.CURRENT_DIR + fileName : Formatting.CURRENT_DIR + "/" + fileName;
                System.out.println(newFilePath);
                if(Formatting.getFileStartCluster(newFilePath) == -1){
                    Formatting.duplicateFile(pathOfFileToDuplicate, Formatting.CURRENT_DIR);
                    createFileBtn.setVisible(true);
                    propertiesButton.setVisible(true);
                    renameButton.setVisible(true);
                    deleteButton.setVisible(true);
                    moveButton.setVisible(true);
                    duplicateButton.setVisible(true);

                    duplicateHereButton.setVisible(false);
                    cancelButton.setVisible(false);
                } else {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/filename_unavailable.fxml"));
                        Parent root1 = (Parent) fxmlLoader.load();
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root1));
                        stage.show();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            refreshInfo();
        });

        tv.setRowFactory( tv -> {
            TableRow<File> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    File file = row.getItem();
                    String newDir ;
                    if(Formatting.CURRENT_DIR.equals("/")) newDir =Formatting.CURRENT_DIR + file.getFullFileName();
                    else newDir = Formatting.CURRENT_DIR + "/" + file.getFullFileName();
                    if(file.getType().equals("Folder")){
                        if(!IS_ADMIN && !Formatting.getFileRights(newDir)[0]){
                                try{
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                                    Parent root1;
                                    root1 = (Parent) fxmlLoader.load();
                                    Stage stage = new Stage();
                                    stage.setScene(new Scene(root1));
                                    stage.show();
                                } catch(Exception e){
                                    e.printStackTrace();
                            }
                        }
                        else {
                            Formatting.CURRENT_DIR = newDir;
                            refreshInfo();
                        }
                    }
                    else {
                        if(!IS_ADMIN && !Formatting.getFileRights(newDir)[0]){
                            try{
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/havent_right_window.fxml"));
                                Parent root1;
                                root1 = (Parent) fxmlLoader.load();
                                Stage stage = new Stage();
                                stage.setScene(new Scene(root1));
                                stage.show();
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        else {
                            try{
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/oscourse/javafxscenes/file_edit_window.fxml"));
                                Parent root1;
                                root1 = (Parent) fxmlLoader.load();
                                ((FileEditWindowController) fxmlLoader.getController()).setFilePath(newDir);
                                Stage stage = new Stage();
                                stage.setScene(new Scene(root1));
                                stage.show();
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
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
        files.addAll(Formatting.getFilesFromFolder(Formatting.CURRENT_DIR, true));
    }


}
