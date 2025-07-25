package com.example.doctorapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(String userUid, int userId, String language) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.KEY_USER_UID, userUid);
        editor.putInt(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_LANGUAGE_ID, language);
        editor.apply();
    }

    public String getUserUid() {
        return prefs.getString(Constants.KEY_USER_UID, "Guest");
    }

    public int getUserId() {
        return prefs.getInt(Constants.KEY_USER_ID, -1);
    }

    public String getLanguage() {
        return prefs.getString(Constants.KEY_LANGUAGE_ID, "en");
    }

    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}