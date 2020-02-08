package com.oscourse.filesystem;

import javafx.util.Pair;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;

public class Formatting {


    public final static short NAME_OFFSET = 0;
    public final static short CLUSTERS_COUNT_OFFSET = 10;
    public final static short CLUSTERS_SIZE_OFFSET = 14;
    public final static short FAT1_OFFSET_OFFSET = 16;
    public final static short FAT2_OFFSET_OFFSET = 18;
    public final static short ROOT_DIRECTORY_OFFSET_OFFSET = 22;
    public final static short USERS_INFO_OFFSET_OFFSET = 26;
    public final static short DATA_OFFSET_OFFSET = 30;
    public final static short FREE_UID_OFFSET = 34;

    public final static short FILE_GAP = 50; // размер записи в корневом каталоге
    public final static short USERS_INFO_GAP = 28;

    public final static short USERNAME_OFFSET = 1;
    public final static short PASSWORD_OFFSET = 13;
    public final static short IS_ADMIN_OFFSET = 27;

    public static short FAT_GAP; // размер записи в fat
    public static short CLUSTER_SIZE;

    public static String CURRENT_FS_NAME;
    public static Byte CURRENT_UID;
    public static boolean IS_ADMIN;
    public static String CURRENT_DIR;

    public static short EXTENSIONS_OFFSET = 20;
    public static short MODES_OFFSET = 23;
    public static short UID_OFFSET = 24;
    public static short FILE_SIZE_OFFSET = 25;
    public static short CREATE_DATE_OFFSET = 29;
    public static short MODIFY_DATE_OFFSET = 37;
    public static short FLAGS_OFFSET = 45;
    public static short CLUSTER_NUMBER_OFFSET = 46;


    public static void formattingFS(short clusterSize, int hddSize, String fsName, String username, String password) throws IOException { // clusterSize - размер кластера ФС hddSize - размер HDD fsName - имя ФС
        //  суперблок
        Integer countOfClusters = hddSize / clusterSize; // кол-во кластеров в ФС
        Integer countOfBytesInRootDirectory = 40960; // кол-во байт в корневом каталоге
        int countOfBytesInFAT;
        if (countOfClusters < Math.pow(2, 16) - 1) countOfBytesInFAT = countOfClusters * 2; // кол-во байт в таблице FAT
        else countOfBytesInFAT = countOfClusters * 4;
        short FAT1offset = clusterSize; // смещение FAT1
        int FAT2offset = FAT1offset + countOfBytesInFAT; // смещение FAT2
        int rootDirectoryOffset = FAT2offset + countOfBytesInFAT; // смещение корневого каталога
        int usersInfoOffset = rootDirectoryOffset + countOfBytesInRootDirectory; // cмещение таблицы пользователей
        int clustersCountForUserInfo = (int) Math.ceil(((28.0 * 256.0) / clusterSize));
        int dataOffset = clusterSize * (int) Math.ceil(new Double((usersInfoOffset + (clustersCountForUserInfo * clusterSize)) / clusterSize)); //смещение области с данными

        FileOutputStream fos = new FileOutputStream("/Users/bogdan/Desktop/OScourse/" + fsName);
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + fsName, "rw");

        raf.write(new byte[hddSize]); // заполняем файл байтами
        raf.seek(0); // смещение курсора на 0 позицию
        raf.writeBytes(fsName);
        raf.seek(CLUSTERS_COUNT_OFFSET);
        raf.writeInt(countOfClusters); // записываем кол-во кластеров
        raf.writeShort(clusterSize); // записываем размер кластера
        raf.writeShort(FAT1offset); // записываем смещение ФАТ1
        raf.writeInt(FAT2offset); // записываем смещение ФАТ2
        raf.writeInt(rootDirectoryOffset); // записываем смещение корневого каталога
        raf.writeInt(usersInfoOffset); // записываем смещение таблицы юзеров
        raf.writeInt(dataOffset); // записываем смещение data
        raf.writeByte(1);
        raf.close();
        fos.close();
        CURRENT_FS_NAME = fsName;
        createUser(username, password, true);
    }

    public static int createUser(String username, String password, boolean isAdmin) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(FREE_UID_OFFSET);
            byte freeUID = raf.readByte();
            if (userSearch(username) != null) return -2;
            raf.seek(DATA_OFFSET_OFFSET);
            int endByte = raf.readInt();
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int startByte = raf.readInt();
            while (startByte + USERS_INFO_GAP < endByte) {
                raf.seek(startByte);
                if (raf.readByte() == 0x00) {
                    raf.seek(startByte);
                    raf.write(new byte[USERS_INFO_GAP]);
                    raf.seek(startByte);
                    raf.writeByte(freeUID);
                    raf.writeBytes(username);
                    raf.seek(startByte + PASSWORD_OFFSET);
                    raf.writeBytes(password);
                    raf.seek(startByte + IS_ADMIN_OFFSET);
                    raf.writeByte(isAdmin ? 1 : 0);
                    raf.seek(FREE_UID_OFFSET);
                    raf.writeByte(++freeUID);
                    raf.close();
                    return 1;
                }
                startByte += USERS_INFO_GAP;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Pair<Byte, String> userSearch(String username) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(DATA_OFFSET_OFFSET);
            int end = raf.readInt();
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int userInfoOffset = raf.readInt();
            raf.seek(userInfoOffset);
            while (userInfoOffset < end) {
                byte uid = raf.readByte();
                if (uid == 0x00) {
                    userInfoOffset += USERS_INFO_GAP;
                    continue;
                }
                byte[] login = new byte[12];
                raf.read(login);
                String foundUsername = new String(login);
                if (foundUsername.contains("\u0000"))
                    foundUsername = foundUsername.substring(0, foundUsername.indexOf("\u0000"));
                if (foundUsername.equals(username)) {
                    byte[] password = new byte[14];
                    raf.read(password);
                    String foundPassword = new String(password);
                    if (foundPassword.contains("\u0000"))
                        foundPassword = foundPassword.substring(0, foundPassword.indexOf("\u0000"));
                    return new Pair<>(uid, foundPassword);
                }
                userInfoOffset += USERS_INFO_GAP;
                raf.seek(userInfoOffset);
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getUsernameByUid(byte UID) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(DATA_OFFSET_OFFSET);
            int end = raf.readInt();
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int offset = raf.readInt();
            while (offset < end) {
                raf.seek(offset);
                if (raf.readByte() == UID) {
                    raf.seek(offset + 1);
                    byte[] username = new byte[12];
                    raf.read(username);
                    return new String(username);
                }
                offset += USERS_INFO_GAP;
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(DATA_OFFSET_OFFSET);
            int endByte = raf.readInt();
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int startByte = raf.readInt();
            while (startByte + USERS_INFO_GAP < endByte) {
                raf.seek(startByte);
                byte UID = raf.readByte();
                if (UID == 0x00) {
                    startByte += USERS_INFO_GAP;
                    continue;
                }
                byte[] usernameByteArr = new byte[12];
                byte[] passwordByteArr = new byte[14];
                byte isAdmin;

                raf.read(usernameByteArr);
                raf.read(passwordByteArr);
                isAdmin = raf.readByte();

                String username = new String(usernameByteArr);
                String password = new String(passwordByteArr);

                if (username.contains("\u0000")) username = username.substring(0, username.indexOf("\u0000"));
                if (password.contains("\u0000")) password = password.substring(0, password.indexOf("\u0000"));
                users.add(new User(UID, username, password, isAdmin == 1 ? UID == 1 ? "Root" : "Admin " : "User"));

                startByte += USERS_INFO_GAP;
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public static boolean isAdmin(byte UID) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(DATA_OFFSET_OFFSET);
            int endByte = raf.readInt();
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int startByte = raf.readInt();
            while (startByte + USERS_INFO_GAP < endByte) {
                raf.seek(startByte);
                if (raf.readByte() == UID) {
                    raf.seek(startByte + IS_ADMIN_OFFSET);
                    byte isAdmin = raf.readByte();
                    raf.close();
                    return isAdmin == 1;
                }
                startByte += USERS_INFO_GAP;
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean[] getFileRights(String path) {
        if (IS_ADMIN || path.equals("/")) return new boolean[]{true, true, true};
        boolean owner = false;
        int startByte = getStartByteOfFile(path);
        if (startByte == -1) return null;
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(startByte + UID_OFFSET);
            if (CURRENT_UID == raf.readByte()) owner = true;
            raf.seek(startByte + MODES_OFFSET);
            byte[] modes = new byte[1];
            raf.read(modes);
            BitSet bs = BitSet.valueOf(modes);
            if (owner) return new boolean[]{bs.get(6), bs.get(5), bs.get(4)};
            else return new boolean[]{bs.get(3), bs.get(2), bs.get(1)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int createFileInFolder(boolean type, String path, String name) {
        if (path.equals("/")) {
            if (getStartByteOfFile(path + name) != -1) return -1; // проверка на существование файла
            int a = freePlaceInRootDirectory();
            if (a != -1) {
                createFile(a, type, name);
            } else return -1;
        } else {
            if (getStartByteOfFile(path + "/" + name) != -1) return -1;
            try {
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
                int startCluster = getFileStartCluster(path);
                int offset = (startCluster * CLUSTER_SIZE) - CLUSTER_SIZE;
                int clusterEnd = startCluster * CLUSTER_SIZE;
                while (true) {
                    while (offset + 50 < clusterEnd) {
                        raf.seek(offset);
                        if (raf.readByte() == 0x00) {
                            createFile(offset, type, name);
                            increaseSizeFoldersInPath(path, 50);
                            raf.close();
                            return 1;
                        }
                        offset += 50;
                    }
                    raf.seek(FAT1_OFFSET_OFFSET);
                    int fat1 = raf.readShort();
                    fat1 += (startCluster * FAT_GAP) - FAT_GAP;
                    raf.seek(fat1);
                    int nextCluster = 0;
                    switch (FAT_GAP) {
                        case 2:
                            nextCluster = raf.readShort();
                            break;
                        case 4:
                            nextCluster = raf.readInt();
                            break;
                    }
                    if (nextCluster > 0) {
                        offset = (nextCluster * CLUSTER_SIZE) - CLUSTER_SIZE;
                        clusterEnd = nextCluster * CLUSTER_SIZE;
                    } else {
                        int freeCluster = freeCluster();
                        // очищение данных кластера, если это папка
                        if(type == true){
                            raf.seek((freeCluster * CLUSTER_SIZE) - CLUSTER_SIZE);
                            raf.write(new byte[CLUSTER_SIZE]);
                        }
                        //
                        raf.seek(fat1);
                        switch (FAT_GAP) {
                            case 2:
                                raf.writeShort(freeCluster);
                                break;
                            case 4:
                                raf.writeInt(freeCluster);
                                break;
                        }
                        offset = (freeCluster * CLUSTER_SIZE) - CLUSTER_SIZE;
                        clusterEnd = freeCluster * CLUSTER_SIZE;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    public static int createFile(int offset, boolean type, String name) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(offset);
            raf.write(new byte[FILE_GAP]);
            raf.seek(offset);
            String fileName = name;
            String fileExtension = "";
            if (name.lastIndexOf(".") != -1) {
                fileName = name.substring(0, name.lastIndexOf("."));
                fileExtension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            raf.writeBytes(fileName);
            raf.seek(offset + Formatting.EXTENSIONS_OFFSET);
            raf.writeBytes(fileExtension);
            raf.seek(offset + Formatting.MODES_OFFSET); // int(104) - frw-r--    int(232) -  drw-r--
            if (type) raf.write(getByteFromBits("11101000"));
            else raf.write(getByteFromBits("01101000"));
            raf.write(Formatting.CURRENT_UID);
            raf.writeInt(0);
            String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
            raf.writeBytes(date);
            raf.writeBytes(date);
            raf.writeByte(0);
            int cluster = freeCluster();
            raf.writeInt(cluster);

            // очищение данных кластера, если это папка
            if(type == true){
                raf.seek((cluster * CLUSTER_SIZE) - CLUSTER_SIZE);
                raf.write(new byte[CLUSTER_SIZE]);
            }
            //

            raf.seek(FAT1_OFFSET_OFFSET);
            short fat1 = raf.readShort();
            fat1 += (cluster * FAT_GAP) - FAT_GAP;
            raf.seek(fat1);
            switch (FAT_GAP) {
                case 2:
                    raf.writeShort(-1);
                    break;
                case 4:
                    raf.writeInt(-1);
                    break;
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    public static int deleteFile(String path) {
        int startByte = getStartByteOfFile(path);
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(startByte + FILE_SIZE_OFFSET);
            int size = raf.readInt();
            raf.seek(startByte + CLUSTER_NUMBER_OFFSET);
            int clusterNumber = raf.readInt();
            raf.seek(FAT1_OFFSET_OFFSET);
            short FAT1offset = raf.readShort();
            while (true) {
                raf.seek(FAT1offset + (clusterNumber * FAT_GAP) - FAT_GAP);
                int prevClusterNumber = clusterNumber;
                switch (FAT_GAP) {
                    case 2:
                        clusterNumber = raf.readShort();
                        break;
                    case 4:
                        clusterNumber = raf.readInt();
                        break;
                }
                raf.seek(FAT1offset + (prevClusterNumber * FAT_GAP) - FAT_GAP);
                switch (FAT_GAP) {
                    case 2:
                        raf.writeShort(0x00);
                        break;
                    case 4:
                        raf.writeInt(0x00);
                        break;
                }
                if (clusterNumber == -1) {
                    break;
                }
            }
            raf.seek(startByte);
            raf.writeByte(0x00);
            increaseSizeFoldersInPath(path.substring(0, path.lastIndexOf("/")), -1 * (size + FILE_GAP));
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int deleteFolder(String path) {
        int startByteOfFile = getStartByteOfFile(path);
        int clusterNumber = getFileStartCluster(path);

        String thisPath = path.substring(0, path.lastIndexOf("/"));

        int startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
        int endByte = clusterNumber * CLUSTER_SIZE;
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            while (true) {
                while (startByte + FILE_GAP < endByte) {
                    raf.seek(startByte);
                    if (raf.readByte() == 0x00) {
                        startByte += FILE_GAP;
                        continue;
                    }
                    raf.seek(startByte + MODES_OFFSET);
                    byte[] modes = new byte[1];
                    raf.read(modes);
                    BitSet bs = BitSet.valueOf(modes);
                    if (bs.get(7)) {
                        raf.seek(startByte);
                        byte[] nameByteArr = new byte[20];
                        raf.read(nameByteArr);
                        String name = new String(nameByteArr);
                        if (name.contains("\u0000")) name = name.substring(0, name.indexOf("\u0000"));
                        deleteFolder(thisPath + "/" + name);
                    } else {
                        raf.seek(startByte);
                        byte[] nameByteArr = new byte[20];
                        byte[] extensionByteArr = new byte[3];

                        raf.read(nameByteArr);
                        raf.read(extensionByteArr);

                        String name = new String(nameByteArr);
                        String extension = new String(extensionByteArr);

                        if (name.contains("\u0000")) name = name.substring(0, name.indexOf("\u0000"));
                        if (extension.contains("\u0000"))
                            extension = extension.substring(0, extension.indexOf("\u0000"));

                        deleteFile(extension.equals("") ? path + "/" + name : path + "/" + name + "." + extension);
                    }
                    startByte += FILE_GAP;
                }
                raf.seek(FAT1_OFFSET_OFFSET);
                int fat1 = raf.readShort();
                fat1 += (clusterNumber * FAT_GAP) - FAT_GAP;
                raf.seek(fat1);
                clusterNumber = FAT_GAP == 2 ? raf.readShort() : raf.readInt();
                raf.seek(fat1);
                switch (FAT_GAP) {
                    case 2:
                        raf.writeShort(0x00);
                        break;
                    case 4:
                        raf.writeInt(0x00);
                        break;
                }
                if (clusterNumber > 0) {
                    startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                    endByte = clusterNumber * CLUSTER_SIZE;
                } else {
                    raf.seek(startByteOfFile);
                    raf.writeByte(0x00);
                    increaseSizeFoldersInPath(path.substring(0, path.lastIndexOf("/")), -1 * FILE_GAP);
                    raf.close();
                    return 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static boolean haveRightsToDeleteFolder(String path){
        if(IS_ADMIN) return true;
        ArrayList<File> files = getFilesFromFolder(path, false);
        for(File file : files){
            switch(file.getType()){
                case "File":
                    if(getFileRights(path.equals("/") ? path + file.getFullFileName() : path + "/" + file.getFullFileName())[1]) continue;
                    return false;
                case "Folder":
                    String filePath = path.equals("/") ? path + file.getFullFileName() : path + "/" + file.getFullFileName();
                    if(getFileRights(filePath)[1] && haveRightsToDeleteFolder(filePath)) continue;
                    return false;
            }
        }
        return true;
    }

    public static int freePlaceInRootDirectory() {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int end = raf.readInt();
            raf.seek(ROOT_DIRECTORY_OFFSET_OFFSET);
            int pos = raf.readInt();
            while (pos + FILE_GAP < end) {
                raf.seek(pos);
                if (raf.readByte() == 0x00) {
                    raf.close();
                    return pos;
                } else pos += 50;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static ArrayList<File> readFilesInRootDirectory(boolean withHidden) {
        ArrayList<File> files = new ArrayList<>();
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int end = raf.readInt();
            raf.seek(ROOT_DIRECTORY_OFFSET_OFFSET);
            int pos = raf.readInt();
            while (pos + FILE_GAP < end) {
                raf.seek(pos);
                byte[] nameByteArr = new byte[20];
                byte[] sizeByteArr = new byte[4];
                byte[] extensionByteArr = new byte[3];
                byte[] dateByteArr = new byte[8];
                byte[] typeByteArr = new byte[1];
                raf.read(nameByteArr);
                if (nameByteArr[0] == 0x00) {
                    pos += 50;
                    continue;
                }
                byte[] flags = new byte[1];
                raf.seek(pos + FLAGS_OFFSET);
                raf.read(flags);
                BitSet bs = BitSet.valueOf(flags);
                if(!IS_ADMIN && withHidden){
                    if(bs.get(6)) {
                        pos += FILE_GAP;
                        continue;
                    }
                }
                raf.seek(pos + EXTENSIONS_OFFSET);
                raf.read(extensionByteArr);
                raf.read(typeByteArr);
                raf.seek(pos + FILE_SIZE_OFFSET);
                raf.read(sizeByteArr);
                raf.seek(pos + CREATE_DATE_OFFSET);
                raf.read(dateByteArr);
                String name = new String(nameByteArr);
                if (name.contains("\u0000")) name = name.substring(0, name.indexOf("\u0000"));
                Integer size = ByteBuffer.wrap(sizeByteArr).getInt();
                String extension = new String(extensionByteArr);
                String date = dateWithDots(new String(dateByteArr));
                BitSet bitSet = BitSet.valueOf(typeByteArr);
                String type = bitSet.get(7) ? "Folder" : "File";
                Float sizeKb = ((float) size) / 1024;
                files.add(new File(name, String.format("%.1f", sizeKb) + " Kb", extension, date, type, bs.get(7)));
                pos += 50;

            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public static String dateWithDots(String date) {
        StringBuffer returnDate = new StringBuffer(date);
        returnDate.insert(4, ".");
        returnDate.insert(2, ".");
        return returnDate.toString();
    }

    public static int freeCluster() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
        raf.seek(DATA_OFFSET_OFFSET);
        int dataOffset = raf.readInt();
        int startCluster = dataOffset / CLUSTER_SIZE + 1;
        raf.seek(FAT2_OFFSET_OFFSET);
        int end = raf.readInt();
        raf.seek(FAT1_OFFSET_OFFSET);
        short fat1 = raf.readShort();
        int pos = fat1;
        while (pos + FAT_GAP < end) {
            pos = fat1 + (startCluster * FAT_GAP) - FAT_GAP;
            raf.seek(pos);
            int b = FAT_GAP == 2 ? raf.readShort() : raf.readInt();
            if (b == 0x00) {
                return startCluster;
            }
            startCluster += 1;
        }
        raf.close();

        return -1;
    }

    public static byte getByteFromBits(String bits) {
        BitSet bs = BitSet.valueOf(new byte[]{});
        for (int i = 0; i < 8; i++) {
            bs.set(7 - i, bits.charAt(i) == '1');
        }
        byte[] b = bs.toByteArray();
        return b[0];
    }

    public static int getFileStartCluster(String path) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            int startByte = getStartByteOfFile(path);
            if(startByte == -1) return -1;
            else {
                raf.seek(startByte + CLUSTER_NUMBER_OFFSET);
                return raf.readInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static ArrayList<File> getFilesFromFolder(String path, boolean withHidden) {
        ArrayList<File> files = new ArrayList<>();
        if (path.equals("/")) return readFilesInRootDirectory(withHidden);
        else {
            try {
                int clusterNumber = getFileStartCluster(path);
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
                int startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                int endByte = clusterNumber * CLUSTER_SIZE;
                while (true) {
                    while (startByte + FILE_GAP < endByte) {
                        raf.seek(startByte);
                        if (raf.readByte() == 0x00) {
                            startByte += FILE_GAP;
                            continue;
                        }
                        byte[] nameByteArr = new byte[20];
                        byte[] sizeByteArr = new byte[4];
                        byte[] extensionByteArr = new byte[3];
                        byte[] dateByteArr = new byte[8];
                        byte[] typeByteArr = new byte[1];
                        byte[] flags = new byte[1];
                        raf.seek(startByte + FLAGS_OFFSET);
                        raf.read(flags);
                        BitSet bs = BitSet.valueOf(flags);
                        if(!IS_ADMIN && withHidden){
                            if(bs.get(6)) {
                                System.out.println("HERE");
                                startByte += FILE_GAP;
                                continue;
                            }
                        }
                        raf.seek(startByte);
                        raf.read(nameByteArr);
                        raf.read(extensionByteArr);
                        raf.read(typeByteArr);
                        raf.seek(startByte + FILE_SIZE_OFFSET);
                        raf.read(sizeByteArr);
                        raf.seek(startByte + CREATE_DATE_OFFSET);
                        raf.read(dateByteArr);
                        String name = new String(nameByteArr);
                        if(name.contains("\u0000")) name = name.substring(0, name.indexOf("\u0000"));
                        Integer size = ByteBuffer.wrap(sizeByteArr).getInt();
                        String extension = new String(extensionByteArr);
                        String date = dateWithDots(new String(dateByteArr));
                        BitSet bitSet = BitSet.valueOf(typeByteArr);
                        String type = bitSet.get(7) ? "Folder" : "File";
                        Float sizeKb = ((float) size) / 1024;
                        files.add(new File(name, String.format("%.1f", sizeKb) + " Kb", extension, date, type, bs.get(7)));
                        startByte += FILE_GAP;
                    }
                    raf.seek(FAT1_OFFSET_OFFSET);
                    int fat1 = raf.readShort();
                    fat1 += (clusterNumber * FAT_GAP) - FAT_GAP;
                    raf.seek(fat1);
                    int nextCluster = 0;
                    switch (FAT_GAP) {
                        case 2:
                            clusterNumber = raf.readShort();
                            break;
                        case 4:
                            clusterNumber = raf.readInt();
                            break;
                    }
                    if (clusterNumber > 0) {
                        startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                        endByte = clusterNumber * CLUSTER_SIZE;
                    } else {
                        raf.close();
                        return files;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return files;
        }
    }

    public static int getStartByteOfFile(String path) {
        String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
        if (path.indexOf("/") == path.lastIndexOf("/")) path = "/";
        else path = path.substring(0, path.lastIndexOf("/"));
        String fileExtension;
        if (fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        } else fileExtension = null;
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            int startByte;
            int endByte;
            int clusterNumber = 0;
            if (path.equals("/")) {
                raf.seek(ROOT_DIRECTORY_OFFSET_OFFSET);
                startByte = raf.readInt();
                raf.seek(USERS_INFO_OFFSET_OFFSET);
                endByte = raf.readInt();
            } else {
                clusterNumber = getFileStartCluster(path);
                startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                endByte = (clusterNumber * CLUSTER_SIZE);
            }
            if (fileExtension == null) {
                while (true) {
                    while (startByte + FILE_GAP < endByte) {
                        raf.seek(startByte);
                        byte[] nameByteArr = new byte[20];
                        raf.read(nameByteArr);
                        String name = new String(nameByteArr);
                        if (name.contains("\u0000")) name = name.substring(0, name.indexOf("\u0000"));
                        if (fileName.equals(name)) return startByte;
                        else startByte += FILE_GAP;
                    }
                    if (path.equals("/")) return -1;
                    raf.seek(FAT1_OFFSET_OFFSET);
                    int fat1 = raf.readShort();
                    fat1 += (clusterNumber * FAT_GAP) - FAT_GAP;
                    raf.seek(fat1);
                    switch (FAT_GAP) {
                        case 2:
                            clusterNumber = raf.readShort();
                            break;
                        case 4:
                            clusterNumber = raf.readInt();
                            break;
                    }
                    if (clusterNumber > 0) {
                        startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                        endByte = clusterNumber * CLUSTER_SIZE;
                    } else {
                        raf.close();
                        return -1;
                    }
                }
            } else {
                while (true) {
                    while (startByte + FILE_GAP < endByte) {
                        raf.seek(startByte);
                        byte[] nameByteArr = new byte[20];
                        byte[] extensionByteArr = new byte[3];
                        raf.read(nameByteArr);
                        raf.seek(startByte + EXTENSIONS_OFFSET);
                        raf.read(extensionByteArr);
                        String name = new String(nameByteArr);
                        String extension = new String(extensionByteArr);
                        if (name.contains("\u0000")) name = name.substring(0, name.indexOf("\u0000"));
                        if (extension.contains("\u0000"))
                            extension = extension.substring(0, extension.indexOf("\u0000"));
                        if (fileName.equals(name) && fileExtension.equals(extension)) return startByte;
                        else startByte += FILE_GAP;
                    }
                    raf.seek(FAT1_OFFSET_OFFSET);
                    int fat1 = raf.readShort();
                    fat1 += (clusterNumber * FAT_GAP) - FAT_GAP;
                    raf.seek(fat1);
                    switch (FAT_GAP) {
                        case 2:
                            clusterNumber = raf.readShort();
                            break;
                        case 4:
                            clusterNumber = raf.readInt();
                            break;
                    }
                    if (clusterNumber > 0) {
                        startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                        endByte = clusterNumber * CLUSTER_SIZE;
                    } else {
                        raf.close();
                        return -1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void increaseSizeFoldersInPath(String path, int count) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");

            while (!path.equals("") && !path.equals("/")) {
                int startByte = getStartByteOfFile(path);
                raf.seek(startByte + FILE_SIZE_OFFSET);
                int size = raf.readInt();
                raf.seek(startByte + FILE_SIZE_OFFSET);
                raf.writeInt(size + count);
                path = path.substring(0, path.lastIndexOf("/"));
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDataFromFile(String filePath) {
        int clusterNumber = getFileStartCluster(filePath);
        int startByte = getStartByteOfFile(filePath);
        String data = "";
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(FAT1_OFFSET_OFFSET);
            int fat1Offset = raf.readShort();
            raf.seek(startByte + FILE_SIZE_OFFSET);
            int size = raf.readInt();
            while (size > 0) {
                int pos = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                raf.seek(pos);
                byte[] dataArray = size > CLUSTER_SIZE ? new byte[CLUSTER_SIZE] : new byte[size];
                raf.read(dataArray);
                data += new String(dataArray);

                raf.seek(fat1Offset + (clusterNumber * FAT_GAP) - FAT_GAP);
                clusterNumber = FAT_GAP == 2 ? raf.readShort() : raf.readInt();
                size -= CLUSTER_SIZE;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static int saveFileData(String data, String filePath) {
        int clusterNumber = getFileStartCluster(filePath);
        int startByte = getStartByteOfFile(filePath);

        byte[] dataByteArr = data.getBytes();
        int size = dataByteArr.length;
        int pointer = dataByteArr.length;
        int currByte = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(FAT1_OFFSET_OFFSET);
            int fat1Offset = raf.readShort();
            while (pointer > 0) {
                int pos = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                raf.seek(pos);
                raf.write(pointer > CLUSTER_SIZE ? Arrays.copyOfRange(dataByteArr, currByte, currByte + CLUSTER_SIZE) : Arrays.copyOfRange(dataByteArr, currByte, currByte + pointer));

                raf.seek(fat1Offset + (clusterNumber * FAT_GAP) - FAT_GAP);
                int prevCluster = clusterNumber;
                clusterNumber = FAT_GAP == 2 ? raf.readShort() : raf.readInt();
                pointer -= CLUSTER_SIZE;
                currByte += CLUSTER_SIZE;
                if (pointer > 0 && clusterNumber == -1) {
                    int newCluster = freeCluster();
                    raf.seek(fat1Offset + (prevCluster * FAT_GAP) - FAT_GAP);
                    switch (FAT_GAP) {
                        case 2:
                            raf.writeShort(newCluster);
                            break;
                        case 4:
                            raf.writeInt(newCluster);
                            break;
                    }
                    clusterNumber = newCluster;
                    raf.seek(fat1Offset + (clusterNumber * FAT_GAP) - FAT_GAP);
                    switch (FAT_GAP) {
                        case 2:
                            raf.writeShort(-1);
                            break;
                        case 4:
                            raf.writeInt(-1);
                            break;
                    }
                }
                if (pointer <= 0) {
                    raf.seek(fat1Offset + (clusterNumber * FAT_GAP) - FAT_GAP);
                    switch (FAT_GAP) {
                        case 2:
                            raf.writeShort(-1);
                            break;
                        case 4:
                            raf.writeInt(-1);
                            break;
                    }
                }
            }
            raf.seek(startByte + FILE_SIZE_OFFSET);
            int prevSize = raf.readInt();
            raf.seek(startByte + FILE_SIZE_OFFSET);
            raf.writeInt(size);
            String folderPath = filePath.substring(0, filePath.lastIndexOf("/"));
            System.out.println(folderPath);
            increaseSizeFoldersInPath(folderPath, -1 * prevSize);
            increaseSizeFoldersInPath(folderPath, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static int moveFile(String filePath, String newPath) {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            int startByteOfFile = getStartByteOfFile(filePath);
            raf.seek(startByteOfFile);
            byte[] fileRecord = new byte[50];
            raf.read(fileRecord);
            raf.seek(startByteOfFile + FILE_SIZE_OFFSET);
            int size = raf.readInt();
            raf.seek(startByteOfFile);
            raf.writeByte(0x00);
            int freePlace;
            if (newPath.equals("/")) {
                freePlace = freePlaceInRootDirectory();
            } else {
                int clusterNumber = getFileStartCluster(newPath);
                a:
                while (true) {
                    int startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                    int endByte = (clusterNumber * CLUSTER_SIZE);
                    while (startByte + FILE_GAP < endByte) {
                        raf.seek(startByte);
                        if (raf.readByte() == 0x00) {
                            freePlace = startByte;
                            break a;
                        }
                        startByte += FILE_GAP;
                    }

                    raf.seek(FAT1_OFFSET_OFFSET);
                    int fat1 = raf.readShort();
                    fat1 += (clusterNumber * FAT_GAP) - FAT_GAP;
                    raf.seek(fat1);
                    int nextCluster = 0;
                    switch (FAT_GAP) {
                        case 2:
                            nextCluster = raf.readShort();
                            break;
                        case 4:
                            nextCluster = raf.readInt();
                            break;
                    }
                    if (nextCluster < 0) {
                        int freeCluster = freeCluster();
                        raf.seek(fat1);
                        switch (FAT_GAP) {
                            case 2:
                                raf.writeShort(freeCluster);
                                break;
                            case 4:
                                raf.writeInt(freeCluster);
                                break;
                        }
                    }
                }
            }
            raf.seek(freePlace);
            raf.write(fileRecord);
            String oldPath = filePath.substring(0, filePath.lastIndexOf("/"));
            increaseSizeFoldersInPath(oldPath, -1 * (size + FILE_GAP));
            increaseSizeFoldersInPath(newPath, size + FILE_GAP);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return 1;
    }

    public static int duplicateFile(String filePath, String newPath){
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            int startByteOfFile = getStartByteOfFile(filePath);
            raf.seek(startByteOfFile);
            byte[] fileRecord = new byte[50];
            raf.read(fileRecord);
            raf.seek(startByteOfFile + FILE_SIZE_OFFSET);
            int size = raf.readInt();
            int freePlace;
            if (newPath.equals("/")) {
                freePlace = freePlaceInRootDirectory();
            } else {
                int clusterNumber = getFileStartCluster(newPath);
                a:
                while (true) {
                    int startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                    int endByte = (clusterNumber * CLUSTER_SIZE);
                    while (startByte + FILE_GAP < endByte) {
                        raf.seek(startByte);
                        if (raf.readByte() == 0x00) {
                            freePlace = startByte;
                            break a;
                        }
                        startByte += FILE_GAP;
                    }

                    raf.seek(FAT1_OFFSET_OFFSET);
                    int fat1 = raf.readShort();
                    fat1 += (clusterNumber * FAT_GAP) - FAT_GAP;
                    raf.seek(fat1);
                    int nextCluster = 0;
                    switch (FAT_GAP) {
                        case 2:
                            nextCluster = raf.readShort();
                            break;
                        case 4:
                            nextCluster = raf.readInt();
                            break;
                    }
                    if (nextCluster < 0) {
                        int freeCluster = freeCluster();
                        raf.seek(fat1);
                        switch (FAT_GAP) {
                            case 2:
                                raf.writeShort(freeCluster);
                                break;
                            case 4:
                                raf.writeInt(freeCluster);
                                break;
                        }
                    }
                }
            }
            raf.seek(freePlace);
            raf.write(fileRecord);
            int freeCluster = freeCluster();
            System.out.println("freeCl " + freeCluster);
            raf.seek(freePlace + CLUSTER_NUMBER_OFFSET);
            raf.writeInt(freeCluster);
            raf.seek(FAT1_OFFSET_OFFSET);
            short fat1 = raf.readShort();
            fat1 += (freeCluster * FAT_GAP) - FAT_GAP;
            raf.seek(fat1);
            switch (FAT_GAP) {
                case 2:
                    raf.writeShort(-1);
                    break;
                case 4:
                    raf.writeInt(-1);
                    break;
            }
            String fp = newPath + "/" + filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            saveFileData(getDataFromFile(filePath), fp);
            String oldPath = filePath.substring(0, filePath.lastIndexOf("/"));
            increaseSizeFoldersInPath(newPath, size + FILE_GAP);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return 1;
    }
//
//    public static int duplicateFolder(String filePath, String newPath){
//        int startByteOfFile = getStartByteOfFile(filePath);
//        int clusterNumber = getFileStartCluster(filePath);
//        try{
//            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
//            byte[] fileRecord = new byte[50];
//            raf.seek(startByteOfFile);
//            raf.read(fileRecord);
//
//
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        return 1;
//    }

    public static int deleteUser(byte UID){
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(DATA_OFFSET_OFFSET);
            int dataOffset = raf.readInt();
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int offset = raf.readInt();
            while(offset + USERS_INFO_GAP < dataOffset){
                raf.seek(offset);
                byte userUID = raf.readByte();
                if(userUID == 0x00){
                    offset += USERS_INFO_GAP;
                    continue;
                }
                if(userUID == UID){
                    raf.seek(offset);
                    raf.writeByte(0x00);
                    return 1;
                }
                offset += USERS_INFO_GAP;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
}
