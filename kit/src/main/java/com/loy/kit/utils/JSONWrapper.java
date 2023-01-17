package com.loy.kit.utils;

import androidx.annotation.NonNull;

import com.loy.kit.log.SdkLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JSONWrapper {
    public static final String TAG = JSONWrapper.class.getSimpleName();

    private final JSONObject mOriginJSONObject;
    private String mOriginJson;

    private JSONWrapper() {
        mOriginJSONObject = new JSONObject();
    }

    private JSONWrapper(String jsonStr) {
        mOriginJson = jsonStr;
        mOriginJSONObject = toJsonObject(jsonStr);
    }

    public JSONWrapper(JSONObject originJSONObject) {
        mOriginJSONObject = originJSONObject;
    }

    public JSONWrapper put(String key, Object value) {
        jsonPut(this.mOriginJSONObject, key, value);
        return this;
    }

    public boolean replaceKey(String oldKey, String newKey) {
        if (mOriginJSONObject.has(oldKey) && !mOriginJSONObject.has(newKey)) {
            Object o = mOriginJSONObject.opt(oldKey);
            mOriginJSONObject.remove(oldKey);
            put(newKey, o);
            return true;
        }
        return false;
    }

    public static void replaceKeyRec(JSONWrapper objectWrapper, String oldKey, String newKey) throws JSONException {
        objectWrapper.replaceKey(oldKey, newKey);

        JSONObject originJSONObject = objectWrapper.getOriginJSONObject();
        Iterator<String> keys = originJSONObject.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            Object o = originJSONObject.get(k);
            if (o instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) o;
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object obj = jsonArray.get(i);
                    if (obj instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) obj;
                        replaceKeyRec(JSONWrapper.fromJson(jsonObject), oldKey, newKey);
                    }
                }
            }
        }
    }

    public String getOriginJson() {
        return mOriginJson;
    }

    public JSONObject getObject(String key) {
        String data = mOriginJSONObject.optString(key);
        return data.isEmpty() ? new JSONObject() : toJsonObject(data);
    }

    public JSONWrapper getJSONObjectWrapper(String key) {
        return JSONWrapper.fromJson(mOriginJSONObject.optJSONObject(key));
    }

    public JSONArray getArray(String key) {
        String data = mOriginJSONObject.optString(key);
        return data.isEmpty() ? new JSONArray() : toJsonArray(data);
    }

    public String getString(String key) {
        return mOriginJSONObject.optString(key);
    }

    public int getInt(String key) {
        return mOriginJSONObject.optInt(key);
    }

    public long getLong(String key) {
        return mOriginJSONObject.optLong(key);
    }

    public double getDouble(String key) {
        return mOriginJSONObject.optDouble(key);
    }

    public boolean getBoolean(String key) {
        return mOriginJSONObject.optBoolean(key);
    }

    public JSONObject getOriginJSONObject() {
        return mOriginJSONObject;
    }

    public static JSONWrapper getInstance() {
        return new JSONWrapper();
    }

    public static JSONWrapper fromJson(String jsonString) {
        return new JSONWrapper(jsonString);
    }

    public static JSONWrapper fromJson(JSONObject jsonObject) {
        return new JSONWrapper(jsonObject);
    }

    public static boolean jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
            return true;
        } catch (JSONException e) {
            SdkLog.e(TAG, "jsonPut err:" + e.getMessage());
            return false;
        }
    }

    @NonNull
    public static JSONObject toJsonObject(String data) {
        try {
            return new JSONObject(data);
        } catch (JSONException e) {
            SdkLog.e(TAG, "toJsonObject err:" + e.getMessage());
            return new JSONObject();
        }
    }

    @NonNull
    public static JSONArray toJsonArray(String data) {
        try {
            return new JSONArray(data);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return mOriginJSONObject.toString();
    }
}
