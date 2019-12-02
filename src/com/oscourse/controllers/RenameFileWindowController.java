package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ResourceBundle;

public class RenameFileWindowController implements Initializable{

    @FXML
    private TextField fileNameField;

    @FXML
    private Button saveButton;

    @FXML
    private Label errorLabel;

    private String fileName;
    private int startByte;
    private String fullPath;
    private String fileType;

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setStartByte(int startByte) {
        this.startByte = startByte;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveButton.sceneProperty().addListener(((observable, oldValue, newValue) -> {
            System.out.println(fileType);
            fileNameField.setText(fileName);
        }));

        saveButton.setOnMouseClicked(event -> {
            String newFileName = fileNameField.getText();
            if(newFileName.equals("")) errorLabel.setText("File name cannot be empty");
            else if(newFileName.contains(".") && newFileName.indexOf(".") != newFileName.lastIndexOf(".")){
                errorLabel.setText("Name cannot contain .");
            } else if(fileType.equals("Folder") && newFileName.contains(".")){
                errorLabel.setText("Folders cannot have extensions");
            } else if(newFileName.contains(".") && newFileName.substring(0, newFileName.indexOf(".")).length() > 20){
                errorLabel.setText("Maximum name length 20");
            } else if(!newFileName.contains(".") && newFileName.length() > 20){
                errorLabel.setText("Maximum name length 20");
            } else if(newFileName.contains(".") && newFileName.substring(newFileName.indexOf(".") + 1, newFileName.length()).length() > 3){
                errorLabel.setText("Maximum extension length 3");
            } else {
                int checkFileByte = Formatting.getStartByteOfFile(fullPath + newFileName);
                if (checkFileByte == -1 || checkFileByte == startByte) {
                    try{
                        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + Formatting.CURRENT_FS_NAME, "rw");
                        String extension = "\u0000\u0000\u0000";
                        if(newFileName.contains(".")) {
                            extension = newFileName.substring(newFileName.indexOf(".") + 1, newFileName.length());
                            newFileName = newFileName.substring(0, newFileName.indexOf("."));
                        }
                        raf.seek(startByte);
                        raf.write(new byte[20]);
                        raf.seek(startByte);
                        raf.writeBytes(newFileName);
                        raf.seek(startByte + Formatting.EXTENSIONS_OFFSET);
                        raf.write(new byte[3]);
                        raf.seek(startByte + Formatting.EXTENSIONS_OFFSET);
                        raf.writeBytes(extension);
                        raf.close();
                        MainWindowController.updateInfoAboutFiles();
                        ((Stage)errorLabel.getScene().getWindow()).close();

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                } else {
                    errorLabel.setText("Name not available");
                }
            }
        });
    }
}
