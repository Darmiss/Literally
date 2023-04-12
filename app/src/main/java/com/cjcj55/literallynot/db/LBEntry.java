package com.cjcj55.literallynot.db;

import androidx.annotation.NonNull;

public class LBEntry {
    private int userId;
    private String username;
    private int numFiles;
    private String firstName;
    private String lastName;

    public LBEntry(int userId, String username, int numFiles, String firstName, String lastName) {
        this.userId = userId;
        this.username = username;
        this.numFiles = numFiles;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @NonNull
    @Override
    public String toString() {
        return this.numFiles + ", " + this.userId + ", " + this.username + ", " + this.firstName + ", " + this.lastName;
    }

    public int getNumFiles(){
        return this.numFiles;
    }

    public String getName(){
        return this.username;
    }
}