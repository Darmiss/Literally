package com.cjcj55.literallynot.db;

public class AudioFile {
    private int id;
    private int userId;
    private String dateTime;
    private String filePath;

    public AudioFile(int id, int userId, String dateTime, String filePath) {
        this.id = id;
        this.userId = userId;
        this.dateTime = dateTime;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getFilePath() {
        return filePath;
    }
}

