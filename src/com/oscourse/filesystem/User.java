package com.oscourse.filesystem;

public class User {

    byte UID;
    String username;
    String password;
    String type;

    public User(byte UID, String username, String password, String type) {
        this.UID = UID;
        this.username = username;
        this.password = password;
        this.type = type;
    }

    public byte getUID() {
        return UID;
    }

    public void setUID(byte UID) {
        this.UID = UID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
