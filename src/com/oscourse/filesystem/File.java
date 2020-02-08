package com.oscourse.filesystem;

import java.util.Date;

public class File {
    String name;
    String size;
    String extension;
    String date;
    String type;
    boolean isSystem;

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public File(String name, String size, String extension, String date, String type, boolean isSystem) {
        this.name = name;
        this.size = size;
        if(extension.contains("\u0000")) this.extension = extension.substring(0, extension.indexOf("\u0000"));
        else this.extension = extension;
        this.date = date;
        this.type = type;
        this.isSystem = isSystem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullFileName(){
        if(extension == null || extension.equals("")){
            return name;
        } else {
            return name + "." + extension;
        }
    }
}
