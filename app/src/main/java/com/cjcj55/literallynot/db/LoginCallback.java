package com.cjcj55.literallynot.db;

public interface LoginCallback {
    void onSuccess(int userId, String username, String firstName, String lastName);
    void onFailure();
}
