package com.oscourse.filesystem;

import java.util.Date;

public class File {
    String name;
    String size;
    String extension;
    String date;
    String type;


    public File(String name, String size, String extension, String date, String type) {
        this.name = name;
        this.size = size;
        this.extension = extension;
        this.date = date;
        this.type = type;
    }

}
