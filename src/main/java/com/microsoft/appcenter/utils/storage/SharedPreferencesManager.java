package com.microsoft.appcenter.utils.storage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesManager {
    private final static JSONObject mPreferences;

    static {
        JSONObject obj;
        try(FileInputStream is = new FileInputStream(getPreferencesPath())) {
            JSONTokener tokener = new JSONTokener(is);
            obj = new JSONObject(tokener);
        } catch (Throwable ignored) {
            obj = new JSONObject();
        }
        mPreferences = obj;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (mPreferences.has(key)) {
            return mPreferences.getBoolean(key);
        }
        return defaultValue;
    }

    public static void putBoolean(String key, boolean value) {
        mPreferences.put(key, value);
        save();
    }

    public static void putInt(String key, int value) {
        mPreferences.put(key, value);
        save();
    }

    public static long getLong(String key) {
        return mPreferences.has(key) ? mPreferences.getLong(key) : 0;
    }

    public static void putLong(String key, long value) {
        mPreferences.put(key, value);
        save();
    }

    public static String getString(String key, String defaultValue) {
        if (mPreferences.has(key)) {
            return mPreferences.getString(key);
        }
        return defaultValue;
    }

    public static void putString(String key, String value) {
        mPreferences.put(key, value);
        save();
    }

    public static Set<String> getStringSet(String key) {
        if (!mPreferences.has(key)) return null;
        JSONArray arr = mPreferences.getJSONArray(key);
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < arr.length(); i++) {
            set.add(arr.getString(i));
        }
        return set;
    }

    public static void putStringSet(String key, Set<String> value) {
        mPreferences.put(key, value);
        save();
    }

    public static void remove(String key) {
        mPreferences.remove(key);
        save();
    }

    private static String getPreferencesPath() {
        return System.getProperty("com.microsoft.appcenter.preferences", ".appcenter-preferences.json");
    }

    private static void save() {
        try {
            mPreferences.write(new FileWriter(getPreferencesPath())).close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
