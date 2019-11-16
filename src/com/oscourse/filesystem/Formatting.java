package com.oscourse.filesystem;
import com.oscourse.parameters.FileSystemParameters;
import javafx.util.Pair;
import java.io.*;
import java.nio.ByteBuffer;

import static com.oscourse.parameters.FileSystemParameters.*;

public class Formatting {


    public static void formattingFS(short clusterSize, int hddSize, String fsName, String username, String password) throws IOException{ // clusterSize - размер кластера ФС hddSize - размер HDD fsName - имя ФС
        //  суперблок
        Integer countOfClusters = hddSize / clusterSize; // кол-во кластеров в ФС
        Integer countOfBytesInRootDirectory = 40960; // кол-во байт в корневом каталоге
        int countOfBytesInFAT;
        if(countOfClusters < Math.pow(2, 16) - 1) countOfBytesInFAT = countOfClusters * 2; // кол-во байт в таблице FAT
        else countOfBytesInFAT = countOfClusters * 4;
        short FAT1offset = clusterSize; // смещение FAT1
        int FAT2offset = FAT1offset + countOfBytesInFAT; // смещение FAT2
        int rootDirectoryOffset = FAT2offset + countOfBytesInFAT; // смещение корневого каталога
        int usersInfoOffset = rootDirectoryOffset + countOfBytesInRootDirectory; // cмещение таблицы пользователей
        int clustersCountForUserInfo = (int) Math.ceil(((27.0 * 256.0) / clusterSize));
        int dataOffset = usersInfoOffset + (clustersCountForUserInfo * clusterSize); //смещение области с данными

        FileOutputStream fos = new FileOutputStream("/Users/bogdan/Desktop/OScourse/" + fsName);
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + fsName, "rw");

        raf.write(new byte[hddSize]); // заполняем файл байтами
        raf.seek(0); // смещение курсора на 0 позицию
        raf.writeBytes(fsName);
        raf.seek(clustersCountOffset);
        raf.write(ByteBuffer.allocate(4).putInt(countOfClusters).array()); // записываем кол-во кластеров
        raf.write(ByteBuffer.allocate(2).putShort(clusterSize).array()); // записываем размер кластера
        raf.write(ByteBuffer.allocate(2).putShort(FAT1offset).array()); // записываем смещение ФАТ1
        raf.write(ByteBuffer.allocate(4).putInt(FAT2offset).array()); // записываем смещение ФАТ2
        raf.write(ByteBuffer.allocate(4).putInt(rootDirectoryOffset).array()); // записываем смещение корневого каталога
        raf.write(ByteBuffer.allocate(4).putInt(usersInfoOffset).array()); // записываем смещение таблицы юзеров
        raf.write(ByteBuffer.allocate(4).putInt(dataOffset).array()); // записываем смещение data
        raf.seek(usersInfoOffset);// записываем UID
        raf.write(ByteBuffer.allocate(1).put(new Integer(1).byteValue()).array());
        raf.writeBytes(username);
        raf.seek(usersInfoOffset + 13);
        raf.writeBytes(password);
        raf.close();
        fos.close();
    }

    public static Pair<Byte, String> userSearch(String username)  {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + FileSystemParameters.currentFsName, "rw");
            raf.seek(FileSystemParameters.usersInfoOffsetOffset);
            int userInfoOffset = raf.readInt();
            raf.seek(userInfoOffset);
            while(true){
                byte uid = raf.readByte();
                if(uid == 0x00) {
                    return null;
                }
                byte[] login = new byte[12];
                raf.read(login);
                String foundUsername = new String(login);
                foundUsername = foundUsername.substring(0,foundUsername.indexOf(0x00));
                if(foundUsername.equals(username)){
                    byte[] password = new byte[14];
                    raf.read(password);
                    String foundPassword = new String(password);
                    foundPassword = foundPassword.substring(0, foundPassword.indexOf(0x00));
                    return new Pair<>(uid, foundPassword);
                }
                userInfoOffset += FileSystemParameters.usersInfoGap;
                raf.seek(userInfoOffset);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void open(String path){

    }

    public static int createFile(String path, String name, String extension, short uid, byte modes) throws IOException{
        if(name.length() > 20 || extension.length() > 3) return 0;
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + currentFsName, "rw");
        if (path.equals("/")){
            int freePlace = freePlaceInRootDirectory();
            raf.seek(freePlace);
            raf.writeBytes(name);
            raf.seek(freePlace + 20);
            
        }
        return 1;
    }


    // EDIT: ПОКА pos < dataOffset
    public static int freePlaceInRootDirectory() throws IOException{
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + currentFsName, "rw");
        raf.seek(rootDirectoryOffsetOffset);
        int pos = raf.readInt();
        raf.seek(pos);
        while (true) {
            if(raf.readByte() == 0x00){
                raf.close();
                return pos;
            } else pos+=48;
        }
    }

    public static int freeCluster() throws IOException{
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + currentFsName, "rw");
        raf.seek(fat1OffsetOffset);
        short pos = raf.readShort();
        raf.seek(pos);
        return 0;
    }

}
