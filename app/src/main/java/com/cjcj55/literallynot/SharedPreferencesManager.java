package com.cjcj55.literallynot;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // Method to save login state
    public static void setLoginState(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // Method to retrieve login state
    public static boolean getLoginState(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
    }

    // Method to clear login state
    public static void clearLoginState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.apply();
    }

}
