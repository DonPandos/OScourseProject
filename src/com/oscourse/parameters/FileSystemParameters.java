package com.oscourse.parameters;

public class FileSystemParameters {

    public final static short nameOffset = 0;
    public final static short clustersCountOffset = 10;
    public final static short clusterSizeOffset = 14;
    public final static short fat1OffsetOffset = 16;
    public final static short fat2OffsetOffset = 18;
    public final static short rootDirectoryOffsetOffset = 22;
    public final static short usersInfoOffsetOffset = 26;
    public final static short dataOffsetOffset = 30;

    public final static short rootDirectoryGap = 48; // размер записи в корневом каталоге
    public final static short usersInfoGap = 27;

    public final static short usernameSize = 12;
    public final static short passwordSize = 14;

    public static short fatGap; // размер записи в fat
    public static short clusterSize;

    public static String currentFsName;
    public static Byte currentUID;

    //file info
    public static short extensionsOffset = 20;
    public static short modesOffset = 23;
    public static short uidOffset = 24;
    public static short fileSizeOffset = 25;
    public static short createDateOffset = 29;
    public static short modifyDateOffset = 37;
    public static short flagsOffset = 45;
    public static short clusterNumberOffset = 46;
}
