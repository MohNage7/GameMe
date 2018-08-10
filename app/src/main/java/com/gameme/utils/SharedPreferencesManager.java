package com.gameme.utils;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.gameme.GameMe;
import com.gameme.login.model.User;
import com.google.gson.Gson;
import com.securepreferences.SecurePreferences;


public class SharedPreferencesManager {

    private static final String DEFAULT_APP_PREFS_NAME = "gameme-default-prefs";
    private static SharedPreferences prefs;

    private static SharedPreferences getPrefs(String prefsName) {
        if (TextUtils.isEmpty(prefsName))
            prefsName = DEFAULT_APP_PREFS_NAME;

        if (prefs == null) {
            prefs = new SecurePreferences(GameMe.get(), "gameme", prefsName);
        }
        return prefs;
    }

    public static void saveString(String key, String value) {
        saveString(null, key, value);
    }

    public static void saveString(String prefsName, String key, String value) {
        getPrefs(prefsName).edit().putString(key, value).apply();

    }

    public static void saveLoggedUserObject(User loggedUserModel) {
        saveLoggedUserObject(null, Constants.QuickSharedPref.LOGGED_USER, loggedUserModel);
    }

    private static void saveLoggedUserObject(String prefsName, String key, User loggedUserModel) {
        Gson gson = new Gson();
        String json = gson.toJson(loggedUserModel);
        getPrefs(prefsName).edit().putString(key, json).apply();
    }


    public static
    @Nullable
    User getLoggedUserObject() {
        return getLoggedUserObject(null, Constants.QuickSharedPref.LOGGED_USER);
    }

    private static
    @Nullable
    User getLoggedUserObject(String prefsName, String key) {
        Gson gson = new Gson();
        String json = getPrefs(prefsName).getString(key, null);
        User obj = gson.fromJson(json, User.class);
        return obj;
    }


    public static void removeLoggedUserObject() {
        getPrefs(null).edit().remove(Constants.QuickSharedPref.LOGGED_USER).apply();
    }


    public static
    @Nullable
    String getString(String key) {
        return getString(null, key);
    }

    public static
    @Nullable
    String getString(String prefsName, String key) {
        return getPrefs(prefsName).getString(key, null);
    }

    public static void saveBoolean(String key, boolean value) {
        saveBoolean(null, key, value);
    }

    public static void saveBoolean(String prefsName, String key, boolean value) {
        getPrefs(prefsName).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key) {
        return getBoolean(null, key);
    }

    public static boolean getBoolean(String prefsName, String key) {
        return getPrefs(prefsName).getBoolean(key, false);
    }

    public static boolean isFirstRun() {
        return getBooleanWithTrueDefault("first_run");
    }

    public static boolean getBooleanWithTrueDefault(String key) {
        return getPrefs(null).getBoolean(key, true);
    }

    public static void setFirstRun() {
        saveBoolean("first_run", false);
    }
}
