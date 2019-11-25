package com.oscourse.filesystem;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import javafx.util.Pair;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
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

    public final static short FILE_GAP = 50; // размер записи в корневом каталоге
    public final static short USERS_INFO_GAP = 27;

    public final static short USERNAME_SIZE = 12;
    public final static short PASSWORD_SIZE = 14;

    public static short FAT_GAP; // размер записи в fat
    public static short CLUSTER_SIZE;

    public static String CURRENT_FS_NAME;
    public static Byte CURRENT_UID;
    public static String CURRENT_DIR;

    //file info
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
        int clustersCountForUserInfo = (int) Math.ceil(((27.0 * 256.0) / clusterSize));
        int dataOffset = usersInfoOffset + (clustersCountForUserInfo * clusterSize); //смещение области с данными

        FileOutputStream fos = new FileOutputStream("/Users/bogdan/Desktop/OScourse/" + fsName);
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + fsName, "rw");

        raf.write(new byte[hddSize]); // заполняем файл байтами
        raf.seek(0); // смещение курсора на 0 позицию
        raf.writeBytes(fsName);
        raf.seek(CLUSTERS_COUNT_OFFSET);
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

    public static Pair<Byte, String> userSearch(String username) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(USERS_INFO_OFFSET_OFFSET);
            int userInfoOffset = raf.readInt();
            raf.seek(userInfoOffset);
            while (true) {
                byte uid = raf.readByte();
                if (uid == 0x00) {
                    return null;
                }
                byte[] login = new byte[12];
                raf.read(login);
                String foundUsername = new String(login);
                foundUsername = foundUsername.substring(0, foundUsername.indexOf(0x00));
                if (foundUsername.equals(username)) {
                    byte[] password = new byte[14];
                    raf.read(password);
                    String foundPassword = new String(password);
                    foundPassword = foundPassword.substring(0, foundPassword.indexOf(0x00));
                    return new Pair<>(uid, foundPassword);
                }
                userInfoOffset += USERS_INFO_GAP;
                raf.seek(userInfoOffset);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void open(String path) {

    }

    public static int createFileInFolder(boolean type, String path, String name) {
        if (path.equals("/")) {
            int a = freePlaceInRootDirectory();
            if (a != -1) {
                createFile(a, type, name);
            } else return -1;
        } else {
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
            String fileName = name;
            String fileExtension = "";
            if (name.lastIndexOf(".") != -1) {
                fileName = name.substring(0, name.lastIndexOf("."));
                fileExtension = name.substring(name.lastIndexOf(".") + 1, name.length());
            }
            System.out.println(fileName);
            System.out.println(fileExtension);
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
            short cluster = freeCluster();
            raf.writeInt(cluster);

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

//    public static int createFile(String path, String name, String extension, short uid, byte modes, byte flags, int clusterNumber) {
//        if(name.length() > 20 || extension.length() > 3) return 0;
//        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
//        if (path.equals("/")){
//            int freePlace = freePlaceInRootDirectory();
//            raf.seek(freePlace);
//            raf.writeBytes(name);
//            raf.seek(freePlace + EXTENSIONS_OFFSET);
//            raf.writeBytes(extension);
//            raf.seek(freePlace + MODES_OFFSET);
//            raf.writeByte(modes);
//            raf.writeShort(uid);
//            raf.writeInt(0);
//            String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
//            raf.writeBytes(date);
//            raf.writeBytes(date);
//            raf.writeByte(flags);
//            raf.writeInt(clusterNumber);
////            raf.write
//            raf.close();
//            System.out.println("im here");
//        }
//        return 1;
//    }


    // EDIT: ПОКА pos < dataOffset
    public static int freePlaceInRootDirectory() {
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(DATA_OFFSET_OFFSET);
            int dataOffset = raf.readInt();
            raf.seek(ROOT_DIRECTORY_OFFSET_OFFSET);
            int pos = raf.readInt();
            while (pos < dataOffset) {
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

    public static ArrayList<File> readFilesInRootDirectory() {
        ArrayList<File> files = new ArrayList<>();
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            raf.seek(DATA_OFFSET_OFFSET);
            int dataOffset = raf.readInt();
            raf.seek(ROOT_DIRECTORY_OFFSET_OFFSET);
            int pos = raf.readInt();
            while (pos < dataOffset) {
                raf.seek(pos);
                byte[] nameByteArr = new byte[20];
                byte[] sizeByteArr = new byte[4];
                byte[] extensionByteArr = new byte[3];
                byte[] dateByteArr = new byte[8];
                byte[] typeByteArr = new byte[1];
                raf.read(nameByteArr);
                if (nameByteArr[0] == 0x00) break;
                raf.read(extensionByteArr);
                raf.read(typeByteArr);
                raf.seek(pos + FILE_SIZE_OFFSET);
                raf.read(sizeByteArr);
                raf.seek(pos + CREATE_DATE_OFFSET);
                raf.read(dateByteArr);
                String name = new String(nameByteArr);
                name = name.substring(0, name.indexOf("\u0000"));
                Integer size = ByteBuffer.wrap(sizeByteArr).getInt();
                String extension = new String(extensionByteArr);
                String date = dateWithDots(new String(dateByteArr));
                BitSet bitSet = BitSet.valueOf(typeByteArr);
                String type = bitSet.get(7) ? "Папка" : "Файл";
                files.add(new File(name, size.toString() + " б", extension, date, type));
                pos += 50;

            }
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

    public static short freeCluster() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
        raf.seek(DATA_OFFSET_OFFSET);
        int dataOffset = raf.readInt();
        int startCluster = dataOffset / CLUSTER_SIZE;
        raf.seek(FAT2_OFFSET_OFFSET);
        int end = raf.readInt();
        raf.seek(FAT1_OFFSET_OFFSET);
        short pos = raf.readShort();


        if (FAT_GAP == 2) {
            pos += (startCluster * 2) - 2;
            raf.seek(pos);
            while (pos < end) {
                if (raf.readShort() == 0x00) {
                    return (short) startCluster;
                }
                startCluster += 1;
            }
        } else {
            pos += (startCluster * 4) - 4;
            raf.seek(pos);
            while (pos < end) {
                if (raf.readInt() == 0x00) {
                    return (short) startCluster;
                }
                startCluster += 1;
            }
        }

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
        if (path.indexOf("/") == path.lastIndexOf("/")) {
            String fileName = path.substring(path.indexOf("/") + 1, path.length());
            try {
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
                raf.seek(DATA_OFFSET_OFFSET);
                int rootEnd = raf.readInt();
                raf.seek(ROOT_DIRECTORY_OFFSET_OFFSET);
                int rootPos = raf.readInt();
                while (rootPos < rootEnd) {
                    raf.seek(rootPos);
                    byte[] nameByteArr = new byte[20];
                    raf.read(nameByteArr);
                    if (nameByteArr[0] == 0x00) break;
                    String name = new String(nameByteArr);
                    name = name.substring(0, name.indexOf("\u0000"));
                    if (fileName.equals(name)) {
                        rootPos += CLUSTER_NUMBER_OFFSET;
                        raf.seek(rootPos);
                        return raf.readInt();
                    }
                    rootPos += FILE_GAP;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            path = path.substring(1, path.length()); // /home/src -> home/src
            String filename = path.substring(0, path.indexOf("/")); // home
            path = path.substring(path.indexOf("/"), path.length()); // /src
            try {
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
                raf.seek(DATA_OFFSET_OFFSET);
                int rootEnd = raf.readInt();
                raf.seek(ROOT_DIRECTORY_OFFSET_OFFSET);
                int rootPos = raf.readInt();
                while (rootPos < rootEnd) {
                    raf.seek(rootPos);
                    byte[] nameByteArr = new byte[20];
                    raf.read(nameByteArr);
                    if (nameByteArr[0] == 0x00) break;
                    String name = new String(nameByteArr);
                    name = name.substring(0, name.indexOf("\u0000"));
                    if (filename.equals(name)) {
                        raf.seek(rootPos + CLUSTER_NUMBER_OFFSET);
                        int clusterNumber = raf.readInt();
                        return getFileStartCluster(clusterNumber, path);
                    }
                    rootPos += FILE_GAP;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return -1;
    }

    private static int getFileStartCluster(int clusterNumber, String path) {
        String fileName = "";
        if (path.indexOf("/") == path.lastIndexOf("/")) {
            fileName = path.substring(1, path.length());
            path = "";
        } else {
            path = path.substring(1, path.length()); // /home/src -> home/src
            fileName = path.substring(0, path.indexOf("/")); // home
            path = path.substring(path.indexOf("/"), path.length()); //
        }
        try {
            RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
            int startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
            int endByte = clusterNumber * CLUSTER_SIZE;
            while (true) {
                System.out.println("WHILE TRUE");
                while (startByte + 50 < endByte) {
                    raf.seek(startByte);
                    byte[] nameByteArr = new byte[20];
                    raf.read(nameByteArr);
                    if (nameByteArr[0] == 0x00) break;
                    String name = new String(nameByteArr);
                    name = name.substring(0, name.indexOf("\u0000"));
                    if (fileName.equals(name)) {
                        raf.seek(startByte + CLUSTER_NUMBER_OFFSET);
                        if (path.equals("")) return raf.readInt();
                        else return getFileStartCluster(raf.readInt(), path);
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
                    return -1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    public static ArrayList<File> getFilesFromFolder(String path) {
        ArrayList<File> files = new ArrayList<>();
        if (path.equals("/")) return readFilesInRootDirectory();
        else {
            try {
                int clusterNumber = getFileStartCluster(path);
                RandomAccessFile raf = new RandomAccessFile("/Users/bogdan/Desktop/OScourse/" + CURRENT_FS_NAME, "rw");
                int startByte = (clusterNumber * CLUSTER_SIZE) - CLUSTER_SIZE;
                int endByte = clusterNumber * CLUSTER_SIZE;
                while (true) {
                    while (startByte + 50 < endByte) {
                        raf.seek(startByte);
                        byte[] nameByteArr = new byte[20];
                        byte[] sizeByteArr = new byte[4];
                        byte[] extensionByteArr = new byte[3];
                        byte[] dateByteArr = new byte[8];
                        byte[] typeByteArr = new byte[1];
                        raf.read(nameByteArr);
                        if (nameByteArr[0] == 0x00) break;
                        raf.read(extensionByteArr);
                        raf.read(typeByteArr);
                        raf.seek(startByte + FILE_SIZE_OFFSET);
                        raf.read(sizeByteArr);
                        raf.seek(startByte + CREATE_DATE_OFFSET);
                        raf.read(dateByteArr);
                        String name = new String(nameByteArr);
                        name = name.substring(0, name.indexOf("\u0000"));
                        Integer size = ByteBuffer.wrap(sizeByteArr).getInt();
                        String extension = new String(extensionByteArr);
                        String date = dateWithDots(new String(dateByteArr));
                        BitSet bitSet = BitSet.valueOf(typeByteArr);
                        String type = bitSet.get(7) ? "Папка" : "Файл";
                        files.add(new File(name, size.toString() + " б", extension, date, type));
                        startByte += 50;
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
                        return files;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return files;
        }
    }

    public static int getStartByteOfFile(String path){
        
    }

    public static void increaseSizeFoldersInPath(String path){

    }

}
