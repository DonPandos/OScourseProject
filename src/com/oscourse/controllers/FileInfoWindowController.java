package com.oscourse.controllers;

import com.oscourse.filesystem.Formatting;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.RandomAccessFile;
import java.net.URL;
import java.util.BitSet;
import java.util.ResourceBundle;

import static com.oscourse.filesystem.Formatting.CURRENT_FS_NAME;

public class FileInfoWindowController implements Initializable{

    @FXML
    private CheckBox ownerReadCheck;

    @FXML
    private CheckBox ownerWriteCheck;

    @FXML
    private CheckBox ownerExecuteCheck;

    @FXML
    private CheckBox othersReadCheck;

    @FXML
    private CheckBox othersWriteCheck;

    @FXML
    private CheckBox othersExecuteCheck;

    @FXML
    private CheckBox hideCheck;

    @FXML
    private CheckBox systemCheck;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label fileNameLabel;

    @FXML
    private Label fileExtensionLabel;

    @FXML
    private Label fileSizeLabel;

    @FXML
    private Label fileTypeLabel;

    @FXML
    private Label fileOwnerLabel;

    @FXML
    private Label fileCreateDateLabel;

    @FXML
    private Label fileModifyDateLabel;

    private int startByteOfFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        fileCreateDateLabel.sceneProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
                raf.seek(startByteOfFile);
                byte[] fileNameByteArr = new byte[20];
                byte[] extensionByteArr = new byte[3];
                byte[] modes = new byte[1];
                byte UID;
                int fileSize;
                byte[] createDateByteArr = new byte[8];
                byte[] modifyDateByteArr = new byte[8];
                byte[] flags = new byte[1];

                raf.read(fileNameByteArr);
                raf.read(extensionByteArr);
                raf.read(modes);
                UID = raf.readByte();
                fileSize = raf.readInt();
                raf.read(createDateByteArr);
                raf.read(modifyDateByteArr);
                raf.read(flags);

                fileNameLabel.setText(new String(fileNameByteArr));
                fileExtensionLabel.setText(new String(extensionByteArr));
                fileSizeLabel.setText(String.valueOf(fileSize) + " б");
                fileOwnerLabel.setText(Formatting.getUsernameByUid(UID));
                fileCreateDateLabel.setText(Formatting.dateWithDots(new String(createDateByteArr)));
                fileModifyDateLabel.setText(Formatting.dateWithDots(new String(modifyDateByteArr)));

                BitSet bs = BitSet.valueOf(modes);

                if(bs.get(7)) fileTypeLabel.setText("Folder");
                else fileTypeLabel.setText("File");

                if(bs.get(6)) ownerReadCheck.setSelected(true);
                if(bs.get(5)) ownerWriteCheck.setSelected(true);
                if(bs.get(4)) ownerExecuteCheck.setSelected(true);
                if(bs.get(3)) othersReadCheck.setSelected(true);
                if(bs.get(2)) othersWriteCheck.setSelected(true);
                if(bs.get(1)) othersExecuteCheck.setSelected(true);

                bs = BitSet.valueOf(flags);

                if(bs.get(7)) systemCheck.setSelected(true);
                if(bs.get(6)) hideCheck.setSelected(true);

                String fileName = new String(fileNameByteArr);
                if(fileName.contains("\u0000")) fileName = fileName.substring(0, fileName.indexOf("\u0000"));
                boolean fileRights;
                if(Formatting.CURRENT_DIR.equals("/")){
                    fileRights = Formatting.getFileRights(Formatting.CURRENT_DIR + fileName)[1];

                }
                else {
                    fileRights = Formatting.getFileRights(Formatting.CURRENT_DIR + "/" + fileName)[1];
                }
                saveButton.setVisible(fileRights);
                hideCheck.setDisable(!fileRights);
                systemCheck.setDisable(!fileRights);
                ownerReadCheck.setDisable(!fileRights);
                ownerWriteCheck.setDisable(!fileRights);
                ownerExecuteCheck.setDisable(!fileRights);
                othersReadCheck.setDisable(!fileRights);
                othersWriteCheck.setDisable(!fileRights);
                othersExecuteCheck.setDisable(!fileRights);

            } catch(Exception e){
                e.printStackTrace();
            }
        }));
        cancelButton.setOnMouseClicked(event -> {
            ((Stage)systemCheck.getScene().getWindow()).close();
        });

        saveButton.setOnMouseClicked(event -> {
            String modes = fileTypeLabel.getText().equals("Folder") ? "1" : "0";
            System.out.println("mode " + modes);
            modes += ownerReadCheck.isSelected() ? "1" : "0";
            modes += ownerWriteCheck.isSelected() ? "1" : "0";
            modes += ownerExecuteCheck.isSelected() ? "1" : "0";
            modes += othersReadCheck.isSelected() ? "1" : "0";
            modes += othersWriteCheck.isSelected() ? "1" : "0";
            modes += othersExecuteCheck.isSelected() ? "1" : "0";
            modes += "0";

            String flags = "";

            flags += systemCheck.isSelected() ? "1" : "0";
            flags += hideCheck.isSelected() ? "1" : "0";

            flags += "000000";
            byte modesByte = Formatting.getByteFromBits(modes);
            byte flagsByte;
            if (flags.equals("00000000")) flagsByte = 0x00;
            else flagsByte = Formatting.getByteFromBits(flags);


            try {
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
                raf.seek(startByteOfFile + Formatting.MODES_OFFSET);
                raf.writeByte(modesByte);
                raf.seek(startByteOfFile + Formatting.FLAGS_OFFSET);
                raf.writeByte(flagsByte);
                raf.close();
            } catch(Exception e){
                e.printStackTrace();
            }

            ((Stage)systemCheck.getScene().getWindow()).close();
            MainWindowController.updateInfoAboutFiles();

        });
    }

    public void setStartByteOfFile(int startByteOfFile) {
        this.startByteOfFile = startByteOfFile;
    }
}
