package com.loy.kit.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;

/**
 * @author Loy
 * @time 2021/3/29 17:42
 * @des
 */
public class JsonUtil {

    private static class Holder{
        private static final Gson Instance = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    }

    private static class JsonParserHolder{
        private static final JsonParser Instance = new JsonParser();
    }

    public static Gson getGson() {
        return Holder.Instance;
    }

    public static JsonElement parse(String jsonStr){
        JsonElement jsonElement = JsonParserHolder.Instance.parse(jsonStr);
        return jsonElement;
    }

    public static JsonObject getJsonObject(String jsonStr){
        JsonElement jsonElement = parse(jsonStr);
        if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject();
        }
        return null;
    }

    public static JsonArray getJsonArray(String jsonStr){
        JsonElement jsonElement = parse(jsonStr);
        if (jsonElement.isJsonArray()) {
            return jsonElement.getAsJsonArray();
        }
        return null;
    }

    public static String toJson(Object o) {
        return getGson().toJson(o);
    }

    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        return getGson().fromJson(jsonStr, clazz);
    }

    public static <T> T fromJson(String jsonStr, Type type) {
        return getGson().fromJson(jsonStr, type);
    }
}
